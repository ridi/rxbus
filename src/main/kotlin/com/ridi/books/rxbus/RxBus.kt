package com.ridi.books.rxbus

import rx.Subscription
import rx.functions.Action1
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

/**
 * Created by kering on 2017. 1. 12..
 */
object RxBus {
    private val busSubject = SerializedSubject<Any, Any>(PublishSubject.create())

    @Suppress("UNCHECKED_CAST")
    fun <T> register(eventClass: Class<T>, onNext: Action1<T>): Subscription {
        return busSubject.filter { event -> event.javaClass == eventClass }
                .map { obj -> obj as T }
                .subscribe(onNext)
    }

    fun post(event: Any) = busSubject.onNext(event)
}
