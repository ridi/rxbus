package com.ridi.books.rxbus

import rx.functions.Action1
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import rx.subscriptions.CompositeSubscription

/**
 * Created by kering on 2017. 1. 12..
 */
object RxBus {
    private val eventsSubject = SerializedSubject<Any, Any>(PublishSubject.create())
    private val subscriptions = hashMapOf<Any, CompositeSubscription>()

    @Suppress("UNCHECKED_CAST")
    fun <T> register(owner: Any, eventClass: Class<T>, onNext: Action1<T>) {
        val subscription = eventsSubject.filter { event -> event.javaClass == eventClass }
                .map { obj -> obj as T }
                .subscribe(onNext)
        subscriptions[owner]?.run {
            add(subscription)
        } ?: run {
            subscriptions[owner] = CompositeSubscription(subscription)
        }
    }

    fun unregister(owner: Any) {
        subscriptions[owner]?.clear()
        subscriptions.remove(owner)
    }

    fun post(event: Any) = eventsSubject.onNext(event)
}
