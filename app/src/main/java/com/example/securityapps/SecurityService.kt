@file:Suppress("DEPRECATION")

package com.example.securityapps


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.auth.FirebaseAuth
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*


@Suppress("DEPRECATION", "NAME_SHADOWING")
class SecurityService : Service(), LifecycleOwner,LocationListener {
    private lateinit var socket: Socket
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private lateinit var firebaseAuth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentIntent: Intent
    private lateinit var locationManager: LocationManager
    private  lateinit var findphone : String
private lateinit var gps: String
private lateinit var message: String
private lateinit var camera : String
private lateinit var foundphone: String
    private lateinit var lifecycleRegistry: LifecycleRegistry
    companion object {
        private const val TAG = "ScreenRecordingService"
        var mediaPlayer: MediaPlayer? = null
        private const val FOREGROUND_SERVICE_ID = 1
        // Add your notification channel ID here
        private const val NOTIFICATION_CHANNEL_ID = "HALLWAY"
    }
    override fun onCreate() {
        super.onCreate()
        socket = IO.socket("http://192.168.10.217:5000/")
       mediaPlayer=MediaPlayer.create(this,R.raw.findphone)
        lifecycleRegistry = LifecycleRegistry(this)
        firebaseAuth = FirebaseAuth.getInstance()
        findphone=firebaseAuth.uid.toString()+"findphone"
        gps=firebaseAuth.uid.toString()+"gps_data"
        message=firebaseAuth.uid.toString()+"message"
        camera=firebaseAuth.uid.toString()+"camera"
        foundphone=firebaseAuth.uid.toString()+"foundphone"
        Log.i("hello",findphone)
        fun onStart() {
            // Start the camera when the service is started
            Log.println(Log.DEBUG, "print", "this ")

        }
        // Handle other lifecycle events as needed
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            // Disconnect socket and perform other cleanup tasks
            socket.disconnect()
        }
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        currentIntent=intent
        startForegroundService()
        connectToSocket()
        return START_STICKY
    }
    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(FOREGROUND_SERVICE_ID, notification)
    }
    private fun createNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Security Service")
            .setContentText("Running in the background")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        return notificationBuilder.build()
    }
    @SuppressLint("QueryPermissionsNeeded", "WrongConstant")
    private fun connectToSocket() {
        socket.on(Socket.EVENT_CONNECT) {
        }
        socket.on(Socket.EVENT_DISCONNECT) {
            // ...
            //TODO
        }

        socket.on(message) { args ->
            val phoneNumber = args[0] as String
            val uri = Uri.fromParts("tel", phoneNumber, null)
            val extras = Bundle()
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
            if (ActivityCompat.checkSelfPermission(
                    this@SecurityService,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
                telecomManager.placeCall(uri, extras)
                return@on
            } else {
                val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
                telecomManager.placeCall(uri, extras)

            }


        }

        socket.on(camera) {

            val mainIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                setClassName("com.example.securityapps", "com.example.securityapps.MainActivity")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

            }
            startActivity(mainIntent)

            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                setClassName(
                    "com.example.securityapps",
                    "com.example.securityapps.SecurityMeasures"
                )

                flags =    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK


            }
            startActivity(intent)



            if (ContextCompat.checkSelfPermission(
                    this@SecurityService,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {

            } else {
                // Camera permission is already granted, proceed with opening the camera
                Log.println(Log.DEBUG, "print", "tag")
                // Launch MainActivity (launcher activity)
                openSystemWindow()
                val mainIntent = Intent().apply {
                    setClassName(
                        "com.example.securityapps",
                        "com.example.securityapps.MainActivity"
                    )
                    flags =  Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK


                }
                startActivity(mainIntent)

                val intent = Intent().apply {
                    setClassName(
                        "com.example.securityapps",
                        "com.example.securityapps.SecurityMeasures"
                    )

                    flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK


                }
                startActivity(intent)


            }
        }
        socket.on(gps)
        {
            openSystemWindow()
            val mainIntent = Intent().apply {
                setClassName(
                    "com.example.securityapps",
                    "com.example.securityapps.MainActivity"
                )
                flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK


            }
            startActivity(mainIntent)
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(
                    this@SecurityService,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

            } else {
                handler.post {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        5f,
                        this
                    )

                }

            }
        }
        socket.on(foundphone)
        {
            if(Settings.canDrawOverlays(this@SecurityService))
            {
                val mainIntent = Intent().apply{
                    setClassName(
                        "com.example.securityapps",
                        "com.example.securityapps.MainActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK               }
                startActivity(mainIntent)
            }
            mediaPlayer?.apply {
                pause()
            }

        }
        socket.on(findphone)
        {
            if (Settings.canDrawOverlays(this@SecurityService)) {
                val mainIntent = Intent().apply {
                    setClassName(
                        "com.example.securityapps",
                        "com.example.securityapps.MainActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(mainIntent)


            }
        setVolumeToMax(this@SecurityService)
            mediaPlayer?.apply {
                setVolume(1.0f, 1.0f)

                if (isPlaying) {
                    pause()
                    start()
                } else {
                    start()
                }
            }

        }



        socket.connect()
    }





    override fun onDestroy() {
        super.onDestroy()

        mediaPlayer?.release()
        mediaPlayer = null
        socket.disconnect()
    }
    fun setVolumeToMax(context: Context)
    {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
    }
    private fun openSystemWindow() {
        if (Settings.canDrawOverlays(this@SecurityService)) {
            // Permission granted, proceed with opening the system window
            // Your code to open the system window goes here

            val intent = Intent().apply {
                setClassName(
                    "com.example.securityapps",
                    "com.example.securityapps.SecurityMeasures"
                )

                flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK


            }
            startActivity(intent)
            Log.i("worked","working")

        } else {
            Log.i("worked","not working")
            // Permission not granted, show system window permission request alert
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }


    fun getsLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override val lifecycle: Lifecycle
        get() = getsLifecycle()


    inner class LocalBinder : Binder() {
        fun getService(): SecurityService = this@SecurityService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude.toString()
        val long = location.longitude.toString()
        val coods = JSONArray().apply {
            put(lat)
            put(long)
        }

        val jsons = JSONObject().apply {
            put("coods", coods)
        }

        val mediaType = "application/json".toMediaType()
        val jsonString = jsons.toString()
        val requestBody = RequestBody.create(mediaType, jsonString)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.93.217:5000/received_gps")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.i("error", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseString = response.body?.string()
                        if (responseString != null) {
                            Log.i("response", responseString)
                        }
                    } else {
                        Log.i("error", response.toString())
                    }
                }
            })
        } catch (e: IOException) {
            Log.i("error", e.toString())
        }

        Log.i("location", lat)
        Log.i("location", long)
    }


}
