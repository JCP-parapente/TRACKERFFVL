package com.tracker.trackerffvl

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Helper {
    companion object {
        fun formatMessage(message: String): CharSequence {
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.FRANCE) // Format date as 24-hour time
            val date = dateFormat.format(Date())
            return "\n[$date] $message" // Add new log message
        }
    }
}