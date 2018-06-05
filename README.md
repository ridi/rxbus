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
    maven { url "https://jitpack.io" }
    ...
}
```

Then you can include this library by adding dependency script to build.gradle file of your project.

```
dependencies {
    ...
    compile 'com.github.ridi.RxBus:rxbus:<version>'

    // Or including Android plugin
    compile 'com.github.ridi.RxBus:rxbus-android:<version>'
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

### Android plugin

#### RxActivityLifecycleProviderPool

Initializing

```kotlin
class SomeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RxActivityLifecycleProviderPool.init(this)
    }
}
```

Usage

```kotlin
class SomeActivity : Activity() {
    override fun onCreate() {
        super.onCreate()
      
        RxBus.asObservable(Event::class.java)
            .compose(RxActivityLifecycleProviderPool.provider(this).bindToLifecycle())
            .subscribe { ... } // This subscription will survive until onDestroy()
      
        RxBus.asObservable(Event::class.java)
            .compose(RxActivityLifecycleProviderPool.provider(this)
                    .bindUntilEvent(ActivityEvent.PAUSE))
            .subscribe { ... } // This subscription will survive until onPause()
      
        Observable.create(...)
      		.compose(RxActivityLifecycleProviderPool.provider(this))
            .subscribe { ... } // Not only RxBus observable
    }
  
    override fun onStart() {
        super.onStart()
        RxBus.asObservable(Event::class.java)
            .compose(RxActivityLifecycleProviderPool.provider(this).bindToLifecycle())
            .subscribe { ... } // This subscription will survive until onStop()
    }
  
    override fun onResume() {
        super.onResume()
        RxBus.asObservable(Event::class.java)
            .compose(RxActivityLifecycleProviderPool.provider(this).bindToLifecycle())
            .subscribe { ... } // This subscription will survive until onPause()
    }
}
```

#### Simple extensions for Activity, Fragment, View

```kotlin
class SomeActivity : Activity() {
    ...
        // Automatic binding to activity lifecycle by RxActivityLifecycleProviderPool
        rxBusObservable(Event::class.java).subscribe { ... }
    ...
}
```

```kotlin
class SomeFragment : Fragment() {
    ...
        // Automatic binding to attachment/detachment of fragments' view
        rxBusObservable(Event::class.java).subscribe { ... }
    ...
}
```

```kotlin
class SomeView : View {
    ...
        // Automatic binding to attachment/detachment of view
        rxBusObservable(Event::class.java).subsribe { ... }
    ...
}
```

