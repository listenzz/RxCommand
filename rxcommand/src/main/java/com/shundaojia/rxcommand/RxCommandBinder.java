package com.shundaojia.rxcommand;

import android.view.View;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by listen on 2017/3/16.
 * Helper to bind a view with a command;
 */

public class RxCommandBinder {
    public static <T> Disposable bind(View view, RxCommand<T> command) {
        return bind(view, command, null);
    }
    public static <T> Disposable bind(final View view, final RxCommand<T> command, final Object obj) {
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command.execute(obj);
            }
        });
        return command.enabled().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean enabled) throws Exception {
                view.setEnabled(enabled);
            }
        });
    }
}
