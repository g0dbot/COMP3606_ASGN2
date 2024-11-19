package com.example.comp3606asgn2

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import java.util.UUID
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client.builder

class MainActivity : AppCompatActivity(), LocationListener {

    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var locationManager: LocationManager
    private var mqttClient: Mqtt5BlockingClient? = null
    private lateinit var studentId: EditText
    private var isPublishing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        studentId = findViewById(R.id.studentId)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mqttSetup()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }

        findViewById<Button>(R.id.startPublishingBtn).setOnClickListener {
            if (!isPublishing) {
                startPublishing()
            } else {
                stopPublishing()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPublishing) {
            locationManager.removeUpdates(this)
        }
        mqttClient?.disconnect()
    }

    private fun mqttSetup() {
        mqttClient = builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.sundaebytestt.com")
            .serverPort(1883)
            .buildBlocking()

        try {
            mqttClient?.connect()
            Toast.makeText(applicationContext, "MQTT Connected", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "MQTT Failed to Connect", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPublishing() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            if (mqttClient?.state == null) {
                Toast.makeText(this, "MQTT Client not connected", Toast.LENGTH_SHORT).show()
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, this)
            isPublishing = true
        }
    }

    private fun stopPublishing() {
        if (isPublishing) {
            locationManager.removeUpdates(this)
            isPublishing = false
        }
    }

    override fun onLocationChanged(location: Location) {
        val studentIdText = studentId.text.toString()
        if (studentIdText.isNotEmpty()) {
            val payload = "STUDENT_ID:$studentIdText,LATITUDE:${location.latitude},LONGITUDE:${location.longitude}"
            try {
                mqttClient?.publishWith()?.topic("assignment/location")
                    ?.payload(payload.toByteArray())
                    ?.send()
                Toast.makeText(this, "MQTT Published", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "MQTT Failed to Publish", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Student ID is empty", Toast.LENGTH_SHORT).show()
        }
    }
}