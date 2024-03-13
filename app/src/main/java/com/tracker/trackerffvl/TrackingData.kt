package com.tracker.trackerffvl
// refer to https://data.ffvl.fr/api/?help=tracker

private const val API_KEY = "zozozlafritte"
private const val MODE = "push" // push or read
private const val DEVICE_TYPE = "Tracker FFVL - Android Application"

object TrackingData {
    const val apiKey: String = API_KEY
    var trackerKey: String? = ""
    var mode: String = MODE // push or read
    var sos: Int = 0 // 1 means, the device is sending SOS
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Int = 0
    var deviceType: String = DEVICE_TYPE
    var deviceVersion: String = ""
    var flyingState: String = "" // 1 if flying, 0 if not flying, empty if unknown
    var deviceId: String = ""
    var vSpeed: Double = 0.0
    var hSpeed: Double = 0.0
    var heading: Double = 0.0
    var ts: Long = 0
    var temperature: Double = 0.0
    var pressure: Double = 0.0
    var batteryLevel: Int = 0
    var batteryVoltage: Double = 0.0
    var categoryName: String = ""
}



