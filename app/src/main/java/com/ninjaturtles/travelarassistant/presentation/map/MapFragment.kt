package com.ninjaturtles.travelarassistant.presentation.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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
    private lateinit var destinationMarker: Symbol
    private lateinit var hoveringMarker: ImageView
    private lateinit var origin: LatLng
    private lateinit var destination: LatLng

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
        viewModel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]
        Mapbox.getInstance(requireContext(), BuildConfig.MAPBOX_DOWNLOADS_TOKEN)
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
                    viewModel.startTrackLocation()
                }
            }
            CAMERA_PERMISSION -> {
                if(grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    openArView()
                }
            }
        }
    }

    private fun observe() {
        viewModel.locationLD.observe(this, ::drawMarker)
        viewModel.placeMarkerLD.observe(this, { placeMarker() })
        viewModel.removeDestinationMarkerLD.observe(this, { removeDestinationMarker() })
    }

    private fun checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
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
            openArView()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION)
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

    private fun drawMarker(location: Location) {
        if(::symbolManager.isInitialized) {
            origin = LatLng(location.latitude, location.longitude)
            symbolManager.create(
                SymbolOptions().withLatLng(LatLng(location))
                    .withIconImage("originMarker")
            )
        }
    }

    private fun placeMarker() {
        mapboxMap?.cameraPosition?.target?.let { location ->
            destination = location
            destinationMarker = symbolManager.create(
                SymbolOptions().withLatLng(LatLng(location))
                    .withIconImage("destinationMarker")
            )
            viewModel.destination = location
        }
    }

    private fun removeDestinationMarker() {
        symbolManager.delete(destinationMarker)
    }

    private fun openArView() {
        val arguments = Bundle().apply {
            putFloat("originLongitude", origin.longitude.toFloat())
            putFloat("originLatitude", origin.latitude.toFloat())
            putFloat("destinationLongitude", destination.longitude.toFloat())
            putFloat("destinationLatitude", destination.latitude.toFloat())
        }
        findNavController().navigate(R.id.action_mapFragment_to_ARFragment, arguments)
    }

    companion object {
        private const val ACCESS_LOCATION_PERMISSION = 1
        private const val CAMERA_PERMISSION = 2
    }
}