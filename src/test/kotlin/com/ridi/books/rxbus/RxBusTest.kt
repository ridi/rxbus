package com.ridi.books.rxbus

import net.jodah.concurrentunit.Waiter
import org.junit.Assert
import org.junit.Test
import rx.Subscription
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

/**
 * Created by kering on 2017. 1. 12..
 */
class RxBusTest {
    private val threadWaitingTimeoutMs = 10000L
    open class Event
    class ChildEvent : Event()

    @Test
    fun testSubscribeAndPost() {
        var calledEvent: Event? = null
        val subscription = RxBus.subscribe(Event::class.java, Action1 { e -> calledEvent = e })
        try {
            val event = Event()
            RxBus.post(event)
            Assert.assertEquals(event, calledEvent)
        } finally {
            subscription.unsubscribe()
        }
    }

    @Test
    fun testEventHierarchy() {
        var countAny = 0
        var countEvent = 0
        var countChildEvent = 0

        val subscription = CompositeSubscription(
                RxBus.subscribe(Any::class.java, Action1 {
                    countAny++
                })
        )

        try {
            RxBus.post(Any())
            Assert.assertEquals(1, countAny)

            subscription.add(RxBus.subscribe(Event::class.java, Action1 { countEvent++ }))
            RxBus.post(Event())
            Assert.assertEquals(2, countAny)
            Assert.assertEquals(1, countEvent)

            subscription.add(RxBus.subscribe(ChildEvent::class.java, Action1 { countChildEvent++ }))
            RxBus.post(ChildEvent())
            Assert.assertEquals(3, countAny)
            Assert.assertEquals(2, countEvent)
            Assert.assertEquals(1, countChildEvent)

            RxBus.post(Event())
            Assert.assertEquals(4, countAny)
            Assert.assertEquals(3, countEvent)
            Assert.assertEquals(1, countChildEvent)

            RxBus.post(Any())
            Assert.assertEquals(5, countAny)
            Assert.assertEquals(3, countEvent)
            Assert.assertEquals(1, countChildEvent)
        } finally {
            subscription.unsubscribe()
        }
    }

    @Test
    fun testSticky() {
        var calledEvent: Event? = null
        var count = 0
        var event = Event()
        var subscription: Subscription? = null

        try {
            RxBus.postSticky(event)
            Assert.assertEquals(event, RxBus.getStickyEvent(Event::class.java))

            subscription = RxBus.subscribe(Event::class.java, Action1 { e ->
                calledEvent = e
                count++
            }, sticky = true)
            Assert.assertEquals(event, calledEvent)
            Assert.assertEquals(1, count)
            RxBus.post(event)
            Assert.assertEquals(2, count)
            subscription.unsubscribe()
            Assert.assertEquals(event, RxBus.removeStickyEvent(Event::class.java))

            subscription = RxBus.subscribe(Event::class.java, Action1 { count++ }, sticky = true)
            Assert.assertEquals(2, count)
            subscription.unsubscribe()

            calledEvent = null
            event = Event()
            RxBus.postSticky(event)
            subscription = RxBus.subscribe(Event::class.java, Action1 { e ->
                calledEvent = e
                count++
            })
            Assert.assertNull(calledEvent)
            Assert.assertEquals(2, count)
            RxBus.post(event)
            Assert.assertEquals(event, calledEvent)
            Assert.assertEquals(3, count)
            RxBus.postSticky(event)
            Assert.assertEquals(4, count)
            subscription.unsubscribe()
            Assert.assertEquals(event, RxBus.removeStickyEvent(Event::class.java))
        } finally {
            subscription!!.unsubscribe()
        }
    }

    @Test
    fun testScheduler() {
        val waiter = Waiter()
        var calledThreadId = Long.MIN_VALUE
        val callback = Action1<Event> {
            calledThreadId = Thread.currentThread().id
            waiter.resume()
        }

        var subscription = RxBus.subscribe(Event::class.java, callback,
                scheduler = Schedulers.newThread())
        try {
            RxBus.post(Event())
            waiter.await(threadWaitingTimeoutMs)
            Assert.assertNotEquals(Thread.currentThread().id, calledThreadId)
            subscription.unsubscribe()

            RxBus.postSticky(Event())
            subscription = RxBus.subscribe(Event::class.java, callback,
                    sticky = true, scheduler = Schedulers.newThread())
            waiter.await(threadWaitingTimeoutMs)
            Assert.assertNotEquals(Thread.currentThread().id, calledThreadId)
        } finally {
            subscription.unsubscribe()
        }
    }

    @Test
    fun testPriority() {
        val stack = Stack<Int>()
        val subscription = CompositeSubscription(
            RxBus.subscribe(Event::class.java, Action1 {
                stack.push(1)
            }),
            RxBus.subscribe(Event::class.java, Action1 {
                stack.push(0)
            }, priority = 1)
        )

        try {
            RxBus.post(Event())
            Assert.assertEquals(1, stack.pop())
            Assert.assertEquals(0, stack.pop())
        } finally {
            subscription.unsubscribe()
        }
    }

    @Test
    fun testErrorHandler() {
        val message = "exception!"
        var receivedMessage: String? = null
        val subscription = RxBus.subscribe(Event::class.java, Action1 {
            throw RuntimeException(message)
        })
        RxBus.addErrorHandler(Action1 { throwable -> receivedMessage = throwable.message })
        try {
            RxBus.post(Event())
            Assert.assertEquals(message, receivedMessage)
        } finally {
            subscription.unsubscribe()
        }
    }
}
