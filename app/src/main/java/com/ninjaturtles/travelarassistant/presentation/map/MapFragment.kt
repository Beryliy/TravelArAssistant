package com.ninjaturtles.travelarassistant.presentation.map

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.ninjaturtles.travelarassistant.AssistantApplication
import com.ninjaturtles.travelarassistant.BuildConfig
import com.ninjaturtles.travelarassistant.R
import com.ninjaturtles.travelarassistant.databinding.FragmentMapBinding
import com.ninjaturtles.travelarassistant.global.BaseFragment
import kotlinx.android.synthetic.main.fragment_map.*
import javax.inject.Inject

class MapFragment : BaseFragment() {
    private lateinit var viewModel: MapViewModel
    private var mapboxMap: MapboxMap? = null
    private lateinit var style: Style
    private lateinit var symbolManager: SymbolManager
    private var originMarker: Symbol? = null
    private var destinationMarker: Symbol? = null
    private val locationManager: LocationManager by lazy {
        requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
    }
    private val settingsClient: SettingsClient by lazy {
        LocationServices.getSettingsClient(requireContext())
    }
    private val locaitonRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10_000L
            fastestInterval = 2_000L
        }
    }
    private val locationSettingsRequest: LocationSettingsRequest by lazy {
        LocationSettingsRequest.Builder()
            .addLocationRequest(locaitonRequest)
            .build()
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val layoutResourceId: Int
        get() = R.layout.fragment_map

    override fun onAttach(context: Context) {
        (context.applicationContext as AssistantApplication).applicationComponent
            .mapComponent()
            .create()
            .inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_DOWNLOADS_TOKEN)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]
        observe()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentMapBinding.inflate(inflater, container, false).apply {
            viewModel = this@MapFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        open_ar_b.setOnClickListener {
            checkCameraPermission()
        }
        mapView.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
                initStyle(style)
                this.style = style
                symbolManager = SymbolManager(mapView, mapboxMap, style).apply { iconAllowOverlap = true }
                viewModel.originLD.observe(viewLifecycleOwner, ::drawOriginMarker)
                viewModel.destinationLD.observe(viewLifecycleOwner, ::drawDestinationMarker)
            }
            this.mapboxMap = mapboxMap

        }
        checkLocationPermission()
    }

    override fun onStart() {
        super.onStart()
        mapView.onResume()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            ACCESS_LOCATION_PERMISSION -> {
                if(grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    turnOnGps()
                    viewModel.startTrackLocation()
                } else {
                    showLocationPermissionDeniedDialog()
                }
            }
            CAMERA_PERMISSION -> {
                if(grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    viewModel.openArView()
                } else {
                    showCameraPermissionDeniedDialog()
                }
            }
        }
    }

    private fun observe() {
        viewModel.placeMarkerLD.observe(this, { placeMarker() })
        viewModel.removeDestinationMarkerLD.observe(this, { removeDestinationMarker() })
        viewModel.openArLD.observe(this, { originDestination ->
            val arguments = Bundle().apply {
                putFloat("originLongitude", originDestination.first.longitude.toFloat())
                putFloat("originLatitude", originDestination.first.latitude.toFloat())
                putFloat("destinationLongitude", originDestination.second.longitude.toFloat())
                putFloat("destinationLatitude", originDestination.second.latitude.toFloat())
            }
            findNavController().navigate(R.id.action_mapFragment_to_ARFragment, arguments)
        })
        viewModel.pointsNotSelectedLD.observe(this, {
            showPointsNotSelectedDialog()
        })
    }

    private fun checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            turnOnGps()
            viewModel.startTrackLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_PERMISSION)
        }
    }

    private fun checkCameraPermission() {
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            viewModel.openArView()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
        }
    }

    private fun turnOnGps() {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnFailureListener{ exception ->
                    when((exception as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            (exception as ResolvableApiException).startResolutionForResult(
                                requireActivity(),
                                1
                            )
                        } catch(sie: IntentSender.SendIntentException) {
                            Log.i("map", "PendingIntent unable to execute request.")
                        }
                    }
                }
        }
    }

    private fun initStyle(style: Style) {
        BitmapUtils.getBitmapFromDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_marker, null)
        )?.let { markerBitmap ->
            style.addImage("originMarker", markerBitmap)
        }
        BitmapUtils.getBitmapFromDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_marker_red, null)
        )?.let { markerBitmap ->
            style.addImage("destinationMarker", markerBitmap)
        }
    }

    private fun drawOriginMarker(location: LatLng) {
        if(::symbolManager.isInitialized) {
            originMarker?.let {
                symbolManager.delete(it)
            }
            originMarker = symbolManager.create(
                SymbolOptions().withLatLng(location)
                    .withIconImage("originMarker")
            )
        }
    }

    private fun drawDestinationMarker(location: LatLng) {
        destinationMarker = symbolManager.create(
            SymbolOptions().withLatLng(LatLng(location))
                .withIconImage("destinationMarker")
        )
    }

    private fun placeMarker() {
        mapboxMap?.cameraPosition?.target?.let { location ->
            viewModel.setDestinationLocation(LatLng(location.latitude, location.longitude))
        }
    }

    private fun removeDestinationMarker() {
        destinationMarker?.let {
            symbolManager.delete(it)
        }
    }

    private fun showLocationPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(resources.getString(R.string.location_permission_denied))
            .setCancelable(true)
            .show()
    }

    private fun showCameraPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.camera_permission_denied)
            .setCancelable(true)
            .show()
    }

    private fun showPointsNotSelectedDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.points_not_specified)
            .setCancelable(true)
            .show()
    }

    companion object {
        private const val ACCESS_LOCATION_PERMISSION = 1
        private const val CAMERA_PERMISSION = 2
    }
}