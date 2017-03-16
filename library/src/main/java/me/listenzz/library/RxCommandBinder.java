package me.listenzz.library;

import android.view.View;

import io.reactivex.disposables.Disposable;

/**
 * Created by listen on 2017/3/16.
 * Helper to bind a view with a command;
 */

public class RxCommandBinder {
    public static <T> Disposable bind(View view, RxCommand<T> command) {
        return bind(view, command, null);
    }

    public static <T> Disposable bind(View view, RxCommand<T> command, Object obj) {
        view.setClickable(true);
        view.setOnClickListener(v -> command.execute(obj));
        return command.enabled().subscribe(view::setEnabled);
    }
}
