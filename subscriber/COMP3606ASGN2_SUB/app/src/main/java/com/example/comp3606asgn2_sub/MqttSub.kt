package com.example.comp3606asgn2_sub

import android.content.Context
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.*
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

class MqttSub(private val context: Context) {

    private val serverHost = "broker.sundaebytestt.com"
    private val serverPort = 1883
    private val topic = "assignment/location"
    private lateinit var mqttClient: Mqtt5AsyncClient

    var updateMap: ((String, Double, Double) -> Unit)? = null

    fun connect() {
        mqttClient = MqttClient.builder()
            .useMqttVersion5()
            .identifier("Android-Subscriber")
            .serverHost(serverHost)
            .serverPort(serverPort)
            .buildAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                mqttClient.connect().await()

                mqttClient.subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .send()
                    .await()

                mqttClient.publishes(Mqtt5Publish::class.java).collect { message ->
                    val payload = message.payloadAsBytes?.toString(Charsets.UTF_8)
                    payload?.let {
                        try {
                            val (deviceId, latitude, longitude, speed) = parsePayload(it)
                            DbHelper(context).insertLocation(deviceId, latitude, longitude, speed)
                            updateMap?.invoke(deviceId, latitude, longitude)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun parsePayload(payload: String): Tuple4<String, Double, Double, Float> {
        return payload.split(",").let { parts ->
            Tuple4(
                parts[0].split(":")[1],
                parts[1].split(":")[1].toDouble(),
                parts[2].split(":")[1].toDouble(),
                parts[3].split(":")[1].toFloat()
            )
        }
    }
}
