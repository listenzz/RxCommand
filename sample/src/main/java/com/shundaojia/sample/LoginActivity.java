package com.shundaojia.sample;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.shundaojia.live.Live;
import com.shundaojia.rxcommand.RxCommandBinder;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class LoginActivity extends AppCompatActivity implements LifecycleRegistryOwner{

    private final LifecycleRegistry registry = new LifecycleRegistry(this);

    @BindView(R.id.phone_number)
    EditText phoneNumberEditText;
    @BindView(R.id.captcha_button)
    Button captchaButton;
    @BindView(R.id.captcha)
    EditText captchaEditText;
    @BindView(R.id.login_button)
    Button loginButton;

    LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        viewModel = new LoginViewModel();

        // bind view model
        RxTextView
                .textChanges(phoneNumberEditText)
                .compose(Live.bindLifecycle(this))
                .subscribe(viewModel::setPhoneNumber);

        RxTextView
                .textChanges(captchaEditText)
                .compose(Live.bindLifecycle(this))
                .subscribe(viewModel::setVerificationCode);

        RxCommandBinder
                .bind(captchaButton, viewModel.verificationCodeCommand(), Live.bindLifecycle(this));
        RxCommandBinder
                .bind(loginButton, viewModel.loginCommand(), Live.bindLifecycle(this));

        // captcha
        viewModel.verificationCodeCommand()
                .executing()
                .compose(Live.bindLifecycle(this))
                .subscribe(executing -> {
                    if (executing) {
                        captchaButton.setText("Fetch...");
                    } else {
                        captchaButton.setText("Fetch Captcha");
                    }
                });

        viewModel.verificationCodeCommand()
                .switchToLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(Live.bindLifecycle(this))
                .subscribe(result -> Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show());

        // countdown
        viewModel.countdownCommand()
                .executing()
                .compose(Live.bindLifecycle(this))
                .subscribe(executing -> {
                    if (!executing) {
                        captchaButton.setText("Fetch Captcha");
                    }
                });

        viewModel.countdownCommand()
                .switchToLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(Live.bindLifecycle(this))
                .subscribe(s -> captchaButton.setText(s));

        // login
        viewModel.loginCommand()
                .executing()
                .compose(Live.bindLifecycle(this))
                .subscribe(executing -> {
                    if (executing) {
                        loginButton.setText("Login...");
                    } else {
                        loginButton.setText("Login");
                    }
                });

        Observable.merge(
                    viewModel.verificationCodeCommand().errors(),
                    viewModel.loginCommand().errors())
                .compose(Live.bindLifecycle(this))
                .subscribe(throwable ->
                        Toast.makeText(LoginActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show()
                );

        viewModel.loginCommand()
                .switchToLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(Live.bindLifecycle(this))
                .subscribe(success -> {
                    if (success) {
                        Toast.makeText(LoginActivity.this, "Login success!! Now goto the MainActivity.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login fail!!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return registry;
    }
}
