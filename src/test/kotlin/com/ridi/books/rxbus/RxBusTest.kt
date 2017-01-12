package com.ridi.books.rxbus

import org.junit.Assert
import org.junit.Test

import rx.functions.Action1

/**
 * Created by kering on 2017. 1. 12..
 */
class RxBusTest {
    class Event

    @Test
    fun simpleEventTest() {
        RxBus.register(Event::class.java, Action1 {
            Assert.assertTrue(true)
        })
        RxBus.post(Event())


    }

    class IntValueEvent(val value: Int)

    @Test
    fun intValueEventTest() {
        RxBus.register(IntValueEvent::class.java, Action1 { e ->
            Assert.assertEquals(e.value, 100)
        })
        RxBus.post(IntValueEvent(100))
    }
}
