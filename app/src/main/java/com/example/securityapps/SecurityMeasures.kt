@file:Suppress("DEPRECATION")

package com.example.securityapps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.socket.client.Socket
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.BufferedSink
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class SecurityMeasures : AppCompatActivity() {
    private lateinit var socket: Socket
    private lateinit var btnSend: Button
    private lateinit var etMessage: TextView
    private lateinit var telecomManager: TelecomManager
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
private lateinit var HomeButton : Button
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val OTHER_PERMISSION_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_measures)
        HomeButton= findViewById(R.id.homebutton)
HomeButton.setOnClickListener {
    val intent = Intent(this@SecurityMeasures,MainActivity::class.java)
    startActivity(intent)
}
viewFinder = findViewById(R.id.viewFinder)




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
            } else {
                // Camera permission is already granted, proceed with opening the camera
                startCamera()


            }
        }



    override fun onDestroy() {
        super.onDestroy()

    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Camera permission granted, proceed with opening the camera
                    startCamera()
                    takePhoto()
                } else {
                    // Camera permission denied, handle accordingly
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            OTHER_PERMISSION_REQUEST_CODE -> {
                // Handle other permission requests if needed
                // ...
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                takePhoto()
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create a file to save the captured image
        val outputDirectory = getOutputDirectory()
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Capture the image
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    // Process the captured image, send it, or perform any other desired actions
                    sendImageToServer(savedUri)

                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle the error
                }
            }
        )
    }

    private fun sendImageToServer(imageUri: Uri) {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()

            .setType(MultipartBody.FORM)
            .addFormDataPart("uploaded-file", imageUri.lastPathSegment,
                object : RequestBody() {
                    override fun contentType(): MediaType? {
                        return contentResolver.getType(imageUri)?.let { it.toMediaTypeOrNull() }
                    }

                    override fun writeTo(sink: BufferedSink) {
                        val inputStream = contentResolver.openInputStream(imageUri)
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
                            sink.write(buffer, 0, bytesRead)
                        }
                        inputStream?.close()
                    }
                })
            .build()

        val request = Request.Builder()
            .url("http://192.168.10.217:5000/upload_images_illegal_access") // Replace with your server URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle the failure
                runOnUiThread {
                    Toast.makeText(this@SecurityMeasures, "Failed to send image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle the response
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@SecurityMeasures, "Image sent successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SecurityMeasures, "Failed to send image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull().let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
}
