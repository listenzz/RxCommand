# RxCommand
A command is an Observable triggered in response to some action, typicallyUI-related.

It manage the extra states, such as loading, enabled, errors for you, when using RxJava2 implement the functions of your ViewModel.

## 博客

[Android 生命周期架构组件与 RxJava 完美协作](https://listenzz.github.io/android-lifecyle-works-perfectly-with-rxjava.html)

## Code like this

ViewModel

```java
public class MyViewModel extends ViewModel {

    public final RxCommand<List<User>> usersCommand;

    public MyViewModel(final UserRepository userRepository) {

        usersCommand = RxCommand.create(o -> {
                return userRepository.getUsers();
            });
    }
}
```

Activity


```java
public class MyActivity extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        MyViewModel viewModel = ViewModelProviders.of(this).get(MyViewModel.class);

        viewModel.usersCommand
                .switchToLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(Live.bindLifecycle(this))
                .subscribe(users -> {
                    // update UI
                });

        viewModel.usersCommand
                .executing()
                .compose(Live.bindLifecycle(this))
                .subscribe(executing -> {
                    // show or hide loading
                })

        viewModel.usersCommand
                .errors()
                .compose(Live.bindLifecycle(this))
                .subscribe(throwable -> {
                    // show error message
                });
    }
}
```

## Usage

```gradle
buildscript {
    repositories {
        jcenter()
        maven { url 'https://maven.google.com' }
    }
}
``` 

```gradle
dependencies {

    //  using Support Library 26.1+
    compile 'com.android.support:appcompat-v7:26.1.0'
    compile 'com.android.support:support-v4:26.1.0'
    compile 'com.android.support:design:26.1.0'

    // RxJava
    compile 'io.reactivex.rxjava2:rxjava:2.1.0'
    compile 'io.reactivex.rxjava2:rxandroid:2.0.1'

    // Live
    compile 'com.shundaojia:live:1.0.2'

    // RxCommand
    compile 'com.shundaojia:rxcommand:1.2.2'
    compile 'android.arch.lifecycle:extensions:1.0.0' // for ViewModel

}
```
