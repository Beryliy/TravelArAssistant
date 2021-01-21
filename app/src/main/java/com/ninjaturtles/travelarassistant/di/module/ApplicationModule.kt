package com.ninjaturtles.travelarassistant.di.module

import android.content.Context

import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
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

        @Provides
        @Singleton
        fun provideLocationRequest() = LocationEngineRequest.Builder(INTERVAL)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(MAX_WAIT_TIME)
            .build()

        private const val INTERVAL = 1000L
        private const val MAX_WAIT_TIME = 5000L
    }

}