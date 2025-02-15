package com.kotlinclasses

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import java.util.*

class TPGoogleMap(
    private val initialLocation: LatLng,
    private val mapView: MapView,
    private val cameraIdleListener: GoogleMap.OnCameraIdleListener
) {
    private var map: GoogleMap? = null
    private var circles = mutableListOf<Circle>()
    private var markers = mutableListOf<Marker>()
    
    var userLocationMarker: Marker? = null
    var defaultZoom: Float = 16.0f
    var maximumZoomOut: Float = 12.0f
    var maximumZoomIn: Float = 16.0f

    init {
        initializeMap()
        // تأكد من تهيئة Places API
        if (!Places.isInitialized()) {
            Places.initialize(mapView.context, "AIzaSyDxpEqZgRBofdqgwsu-uMpfEBWF7LNpKlA")
        }
    }

    private fun initializeMap() {
        mapView.getMapAsync { googleMap ->
            map = googleMap
            
            map?.apply {
                setOnCameraIdleListener(cameraIdleListener)
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isCompassEnabled = true
                    isMyLocationButtonEnabled = true
                }
                
                moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, defaultZoom))
                
                if (hasLocationPermission()) {
                    isMyLocationEnabled = true
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            mapView.context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            mapView.context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        if (!hasLocationPermission()) {
            callback(null)
            return
        }

        val locationManager = mapView.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (lastKnownLocation != null) {
                val currentLocation = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                callback(currentLocation)
            } else {
                callback(null)
            }
        } catch (e: SecurityException) {
            callback(null)
        }
    }

    fun displayMap() {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, defaultZoom))
    }

    fun displayCurrentUserLocation() {
        map?.let { googleMap ->
            val markerOptions = MarkerOptions()
                .position(initialLocation)
                .title("موقعك الحالي")
            userLocationMarker = googleMap.addMarker(markerOptions)
        }
    }

    fun createMarkers(coordinates: List<LatLng>, IDs: List<String?>, iconViews: List<View?>) {
        if (coordinates.size != IDs.size || coordinates.size != iconViews.size) {
            return
        }

        map?.let { googleMap ->
            coordinates.forEachIndexed { index, latLng ->
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(IDs[index])
                googleMap.addMarker(markerOptions)?.let { markers.add(it) }
            }
        }
    }

    fun displayMarkersOnMap() {
        markers.forEach { marker ->
            marker.isVisible = true
        }
    }

    fun displayMarkersOnMap(markersToShow: List<Marker>) {
        markersToShow.forEach { marker ->
            marker.isVisible = true
        }
    }

    fun getMarker(id: String): Marker? {
        return markers.find { it.title == id }
    }

    fun zoomIn(toValue: Float, inCoordinate: LatLng) {
        val zoomValue = if (toValue > maximumZoomIn) maximumZoomIn else toValue
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(inCoordinate, zoomValue)
        )
    }

    fun zoomOut(toValue: Float, inCoordinate: LatLng) {
        val zoomValue = if (toValue < maximumZoomOut) maximumZoomOut else toValue
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(inCoordinate, zoomValue)
        )
    }

    fun zoomInToMaximumValue(inCoordinate: LatLng) {
        zoomIn(maximumZoomIn, inCoordinate)
    }

    fun zoomOutToMaximumValue(inCoordinate: LatLng) {
        zoomOut(maximumZoomOut, inCoordinate)
    }

    fun createCircle(
        id: String,
        location: LatLng,
        color: Int = Color.argb(128, 51, 255, 51),
        strokeWidth: Float = 2.0f,
        radius: Double = 200.0
    ) {
        map?.let { googleMap ->
            val circleOptions = CircleOptions()
                .center(location)
                .radius(radius)
                .fillColor(color)
                .strokeWidth(strokeWidth)
            googleMap.addCircle(circleOptions)?.let {
                circles.add(it)
            }
        }
    }

    fun clearMarkersByIds(ids: List<String>) {
        markers.filter { marker -> marker.title in ids }.forEach { marker ->
            marker.remove()
        }
        markers.removeAll { marker -> marker.title in ids }
    }

    fun clearMarkerObjects(markerList: List<Marker>) {
        markerList.forEach { marker ->
            marker.remove()
            markers.remove(marker)
        }
    }

    fun clearCirclesByIds(ids: List<String>) {
        circles.forEach { circle ->
            circle.remove()
        }
        circles.clear()
    }

    fun clearCircleObjects(circleList: List<Circle>) {
        circleList.forEach { circle ->
            circle.remove()
            circles.remove(circle)
        }
    }

    fun navigateToLocation(location: LatLng) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(location, defaultZoom)
        )
    }

    fun getCountryName(location: LatLng, callback: (String?, Exception?) -> Unit) {
        try {
            val geocoder = Geocoder(mapView.context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                callback(addresses[0].countryName, null)
            } else {
                callback(null, Exception("لم يتم العثور على عنوان"))
            }
        } catch (e: Exception) {
            callback(null, e)
        }
    }

    fun getCountryCoordinate(countryName: String, callback: (LatLng?, Error?) -> Unit) {
        try {
            val geocoder = Geocoder(mapView.context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(countryName, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = LatLng(addresses[0].latitude, addresses[0].longitude)
                callback(location, null)
            } else {
                callback(null, Error("لم يتم العثور على الموقع"))
            }
        } catch (e: Exception) {
            callback(null, Error(e.message))
        }
    }

    fun getCityName(location: LatLng, callback: (String?, Error?) -> Unit) {
        try {
            val geocoder = Geocoder(mapView.context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                callback(addresses[0].locality, null)
            } else {
                callback(null, Error("لم يتم العثور على مدينة"))
            }
        } catch (e: Exception) {
            callback(null, Error(e.message))
        }
    }

    fun getPlacePredictions(query: String, callback: (List<String>, Exception?) -> Unit) {
        try {
            val token = AutocompleteSessionToken.newInstance()
            val request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.REGIONS)
                .setSessionToken(token)
                .setQuery(query)
                .build()

            Places.createClient(mapView.context).findAutocompletePredictions(request)
                .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                    val predictions = response.autocompletePredictions.map { 
                        it.getFullText(null).toString()
                    }
                    callback(predictions, null)
                }
                .addOnFailureListener { exception: Exception ->
                    if (exception is ApiException) {
                        Log.e("Places", "Place not found: " + exception.statusCode)
                    }
                    callback(emptyList(), exception)
                }
        } catch (e: Exception) {
            callback(emptyList(), e)
        }
    }

    fun moveToCurrentLocation() {
        getCurrentLocation { location ->
            location?.let { latLng ->
                navigateToLocation(latLng)
                // إزالة العلامة القديمة إن وجدت
                userLocationMarker?.remove()
                // إضافة علامة جديدة
                map?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("موقعك الحالي")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )?.let {
                    userLocationMarker = it
                }
            }
        }
    }

    /**
     * تمثيل لنتائج البحث داخل الدائرة
     * @property markers قائمة العلامات داخل الدائرة
     * @property count عدد العلامات داخل الدائرة
     * @property titles قائمة بعناوين العلامات داخل الدائرة
     */
    data class CircleSearchResult(
        val markers: List<Marker>,
        val count: Int,
        val titles: List<String>
    )

    /**
     * الحصول على العلامات الموجودة داخل دائرة محددة
     * @param circleId معرف الدائرة المراد البحث فيها
     * @return نتيجة البحث تحتوي على العلامات وعددها وعناوينها
     */
    fun GetMarkersInsideCircle(circleId: String): CircleSearchResult {
        val circle = circles.find { it.id == circleId } ?: return CircleSearchResult(emptyList(), 0, emptyList())
        
        val markersInside = markers.filter { marker ->
            val distance = FloatArray(1)
            android.location.Location.distanceBetween(
                circle.center.latitude,
                circle.center.longitude,
                marker.position.latitude,
                marker.position.longitude,
                distance
            )
            distance[0] <= circle.radius
        }

        return CircleSearchResult(
            markers = markersInside,
            count = markersInside.size,
            titles = markersInside.mapNotNull { it.title }
        )
    }

    /**
     * الحصول على العلامات الموجودة داخل جميع الدوائر
     * @return خريطة تحتوي على نتائج البحث لكل دائرة
     */
    fun GetMarkersInsideCircles(): Map<String, CircleSearchResult> {
        return circles.associate { circle ->
            val markersInside = markers.filter { marker ->
                val distance = FloatArray(1)
                android.location.Location.distanceBetween(
                    circle.center.latitude,
                    circle.center.longitude,
                    marker.position.latitude,
                    marker.position.longitude,
                    distance
                )
                distance[0] <= circle.radius
            }
            
            circle.id to CircleSearchResult(
                markers = markersInside,
                count = markersInside.size,
                titles = markersInside.mapNotNull { it.title }
            )
        }
    }
}