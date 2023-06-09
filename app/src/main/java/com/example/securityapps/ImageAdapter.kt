
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.securityapps.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@Suppress("NAME_SHADOWING")
class ImageAdapter(private val imagePathList: List<String>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var checkedImages: MutableList<String>? = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePath = imagePathList[position]

        holder.imageView.setImageURI(Uri.parse(imagePath))
     holder.root_layout?.setOnClickListener {
         holder.checkBox.isChecked = !holder.checkBox.isChecked
     }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                try {
                    if (checkedImages == null) {
                        checkedImages = mutableListOf()
                    }
                    checkedImages!!.add(imagePath)
                    if (checkedImages?.isNotEmpty()!!) {
                        val activity = holder.itemView.context as AppCompatActivity
                        val transferButton: Button = activity.findViewById(R.id.airdrop_button)
                        transferButton.visibility = View.VISIBLE
                        transferButton.setOnClickListener {
                            // Implement your logic to transfer the selected images here
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val checkedImages = getCheckedImages()
                                    for (imagePath in checkedImages) {
                                        val imageUri = Uri.parse(imagePath)
                                        val imageStream = holder.itemView.context.contentResolver.openInputStream(imageUri)
                                        if (imageStream != null) {
                                            // Decode the bitmap from the input stream
                                            val options = BitmapFactory.Options()
                                            options.inJustDecodeBounds = true
                                            BitmapFactory.decodeStream(imageStream, null, options)
                                            imageStream.close()
                                            val width = options.outWidth
                                            val height = options.outHeight

                                            // Calculate the sample size to scale down the bitmap to 250x250
                                            var sampleSize = 1
                                            while (width / (2 * sampleSize) >= 250 && height / (2 * sampleSize) >= 250) {
                                                sampleSize *= 2
                                            }

                                            // Decode the bitmap again with the calculated sample size
                                            val imageStream2 =
                                                holder.itemView.context.contentResolver.openInputStream(imageUri)
                                            val scaledBitmapOptions = BitmapFactory.Options()
                                            scaledBitmapOptions.inSampleSize = sampleSize
                                            val scaledBitmap =
                                                BitmapFactory.decodeStream(imageStream2, null, scaledBitmapOptions)

                                            // Create a new file with the scaled bitmap
                                            val imageFile = File(holder.itemView.context.cacheDir, "${imageUri.lastPathSegment.toString()}.jpg")
                                            imageFile.outputStream().use { out ->
                                                scaledBitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                                            }

                                            // Upload the file to the server
                                            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                                            val formData = MultipartBody.Builder()
                                                .setType(MultipartBody.FORM)
                                                .addFormDataPart("uploaded-file", imageFile.name, requestBody)
                                                .build()

                                            val request = Request.Builder()
                                                .url("http://192.168.1.11:5000/upload_images")
                                                .post(formData)
                                                .build()

                                            val client = OkHttpClient()
                                            val response = client.newCall(request).execute()

                                            if (!response.isSuccessful) {
                                                Log.d("this", "Failed to upload file. HTTP error code: ${response.code}")
                                                throw Exception("Failed to upload file. HTTP error code: ${response.code}")
                                            } else {
                                                Log.d("THIS", "File uploaded successfully")
                                                response.close()
                                            }
                                        } else {
                                            Log.d("THIS", "Failed to open image stream")
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }





    override fun getItemCount(): Int {
        return imagePathList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setOnClickListener(any: Any) {

        }

        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val root_layout: View? = itemView.findViewById(R.id.image_card_views)

    }

    fun getCheckedImages(): List<String> {
        return checkedImages!!
    }

    fun clearCheckedImages() {
        checkedImages?.clear()
    }

    fun isChecked(): Boolean {
        return checkedImages?.isNotEmpty()!!
    }
}
