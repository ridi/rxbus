package com.ridi.books.rxbus

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.junit.Assert
import org.junit.Test

class RxBusTest {
    open class Event
    class ChildEvent : Event()

    @Test
    fun testSubscribeAndPost() {
        var calledEvent: Event? = null
        val disposable = RxBus.asObservable(Event::class.java).subscribe { e -> calledEvent = e }

        try {
            val event = Event()
            RxBus.post(event)
            Assert.assertEquals(event, calledEvent)
        } finally {
            disposable.dispose()
        }
    }

    @Test
    fun testEventHierarchy() {
        var countAny = 0
        var countEvent = 0
        var countChildEvent = 0

        val disposables = CompositeDisposable(
                RxBus.asObservable(Any::class.java).subscribe { countAny++ }
        )

        try {
            RxBus.post(Any())
            Assert.assertEquals(1, countAny)

            disposables.add(RxBus.asObservable(Event::class.java).subscribe { countEvent++ })
            RxBus.post(Event())
            Assert.assertEquals(2, countAny)
            Assert.assertEquals(1, countEvent)

            disposables.add(RxBus.asObservable(ChildEvent::class.java).subscribe { countChildEvent++ })
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
            disposables.dispose()
        }
    }

    @Test
    fun testSticky() {
        var calledEvent: Event? = null
        var count = 0
        var event = Event()
        var disposable: Disposable? = null

        try {
            RxBus.postSticky(event)
            Assert.assertEquals(event, RxBus.getStickyEvent(Event::class.java))

            disposable = RxBus.asObservable(Event::class.java, sticky = true).subscribe { e ->
                calledEvent = e
                count++
            }
            Assert.assertEquals(event, calledEvent)
            Assert.assertEquals(1, count)
            RxBus.post(event)
            Assert.assertEquals(2, count)
            disposable.dispose()
            Assert.assertEquals(event, RxBus.removeStickyEvent(Event::class.java))

            disposable = RxBus.asObservable(Event::class.java, sticky = true).subscribe {
                count++
            }
            Assert.assertEquals(2, count)
            disposable.dispose()

            calledEvent = null
            event = Event()
            RxBus.postSticky(event)
            disposable = RxBus.asObservable(Event::class.java).subscribe { e ->
                calledEvent = e
                count++
            }
            Assert.assertNull(calledEvent)
            Assert.assertEquals(2, count)
            RxBus.post(event)
            Assert.assertEquals(event, calledEvent)
            Assert.assertEquals(3, count)
            RxBus.postSticky(event)
            Assert.assertEquals(4, count)
            disposable.dispose()
            Assert.assertTrue(RxBus.removeStickyEvent(event))

            val events = arrayOf(Event(), Any(), ChildEvent())
            events.forEach { RxBus.postSticky(it) }
            val calledEvents = mutableListOf<Any>()
            RxBus.asObservable(Any::class.java, sticky = true).subscribe { e ->
                calledEvents.add(e)
            }

            Assert.assertArrayEquals(arrayOf(events[2], events[0], events[1]), calledEvents.toTypedArray())
            Assert.assertEquals(events[1], RxBus.removeStickyEvent(Any::class.java))
            Assert.assertEquals(events[0], RxBus.removeStickyEvent(Event::class.java))
            Assert.assertEquals(events[2], RxBus.removeStickyEvent(ChildEvent::class.java))
        } finally {
            disposable!!.dispose()
        }
    }

    @Test
    fun testPriority() {
        val list = mutableListOf<Int>()
        val disposables = CompositeDisposable(
            RxBus.asObservable(Event::class.java).subscribe { list.add(0) },
            RxBus.asObservable(Event::class.java, priority = 1).subscribe { list.add(1) }
        )
        try {
            RxBus.post(Event())
            Assert.assertArrayEquals(arrayOf(1, 0), list.toTypedArray())
        } finally {
            disposables.dispose()
        }
    }
}
