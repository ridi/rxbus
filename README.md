# RxBus

[![Build Status](https://travis-ci.org/ridibooks/RxBus.svg?branch=master)](https://travis-ci.org/ridibooks/RxBus)
[![Release](https://jitpack.io/v/ridibooks/RxBus.svg)](https://jitpack.io/#ridibooks/RxBus)

Event bus framework supports sticky events and subscribers' priority based on [RxJava](https://github.com/ReactiveX/RxJava) 2.x

## Getting started

This library is distributed by [jitpack](https://jitpack.io).

You should add jitpack maven repository to build.gradle file of your root project.

```groovy
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

Then you can include this library by adding dependency script to build.gradle file of your project.

```groovy
dependencies {
    ...
    compile 'com.github.ridibooks.RxBus:rxbus:<version>'

    // Including Android plugin
    compile 'com.github.ridibooks.RxBus:rxbus-android:<version>'
    ...
}
```

## How to use

### Defining event class

```kotlin
class Event(val value: Int) {
    override fun toString() = "{value=$value}"
}
```

### Event subscription/posting

```kotlin
RxBus.observable(Event::class.java).subscribe { e ->
    System.out.println(e.toString())
}
RxBus.post(Event(0))
```

Output

```
{value=0}
```

### Sticky events

Sticky event 

```kotlin
RxBus.postSticky(Event(0))
RxBus.observable(Event::class:java, sticky = true).subscribe { e ->
    System.out.println(e.toString())
}
RxBus.post(Event(1))
```

Output

```
{value=0}
{value=1}
```

### Subscription priority

```kotlin
RxBus.observable(Event::class:java, priority = -1).subscribe { e ->
    System.out.println("-1 Priority : $e")
}
RxBus.observable(Event::class:java, priority = 1).subscribe { e ->
    System.out.println("1 Priority : $e")
}
RxBus.observable(Event::class:java).subscribe { e ->
    System.out.println("Default(0) Priority : $e")
}
RxBus.post(Event(0))
```

Output

```
1 Priority : {value=0}
Default(0) Priority : {value=0}
-1 Priority : {value=0}
```

### Android plugin

TBA

#### Activity lifecycle binding

TBA
