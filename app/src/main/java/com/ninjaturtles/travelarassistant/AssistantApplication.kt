package com.ninjaturtles.travelarassistant

import android.app.Application
import com.mapbox.vision.VisionManager
import com.ninjaturtles.travelarassistant.di.component.ApplicationComponent
import com.ninjaturtles.travelarassistant.di.component.DaggerApplicationComponent

class AssistantApplication: Application() {
    val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        VisionManager.init(this, BuildConfig.MAPBOX_DOWNLOADS_TOKEN)
    }
}