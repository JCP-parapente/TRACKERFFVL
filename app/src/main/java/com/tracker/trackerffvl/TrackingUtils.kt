package com.tracker.trackerffvl

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.math.roundToInt

private const val FREQUENCY = "frequency"
private const val TAG = "TrackingUtils"

object TrackingUtils {
    var isTracking: Boolean = false
    var isLocationPermissionGranted: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var data = TrackingData

    fun startTracking(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val selectedFrequency = sharedPreferences.getInt(PREF_FREQUENCY, DEFAULT_FREQUENCY_SECONDS)

        val serviceIntent = Intent(context, TrackingService::class.java)
        serviceIntent.action = TrackingService.Actions.START.toString()
        serviceIntent.putExtra(FREQUENCY, selectedFrequency.toLong())
        ContextCompat.startForegroundService(context, serviceIntent)

        isTracking = true
        Logger.log(context.getString(R.string.tracker_has_started))
        return true
    }

    fun stopTracking(context: Context): Boolean {
        val serviceIntent = Intent(context, TrackingService::class.java)
        serviceIntent.action = TrackingService.Actions.STOP.toString()
        ContextCompat.startForegroundService(context, serviceIntent)

        isTracking = false
        Logger.log(context.getString(R.string.tracker_has_stopped))
        return true
    }

    fun getMode(): String {
        return data.mode
    }

    fun getGPSPosition(context: Context): Triple<Double, Double, Int> {
        if (isLocationPermissionGranted) {
            fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                data.latitude = 0.0
                data.longitude = 0.0
                data.altitude = 0
            } else { // we have permission to use location
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location: Location? ->
                    location?.let {
                        data.latitude = it.latitude
                        data.longitude = it.longitude
                        data.altitude = it.altitude.roundToInt()
                    }
                }.addOnFailureListener { exception: Exception ->
                    // Handle failure to retrieve current location
                    Log.d(TAG, exception.toString())
                }

            }
        }
        return Triple(data.latitude, data.longitude, data.altitude)
    }

    fun getDeviceType(): String {
        return data.deviceType
    }

    fun getDeviceVersion(context: Context): String {
        if (data.deviceVersion == "") {
            data.deviceVersion = try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName.toString()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                // Handle the exception, return a default value, or log an error message
                ""
            }
        }
        return data.deviceVersion
    }

    fun getFlyingState(): String {
        // TODO implement the flying state calculation
        data.flyingState = ""
        return data.flyingState
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        if (data.deviceId == "") {
            data.deviceId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
        return data.deviceId
    }

    fun getTimeStamp(): Long {
        data.ts = System.currentTimeMillis() / 1000
        return data.ts
    }


    fun getBatteryInfo(context: Context): Pair<Int, Double> { // }, uiListener: UIUpdateListener): Pair<Int, Double>? {
        // Get the battery manager
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // Get the battery level
        data.batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        // Get the battery voltage
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        data.batteryVoltage = voltage / 1000.0 // Convert mV to V

        return Pair(data.batteryLevel, data.batteryVoltage)
    }

    fun getCategoryName(context: Context): String {
        // TODO implement a dropdown list to select category
        //data.categoryName = "hike and flight"
        data.categoryName = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
            .getString(PREF_CATEGORY_NAME, "paragliding").toString()
        return data.categoryName
    }


}
