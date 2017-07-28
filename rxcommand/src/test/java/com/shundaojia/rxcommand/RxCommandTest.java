package com.shundaojia.rxcommand;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by listen on 2017/6/16.
 */

public class RxCommandTest {

    private final static String VALUE = "value";

    @Rule
    public ImmediateSchedulersRule schedulers = new ImmediateSchedulersRule();

    @Test
    public void beforeExecution_defaultValue() {
        Observable<Boolean> enabled = Observable.just(true);

        RxCommand<String> command = RxCommand.create(enabled, new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.empty();
            }
        });

        // no next value
        command.executionObservables()
                .test()
                .assertNoValues();

        command.switchToLatest()
                .test()
                .assertNoValues();

        // no error values
        command.errors()
                .test()
                .assertNoValues();

        // not in executing
        command.executing()
                .test()
                .assertValue(false);

        // is in enable state
        command.enabled()
                .test()
                .assertValue(true);
    }

    @Test
    public void afterExecution_defaultValue() {
        Observable<Boolean> enabled = Observable.just(true);

        RxCommand<String> command = RxCommand.create(enabled, new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.empty();
            }
        });

        command.execute(null);

        // no next value
        command.executionObservables()
                .test()
                .assertNoValues();

        command.switchToLatest()
                .test()
                .assertNoValues();

        // no error values
        command.errors()
                .test()
                .assertNoValues();

        // not in executing
        command.executing()
                .test()
                .assertValue(false);

        // is in enable state
        command.enabled()
                .test()
                .assertValue(true);
    }

    @Test
    public void executeWhenNotEnable_emitIllegalStateException() {
        RxCommand<String> command = RxCommand.create(Observable.just(false), new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.empty();
            }
        });

        command.execute(null)
            .test()
            .assertError(IllegalStateException.class);
    }

    @Test
    public void executionObservables_noErrors() {
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                if (o == null) {
                    return Observable.error(new Exception("something wrong"));
                } else {
                    return Observable.just((String)o);
                }
            }
        });


        TestObserver<Observable<String>> testObserver = new TestObserver<>();
        command.executionObservables().subscribe(testObserver);

        command.execute(null);
        command.execute("1");
        command.execute("2");

        testObserver.assertValueCount(3);
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();
    }

    @Test
    public void switchToLatest_noErrors() {
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                if (o == null) {
                    return Observable.error(new Exception("something wrong"));
                } else {
                    return Observable.just((String)o);
                }
            }
        });

        TestObserver<String> testObserver = new TestObserver<>();
        command.switchToLatest().subscribe(testObserver);

        command.execute(null);
        command.execute("1");
        command.execute("2");

        testObserver.assertValueCount(2);
        testObserver.assertValues("1", "2");

    }

    @Test
    public void anErrorOccurredDuringExecution_emitThrowableAsValue() {

        final Throwable throwable = new IOException("something wrong");
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.error(throwable);
            }
        });

        TestObserver<Throwable> testObserver1 = new TestObserver<>();
        TestObserver<Throwable> testObserver2 = new TestObserver<>();
        command.errors().subscribe(testObserver1);
        command.errors().subscribe(testObserver2);

        command.execute(null);

        testObserver1.assertValue(throwable);
        testObserver2.assertValue(throwable);


    }

    @Test
    public void anErrorOccurredDuringExecution_noValue() {
        final Throwable throwable = new Exception("something wrong");
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.error(throwable);
            }
        });

        TestObserver<String> testObserver = new TestObserver<>();
        command.switchToLatest().subscribe(testObserver);

        command.execute(null);

        testObserver.assertNoValues();
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();
    }

    @Test
    public void executionWithError_showAndHideLoading() {
        final Throwable throwable = new IOException("something wrong");
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.<String>error(throwable)
                        .subscribeOn(Schedulers.newThread())
                        .delay(10, TimeUnit.MILLISECONDS);
            }
        });

        command.executing().test().assertValue(false);

        command.execute(null);

        command.executing().test().assertValue(true);


        command.executing().test()
                .awaitDone(15, TimeUnit.MILLISECONDS)
                .assertValues(true, false);

