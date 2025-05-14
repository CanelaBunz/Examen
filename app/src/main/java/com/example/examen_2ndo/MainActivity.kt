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

    // Variables para la cámara y sensores (no se usan aquí, se han movido a CameraSensorActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar botones de la pantalla principal
        val btnPhp = findViewById<Button>(R.id.btnPhp)
        val btnRest = findViewById<Button>(R.id.btnRest)
        val btnGeolocation = findViewById<Button>(R.id.btnGeolocation)
        val btnPersistence = findViewById<Button>(R.id.btnPersistence)
        val btnCameraSensor = findViewById<Button>(R.id.btnCameraSensor)

        // No se necesita código adicional aquí, los botones ya están definidos en el layout XML

        // Configurar los eventos de click para cada botón
        btnPhp.setOnClickListener { v: View? -> openPhpActivity() }
        btnRest.setOnClickListener { v: View? -> openRestActivity() }
        btnGeolocation.setOnClickListener { v: View? -> checkPermissionsAndOpenGeolocation() }
        btnPersistence.setOnClickListener { v: View? -> openPersistenceActivity() }
        btnCameraSensor.setOnClickListener { v: View? -> openCameraSensorActivity() }
    }



    // Método para abrir la actividad de PHP
    private fun openPhpActivity() {
        val intent = Intent(this, PhpActivity::class.java)
        startActivity(intent)
    }

    // Método para abrir la actividad REST
    private fun openRestActivity() {
        val intent = Intent(this, RestActivity::class.java)
        startActivity(intent)
    }

    // Método para verificar permisos de ubicación antes de abrir la actividad de geolocalización
    private fun checkPermissionsAndOpenGeolocation() {
        // Verificar si tenemos los permisos necesarios
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no tenemos permisos, los solicitamos al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Si ya tenemos permisos, abrimos la actividad
            openGeolocationActivity()
        }
    }

    // Método para abrir la actividad de geolocalización
    private fun openGeolocationActivity() {
        val intent = Intent(this, GeolocationActivity::class.java)
        startActivity(intent)
    }

    // Método para abrir la actividad de persistencia
    private fun openPersistenceActivity() {
        val intent = Intent(
            this,
            PersistenceActivity::class.java
        )
        startActivity(intent)
    }

    // Método para abrir la actividad de cámara y sensores
    private fun openCameraSensorActivity() {
        val intent = Intent(this, CameraActivity::class.java)

        startActivity(intent)
    }

    // Método que se llama cuando el usuario responde a la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Verificar si el usuario concedió los permisos
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si los permisos fueron concedidos, abrimos la actividad
                openGeolocationActivity()
            } else {
                // Si los permisos fueron denegados, mostramos un mensaje
                Toast.makeText(
                    this,
                    "Se necesitan permisos de ubicación para esta función",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // No se necesitan métodos para cámara y sensores, se han movido a CameraSensorActivity

    // Constantes utilizadas en la actividad
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100 // Código para identificar la solicitud de permisos
    }
}
