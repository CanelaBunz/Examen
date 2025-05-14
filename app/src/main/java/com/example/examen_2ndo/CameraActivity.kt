package com.example.examen_2ndo

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var previewView: PreviewView
    private lateinit var previewImage: ImageView
    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.viewFinder)
        previewImage = findViewById(R.id.previewImage)

        val btnCapture = findViewById<Button>(R.id.captureButton)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDiscard = findViewById<Button>(R.id.btnDiscard)

        setupPermissions()
        setupButtons(btnCapture, btnSave, btnDiscard)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupButtons(
        btnCapture: Button,
        btnSave: Button,
        btnDiscard: Button
    ) {
        btnCapture.setOnClickListener { takePhoto() }
        btnSave.setOnClickListener { currentPhotoUri?.let { saveToGallery(it); resetCamera() } }
        btnDiscard.setOnClickListener { deleteTempFile(); resetCamera() }
    }

    private fun setupPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (exc: Exception) {
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageCapture = ImageCapture.Builder().build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Toast.makeText(this, "Error al configurar cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            externalCacheDir,
            SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    currentPhotoUri = Uri.fromFile(photoFile)
                    runOnUiThread { showPreview() }
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Error al capturar: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun showPreview() {
        previewView.visibility = android.view.View.GONE
        previewImage.visibility = android.view.View.VISIBLE
        findViewById<Button>(R.id.captureButton).visibility = android.view.View.GONE
        findViewById<Button>(R.id.btnSave).visibility = android.view.View.VISIBLE
        findViewById<Button>(R.id.btnDiscard).visibility = android.view.View.VISIBLE
        previewImage.setImageURI(currentPhotoUri)
    }

    private fun saveToGallery(uri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Foto_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
            }
        }

        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { newUri ->
            contentResolver.openOutputStream(newUri)?.use { output ->
                contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "Foto guardada en galería", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTempFile() {
        currentPhotoUri?.path?.let { File(it).delete() }
        Toast.makeText(this, "Foto descartada", Toast.LENGTH_SHORT).show()
    }

    private fun resetCamera() {
        previewView.visibility = android.view.View.VISIBLE
        previewImage.visibility = android.view.View.GONE
        findViewById<Button>(R.id.captureButton).visibility = android.view.View.VISIBLE
        findViewById<Button>(R.id.btnSave).visibility = android.view.View.GONE
        findViewById<Button>(R.id.btnDiscard).visibility = android.view.View.GONE
        currentPhotoUri = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> handlePermissionResult(permissions, grantResults)
        }
    }

    private fun handlePermissionResult(permissions: Array<String>, grantResults: IntArray) {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            checkPermanentDenials(permissions, grantResults)
        }
    }

    private fun checkPermanentDenials(permissions: Array<String>, grantResults: IntArray) {
        permissions.forEachIndexed { index, permission ->
            if (!shouldShowRequestPermissionRationale(permission) &&
                grantResults[index] == PackageManager.PERMISSION_DENIED
            ) {
                showSettingsDialog()
                return
            }
        }
        Toast.makeText(this, "Permisos requeridos para usar la cámara", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permisos requeridos")
            .setMessage("Debes habilitar los permisos en Configuración > Aplicaciones > Tu app")
            .setPositiveButton("Abrir configuración") { _, _ ->
                openAppSettings()
                finish()
            }
            .setNegativeButton("Cancelar") { _, _ -> finish() }
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }.also { startActivity(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 1001

        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}