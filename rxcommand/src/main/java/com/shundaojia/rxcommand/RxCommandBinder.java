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
    public static <T> Disposable bind(final View view, final RxCommand<T> command) {
        view.setClickable(true);
        view.setOnClickListener(v -> command.execute(null));
        return command.enabled()
                .subscribe(view::setEnabled);
    }

    public static <T> void bind(final View view, final RxCommand<T> command, ObservableTransformer<Boolean, Boolean> takeUntil) {
        view.setClickable(true);
        view.setOnClickListener(v -> command.execute(null));
        command.enabled()
                .compose(takeUntil)
                .subscribe(view::setEnabled);
    }

}
