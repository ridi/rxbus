package com.ridi.books.rxbus

import rx.Scheduler
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

/**
 * Created by kering on 2017. 1. 12..
 */
object RxBus {
    private val subject = SerializedSubject<Any, Any>(PublishSubject.create())

    @JvmStatic
    fun <T> subscribe(eventClass: Class<T>, callback: Action1<T>) =
            subscribe(eventClass, Schedulers.immediate(), callback)

    @JvmStatic
    fun <T> subscribe(eventClass: Class<T>, scheduler: Scheduler, callback: Action1<T>) =
            subject.ofType(eventClass).observeOn(scheduler).subscribe(callback)

    @JvmStatic
    fun post(event: Any) = subject.onNext(event)
}
