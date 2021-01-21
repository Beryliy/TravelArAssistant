package com.ninjaturtles.travelarassistant.di.module

import androidx.lifecycle.ViewModel
import com.ninjaturtles.travelarassistant.di.factory.ViewModelKey
import com.ninjaturtles.travelarassistant.presentation.map.MapViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MapModule {
    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    abstract fun bindMapViewModel(
        mapViewModel: MapViewModel
    ): ViewModel
}