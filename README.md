# RxCommand
A lightweight, RxJava-based library that helps ViewModel provide commands to bind to View.


## Features 

* Based on RxJava, has all the advantages of RxJava
* Separate concerns, easy to selectively handle the state of task execution (enabled,executing, error, completion, etc.)
* Make your business logic code centralized for easy reading and maintenance

## How to use 

### Demo

![login](./screenshot/login.png) 

Suppose we need to make a login page.

* When the phone number is illegal, the **Fetch Captcha** button is disable
* When the captcha is being acquired, the **Fetch Captcha** button is disable and displays loading
* When fetching captcha  successfully, the countdown starts, the **Fetch Captcha** button is still disable, the countdown ends, the button is enable again
* When fetching captcha failed, the countdown is not started and the **Fetch Captcha** button restores the enable state
* When the phone number and captcha are valid, the **Login** button is  clickable, otherwise it can not be clicked
* When starting login command, the **Login** button is in disable state, and a loading showing.
* When the login  successful, hide loading, jump to the MainActivity.
* When the login failed, stop loading and prompt for an error.

How to achieve the product request？ All in the sample module.

### Usage

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
