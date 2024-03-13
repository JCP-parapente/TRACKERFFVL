package com.tracker.trackerffvl

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class TrackingWorker(private val context: Context, private val workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val taskNum = inputData.getInt("num", -1)
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

        println("In worker $workerParams.id: batteryLevel = ${data.batteryLevel.toString()}")
        println("In worker $workerParams.id: batteryVoltage = ${data.batteryVoltage.toString()}")
        println("In worker $workerParams.id: latitude = ${data.latitude.toString()}")
        println("In worker $workerParams.id: longitude = ${data.longitude.toString()}")

        //Logger.log("worker id: $workerParams.id")
        Logger.log("task num = $taskNum")
        Logger.log("batteryLevel = ${data.batteryLevel.toString()}")
        //Logger.log("batteryVoltage = ${data.batteryVoltage.toString()}")
        Logger.log("latitude = ${data.latitude.toString()}")
        Logger.log("longitude = ${data.longitude.toString()}")
        Logger.log("altitude = ${data.altitude.toString()}")

        val url : String = "https://data.ffvl.fr/api/?"+
                "key="+data.apiKey+
                "&ffvl_tracker_key="+data.trackerKey+
                "&mode="+data.mode+
                "&latitude="+data.latitude.toString()+
                "&longitude="+data.longitude.toString()+
                "&altitude="+data.altitude.toString()+
                "&ts="+data.ts.toString()+
                "&battery"+data.batteryLevel.toString()+
                "&v_bat"+data.batteryVoltage.toString()

        println(url)
        Logger.log(url)
        //val response = doGetHttpRequest(url)

        return Result.success()
    }



    private fun doGetHttpRequest(urlString: String) {
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
        println(response.toString().substring(0, kotlin.math.min(response.toString().length, 50)))
    }

    private fun onError(response: String?) {
        println("doGetHttpRequest onERROR")
        println(response.toString())
    }


    // for simulation purpose
    private fun collectDummyLatitude(): Double {
        return (0..100).random().toDouble() // Random number for demonstration
    }

    private fun collectDummyLongitude(): Double {
        return (0..100).random().toDouble() // Random number for demonstration
    }

    private fun collectDummyBatteryLevel(): Int {
        // Replace this with actual code to collect battery level
        return (0..100).random() // Random number for demonstration
    }
}
