package com.tracker.trackerffvl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.tracker.trackerffvl.databinding.ActivityMainBinding

const val DEFAULT_FREQUENCY_SECONDS: Int = 60
const val PREFS_NAME = "MyPrefs" // name of the preference where to save
const val PREF_FREQUENCY = "UpdateFrequency" // update frequency
const val PREF_CATEGORY_NAME = "CategoryName" // paragliding, delta, ...
const val PREF_TRACKERKEY = "TrackerKey"
const val TRACKER_KEY_LENGTH = 32

class MainActivity : AppCompatActivity() {
    private var trackerKey: String? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Logger.setLogListener { logs ->
            runOnUiThread {
                binding.textViewLog.text = logs
            }
        }
        binding.textViewLog.movementMethod = ScrollingMovementMethod()

        trackerKey = getTrackerKey()
        if (trackerKey != null && trackerKey.toString().length == TRACKER_KEY_LENGTH) {
            binding.editTextTrackerKey.setText(trackerKey)
            binding.saveButton.text = getString(R.string.key_saved)
            binding.trackingButton.isEnabled = true
        }

        // button to scan the FFVL Tracker code
        binding.scanButton.setOnClickListener {
            // Start QR code scanner
            val options =
                GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .enableAutoZoom().build()
            val scanner = GmsBarcodeScanning.getClient(this, options)
            scanner.startScan().addOnSuccessListener { barcode ->
                // Task completed successfully
                val rawValue: String? = barcode.rawValue
                if (rawValue != null) {
                    binding.editTextTrackerKey.setText(rawValue)
                    saveTrackerKey(rawValue)
                }
            }.addOnCanceledListener {
                // Task canceled
                Logger.log(getString(R.string.scan_cancelled))
            }.addOnFailureListener { e ->
                // Task failed with an exception
                Logger.log(getString(R.string.scan_failed))
                Logger.log(e.toString())
            }

        }

        // action when save button is tapped
        binding.saveButton.setOnClickListener {
            saveTrackerKey(binding.editTextTrackerKey.text.toString())
        }

        // Add a TextWatcher to the editTextTrackerKey
        binding.editTextTrackerKey.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Enable or disable the saveButton based on whether the EditText is empty or not
                binding.saveButton.text = getString(R.string.save)
                binding.saveButton.isEnabled = !s.isNullOrBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No implementation needed
            }
        })

        // action when tracking button is tapped
        binding.trackingButton.setOnClickListener {
            toggleTracking()
        }

        // Get the array of frequencies from resources
        val frequenciesArray = resources.getStringArray(R.array.frequency_options)
        var selectedFrequency = 0
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val savedFrequency = sharedPreferences.getInt(PREF_FREQUENCY, 60)
        //val position = frequenciesArray.indexOf("$savedFrequency seconds")
        val position = frequenciesArray.indexOf("$savedFrequency seconds")
        var previousSelectedItemPosition = position

        if (position != -1) {
            binding.frequencySpinner.setSelection(position)
        }

        // Set a listener for frequency selection changes
        binding.frequencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    // Ensure the selected position is within the range of the frequencies array
                    if (position >= 0 && position < frequenciesArray.size) {
                        // Get the selected frequency string
                        val selectedFrequencyString = frequenciesArray[position]
                        // Extract the frequency value from the string (assuming it's in the format "xxx seconds")
                        selectedFrequency =
                            selectedFrequencyString.substringBefore(" ").toIntOrNull() ?: return
                        // save the selected update frequency
                        editor.putInt(PREF_FREQUENCY, selectedFrequency)
                        editor.apply()
                    }

                    if (position != previousSelectedItemPosition) {
                        Logger.log(
                            getString(R.string.frequencyChanged) + " $selectedFrequency " + getString(
                                R.string.seconds
                            )
                        )
                    }
                    previousSelectedItemPosition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }

        val categoryNameArray = resources.getStringArray(R.array.category_name_options)
        var savedCategoryName = sharedPreferences.getString(PREF_CATEGORY_NAME, "paragliding")
        val categoryNamePosition = categoryNameArray.indexOf("$savedCategoryName")
        var previousSelectedCategoryNamePosition = categoryNamePosition
        if (categoryNamePosition != -1) {
            binding.categoryNameSpinner.setSelection(categoryNamePosition)
        }
        binding.categoryNameSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    // Ensure the selected position is within the range of the categoryName array
                    if (position >= 0 && position < categoryNameArray.size) {
                        savedCategoryName = categoryNameArray[position]
                        // save the selected category name
                        editor.putString(PREF_CATEGORY_NAME, savedCategoryName)
                        editor.apply()
                    }

                    if (position != previousSelectedCategoryNamePosition) {
                        Logger.log(getString(R.string.categoryNameChanged) + " $savedCategoryName")
                    }
                    previousSelectedCategoryNamePosition = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }


        //location permission
        if (ActivityCompat.checkSelfPermission(
                this.baseContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                this.baseContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE
                    ),
                    123
                )
            }
        } else {
            TrackingUtils.isLocationPermissionGranted = true
        }
    }

    private fun saveTrackerKey(trackerKey: String) {
        // TODO check the length of the trackerKey string before doing anything

        // Get SharedPreferences instance
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Get editor to edit SharedPreferences
        val editor = sharedPreferences.edit()

        // Save the tracker key
        editor.putString(PREF_TRACKERKEY, trackerKey)
        editor.apply()

        binding.saveButton.text = getString(R.string.key_saved)
        binding.saveButton.isEnabled = false
        binding.trackingButton.isEnabled = true
    }

    private fun getTrackerKey(): String? {
        // Get SharedPreferences instance
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val trackerKey = sharedPreferences.getString(PREF_TRACKERKEY, null)
        val data = TrackingData
        return if (trackerKey.toString().length == 32) {
            data.trackerKey = trackerKey
            data.trackerKey
        } else {
            data.trackerKey = trackerKey
            data.trackerKey
        }
    }

    private fun toggleTracking() {
        if (getTrackerKey() != null) {
            if (!TrackingUtils.isTracking) {
                // start Tracking
                if (TrackingUtils.startTracking(this)) {
                    binding.trackingButton.text = getString(R.string.stop_tracking)
                }
            } else {
                // Stop tracking
                if (TrackingUtils.stopTracking(this)) {
                    binding.trackingButton.text = getString(R.string.start_tracking)
                }
            }
        } else {
            Logger.log(getString(R.string.ffvl_key_missing))
        }
    }

}

