package com.ridi.books.rxbus

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.TreeMap

object RxBus {
    private val subjects = TreeMap<Int, Subject<Any>>()
    private val stickyEventMap = hashMapOf<Class<*>, Any>()
    private val subscriptionCounts = TreeMap<Int, Int>()

    @JvmStatic
    @JvmOverloads
    fun <T : Any> asObservable(eventClass: Class<T>, sticky: Boolean = false, priority: Int = 0): Observable<T> {
        val observable = synchronized(subjects) {
            (subjects[priority] ?: PublishSubject.create<Any>().toSerialized().also {
                subjects[priority] = it
            }).ofType(eventClass)
        }
        return (if (sticky) {
            synchronized(stickyEventMap) {
                stickyEventMap.filter { eventClass.isAssignableFrom(it.key) }
                    .toSortedMap { lhs, rhs ->
                        if (lhs.isAssignableFrom(rhs)) 1 else -1
                    }.map { it.value }
                    .fold(observable) { observable, lastEvent ->
                        observable.mergeWith(ObservableSource { observer ->
                            observer.onNext(eventClass.cast(lastEvent))
                        })
                    }
            }
        } else {
            observable
        }).doOnSubscribe {
            synchronized(subjects) {
                subscriptionCounts[priority] = subscriptionCounts[priority]?.let { count -> count + 1 } ?: 1
            }
        }.doOnDispose {
            synchronized(subjects) {
                subscriptionCounts[priority] = subscriptionCounts[priority]?.let { count -> count - 1 } ?: 0
                if (subscriptionCounts[priority] == 0) {
                    subjects.remove(priority)
                    subscriptionCounts.remove(priority)
                }
            }
        }
    }

    @JvmStatic
    fun post(event: Any) = synchronized(subjects) {
        subjects.descendingMap().toMap().forEach {
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
            return if (stickyEventMap[eventClass] === event) {
                stickyEventMap.remove(eventClass)
                true
            } else {
                false
            }
        }
    }
}
