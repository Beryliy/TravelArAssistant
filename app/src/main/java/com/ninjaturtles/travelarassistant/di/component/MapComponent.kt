package com.ninjaturtles.travelarassistant.di.component

import com.ninjaturtles.travelarassistant.di.scope.MapScope
import com.ninjaturtles.travelarassistant.presentation.map.MapFragment
import dagger.Subcomponent

@MapScope
@Subcomponent
interface MapComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): MapComponent
    }

    fun inject(mapFragment: MapFragment)
}