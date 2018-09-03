# RxBus

[![Build Status](https://travis-ci.org/ridi/RxBus.svg?branch=master)](https://travis-ci.org/ridi/RxBus)
[![Release](https://jitpack.io/v/ridi/RxBus.svg)](https://jitpack.io/#ridi/RxBus)

Event bus framework supports sticky events and subscribers' priority based on [RxJava](https://github.com/ReactiveX/RxJava) 2.x

## Getting started

This library is distributed by [jitpack](https://jitpack.io).

You should add jitpack maven repository to build.gradle file of your project.

```
repositories {
    ...
    maven { url 'https://jitpack.io' }
    ...
}
```

Then you can include this library by adding dependency script to build.gradle file of your project.

```
dependencies {
    ...
    compile 'com.github.ridi:rxbus:<version>'
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
RxBus.asObservable(Event::class.java).subscribe { e ->
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
RxBus.asObservable(Event::class.java, sticky = true).subscribe { e ->
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
RxBus.asObservable(Event::class.java, priority = -1).subscribe { e ->
    System.out.println("-1 Priority : $e")
}
RxBus.asObservable(Event::class.java, priority = 1).subscribe { e ->
    System.out.println("1 Priority : $e")
}
RxBus.asObservable(Event::class.java).subscribe { e ->
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
