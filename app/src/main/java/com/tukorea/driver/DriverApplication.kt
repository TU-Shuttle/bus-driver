package com.tukorea.driver

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DriverApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: DriverApplication
            private set
    }
}

