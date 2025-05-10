package com.example.examen_2ndo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar botones
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnSensors = findViewById<Button>(R.id.btnSensors)
        val btnNetwork = findViewById<Button>(R.id.btnNetwork)
        val btnPhp = findViewById<Button>(R.id.btnPhp)
        val btnRest = findViewById<Button>(R.id.btnRest)
        val btnGeolocation = findViewById<Button>(R.id.btnGeolocation)
        val btnPersistence = findViewById<Button>(R.id.btnPersistence)

        // Configurar listeners
        btnCamera.setOnClickListener { v: View? -> openCameraActivity() }
        btnSensors.setOnClickListener { v: View? -> openSensorsActivity() }
        btnNetwork.setOnClickListener { v: View? -> openNetworkActivity() }
        btnPhp.setOnClickListener { v: View? -> openPhpActivity() }
        btnRest.setOnClickListener { v: View? -> openRestActivity() }
        btnGeolocation.setOnClickListener { v: View? -> checkPermissionsAndOpenGeolocation() }
        btnPersistence.setOnClickListener { v: View? -> openPersistenceActivity() }
    }

    private fun openCameraActivity() {
        val intent = Intent(
            this,
            CameraActivity::class.java
        )
        startActivity(intent)
    }

    private fun openSensorsActivity() {
        val intent = Intent(
            this,
            SensorsActivity::class.java
        )
        startActivity(intent)
    }

    private fun openNetworkActivity() {
        val intent = Intent(
            this,
            NetworkActivity::class.java
        )
        startActivity(intent)
    }

    private fun openPhpActivity() {
        val intent = Intent(this, PhpActivity::class.java)
        startActivity(intent)
    }

    private fun openRestActivity() {
        val intent = Intent(this, RestActivity::class.java)
        startActivity(intent)
    }

    private fun checkPermissionsAndOpenGeolocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            openGeolocationActivity()
        }
    }

    private fun openGeolocationActivity() {
        val intent = Intent(this, GeolocationActivity::class.java)
        startActivity(intent)
    }

    private fun openPersistenceActivity() {
        val intent = Intent(
            this,
            PersistenceActivity::class.java
        )
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGeolocationActivity()
            } else {
                Toast.makeText(
                    this,
                    "Se necesitan permisos de ubicación para esta función",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}