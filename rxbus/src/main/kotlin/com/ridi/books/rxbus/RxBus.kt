package com.ridi.books.rxbus

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.*

/**
 * Created by kering on 2017. 1. 12..
 */
object RxBus {
    private val subjects = TreeMap<Int, Subject<Any>>()
    private val stickyEventMap = hashMapOf<Class<*>, Any>()
    private val subscriptionCounts = TreeMap<Int, Int>()

    @JvmStatic
    @JvmOverloads
    fun <T> asObservable(eventClass: Class<T>,
                         sticky: Boolean = false, priority: Int = 0): Observable<T> {
        val observable = synchronized(subjects) {
            (subjects[priority] ?: run {
                val subject = PublishSubject.create<Any>().toSerialized()
                subjects[priority] = subject
                subject
            }).ofType(eventClass)
        }
        return (if (sticky) {
            synchronized(stickyEventMap) {
                stickyEventMap[eventClass]?.let { lastEvent ->
                    observable.mergeWith(Observable.create { subscriber ->
                        subscriber.onNext(eventClass.cast(lastEvent))
                    })
                } ?: observable
            }
        } else {
            observable
        }).doOnSubscribe {
            synchronized(subjects) {
                subscriptionCounts[priority] = subscriptionCounts[priority]?.let { it + 1 } ?: 1
            }
        }.doOnDispose {
            synchronized(subjects) {
                subscriptionCounts[priority] = subscriptionCounts[priority]?.let { it - 1 } ?: 0
                if (subscriptionCounts[priority] == 0) {
                    subjects.remove(priority)
                    subscriptionCounts.remove(priority)
                }
            }
        }
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

    @JvmStatic
    fun <T> getStickyEvent(eventClass: Class<T>): T? {
        synchronized(stickyEventMap) {
            return eventClass.cast(stickyEventMap[eventClass])
        }
    }

    @JvmStatic
    fun <T> removeStickyEvent(eventClass: Class<T>): T? {
        synchronized(stickyEventMap) {
            return eventClass.cast(stickyEventMap.remove(eventClass))
        }
    }

    @JvmStatic
    fun removeStickyEvent(event: Any): Boolean {
        synchronized(stickyEventMap) {
            val eventClass = event.javaClass
            if (stickyEventMap[eventClass] == event) {
                stickyEventMap.remove(eventClass)
                return true
            } else {
                return false
            }
        }
    }
}
