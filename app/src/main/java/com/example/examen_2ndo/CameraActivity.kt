package com.example.examen_2ndo
package com.example.camarasensores

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private val CAMERA_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val btnCamera = findViewById<Button>(R.id.btnOpenCamera)
        imageView = findViewById(R.id.imageView)

        btnCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val image: Bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(image)
        }
    }
}
