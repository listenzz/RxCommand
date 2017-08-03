package com.shundaojia.sample;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

/**
 * Created by listen on 2017/8/2.
 */

public class ViewModelFactory implements ViewModelProvider.Factory{
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel();
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
