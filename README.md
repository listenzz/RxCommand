# RxCommand
A command is an Observable triggered in response to some action, typicallyUI-related.

It manage the extra states, such as loading, enabled, errors for you, when using RxJava2 implement the functions of your ViewModel.


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
                .subcribe(throwable -> {
                    // show error message
                });
    }
}
```

## Demo

Play this video.

<video width="360" height="640" controls>
<source src="https://listenzz.github.io/videos/rxcommand.mp4">
</video>

## Usage

```gradle
buildscript {
    repositories {
        jcenter()
    }
}
``` 

```gradle
dependencies {
    compile 'com.android.support:appcompat-v7:25.2.0'
    compile 'io.reactivex.rxjava2:rxjava:2.1.0'
    compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
    compile 'com.shundaojia:rxcommand:1.1.3'
}
```
