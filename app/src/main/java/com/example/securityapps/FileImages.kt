package com.example.securityapps

import ImageAdapter
import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FileImages : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transferButton: Button // declare a variable for Transfer button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_images)

        // find Transfer button by id
        transferButton = findViewById(R.id.airdrop_button)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, IMAGE_COLUMN_COUNT)

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            // Permission granted, load images from gallery
            val imagePathList = getImagesFromGallery()

            // Attach adapter to RecyclerView
            val adapter = ImageAdapter(imagePathList)
            recyclerView.adapter = adapter
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load images from gallery
                val imagePathList = getImagesFromGallery()
                Log.println(
                    Log.DEBUG,
                    "tag",
                    "working")
                // Attach adapter to RecyclerView
                val adapter = ImageAdapter(imagePathList)
                recyclerView.adapter = adapter

                // Set visibility of Transfer button if at least one image is checked
                val adapterChecked = adapter.isChecked()

            } else {
                // Permission denied
                // Show an error message or do nothing
            }
        }
    }

    private fun getImagesFromGallery(): List<String> {
        val imageList = mutableListOf<String>()
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
        val selection = null // Return all images
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Use EXTERNAL_CONTENT_URI for user's gallery
            projection,
            selection,
            null,
            sortOrder
        )
        cursor?.let {
            val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(idColumnIndex)
                )
                imageList.add(imageUri.toString())
                Log.i("image",imageUri.toString())
            }
            cursor.close()
        }

        return imageList
    }

    companion object {
        private const val IMAGE_COLUMN_COUNT = 3
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1000
    }
}
