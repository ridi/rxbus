package com.ridi.books.rxbus.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.android.RxLifecycleAndroid
import io.reactivex.subjects.BehaviorSubject
import java.util.*

/**
 * Created by kering on 2017. 3. 8..
 */

object RxActivityLifecycleProviderPool {
    private val subjects = WeakHashMap<Activity, BehaviorSubject<ActivityEvent>>()
    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) =
                getOrCreateSubject(activity).onNext(ActivityEvent.CREATE)

        override fun onActivityStarted(activity: Activity) =
                getOrCreateSubject(activity).onNext(ActivityEvent.START)

        override fun onActivityResumed(activity: Activity) =
                getOrCreateSubject(activity).onNext(ActivityEvent.RESUME)

        override fun onActivityPaused(activity: Activity) =
                getOrCreateSubject(activity).onNext(ActivityEvent.PAUSE)

        override fun onActivityStopped(activity: Activity) =
                getOrCreateSubject(activity).onNext(ActivityEvent.STOP)

        override fun onActivityDestroyed(activity: Activity) =
                getOrCreateSubject(activity).onNext(ActivityEvent.DESTROY)

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }
    }

    @Synchronized
    private fun getOrCreateSubject(target: Activity) = subjects[target] ?: run {
        val subject = BehaviorSubject.create<ActivityEvent>()
        subjects[target] = subject
        subject
    }

    private var initialized = false

    @Synchronized
    fun init(application: Application) {
        if (initialized) {
            throw IllegalStateException("RxActivityLifecycleProviderPool was already initialized!")
        }
        initialized = true
        application.registerActivityLifecycleCallbacks(callbacks)
    }

    @Synchronized
    fun provider(target: Activity): LifecycleProvider<ActivityEvent> {
        val subject = subjects[target] ?:
                throw IllegalStateException("No subject for target activity. Maybe RxActivityLifecycleProviderPool wasn't initialized yet.")
        return object : LifecycleProvider<ActivityEvent> {
            override fun <T> bindToLifecycle(): LifecycleTransformer<T> =
                    RxLifecycleAndroid.bindActivity(subject)

            override fun <T> bindUntilEvent(event: ActivityEvent): LifecycleTransformer<T> =
                    RxLifecycle.bindUntilEvent(subject, event)

            override fun lifecycle() = subject.hide()
        }
    }
}
