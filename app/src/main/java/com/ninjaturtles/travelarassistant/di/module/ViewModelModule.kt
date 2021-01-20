package com.ninjaturtles.travelarassistant.di.module

import androidx.lifecycle.ViewModelProvider
import com.ninjaturtles.travelarassistant.di.factory.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}