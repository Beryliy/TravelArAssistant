package com.ninjaturtles.travelarassistant.di.module

import com.ninjaturtles.travelarassistant.dataSource.LocationDataSource
import com.ninjaturtles.travelarassistant.dataSource.LocationDataSourceImpl
import com.ninjaturtles.travelarassistant.dataSource.ResourcesDataSource
import com.ninjaturtles.travelarassistant.dataSource.ResourcesDataSourceImpl
import dagger.Binds
import dagger.Module

@Module
abstract class DataSourceModule {
    @Binds
    abstract fun bindLocationDataSource(
        locationDataSourceImpl: LocationDataSourceImpl
    ): LocationDataSource

    @Binds
    abstract fun bindResourcesDataSource(
        resourcesDataSourceImpl: ResourcesDataSourceImpl
    ): ResourcesDataSource
}