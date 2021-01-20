package com.ninjaturtles.travelarassistant.di.module

import android.content.Context

import com.mapbox.android.core.location.LocationEngineProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class ApplicationModule {

    companion object {
        @Provides
        @Singleton
        fun provideLocationEngine(context: Context) =
            LocationEngineProvider.getBestLocationEngine(context)
    }

}