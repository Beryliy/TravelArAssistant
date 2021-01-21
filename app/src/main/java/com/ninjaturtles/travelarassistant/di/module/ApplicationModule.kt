package com.ninjaturtles.travelarassistant.di.module

import android.content.Context

import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

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

        @Provides
        @Singleton
        fun provideResources(context: Context) = context.resources

        @Provides
        @Singleton
        @Named("io")
        fun provideCoroutineContext(): CoroutineContext = Dispatchers.IO

        private const val INTERVAL = 1000L
        private const val MAX_WAIT_TIME = 5000L
    }

}