package com.example.comp3606asgn2_sub

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "location_db.sql", factory, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createLocationTableQuery = ("CREATE TABLE Locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "deviceId TEXT," +
                "latitude REAL," +
                "longitude REAL," +
                "speed REAL," +
                "timestamp INTEGER)")
        db.execSQL(createLocationTableQuery)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // Handle upgrades if necessary
    }

    fun insertLocation(deviceId: String, latitude: Double, longitude: Double, speed: Float) {
        val values = ContentValues()
        values.put("deviceId", deviceId)
        values.put("latitude", latitude)
        values.put("longitude", longitude)
        values.put("speed", speed)
        values.put("timestamp", System.currentTimeMillis())

        val db = writableDatabase
        db.insert("Locations", null, values)
        db.close()
    }

    fun getDeviceLocations(deviceId: String): List<LocationData> {
        val locations: MutableList<LocationData> = mutableListOf()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Locations WHERE deviceId =?", arrayOf(deviceId))

        if (cursor.moveToFirst()) {
            do {
                locations.add(LocationData(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getDouble(3),
                    cursor.getFloat(4),
                    cursor.getLong(5)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return locations
    }
}

data class LocationData(
    val id: Int,
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val timestamp: Long
)