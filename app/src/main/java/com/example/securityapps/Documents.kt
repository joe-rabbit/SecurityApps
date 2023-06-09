package com.example.securityapps

import PdfNameAdapter
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

class Documents : AppCompatActivity() {

    private lateinit var documentPickerLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var btn: Button
    private lateinit var pdfTextView: TextView
    private lateinit var pdfRecyclerView: RecyclerView
    private lateinit var pdfAdapter: PdfNameAdapter
    private var pdfNames: MutableList<String> = mutableListOf()

    companion object {
        const val PDF_NAMES_KEY = "pdf_names_key"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_documents)

        // Initialize the RecyclerView and adapter
        pdfRecyclerView = findViewById(R.id.pdf_names_recycler_view)
        pdfAdapter = PdfNameAdapter(pdfNames)
        pdfRecyclerView.adapter = pdfAdapter
        pdfRecyclerView.layoutManager = LinearLayoutManager(this)

        // Restore the list of PDF file names if the activity was recreated due to a screen rotation
        savedInstanceState?.let { bundle ->
            pdfNames = bundle.getStringArrayList(PDF_NAMES_KEY) as MutableList<String>
            pdfAdapter.updatePdfNames(pdfNames)
        }

        btn = findViewById(R.id.chose_file_button)
//        pdfTextView = findViewById(R.id.pdf_name_text_view)

        documentPickerLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument(),
            { uri ->
                if (uri != null) {
                    getPDFNameFromUri(uri)
                }
            }
        )

        btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/*"
            }
            documentPickerLauncher.launch(arrayOf(intent.type.toString()))
        }
    }

    fun Documents.Companion.getPDFNameFromUri(uri: Uri) {
        Log.println(Log.DEBUG,"PDFFILE",uri.toString())
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    val pdfName = it.getString(displayNameIndex)
                    pdfNames.add(pdfName)
                    pdfAdapter.notifyItemInserted(pdfNames.lastIndex)
                    pdfAdapter.insertUri(uri)
                }
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(PDF_NAMES_KEY, ArrayList(pdfNames))
    }
}
