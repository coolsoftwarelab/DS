package com.ds.soonda.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class App : Application() {
    companion object {
        lateinit var SharedPrefHelper: SharedPreferences
        lateinit var uuid: String

        var alreadyReceivedRentNum: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        SharedPrefHelper =
            applicationContext.getSharedPreferences("local_storage", Context.MODE_PRIVATE)

    }
}