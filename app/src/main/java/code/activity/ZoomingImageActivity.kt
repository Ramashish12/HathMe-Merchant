package code.activity

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import code.utils.AppSettings
import code.view.BaseActivity
import com.hathme.merchat.android.databinding.ActivityZoomingImageBinding
import java.io.File

class ZoomingImageActivity : BaseActivity() {

    lateinit var binding: ActivityZoomingImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZoomingImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageback.setOnClickListener { onBackPressed() }
        val imgFile = File(AppSettings.getString(AppSettings.KEY_selected_image))
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        binding.imagecampbanner.setImageBitmap(myBitmap)


       // intent.getStringExtra("fileName")?.let {binding.imagecampbanner.setImageBitmap(loadBitmap(it))  }

    }

    private fun loadBitmap(fileName: String): Bitmap? {

        val resolver = contentResolver
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        var bitmap: Bitmap? = null
        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                val stream = resolver.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(stream)
            }
        }

        return bitmap
    }

}