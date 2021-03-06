package com.ninjaturtles.travelarassistant.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ninjaturtles.travelarassistant.di.factory.ViewModelFactory
import com.ninjaturtles.travelarassistant.di.factory.ViewModelKey
import com.ninjaturtles.travelarassistant.presentation.map.MapViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory


}