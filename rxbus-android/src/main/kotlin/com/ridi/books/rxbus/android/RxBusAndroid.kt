package com.ridi.books.rxbus.android

import android.app.Activity
import android.app.Fragment
import android.view.View
import com.ridi.books.rxbus.RxBus
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.Observable

@JvmOverloads
fun <T> Activity.rxBusObservable(eventClass: Class<T>, sticky: Boolean = false, priority: Int = 0): Observable<T> =
    RxBus.asObservable(eventClass, sticky, priority).bindToLifecycle(RxActivityLifecycleProviderPool.provider(this))

@JvmOverloads
fun <T> Activity.rxBusObservable(
    eventClass: Class<T>,
    sticky: Boolean = false,
    priority: Int = 0,
    bindUntil: ActivityEvent
): Observable<T> = RxBus.asObservable(eventClass, sticky, priority)
    .bindUntilEvent(RxActivityLifecycleProviderPool.provider(this), bindUntil)

@JvmOverloads
fun <T> Fragment.rxBusObservable(eventClass: Class<T>, sticky: Boolean = false, priority: Int = 0): Observable<T> =
    RxBus.asObservable(eventClass, sticky, priority)
        .takeWhile { view != null }.bindToLifecycle(view)

@JvmOverloads
fun <T> android.support.v4.app.Fragment.rxBusObservable(
    eventClass: Class<T>,
    sticky: Boolean = false,
    priority: Int = 0
): Observable<T> = RxBus.asObservable(eventClass, sticky, priority)
    .takeWhile { view != null }.bindToLifecycle(view!!)

@JvmOverloads
fun <T> View.rxBusObservable(eventClass: Class<T>, sticky: Boolean = false, priority: Int = 0): Observable<T> =
    RxBus.asObservable(eventClass, sticky, priority).bindToLifecycle(this)
