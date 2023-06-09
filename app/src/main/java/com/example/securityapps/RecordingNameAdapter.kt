package com.example.securityapps

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLDecoder

class RecordingNameAdapter(private var RecordingNames:MutableList<String>): RecyclerView.Adapter<RecordingNameAdapter.RecordingNameViewHolder>() {

 private lateinit var RecordUri:Uri
 private var checkedRecordings: MutableList<String> = mutableListOf()

    class RecordingNameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private val RecordingText : TextView = itemView.findViewById(R.id.recordings_name_text_view)
        val RecordCheckBox: CheckBox = itemView.findViewById(R.id.checkrecordingbox)
        fun bind(RecordingsTexts:String)
        {
            RecordingText.text= RecordingsTexts
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecordingNameAdapter.RecordingNameViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recording_name,parent,false)
        return RecordingNameViewHolder(itemView)

    }

    override fun onBindViewHolder(
        holder: RecordingNameAdapter.RecordingNameViewHolder,
        position: Int
    ) {
      val Records = RecordingNames[position]
        holder.bind(Records)
        val activity = holder.itemView.context as AppCompatActivity
        val transferRecordImages: Button = activity.findViewById(R.id.chose_recording_button)
        holder.RecordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedRecordings.add(Records)
                if (checkedRecordings.isNotEmpty()) {
                    transferRecordImages.setText("Transfer")
                    val audioFile = File(Records)
                    val audioUri: Uri = FileProvider.getUriForFile(
                        activity,
                        activity.packageName + ".provider",
                        audioFile
                    )
                    val mimeType = when (audioFile.extension) {
                        "mp3" -> "audio/mpeg"
                        "opus" -> "audio/opus"
                        "aac", "m4a" -> "audio/aac"
                        else -> "audio/*"
                    }
                    transferRecordImages.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(audioUri, mimeType)
                            flags =
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            val checked_Rec = getCheckRecords()
                            for (checkedRec in checked_Rec) {
                                val recfile = Uri.parse(checkedRec)
                                val decodedFilename = URLDecoder.decode(recfile.toString(),"UTF-8")
                                val filename = decodedFilename.replace("%20", "%20")
                                val recUri =Uri.parse(" content://com.android.providers.media.documents/document/$filename")
                                Log.println(Log.DEBUG,"pdfuri",recUri.toString())
                               try {
                                   val parcelFileDescriptor = holder.itemView.context.contentResolver.openFileDescriptor(RecordUri,"r")
                                   if (parcelFileDescriptor != null) {
                                       val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                                       val httpClient = OkHttpClient()
                                       val requestBody = inputStream.use { input ->
                                           val buffer = ByteArray(5*1024*1024)
                                           var bytesRead: Int
                                           val outputStream = ByteArrayOutputStream()
                                           while (input.read(buffer).also { bytesRead = it } != -1) {
                                               outputStream.write(buffer, 0, bytesRead)
                                           }
                                           val mediaType = "audio/*".toMediaTypeOrNull()
                                           RequestBody.create(mediaType, outputStream.toByteArray())
                                       }
                                       val formData = MultipartBody.Builder()
                                           .setType(MultipartBody.FORM)
                                           .addFormDataPart("uploaded-file", audioUri.lastPathSegment.toString(), requestBody)
                                           .build()
                                       val request = Request.Builder()
                                           .url("http://192.168.1.10:5000/upload_audio")
                                           .post(formData)
                                           .build()
                                       httpClient.newCall(request).execute().use { response ->
                                           if (!response.isSuccessful) {
                                               throw IOException("Unexpected HTTP RESPONSE CODE ${response.code}")
                                           } else {
                                               Log.println(Log.DEBUG, "HTTPRESPONSE", "SUCCESSFUL")
                                               response.close()
                                           }
                                       }
                                   }

                               }
                               catch (e:Exception)
                               {
                                   e.printStackTrace()
                               }
                            }

                        }
                    }



                }
            }


        }}

    private fun getCheckRecords():  List<String> {
        return checkedRecordings

    }

    override fun getItemCount(): Int {
  return RecordingNames.size

    }

    fun updateRecordingNames(names: MutableList<String>) {
        RecordingNames.clear()
        RecordingNames.addAll(names)
        notifyDataSetChanged()
    }

    fun insertUri(uri: Uri) {

        RecordUri=uri
    }

}

