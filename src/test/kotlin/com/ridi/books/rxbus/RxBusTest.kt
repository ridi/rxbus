package com.ridi.books.rxbus

import net.jodah.concurrentunit.Waiter
import org.junit.Assert
import org.junit.Test

import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * Created by kering on 2017. 1. 12..
 */
class RxBusTest {
    open class Event
    class ChildEvent : Event()

    @Test
    fun testSubscribeAndPost() {
        val event = Event()
        val subscription = RxBus.subscribe(Event::class.java, Action1 { e ->
            Assert.assertEquals(e, event)
        })
        RxBus.post(event)
        subscription.unsubscribe()
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
        RxBus.post(Any())
        Assert.assertEquals(countAny, 1)

        subscription.add(RxBus.subscribe(Event::class.java, Action1 { countEvent++ }))
        RxBus.post(Event())
        Assert.assertEquals(countAny, 2)
        Assert.assertEquals(countEvent, 1)

        subscription.add(RxBus.subscribe(ChildEvent::class.java, Action1 { countChildEvent++ }))
        RxBus.post(ChildEvent())
        Assert.assertEquals(countAny, 3)
        Assert.assertEquals(countEvent, 2)
        Assert.assertEquals(countChildEvent, 1)

        RxBus.post(Event())
        Assert.assertEquals(countAny, 4)
        Assert.assertEquals(countEvent, 3)
        Assert.assertEquals(countChildEvent, 1)

        RxBus.post(Any())
        Assert.assertEquals(countAny, 5)
        Assert.assertEquals(countEvent, 3)
        Assert.assertEquals(countChildEvent, 1)

        subscription.clear()
    }

    @Test
    fun testSticky() {
        var count = 0
        val event = Event()
        RxBus.postSticky(event)
        Assert.assertEquals(event, RxBus.getStickyEvent(Event::class.java))

        var subscription = RxBus.subscribeSticky(Event::class.java, Action1 { e ->
            Assert.assertEquals(e, event)
            count++
        })
        Assert.assertEquals(count, 1)
        RxBus.post(event)
        Assert.assertEquals(count, 2)
        subscription.unsubscribe()
        Assert.assertEquals(RxBus.removeStickyEvent(Event::class.java), event)

        subscription = RxBus.subscribeSticky(Event::class.java, Action1 { count++ })
        Assert.assertEquals(count, 2)
        subscription.unsubscribe()

        RxBus.postSticky(event)
        subscription = RxBus.subscribe(Event::class.java, Action1 { e ->
            Assert.assertEquals(e, event)
            count++
        })
        Assert.assertEquals(count ,2)
        RxBus.post(event)
        Assert.assertEquals(count, 3)
        RxBus.postSticky(event)
        Assert.assertEquals(count, 4)
        subscription.unsubscribe()
        Assert.assertEquals(RxBus.removeStickyEvent(Event::class.java), event)
    }

    @Test
    fun testScheduler() {
        val waiter = Waiter()
        val testThread = Thread.currentThread()
        val callback = Action1<Event> {
            Assert.assertNotEquals(testThread.id, Thread.currentThread().id)
            waiter.resume()
        }

        var subscription = RxBus.subscribe(Event::class.java, callback, Schedulers.newThread())
        RxBus.post(Event())
        waiter.await()
        subscription.unsubscribe()

        RxBus.postSticky(Event())
        subscription = RxBus.subscribeSticky(Event::class.java, callback, Schedulers.newThread())
        waiter.await()
        subscription.unsubscribe()
    }
}
