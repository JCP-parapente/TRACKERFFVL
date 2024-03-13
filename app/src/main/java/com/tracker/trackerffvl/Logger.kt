package com.tracker.trackerffvl

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private val logLines = mutableListOf<String>()
    private var logListener: ((String) -> Unit)? = null

    fun setLogListener(listener: (String) -> Unit) {
        logListener = listener
    }

    fun log(message: String) {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.FRANCE) // Format date as 24-hour time
        val date = dateFormat.format(Date())

        val logLine = "[$date] $message"
        logLines.add(logLine)
        logListener?.invoke(logLines.joinToString("\n"))
    }
}