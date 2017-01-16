package com.ridi.books.rxbus

import org.junit.Assert
import org.junit.Test

import rx.functions.Action1
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
}
