package com.example.comp3606asgn2_sub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val pointsList = mutableListOf<CustomMarkerPoints>()
    private lateinit var mqttSub: MqttSub

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mqttSub = MqttSub(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { latLng ->
            addMarkerAtLocation(latLng)
        }
        mqttSub.updateMap = { deviceId, latitude, longitude ->
            addMarkerAtLocation(LatLng(latitude, longitude))
        }
    }

    private fun addMarkerAtLocation(latLng: LatLng) {
        val newCustomPoint = CustomMarkerPoints(pointsList.size + 1, latLng)
        pointsList.add(newCustomPoint)
        mMap.addMarker(MarkerOptions().position(latLng).title("Marker ${newCustomPoint.id}"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        drawPolyline()
    }

    private fun drawPolyline() {
        mMap.clear()
        val latLngPoints = pointsList.map { it.point }
        val polylineOptions = PolylineOptions()
            .addAll(latLngPoints)
            .color(com.google.android.gms.maps.model.Color.BLUE)
            .width(5f)
            .geodesic(true)
        mMap.addPolyline(polylineOptions)
        for (point in pointsList) {
            mMap.addMarker(MarkerOptions().position(point.point).title("Marker ${point.id}"))
        }
        val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
        latLngPoints.forEach { bounds.include(it) }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
    }
}

class CustomMarkerPoints(val id: Int, val point: com.google.android.gms.maps.model.LatLng)