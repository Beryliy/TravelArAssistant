package com.ninjaturtles.travelarassistant.presentation.map


import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
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
    private var placeMarkerStrategy: () -> Unit = ::placeMarker
    private val originMLD = MutableLiveData<LatLng>()
    val originLD: LiveData<LatLng> = originMLD
    private val destinationMLD = MutableLiveData<LatLng>()
    val destinationLD: LiveData<LatLng> = destinationMLD
    private val placeMarkerMLD = MutableLiveData<Unit>()
    val placeMarkerLD: LiveData<Unit> = placeMarkerMLD
    private val removeDestinationMarkerMLD = MutableLiveData<Unit>()
    private val openArMLD = MutableLiveData<Pair<LatLng, LatLng>>()
    val openArLD: LiveData<Pair<LatLng, LatLng>> = openArMLD
    private val pointsNotSelectedMLD = MutableLiveData<Unit>()
    val pointsNotSelectedLD: LiveData<Unit> = pointsNotSelectedMLD
    val removeDestinationMarkerLD: LiveData<Unit> = removeDestinationMarkerMLD
    val hoveringMarkerVisible = ObservableBoolean(true)
    val buttonText = ObservableField(resourcesDataSource.getString(R.string.place_marker))
    val arBtnClickable = ObservableBoolean(false)
    val arBtnColor = ObservableInt(resourcesDataSource.getColor(R.color.grey))

    init {
    }

    fun startTrackLocation() {
        viewModelScope.launch(coroutineContext) {
            locationDataSource.trackLocation().collect { locationResult ->
                when(locationResult) {
                    is LocationResult.Success -> {
                        locationResult.location?.lastLocation?.let { location ->
                            originMLD.postValue(LatLng(location.latitude, location.longitude))
                        }
                        arBtnClickable.set(true)
                        arBtnColor.set(resourcesDataSource.getColor(R.color.blue))
                    }
                    is LocationResult.Failure -> {}
                }
            }
        }
    }

    fun setDestinationLocation(destination: LatLng) {
        destinationMLD.postValue(destination)
    }

    fun openArView() {
        if(originLD.value != null && destinationLD.value != null) {
            openArMLD.postValue(Pair(originLD.value!!, destinationLD.value!!))
        } else {
            pointsNotSelectedMLD.postValue(Unit)
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