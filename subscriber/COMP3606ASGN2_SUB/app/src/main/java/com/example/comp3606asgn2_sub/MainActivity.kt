package com.example.comp3606asgn2_sub

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MqttSub(this).connect()
        startActivity(Intent(this, MapsActivity::class.java))
        finish()
    }
}