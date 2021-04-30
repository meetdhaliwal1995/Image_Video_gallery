package com.codility.galleryimagevideo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var IMAGE_REQUEST = 1
    private var VIDEO_REQUEST = 2
    private var REQUEST_WRITE_PERMISSION = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        * Take Permission Programmatically for WRITE_EXTERNAL_STORAGE
        * */
        takePermission()

        //Click Event For Image Gallery
        btImageGallery.setOnClickListener(View.OnClickListener {
            val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(i, IMAGE_REQUEST)
        })

        //Click Event For Video Gallery
        btVideoGallery.setOnClickListener(View.OnClickListener {
            val videoPickIntent = Intent(Intent.ACTION_PICK)
            videoPickIntent.type = "video/*"
            startActivityForResult(Intent.createChooser(videoPickIntent, "Please pick a video"), VIDEO_REQUEST)
        })
    }

    private fun takePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted..!!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*
        * Set Image in ImageView
        * */
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
            videoView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            val selectedImage: Uri = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val cursor: Cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
            cursor.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath: String = cursor.getString(columnIndex)
            cursor.close()
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath))
            tvPreview.text = "Image Preview"
        }

        /*
         * Play Video in VideoView
         * */
        if (requestCode == VIDEO_REQUEST && resultCode == Activity.RESULT_OK && null != data) {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            tvPreview.text = "Video Preview"
            val pickedVideoUrl = getRealPathFromUri(applicationContext, data!!.data)
            videoView.setVideoPath(pickedVideoUrl)
            // Default Media-Controller
            videoView.setMediaController(MediaController(this));
            // start playing
            videoView.start()
        }
    }

    // Retrieve Video Path from URI.
    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }
}