package com.ninjaturtles.travelarassistant.dataSource

import com.ninjaturtles.travelarassistant.entity.LocationResult
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    fun trackLocation(): Flow<LocationResult>
}