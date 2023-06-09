package com.example.securityapps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView



class Recordings : AppCompatActivity() {
    private lateinit var  Recordings: ActivityResultLauncher<Array<String>>
    private lateinit var btn : Button
    private lateinit var RecordsName : TextView
    private lateinit var RecordsRecyclerView : RecyclerView
    private lateinit var RecordsAdapter : RecordingNameAdapter
    private  var record_names : MutableList<String> = mutableListOf()

    companion object {
        const val RECORD_NAMES_KEY = "pdf_names_key"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)
        //initialise recycler view and adapter
        RecordsRecyclerView = findViewById(R.id.recording_names_recycler_view)
        RecordsAdapter = RecordingNameAdapter(record_names)
        RecordsRecyclerView.adapter= RecordsAdapter
        RecordsRecyclerView.layoutManager = LinearLayoutManager(this)

savedInstanceState?.let {
    bundle ->
    record_names = bundle.getStringArrayList(RECORD_NAMES_KEY) as MutableList<String>
    RecordsAdapter.updateRecordingNames(record_names)




}
        btn= findViewById(R.id.chose_recording_button)
        Recordings = registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
            { uri ->
                if (uri != null) {
                    // Get the file path from the document tree URI
                    val filePath = getRecordingsNameFromUri(uri)
                    // Do something with the recording file path

                }
            }
        )
        btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/mpeg", "audio/opus", "audio/aac", "audio/mp4"))
            }
            Recordings.launch(arrayOf(intent.type.toString()))
        }

    }

    private fun getRecordingsNameFromUri(uri: Uri) {
        Log.println(Log.DEBUG,"record_FILE",uri.toString())
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use{ cursor1 ->
            if (cursor1.moveToFirst()) {
                val displayNameIndex = cursor1.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    val rName = cursor1.getString(displayNameIndex)
                    record_names.add(rName)
                    RecordsAdapter.notifyItemInserted(record_names.lastIndex)
                    RecordsAdapter.insertUri(uri)
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(RECORD_NAMES_KEY, ArrayList(record_names))
    }

}