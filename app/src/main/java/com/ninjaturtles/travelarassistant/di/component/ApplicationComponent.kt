package com.ninjaturtles.travelarassistant.di.component

import android.content.Context
import com.ninjaturtles.travelarassistant.di.module.ApplicationModule
import com.ninjaturtles.travelarassistant.di.module.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}