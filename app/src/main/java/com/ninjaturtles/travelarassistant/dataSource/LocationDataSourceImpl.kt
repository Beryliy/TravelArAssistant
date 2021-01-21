package com.ninjaturtles.travelarassistant.dataSource

import android.os.Looper.getMainLooper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.ninjaturtles.travelarassistant.entity.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationDataSourceImpl @Inject constructor(
    private val locationEngine: LocationEngine,
    private val locationRequest: LocationEngineRequest
) : LocationDataSource {
    override fun trackLocation(): Flow<LocationResult> = callbackFlow {
        val callback = object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                sendBlocking(LocationResult.Success(result))
            }

            override fun onFailure(exception: Exception) {
                sendBlocking(LocationResult.Failure(exception))
            }
        }
        locationEngine.requestLocationUpdates(
            locationRequest,
            callback,
            getMainLooper()
        )
        awaitClose { locationEngine.removeLocationUpdates(callback)}
    }

}