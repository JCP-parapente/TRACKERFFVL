package com.tracker.trackerffvl

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TrackingService : Service() {
    private lateinit var executor: ScheduledExecutorService
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        executor = Executors.newSingleThreadScheduledExecutor()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrackingService::PartialWakeLock")
        wakeLock?.acquire()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action){
            Actions.START.toString() -> {
                val frequency = intent.getLongExtra("frequency", 60L)
                Logger.log("Frequency = $frequency")
                startForeground(NOTIFICATION_ID, createNotification())
                scheduleLocationUploadTask(frequency)
            }
            Actions.STOP.toString() -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
        wakeLock?.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                ""
            }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracker FFVL")
            .setContentText("Service Tracker")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "LocationUploadChannel"
        val channelName = "Location Upload"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    private fun scheduleLocationUploadTask(frequency:Long) {
        executor.scheduleAtFixedRate(
            { uploadLocationData(applicationContext) },
            0,
            frequency,
            TimeUnit.SECONDS
        )
    }

    private fun uploadLocationData(context: Context) {
        Log.d(TAG, "Uploading location data...")

        val data = TrackingData

        data.mode = TrackingUtils.getMode()

        val gpsPosition = TrackingUtils.getGPSPosition(context)
        data.latitude = gpsPosition.first
        data.longitude = gpsPosition.second
        data.altitude = gpsPosition.third

        data.deviceType = TrackingUtils.getDeviceType()
        data.deviceVersion = TrackingUtils.getDeviceVersion(context)
        data.deviceId = TrackingUtils.getDeviceId(context)

        data.ts = TrackingUtils.getTimeStamp()

        val battDetails = TrackingUtils.getBatteryInfo(context)
        data.batteryLevel = battDetails.first
        data.batteryVoltage = battDetails.second

        data.categoryName = TrackingUtils.getCategoryName(context)

        println("batteryLevel = ${data.batteryLevel.toString()}")
        //println("batteryVoltage = ${data.batteryVoltage.toString()}")
        println("latitude = ${data.latitude.toString()}")
        println("longitude = ${data.longitude.toString()}")

        Logger.log("batteryLevel = ${data.batteryLevel.toString()}")
        //Logger.log("batteryVoltage = ${data.batteryVoltage.toString()}")
        Logger.log("latitude = ${data.latitude.toString()}")
        Logger.log("longitude = ${data.longitude.toString()}")
        Logger.log("altitude = ${data.altitude.toString()}")
        Logger.log("category = ${data.categoryName}")

        val url: String = "https://data.ffvl.fr/api/?" +
                "key=" + data.apiKey +
                "&ffvl_tracker_key=" + data.trackerKey +
                "&mode=" + data.mode +
                "&latitude=" + data.latitude.toString() +
                "&longitude=" + data.longitude.toString() +
                "&altitude=" + data.altitude.toString() +
                "&ts=" + data.ts.toString() +
                "&battery=" + data.batteryLevel.toString() +
                "&v_bat=" + data.batteryVoltage.toString() +
                "&category_name=" + Uri.encode(data.categoryName) +
                "&accuracy=0"

        println(url)
        Logger.log("simulate: $url")
        if (data.latitude != 0.0) {
            val response = doGetHttpRequest(context, url)
            Log.d(TAG, response.toString())
        }


        //val response = doGetHttpRequest(context, url)
    }

    private fun doGetHttpRequest(context: Context, urlString: String) {
        val queue = Volley.newRequestQueue(context)

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, urlString,
            { response ->
                onResponse(response)
            },
            { error ->
                onError(error.toString())
            })
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun onResponse(response: String?) {
        println("response: ")
        println(response.toString().substring(0, kotlin.math.min(response.toString().length, 500)))
    }

    private fun onError(response: String?) {
        println("doGetHttpRequest onERROR")
        println(response.toString())
    }

    companion object {
        private const val NOTIFICATION_ID = 12345
        private const val TAG = "TrackingService"
    }

    enum class Actions { START, STOP }
}