package me.listenzz.rxcommand;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import me.listenzz.library.RxCommand;
import timber.log.Timber;

/**
 * Created by listen on 2017/3/16.
 */

public class LoginViewModel {

    private RxCommand<String> countdownCommand;
    private RxCommand<Boolean> loginCommand;
    private RxCommand<String> verificationCodeCommand;

    private Subject<String> phoneNumber;
    private Subject<String> verificationCode;

    private Observable<Boolean> verificationCodeValid;
    private Observable<Boolean> phoneNumberValid;

    public LoginViewModel() {
        phoneNumber = BehaviorSubject.create();
        verificationCode = BehaviorSubject.create();

        verificationCodeValid = verificationCode.map(s -> s.trim().length() == 6);
        phoneNumberValid = phoneNumber.map(s -> s.trim().length() == 11);
    }

    public Subject<String> getPhoneNumber() {
        return phoneNumber;
    }

    public Subject<String> getVerificationCode() {
        return verificationCode;
    }

    public RxCommand<String> getVerificationCodeCommand() {
        if (verificationCodeCommand == null) {
            Observable<Boolean> enabled = Observable.combineLatest(
                    phoneNumberValid,
                    getCountdownCommand().executing(),
                    (valid, executing) -> valid && !executing);

            verificationCodeCommand = RxCommand.create(enabled, o -> {
                String phone = phoneNumber.blockingFirst();
                Timber.i("fetch verification code with %s", phone);
                Observable fetchCode =  fetchVerificationCode(phone);
                Observable countdown =  Observable.defer(() -> getCountdownCommand().execute(null).ignoreElements().toObservable()) ;
                return Observable.concat(fetchCode, countdown);
            });
        }
        return verificationCodeCommand;
    }

    public RxCommand<String> getCountdownCommand() {
        if (countdownCommand == null) {
            countdownCommand = RxCommand.create(o -> Observable
                    .interval(1, TimeUnit.SECONDS)
                    .take(10)//from 0 to 9
                    .map(aLong -> "fetch " + (9 - aLong) + "'"));
        }
        return countdownCommand;
    }

    public RxCommand<Boolean> getLoginCommand() {
        if (loginCommand == null) {
            Observable<Boolean> loginInputValid = Observable.combineLatest(
                    verificationCodeValid,
                    phoneNumberValid,
                    (codeValid, phoneValid) -> codeValid && phoneValid);

            loginCommand = RxCommand.create(loginInputValid, o -> {
                String phone = phoneNumber.blockingFirst();
                String code = verificationCode.blockingFirst();
                return login(phone, code);
            });
        }
        return loginCommand;
    }

    private Observable<Boolean> login(String phoneNumber, String code) {
        return Observable.timer(4, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    if (phoneNumber.equals("18503002163")){
                        return Observable.error(new RuntimeException("the phone number is not yours!"));
                    } else  if (code.equals("123456")) {
                        return Observable.just(true);
                    } else {
                        return Observable.error(new RuntimeException("your code is wrong!!"));
                    }
                });
    }

    private Observable<String> fetchVerificationCode(String phoneNumber) {
        return Observable.timer(2, TimeUnit.SECONDS)
                .map(seconds -> System.currentTimeMillis())
                .map(millis -> millis % 2)
                .flatMap(i -> {
                    if (i == 0) {
                        return Observable.error(new RuntimeException("it seen that your network is disconnected."));
                    }
                    return Observable.just("your code is 123456.");
                });
    }


}