//
//        // wait
//        try {
//            Thread.sleep(15);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    //    command.executing().test().assertValue(false);

    }

    @Test
    public void executionWithValue_showAndHideLoading() {
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.just(VALUE)
                        .subscribeOn(Schedulers.newThread())
                        .delay(10, TimeUnit.MILLISECONDS);
            }
        });

        command.executing().test().assertValue(false);

        command.execute(null);

        command.executing().test().assertValue(true);

        // wait
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        command.executing().test().assertValue(false);
    }

    @Test
    public void executeAnotherTaskWhenExecuting_notAllowed() {

        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.just(VALUE)
                        .subscribeOn(Schedulers.newThread())
                        .delay(10, TimeUnit.MILLISECONDS);
            }
        });

        command.enabled()
                .test()
                .assertValue(true);

        command.execute(null);

        // wait
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        command.enabled()
                .test()
                .assertValue(false);

        command.execute(null)
                .test()
                .assertError(IllegalStateException.class);

        command.errors()
                .test()
                .assertNoValues();
    }

    @Test
    public void executeAnotherTaskWhenAllowingConcurrent_allowed() {

        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.just((String) o)
                        .subscribeOn(Schedulers.newThread())
                        .delay(10, TimeUnit.MILLISECONDS);
            }
        });

        command.setAllowsConcurrentExecution(true);

        command.execute("1");

        // wait
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        command.enabled()
                .test()
                .assertValue(true);

        command.execute("2")
                .test()
                .assertNoErrors();
    }

    @Test
    public void executionObservables_notAllowingConcurrent_onlyExecutionOnce() {
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.just((String)o).subscribeOn(Schedulers.newThread()).delay(10, TimeUnit.MILLISECONDS);
            }
        });

        TestObserver<Observable<String>> testObserver = new TestObserver<>();
        command.executionObservables().subscribe(testObserver);

        command.execute("1");
        command.execute("2");
        command.execute("3");

        // wait
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        testObserver.assertValueCount(1);
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();
    }

    @Test
    public void executionObservables_allowingConcurrent_executionMultiTimes() {
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.just((String)o).subscribeOn(Schedulers.newThread()).delay(10, TimeUnit.MILLISECONDS);
            }
        });

        // allows concurrent
        command.setAllowsConcurrentExecution(true);

        TestObserver<Observable<String>> testObserver = new TestObserver<>();
        TestObserver<String> stringTestObserver = new TestObserver<>();

        command.executionObservables()
                .subscribe(testObserver);

        command.executionObservables()
                .flatMap(new Function<Observable<String>, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(Observable<String> stringObservable) throws Exception {
                        return stringObservable;
                    }
                })
                .subscribe(stringTestObserver);

        command.execute("1");
        command.execute("2");
        command.execute("3");

        // wait
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        testObserver.assertValueCount(3);
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();

        stringTestObserver.assertValueCount(3);
        stringTestObserver.assertValueSet(Arrays.asList("1", "2", "3"));
    }

    @Test
    public void switchToLatest_notAllowingConcurrent_onlyHeadMostValue() {
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.just((String)o).subscribeOn(Schedulers.newThread()).delay(10, TimeUnit.MILLISECONDS);
            }
        });

        TestObserver<String> testObserver = new TestObserver<>();
        command.switchToLatest().subscribe(testObserver);

        command.execute("1");
        command.execute("2");
        command.execute("3");

        // wait
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        testObserver.assertValueCount(1);
        testObserver.assertValue("1");

    }

    @Test
    public void switchToLatest_allowingConcurrent_onlyLatestValue() {
        RxCommand<String> command = RxCommand.create(new Function<Object, Observable<String>>() {
            @Override
            public Observable<String> apply(Object o) throws Exception {
                return Observable.just((String)o).subscribeOn(Schedulers.newThread()).delay(10, TimeUnit.MILLISECONDS);
            }
        });

        // allows concurrent
        command.setAllowsConcurrentExecution(true);

        TestObserver<String> testObserver = new TestObserver<>();
        command.switchToLatest().subscribe(testObserver);

        command.execute("1");
        command.execute("2");
        command.execute("3");

        // wait
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        testObserver.assertValueCount(1);
        testObserver.assertValue("3");

    }
}
