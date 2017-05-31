package com.shundaojia.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import com.shundaojia.rxcommand.RxCommandBinder;


public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.phone_number)
    EditText phoneNumberEditText;
    @BindView(R.id.verification_code_button)
    Button verificationCodeButton;
    @BindView(R.id.verification_code)
    EditText verificationCodeEditText;
    @BindView(R.id.login_button)
    Button loginButton;

    LoginViewModel viewModel;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        viewModel = new LoginViewModel();

        //bind view model
        RxTextView.textChanges(phoneNumberEditText).subscribe(viewModel.phoneNumber());
        RxTextView.textChanges(verificationCodeEditText).subscribe(viewModel.verificationCode());

        Disposable disposable = RxCommandBinder.bind(verificationCodeButton, viewModel.verificationCodeCommand());
        compositeDisposable.add(disposable);
        disposable = RxCommandBinder.bind(loginButton, viewModel.loginCommand());
        compositeDisposable.add(disposable);

        //respond to command

        //fetch verification code
        disposable = viewModel.verificationCodeCommand()
                .executing()
                .subscribe(executing -> {
                    if (executing) {
                        verificationCodeButton.setText("fetch...");
                    } else {
                        verificationCodeButton.setText("fetch code");
                    }
                });
        compositeDisposable.add(disposable);

        disposable = viewModel.verificationCodeCommand()
                .switchToLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show());
        compositeDisposable.add(disposable);


        //countdown
        disposable = viewModel.countdownCommand()
                .executing()
                .subscribe(executing -> {
                    if (!executing) {
                        verificationCodeButton.setText("fetch code");
                    }
                });
        compositeDisposable.add(disposable);

        disposable = viewModel.countdownCommand()
                .switchToLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> verificationCodeButton.setText(s));
        compositeDisposable.add(disposable);

        //login
        disposable = viewModel.loginCommand()
                .executing()
                .subscribe(executing -> {
                    if (executing) {
                        loginButton.setText("login...");
                    } else {
                        loginButton.setText("login");
                    }
                });
        compositeDisposable.add(disposable);

        disposable = Observable.merge(
                viewModel.verificationCodeCommand().errors(),
                viewModel.loginCommand().errors())
                .subscribe(throwable ->
                        Toast.makeText(LoginActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show()
                );
        compositeDisposable.add(disposable);

        disposable = viewModel.loginCommand()
                .switchToLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        Toast.makeText(LoginActivity.this, "login success!! Now goto the MainActivity.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "login fail!!", Toast.LENGTH_LONG).show();
                    }
                });
        compositeDisposable.add(disposable);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
