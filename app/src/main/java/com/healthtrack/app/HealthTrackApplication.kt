package com.healthtrack.app

import android.app.Application
import com.healthtrack.app.di.AppContainer
import com.healthtrack.app.di.DefaultAppContainer

class HealthTrackApplication : Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
