package com.ridi.books.rxbus

import rx.Scheduler
import rx.Subscription
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

/**
 * Created by kering on 2017. 1. 12..
 */
object RxBus {
    private val subject = SerializedSubject<Any, Any>(PublishSubject.create())

    var defaultScheduler = Schedulers.immediate()

    @JvmStatic
    @JvmOverloads
    fun <T> subscribe(eventClass: Class<T>, callback: Action1<T>,
                      scheduler: Scheduler = defaultScheduler): Subscription =
            subject.ofType(eventClass).observeOn(scheduler).subscribe(callback)

    @JvmStatic
    fun post(event: Any) = subject.onNext(event)
}
