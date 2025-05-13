package com.example.examen_2ndo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PhpActivity : AppCompatActivity() {

    // Variables para los elementos de la interfaz
    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var getAllRecordsButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var responseTextView: TextView
    private lateinit var progressBar: ProgressBar
    
    // URL del servidor
    private val serverUrl = "http://10.182.3.20/api.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_php)

        // Encontrar los elementos de la interfaz
        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        getAllRecordsButton = findViewById(R.id.getAllRecordsButton)
        statusTextView = findViewById(R.id.statusTextView)
        responseTextView = findViewById(R.id.responseTextView)
        progressBar = findViewById(R.id.progressBar)

        // Botón para enviar datos
        sendButton.setOnClickListener {
            if (validarDatos()) {
                enviarDatos()
            }
        }

        // Botón para obtener registros
        getAllRecordsButton.setOnClickListener {
            obtenerRegistros()
        }
    }

    // Validar que los datos ingresados sean correctos
    private fun validarDatos(): Boolean {
        val nombre = nameEditText.text.toString().trim()
        val edadTexto = ageEditText.text.toString().trim()
        val mensaje = messageEditText.text.toString().trim()

        // Validar nombre
        if (nombre.isEmpty()) {
            nameEditText.error = "El nombre es requerido"
            return false
        }

        // Validar edad
        if (edadTexto.isEmpty()) {
            ageEditText.error = "La edad es requerida"
            return false
        }

        // Comprobar que la edad sea un número válido
        try {
            val edad = edadTexto.toInt()
            if (edad < 0 || edad > 120) {
                ageEditText.error = "La edad debe estar entre 0 y 120 años"
                return false
            }
        } catch (e: NumberFormatException) {
            ageEditText.error = "La edad debe ser un número"
            return false
        }

        // Validar mensaje
        if (mensaje.isEmpty()) {
            messageEditText.error = "El mensaje es requerido"
            return false
        }

        return true
    }

    // Enviar datos al servidor
    private fun enviarDatos() {
        // Mostrar barra de progreso
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Estado: Enviando datos..."

        // Crear objeto JSON con los datos
        val datosJson = JSONObject()
        datosJson.put("name", nameEditText.text.toString().trim())
        datosJson.put("age", ageEditText.text.toString().trim())
        datosJson.put("message", messageEditText.text.toString().trim())

        // Usar corrutina para no bloquear la interfaz
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Configurar conexión HTTP
                val url = URL(serverUrl)
                val conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "POST"
                conexion.setRequestProperty("Content-Type", "application/json")
                conexion.doOutput = true
                conexion.doInput = true

                // Enviar datos
                val escritor = OutputStreamWriter(conexion.outputStream)
                escritor.write(datosJson.toString())
                escritor.flush()

                // Leer respuesta
                val codigoRespuesta = conexion.responseCode
                val respuesta = leerRespuesta(conexion)

                // Actualizar interfaz en el hilo principal
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    
                    if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                        val jsonRespuesta = JSONObject(respuesta)
                        val exito = jsonRespuesta.optBoolean("success", false)
                        val mensaje = jsonRespuesta.optString("message", "Sin mensaje")
                        
                        if (exito) {
                            // Éxito - limpiar campos
                            statusTextView.text = "Estado: Datos enviados correctamente"
                            responseTextView.text = "Respuesta: $mensaje\n\n$respuesta"
                            
                            nameEditText.text.clear()
                            ageEditText.text.clear()
                            messageEditText.text.clear()
                        } else {
                            // Error del servidor
                            statusTextView.text = "Estado: Error en el servidor"
                            responseTextView.text = "Error: $mensaje\n\n$respuesta"
                        }
                    } else {
                        // Error de conexión
                        statusTextView.text = "Estado: Error de conexión"
                        responseTextView.text = "Error: Código $codigoRespuesta\n\n$respuesta"
                    }
                }
                
                conexion.disconnect()
                
            } catch (e: Exception) {
                // Manejar errores
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    statusTextView.text = "Estado: Error de conexión"
                    responseTextView.text = "Error: ${e.message}"
                    
                    Toast.makeText(
                        this@PhpActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Obtener todos los registros del servidor
    private fun obtenerRegistros() {
        // Mostrar barra de progreso
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Estado: Obteniendo registros..."

        // Usar corrutina para no bloquear la interfaz
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Configurar conexión HTTP
                val url = URL(serverUrl)
                val conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "GET"
                conexion.setRequestProperty("Accept", "application/json")
                conexion.doInput = true

                // Leer respuesta
                val codigoRespuesta = conexion.responseCode
                val respuesta = leerRespuesta(conexion)

                // Actualizar interfaz en el hilo principal
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    
                    if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                        val jsonRespuesta = JSONObject(respuesta)
                        val exito = jsonRespuesta.optBoolean("success", false)
                        
                        if (exito) {
                            val registros = jsonRespuesta.optJSONArray("records")
                            
                            if (registros != null && registros.length() > 0) {
                                // Mostrar registros
                                val textoRegistros = StringBuilder()
                                textoRegistros.append("REGISTROS DE LA BASE DE DATOS:\n\n")
                                
                                for (i in 0 until registros.length()) {
                                    val registro = registros.getJSONObject(i)
                                    textoRegistros.append("ID: ${registro.optString("id", "N/A")}\n")
                                    textoRegistros.append("Nombre: ${registro.optString("nombre", "N/A")}\n")
                                    textoRegistros.append("Edad: ${registro.optString("edad", "N/A")}\n")
                                    textoRegistros.append("Mensaje: ${registro.optString("mensaje", "N/A")}\n")
                                    textoRegistros.append("Fecha: ${registro.optString("fecha", "N/A")}\n")
                                    textoRegistros.append("------------------------\n")
                                }
                                
                                statusTextView.text = "Estado: Registros obtenidos correctamente"
                                responseTextView.text = textoRegistros.toString()
                            } else {
                                statusTextView.text = "Estado: No hay registros"
                                responseTextView.text = "No se encontraron registros en la base de datos."
                            }
                        } else {
                            // Error del servidor
                            val mensaje = jsonRespuesta.optString("message", "Sin mensaje")
                            statusTextView.text = "Estado: Error en el servidor"
                            responseTextView.text = "Error: $mensaje\n\n$respuesta"
                        }
                    } else {
                        // Error de conexión
                        statusTextView.text = "Estado: Error de conexión"
                        responseTextView.text = "Error: Código $codigoRespuesta\n\n$respuesta"
                    }
                }
                
                conexion.disconnect()
                
            } catch (e: Exception) {
                // Manejar errores
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    statusTextView.text = "Estado: Error de conexión"
                    responseTextView.text = "Error: ${e.message}"
                    
                    Toast.makeText(
                        this@PhpActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Función auxiliar para leer la respuesta HTTP
    private fun leerRespuesta(conexion: HttpURLConnection): String {
        val inputStream = conexion.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val respuesta = StringBuilder()
        var linea: String?
        
        while (reader.readLine().also { linea = it } != null) {
            respuesta.append(linea)
        }
        
        reader.close()
        inputStream.close()
        
        return respuesta.toString()
    }
}