package com.ridi.books.rxbus

import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by kering on 2017. 1. 12..
 */
object RxBus {
    private val subject = SerializedSubject<Any, Any>(PublishSubject.create())
    private val stickyEventMap = ConcurrentHashMap<Class<*>, Any>()

    @JvmStatic
    @JvmOverloads
    fun <T> subscribe(eventClass: Class<T>, callback: Action1<T>,
                      scheduler: Scheduler = Schedulers.immediate()): Subscription =
            subject.ofType(eventClass).observeOn(scheduler).subscribe(callback)

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    @JvmOverloads
    fun <T> subscribeSticky(eventClass: Class<T>, callback: Action1<T>,
                            scheduler: Scheduler = Schedulers.immediate()): Subscription {
        synchronized(stickyEventMap) {
            val observable = subject.ofType(eventClass)
            return (stickyEventMap[eventClass]?.let { lastEvent ->
                observable.mergeWith(Observable.create { subscriber ->
                    subscriber.onNext(lastEvent as T)
                })
            } ?: observable).subscribeOn(scheduler).subscribe(callback)
        }
    }

    @JvmStatic
    fun post(event: Any) = subject.onNext(event)

    @JvmStatic
    fun postSticky(event: Any) {
        synchronized(stickyEventMap) {
            stickyEventMap.put(event.javaClass, event)
        }
        post(event)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> getStickyEvent(eventClass: Class<T>): T? {
        synchronized(stickyEventMap) {
            return stickyEventMap[eventClass] as T?
        }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> removeStickyEvent(eventClass: Class<T>): T? {
        synchronized(stickyEventMap) {
            return stickyEventMap.remove(eventClass) as T?
        }
    }
}
