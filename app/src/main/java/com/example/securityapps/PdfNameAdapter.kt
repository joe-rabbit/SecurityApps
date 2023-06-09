

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.securityapps.Documents
import com.example.securityapps.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.*
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.channels.Channels

@Suppress("DEPRECATION")
class PdfNameAdapter(private val pdfNames: MutableList<String>) :
    RecyclerView.Adapter<PdfNameAdapter.PdfNameViewHolder>() {
    private var document = Documents
    private lateinit var PdfUris:Uri
    private var checkPdfFiles: MutableList<String> = mutableListOf()


    class PdfNameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pdfNameTextView: TextView = itemView.findViewById(R.id.pdf_name_text_view)
        val pdfCheckBox: CheckBox = itemView.findViewById(R.id.checkpdfbox)

        fun bind(pdfName: String) {
            pdfNameTextView.text = pdfName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfNameViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pdf_name, parent, false)
        return PdfNameViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PdfNameViewHolder, position: Int) {
        val pdfName = pdfNames[position]
        holder.bind(pdfName)
        val activity = holder.itemView.context as AppCompatActivity
        val transferPdfImages: Button = activity.findViewById(R.id.chose_file_button)

        holder.pdfCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                checkPdfFiles.add(pdfName)
                if (checkPdfFiles.isNotEmpty()) {
                    transferPdfImages.setText("Transfer")
                    // Get the Uri for the selected PDF file
                    val pdfFile = File(pdfName)
                    val pdfUri: Uri = FileProvider.getUriForFile(
                        activity,
                        activity.packageName + ".provider",
                        pdfFile
                    )


                    transferPdfImages.setOnClickListener {
                        // Do something with the PDF file Uri
                        val openPdfIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/pdf"
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            val checkPdf_files = getCheckedPdf()
                            val count = checkPdf_files.size
                            for (PdfFiles in checkPdf_files) {
                                val pdfFile = Uri.parse(PdfFiles)
                                val decodedFilename = URLDecoder.decode(pdfFile.toString(),"UTF-8")
                                val filename = decodedFilename.replace("%20", "%20")
                                val pdfUri =Uri.parse(" content://com.android.providers.media.documents/document/$filename")
                                Log.println(Log.DEBUG,"pdfuri",pdfUri.toString())
                                count - 1
                                try {
                                    val parcelFileDescriptor = holder.itemView.context.contentResolver.openFileDescriptor(PdfUris,"r")
                                    if (parcelFileDescriptor != null) {
                                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                                        val httpClient = OkHttpClient()
                                        val requestBody = inputStream.use { input ->
                                            val MAX_BUFFER_SIZE = 10 * 1024 * 1024 // 10MB

                                            val buffer = ByteBuffer.allocateDirect(MAX_BUFFER_SIZE)
                                            var bytesRead: Int
                                            val outputStream = ByteArrayOutputStream()
                                            val channel = Channels.newChannel(input)

                                            while (channel.read(buffer) != -1) {
                                                buffer.flip()
                                                val bytesToWrite = minOf(buffer.remaining(), MAX_BUFFER_SIZE - outputStream.size())
                                                outputStream.write(buffer.array(), 0, bytesToWrite)
                                                buffer.clear()
                                            }

                                            val mediaType = "application/*".toMediaTypeOrNull()
                                            RequestBody.create(mediaType, outputStream.toByteArray())
                                        }
                                        val formData = MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart("uploaded-file", pdfUri.lastPathSegment.toString(), requestBody)
                                            .build()
                                        val request = Request.Builder()
                                            .url("http://192.168.111.217/upload_docs")
                                            .post(formData)
                                            .build()
                                        httpClient.newCall(request).execute().use {
                                                response ->
                                            if (!response.isSuccessful) {
                                                throw IOException("Unexpected HTTP RESPONSE CODE ${response.code}")
                                            } else {
                                                Log.println(Log.DEBUG, "HTTPRESPONSE", "SUCCESSFUL")
                                                response.close()
                                            }
                                        }
                                    }
                                } catch (e: FileNotFoundException) {
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        Log.d("PDF_FILE_DEBUG", pdfUri.toString())

                    }
                }
            } else {
                checkPdfFiles.remove(pdfName)

                transferPdfImages.setOnClickListener {
                    val toastmaster =
                        Toast.makeText(activity, "go back to refresh", Toast.LENGTH_LONG)
                    toastmaster.show()
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return pdfNames.size
    }

    fun updatePdfNames(names: List<String>) {
        pdfNames.clear()
        pdfNames.addAll(names)
        notifyDataSetChanged()
    }




    private fun getCheckedPdf():  List<String>  {
        return checkPdfFiles

    }

    fun insertUri(uri: Uri) {
        PdfUris=uri

    }

    companion object {
        const val REQUEST_CODE_OPEN_PDF = 1001
    }

}