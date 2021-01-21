package com.ninjaturtles.travelarassistant.presentation.map


import android.location.Location
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.ninjaturtles.travelarassistant.R
import com.ninjaturtles.travelarassistant.dataSource.LocationDataSource
import com.ninjaturtles.travelarassistant.dataSource.ResourcesDataSource
import com.ninjaturtles.travelarassistant.di.scope.MapScope
import com.ninjaturtles.travelarassistant.entity.LocationResult
import com.ninjaturtles.travelarassistant.global.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

@MapScope
class MapViewModel @Inject constructor(
    private val locationDataSource: LocationDataSource,
    private val resourcesDataSource: ResourcesDataSource,
    @Named("io") private val coroutineContext: CoroutineContext
) : BaseViewModel() {
    lateinit var origin: LatLng
    lateinit var destination: LatLng
    private var placeMarkerStrategy: () -> Unit = ::placeMarker
    private val locationMLD = MutableLiveData<Location>()
    val locationLD: LiveData<Location> = locationMLD
    private val placeMarkerMLD = MutableLiveData<Unit>()
    val placeMarkerLD: LiveData<Unit> = placeMarkerMLD
    private val removeDestinationMarkerMLD = MutableLiveData<Unit>()
    val removeDestinationMarkerLD: LiveData<Unit> = removeDestinationMarkerMLD
    val hoveringMarkerVisible = ObservableBoolean(true)
    val buttonText = ObservableField(resourcesDataSource.getString(R.string.place_marker))

    init {

    }

    fun startTrackLocation() {
        viewModelScope.launch(coroutineContext) {
            locationDataSource.trackLocation().collect { locationResult ->
                when(locationResult) {
                    is LocationResult.Success -> {
                        locationResult.location?.lastLocation?.let { location ->
                            locationMLD.postValue(location)
                        }
                    }
                    is LocationResult.Failure -> {}
                }
            }
        }
    }

    fun place() {
        placeMarkerStrategy()
    }

    private fun placeMarker() {
        hoveringMarkerVisible.set(false)
        buttonText.set(resourcesDataSource.getString(R.string.change_destination))
        placeMarkerMLD.postValue(Unit)
        placeMarkerStrategy = ::changeDestination
    }

    private fun changeDestination() {
        hoveringMarkerVisible.set(true)
        buttonText.set(resourcesDataSource.getString(R.string.place_marker))
        removeDestinationMarkerMLD.postValue(Unit)
        placeMarkerStrategy = ::placeMarker
    }

}