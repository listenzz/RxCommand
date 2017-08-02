package com.shundaojia.rxcommand;

import android.view.View;

import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by listen on 2017/3/16.
 * Helper to bind a view with a command;
 */

public class RxCommandBinder {

    @Deprecated
    public static <T>Disposable bind(final View view, final RxCommand<T> command) {
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command.execute(null);
            }
        });

        return command.enabled()
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean enabled) throws Exception {
                            view.setEnabled(enabled);
                        }
                    });
    }

    public static <T> void bind(final View view, final RxCommand<T> command, ObservableTransformer<Boolean, Boolean> takeUntil) {
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command.execute(null);
            }
        });
        command.enabled()
                .compose(takeUntil)
                .subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean enabled) throws Exception {
                view.setEnabled(enabled);
            }
        });
    }

}
