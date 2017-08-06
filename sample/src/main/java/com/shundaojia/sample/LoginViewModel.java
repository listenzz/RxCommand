package com.shundaojia.sample;

import android.arch.lifecycle.ViewModel;

import com.shundaojia.rxcommand.RxCommand;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

/**
 * Created by listen on 2017/3/16.
 */

public class LoginViewModel extends ViewModel{

    private RxCommand<String> _countdownCommand;
    private RxCommand<Boolean> _loginCommand;
    private RxCommand<String> _captchaCommand;

    private Subject<CharSequence> _phoneNumber;
    private Subject<CharSequence> _captcha;

    private Observable<Boolean> _captchaValid;
    private Observable<Boolean> _phoneNumberValid;

    public LoginViewModel() {
        _phoneNumber = BehaviorSubject.create();
        _captcha = BehaviorSubject.create();

        _captchaValid = _captcha.map(s -> s.toString().trim().length() == 6);
        _phoneNumberValid = _phoneNumber.map(s -> s.toString().trim().length() == 11);
    }

    public void setPhoneNumber(CharSequence phoneNumber) {
        _phoneNumber.onNext(phoneNumber);
    }

    public void setCaptcha(CharSequence code) {
        _captcha.onNext(code);
    }

    public RxCommand<String> captchaCommand() {
        if (_captchaCommand == null) {
            Observable<Boolean> enabled = Observable.combineLatest(
                    _phoneNumberValid,
                    countdownCommand().executing(),
                    (valid, executing) -> valid && !executing);

            _captchaCommand = RxCommand.create(enabled, o -> {
                String phone = _phoneNumber.blockingFirst().toString();
                Timber.i("fetch captcha with %s", phone);
                Observable fetchCode =  fetchCaptcha(phone);
                Observable countdown =  Observable.defer(() -> countdownCommand().execute(null).ignoreElements().toObservable()) ;
                return Observable.concat(fetchCode, countdown);
            });
        }
        return _captchaCommand;
    }

    public RxCommand<String> countdownCommand() {
        if (_countdownCommand == null) {
            _countdownCommand = RxCommand.create(o -> Observable
                    .interval(1, TimeUnit.SECONDS)
                    .take(20)//from 0 to 19
                    .map(aLong -> "fetch " + (19 - aLong) + "'"));
        }
        return _countdownCommand;
    }

    public RxCommand<Boolean> loginCommand() {
        if (_loginCommand == null) {
            Observable<Boolean> loginInputValid = Observable.combineLatest(
                    _captchaValid,
                    _phoneNumberValid,
                    (captchaValid, phoneValid) -> captchaValid && phoneValid);

            _loginCommand = RxCommand.create(loginInputValid, o -> {
                String phone = _phoneNumber.blockingFirst().toString();
                String captcha = _captcha.blockingFirst().toString();
                return login(phone, captcha);
            });
        }
        return _loginCommand;
    }

    private Observable<Boolean> login(String phoneNumber, String captcha) {
        return Observable.timer(4, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    if (captcha.equals("123456")) {
                        return Observable.just(true);
                    } else {
                        return Observable.error(new RuntimeException("your captcha is wrong!!"));
                    }
                });
    }

    private Observable<String> fetchCaptcha(String phoneNumber) {
        return Observable.timer(2, TimeUnit.SECONDS)
                .map(i -> "your captcha is 123456.");
    }

}
