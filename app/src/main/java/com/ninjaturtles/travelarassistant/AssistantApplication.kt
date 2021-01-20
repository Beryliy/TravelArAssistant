package com.ninjaturtles.travelarassistant

import android.app.Application
import com.ninjaturtles.travelarassistant.di.component.ApplicationComponent
import com.ninjaturtles.travelarassistant.di.component.DaggerApplicationComponent

class AssistantApplication: Application() {
    val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(applicationContext)
    }
}