package com.ninjaturtles.travelarassistant.presentation.ar

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener
import com.mapbox.services.android.navigation.v5.route.RouteFetcher
import com.mapbox.services.android.navigation.v5.route.RouteListener
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress
import com.mapbox.vision.VisionManager
import com.mapbox.vision.ar.FenceVisualParams
import com.mapbox.vision.ar.LaneVisualParams
import com.mapbox.vision.ar.VisionArManager
import com.mapbox.vision.ar.core.models.ManeuverType
import com.mapbox.vision.ar.core.models.Route
import com.mapbox.vision.ar.core.models.RoutePoint
import com.mapbox.vision.mobile.core.interfaces.VisionEventsListener
import com.mapbox.vision.mobile.core.models.position.GeoCoordinate
import com.mapbox.vision.utils.VisionLogger
import com.ninjaturtles.travelarassistant.BuildConfig
import com.ninjaturtles.travelarassistant.R
import com.ninjaturtles.travelarassistant.global.BaseFragment
import kotlinx.android.synthetic.main.fragment_a_r.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ARFragment : BaseFragment(), ProgressChangeListener, OffRouteListener {

    private lateinit var origin: Point
    private lateinit var destination: Point
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var routeFetcher: RouteFetcher
    private lateinit var lastRouteProgress: RouteProgress
    private lateinit var directionsRoute: DirectionsRoute
    private val arLocationEngine by lazy {
        LocationEngineProvider.getBestLocationEngine(requireContext())
    }
    private val arLocationEngineRequest by lazy {
        LocationEngineRequest.Builder(0)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(1000)
            .build()
    }

    private val locationCallback by lazy {
        object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {}

            override fun onFailure(exception: Exception) {}
        }
    }

    override val layoutResourceId: Int = R.layout.fragment_a_r

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        origin = Point.fromLngLat(
            requireArguments().getFloat("originLongitude").toDouble(),
            requireArguments().getFloat("originLatitude").toDouble()
        )
        destination = Point.fromLngLat(
            requireArguments().getFloat("destinationLongitude").toDouble(),
            requireArguments().getFloat("destinationLatitude").toDouble()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_a_r, container, false)
    }

    override fun onStart() {
        super.onStart()
        startVisionManager()
        startNavigation()
    }

    override fun onStop() {
        super.onStop()
        stopVisionManager()
        stopNavigation()
    }

    private fun startVisionManager() {
        VisionManager.create()
        VisionManager.start()
        VisionManager.visionEventsListener = object : VisionEventsListener {}

        VisionArManager.create(VisionManager)
        ar_view.setArManager(VisionArManager)
        ar_view.setFenceVisible(true)
        ar_view.setLaneVisualParams(LaneVisualParams())
    }

    private fun startNavigation() {
        mapboxNavigation = MapboxNavigation(
            requireContext(),
            BuildConfig.MAPBOX_DOWNLOADS_TOKEN,
            MapboxNavigationOptions.builder().build()
        )
        routeFetcher = RouteFetcher(requireContext(), BuildConfig.MAPBOX_DOWNLOADS_TOKEN)
        routeFetcher.addRouteListener(object : RouteListener {
            override fun onResponseReceived(
                response: DirectionsResponse?,
                routeProgress: RouteProgress?
            ) {
                mapboxNavigation.stopNavigation()
                if (response?.routes()?.isEmpty() == true) {
                    Toast.makeText(requireContext(), "Can not calculate the route requested", Toast.LENGTH_SHORT).show()
                } else {
                    mapboxNavigation.startNavigation(response!!.routes()[0])
                    val route = response.routes()[0]
                    
// Set route progress.
                    VisionArManager.setRoute(
                        Route(
                            route.getRoutePoints(),
                            route.duration()?.toFloat() ?: 0f,
                            "",
                            ""
                        )
                    )
                }
            }

            override fun onErrorReceived(throwable: Throwable?) {
                mapboxNavigation.stopNavigation()
            }

        })
        try {
            arLocationEngine.requestLocationUpdates(
                arLocationEngineRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (se: SecurityException) {
            VisionLogger.e("ARAssistant", se.toString())
        }

        initDirectionsRoute()

        mapboxNavigation.addOffRouteListener(this)
        mapboxNavigation.addProgressChangeListener(this)
    }

    private fun stopVisionManager() {
        VisionArManager.destroy()
        VisionManager.stop()
        VisionManager.destroy()
    }

    private fun stopNavigation() {
            arLocationEngine.removeLocationUpdates(locationCallback)

            mapboxNavigation.removeProgressChangeListener(this)
            mapboxNavigation.removeOffRouteListener(this)
            mapboxNavigation.stopNavigation()
    }

    private fun initDirectionsRoute() {
        NavigationRoute.builder(requireContext())
            .accessToken(BuildConfig.MAPBOX_DOWNLOADS_TOKEN)
            .origin(origin)
            .destination(destination)
            .build()
            .getRoute(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    if (response.body() == null || response.body()!!.routes().isEmpty()) {
                        return
                    }

                    directionsRoute = response.body()!!.routes()[0]
                    response.body()!!.waypoints()?.map { directionsWaypoint ->
                        directionsWaypoint.location()
                    }
                    mapboxNavigation.startNavigation(directionsRoute)

// Set route progress.
                    VisionArManager.setRoute(
                        Route(
                            directionsRoute.getRoutePoints(),
                            directionsRoute.duration()?.toFloat() ?: 0f,
                            "",
                            ""
                        )
                    )
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    private fun DirectionsRoute.getRoutePoints(): Array<RoutePoint> {
        val routePoints = arrayListOf<RoutePoint>()
        legs()?.forEach { leg ->
            leg.steps()?.forEach { step ->
                val maneuverPoint = RoutePoint(
                    GeoCoordinate(
                        latitude = step.maneuver().location().latitude(),
                        longitude = step.maneuver().location().longitude()
                    ),
                    step.maneuver().type().mapToManeuverType()
                )
                routePoints.add(maneuverPoint)

                step.geometry()
                    ?.buildStepPointsFromGeometry()
                    ?.map { geometryStep ->
                        RoutePoint(
                            GeoCoordinate(
                                latitude = geometryStep.latitude(),
                                longitude = geometryStep.longitude()
                            )
                        )
                    }
                    ?.let { stepPoints ->
                        routePoints.addAll(stepPoints)
                    }
            }
        }

        return routePoints.toTypedArray()
    }

    fun String.buildStepPointsFromGeometry(): List<Point> {
        return PolylineUtils.decode(this, Constants.PRECISION_6)
    }

    fun String?.mapToManeuverType(): ManeuverType = when (this) {
        "turn" -> ManeuverType.Turn
        "depart" -> ManeuverType.Depart
        "arrive" -> ManeuverType.Arrive
        "merge" -> ManeuverType.Merge
        "on ramp" -> ManeuverType.OnRamp
        "off ramp" -> ManeuverType.OffRamp
        "fork" -> ManeuverType.Fork
        "roundabout" -> ManeuverType.Roundabout
        "exit roundabout" -> ManeuverType.RoundaboutExit
        "end of road" -> ManeuverType.EndOfRoad
        "new name" -> ManeuverType.NewName
        "continue" -> ManeuverType.Continue
        "rotary" -> ManeuverType.Rotary
        "roundabout turn" -> ManeuverType.RoundaboutTurn
        "notification" -> ManeuverType.Notification
        "exit rotary" -> ManeuverType.RoundaboutExit
        else -> ManeuverType.None
    }

    override fun onProgressChange(location: Location?, routeProgress: RouteProgress) {
        lastRouteProgress = routeProgress
    }

    override fun userOffRoute(location: Location?) {
        routeFetcher.findRouteFromRouteProgress(location, lastRouteProgress)
    }
}