package com.ds.soonda.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log

class App : Application() {
    companion object {
        lateinit var SharedPrefHelper: SharedPreferences
        lateinit var uuid: String
        private var activityState: ActivityState = ActivityState.NONE

        private var running = 0


        fun getActivityState(): ActivityState {
            return activityState
        }
    }

    enum class ActivityState {
        NONE, BACKGROUND, FOREGROUND
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            Log.d("JDEBUG", "onActivityCreated()")
        }

        override fun onActivityStarted(activity: Activity?) {
            Log.d("JDEBUG", "onActivityStarted()")
            if (++running >= 1) {
                activityState = ActivityState.FOREGROUND
            }
        }

        override fun onActivityStopped(activity: Activity?) {
            if (--running <= 0) {
                activityState = ActivityState.BACKGROUND
            }
        }

        override fun onActivityResumed(activity: Activity?) {
            Log.d("JDEBUG", "onActivityResumed()")
        }

        override fun onActivityPaused(activity: Activity?) {
            Log.d("JDEBUG", "onActivityPaused()")
        }


        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            Log.d("JDEBUG", "onActivitySaveInstanceState()")
        }

        override fun onActivityDestroyed(activity: Activity?) {
            Log.d("JDEBUG", "onActivityDestroyed()")
        }
    }

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        SharedPrefHelper =
            applicationContext.getSharedPreferences("local_storage", Context.MODE_PRIVATE)
    }


}