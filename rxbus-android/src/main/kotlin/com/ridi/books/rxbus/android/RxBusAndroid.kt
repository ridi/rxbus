package com.ridi.books.rxbus.android

import android.app.Activity
import android.app.Fragment
import android.view.View
import com.ridi.books.rxbus.RxBus
import com.trello.rxlifecycle2.android.RxLifecycleAndroid
import io.reactivex.Observable

/**
 * Created by kering on 2017. 3. 10..
 */

@JvmOverloads
fun <T> Activity.rxBusObservable(eventClass: Class<T>,
                                 sticky: Boolean = false, priority: Int = 0): Observable<T> =
        RxBus.asObservable(eventClass, sticky, priority)
                .compose(RxActivityLifecycleProviderPool.provider(this).bindToLifecycle())

@JvmOverloads
fun <T> Fragment.rxBusObservable(eventClass: Class<T>,
                                 sticky: Boolean = false, priority: Int = 0): Observable<T> =
        view.rxBusObservable(eventClass, sticky, priority)

@JvmOverloads
fun <T> View.rxBusObservable(eventClass: Class<T>,
                             sticky: Boolean = false, priority: Int = 0): Observable<T> =
        RxBus.asObservable(eventClass, sticky, priority).compose(RxLifecycleAndroid.bindView(this))
