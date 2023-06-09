package com.example.securityapps

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.securityapps.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 2
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val CALL_PERMISSION_REQUEST_CODE = 300
    private val OVERLAY_PERMISSION_REQUEST_CODE = 200
    private val FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE = 400
    private val REQUEST_CODE_PERMISSION = 123
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnaRequestOverlayPermission:Button
    private lateinit var btnRequestCallPermission:Button
    private lateinit var btnRequestLocationPermission:Button
    private lateinit var btnStopMusic:Button

    private lateinit var btnMainRecyclerView:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Login Credentials
auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if(currentUser==null)
        {
            redirectToSignIn()
        }
        // Initialize buttons
         btnRequestCallPermission = findViewById(R.id.button)
        btnRequestLocationPermission = findViewById(R.id.button2)
         btnaRequestOverlayPermission= findViewById(R.id.button3)
        btnStopMusic= findViewById(R.id.button4)

btnMainRecyclerView = findViewById(R.id.button_main)
        // Set buttons initially invisible
        btnRequestCallPermission.visibility = View.INVISIBLE
        btnRequestLocationPermission.visibility = View.INVISIBLE
        btnaRequestOverlayPermission.visibility = View.INVISIBLE


        createNotificationChannel()

btnMainRecyclerView.setOnClickListener {
    startActivity(Intent(this,MainRecyclerView::class.java))

}

        // Request location permission
        btnRequestLocationPermission.setOnClickListener {
            requestLocationPermission()
        }

        // Request call permission
        btnRequestCallPermission.setOnClickListener {
            requestCallPermission()
        }

        btnaRequestOverlayPermission.setOnClickListener {
            requestOverlayPermission()
        }

        btnStopMusic.setOnClickListener {
            if (SecurityService.mediaPlayer?.isPlaying == true) {
                SecurityService.mediaPlayer?.pause()
            }
        }



        // Request camera permission
        requestCameraPermission()

        startSecurityService()
    }

    private fun redirectToSignIn() {
        startActivity(Intent(this,SignInActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()

        // Check if permissions are already granted and update button visibility
        checkPermissionStatus()
    }

    private fun checkPermissionStatus() {
        // Check location permission status
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            findViewById<Button>(R.id.button2).visibility = View.INVISIBLE
        } else {
            findViewById<Button>(R.id.button2).visibility = View.VISIBLE
        }

        // Check call permission status
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            findViewById<Button>(R.id.button).visibility = View.INVISIBLE
        } else {
            findViewById<Button>(R.id.button).visibility = View.VISIBLE
        }

        // Check overlay permission status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            findViewById<Button>(R.id.button3).visibility = View.INVISIBLE
        } else {
            findViewById<Button>(R.id.button3).visibility = View.VISIBLE
        }

        // Check foreground service permission status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        } else {

        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestCallPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestVideoPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE
            )
        } else {

            btnaRequestOverlayPermission.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Overlay permission granted
                findViewById<Button>(R.id.button3).visibility = View.INVISIBLE
            } else {
                // Overlay permission denied or not granted
                findViewById<Button>(R.id.button3).visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("success", "Location permission granted")
                    findViewById<Button>(R.id.button2).visibility = View.INVISIBLE
                } else {
                    Log.d("success_non", "Location permission denied")
                }
            }

            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("success", "Camera permission granted")
                } else {
                    Log.d("success_non", "Camera permission denied")
                }
            }

            CALL_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("success", "Call permission granted")
                    findViewById<Button>(R.id.button).visibility = View.INVISIBLE
                } else {
                    Log.d("success_non", "Call permission denied")
                }
            }

            FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("success_non", "Foreground service permission granted")

                    btnaRequestOverlayPermission.visibility = View.VISIBLE
                } else {
                    Log.d("success_non", "Foreground service permission denied")
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "HALLWAY"
            val channelName = "Hallway Channel"
            val channelDescription = "Notification Channel for Hallway"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startSecurityService() {
        binding.apply {
            val serviceIntent = Intent(this@MainActivity, SecurityService::class.java)
            ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
        }
    }
}
