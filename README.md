# RxBus

[![Build Status](https://travis-ci.org/ridibooks/RxBus.svg?branch=master)](https://travis-ci.org/ridibooks/RxBus)
[![Release](https://jitpack.io/v/ridibooks/RxBus.svg)](https://jitpack.io/#ridibooks/RxBus)

Event bus framework supports sticky events and subscribers' priority based on [RxJava](https://github.com/ReactiveX/RxJava)

## Getting started

This library is distributed by [jitpack](https://jitpack.io).

You should add jitpack maven repository to build.gradle file of your root project.

```
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

Then you can include this library by adding dependency script to build.gradle file of your project.

```
dependencies {
    ...
    compile 'com.github.ridibooks:RxBus:<version>'
    ...
}
```
