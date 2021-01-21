package com.ninjaturtles.travelarassistant.dataSource

import com.mapbox.android.core.location.LocationEngineResult
import java.lang.Exception

sealed class LocationResult {
    data class Success(val location: LocationEngineResult): LocationResult()
    data class Failure(val exception: Exception)
}
