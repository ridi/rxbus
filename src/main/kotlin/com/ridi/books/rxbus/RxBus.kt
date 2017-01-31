package com.ridi.books.rxbus

import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject
import java.util.*

/**
 * Created by kering on 2017. 1. 12..
 */
object RxBus {
    private val subjects = TreeMap<Int, Subject<Any, Any>>()
    private val subscriptionCounts = hashMapOf<Int, Int>()
    private val stickyEventMap = hashMapOf<Class<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    @JvmStatic
    @JvmOverloads
    fun <T> subscribe(eventClass: Class<T>, callback: Action1<T>,
                      sticky: Boolean = false, priority: Int = 0,
                      scheduler: Scheduler = Schedulers.immediate()): Subscription {
        val observable = (subjects[priority] ?: run {
            val subject = SerializedSubject<Any, Any>(PublishSubject.create())
            subjects[priority] = subject
            subject
        }).ofType(eventClass)

        subscriptionCounts[priority] = (subscriptionCounts[priority] ?: 0) + 1
        return ((if (sticky) stickyEventMap[eventClass] else null)?.let { lastEvent ->
            observable.mergeWith(Observable.create { subscriber ->
                subscriber.onNext(lastEvent as T)
            })
        } ?: observable).doOnUnsubscribe {
            synchronized(this) {
                subscriptionCounts[priority]?.let { count ->
                    if (count > 1) {
                        subscriptionCounts[priority] = count - 1
                    } else {
                        subscriptionCounts.remove(priority)
                        subjects.remove(priority)
                    }
                }
            }
        }.observeOn(scheduler).subscribe(callback)
    }

    @JvmStatic
    fun post(event: Any) = synchronized(subjects) {
        subjects.descendingMap().forEach {
            it.value.onNext(event)
        }
    }

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
