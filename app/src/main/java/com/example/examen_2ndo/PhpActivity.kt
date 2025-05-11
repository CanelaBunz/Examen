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

    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var getAllRecordsButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var responseTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_php)

        // Inicializar vistas
        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        getAllRecordsButton = findViewById(R.id.getAllRecordsButton)
        statusTextView = findViewById(R.id.statusTextView)
        responseTextView = findViewById(R.id.responseTextView)
        progressBar = findViewById(R.id.progressBar)

        // Configurar listener del botón de enviar datos
        sendButton.setOnClickListener {
            if (validateInputs()) {
                sendDataToServer()
            }
        }

        // Configurar listener del botón de obtener todos los registros
        getAllRecordsButton.setOnClickListener {
            getAllRecordsFromServer()
        }
    }

    private fun validateInputs(): Boolean {
        val name = nameEditText.text.toString().trim()
        val ageStr = ageEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()

        if (name.isEmpty()) {
            nameEditText.error = "El nombre es requerido"
            return false
        }

        if (ageStr.isEmpty()) {
            ageEditText.error = "La edad es requerida"
            return false
        }

        try {
            val age = ageStr.toInt()
            if (age < 0 || age > 120) {
                ageEditText.error = "La edad debe estar entre 0 y 120 años"
                return false
            }
        } catch (e: NumberFormatException) {
            ageEditText.error = "La edad debe ser un número válido"
            return false
        }

        if (message.isEmpty()) {
            messageEditText.error = "El mensaje es requerido"
            return false
        }

        return true
    }

    private fun sendDataToServer() {
        // Mostrar progreso
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Estado: Enviando datos..."

        // Crear objeto JSON con los datos
        val jsonObject = JSONObject()
        jsonObject.put("name", nameEditText.text.toString().trim())
        jsonObject.put("age", ageEditText.text.toString().trim())
        jsonObject.put("message", messageEditText.text.toString().trim())

        // Usar coroutines para operaciones de red
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // URL del servidor PHP (reemplazar con la URL real donde se aloje api.php)
                // Nota: Esta URL debe ser actualizada con la dirección donde se aloje el script api.php
                // Para un servidor Linux con Apache2, la URL podría ser algo como:
                // "http://tu-ip-o-dominio/api.php" o "http://192.168.1.x/api.php" (red local)
                val url = URL("http://192.168.1.84/api.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true

                // Enviar datos JSON
                val outputStreamWriter = OutputStreamWriter(connection.outputStream)
                outputStreamWriter.write(jsonObject.toString())
                outputStreamWriter.flush()

                // Leer respuesta
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?

                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }

                    bufferedReader.close()
                    inputStream.close()

                    // Procesar respuesta en el hilo principal
                    withContext(Dispatchers.Main) {
                        try {
                            val jsonResponse = JSONObject(stringBuilder.toString())
                            val success = jsonResponse.optBoolean("success", false)
                            val message = jsonResponse.optString("message", "No message")

                            if (success) {
                                statusTextView.text = "Estado: Datos enviados correctamente"
                                responseTextView.text = "Respuesta: $message\n\nJSON completo:\n${jsonResponse.toString(4)}"

                                // Limpiar campos si fue exitoso
                                nameEditText.text.clear()
                                ageEditText.text.clear()
                                messageEditText.text.clear()
                            } else {
                                statusTextView.text = "Estado: Error en el servidor"
                                responseTextView.text = "Error: $message\n\nJSON completo:\n${jsonResponse.toString(4)}"
                            }
                        } catch (e: Exception) {
                            statusTextView.text = "Estado: Error al procesar la respuesta"
                            responseTextView.text = "Error: ${e.message}\n\nRespuesta cruda:\n$stringBuilder"
                        }

                        progressBar.visibility = View.GONE
                    }
                } else {
                    // Manejar error en el hilo principal
                    withContext(Dispatchers.Main) {
                        statusTextView.text = "Estado: Error en la conexión"
                        responseTextView.text = "Error: Código de respuesta $responseCode"
                        progressBar.visibility = View.GONE
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {
                // Manejar excepción en el hilo principal
                withContext(Dispatchers.Main) {
                    statusTextView.text = "Estado: Error de conexión"
                    responseTextView.text = "Error: ${e.message}"
                    progressBar.visibility = View.GONE

                    Toast.makeText(
                        this@PhpActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun getAllRecordsFromServer() {
        // Mostrar progreso
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Estado: Obteniendo registros..."

        // Usar coroutines para operaciones de red
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // URL del servidor PHP
                val url = URL("http://192.168.1.84/api.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.doInput = true

                // Leer respuesta
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?

                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }

                    bufferedReader.close()
                    inputStream.close()

                    // Procesar respuesta en el hilo principal
                    withContext(Dispatchers.Main) {
                        try {
                            val jsonResponse = JSONObject(stringBuilder.toString())
                            val success = jsonResponse.optBoolean("success", false)
                            val message = jsonResponse.optString("message", "No message")

                            if (success) {
                                val records = jsonResponse.optJSONArray("records")
                                if (records != null && records.length() > 0) {
                                    // Construir una cadena formateada con todos los registros
                                    val formattedRecords = StringBuilder()
                                    formattedRecords.append("REGISTROS DE LA BASE DE DATOS:\n\n")

                                    for (i in 0 until records.length()) {
                                        val record = records.getJSONObject(i)
                                        formattedRecords.append("ID: ${record.optString("id", "N/A")}\n")
                                        formattedRecords.append("Nombre: ${record.optString("nombre", "N/A")}\n")
                                        formattedRecords.append("Edad: ${record.optString("edad", "N/A")}\n")
                                        formattedRecords.append("Mensaje: ${record.optString("mensaje", "N/A")}\n")
                                        formattedRecords.append("Fecha: ${record.optString("fecha", "N/A")}\n")
                                        formattedRecords.append("------------------------\n")
                                    }

                                    statusTextView.text = "Estado: Registros obtenidos correctamente"
                                    responseTextView.text = formattedRecords.toString()
                                } else {
                                    statusTextView.text = "Estado: No hay registros en la base de datos"
                                    responseTextView.text = "No se encontraron registros en la base de datos."
                                }
                            } else {
                                statusTextView.text = "Estado: Error en el servidor"
                                responseTextView.text = "Error: $message\n\nJSON completo:\n${jsonResponse.toString(4)}"
                            }
                        } catch (e: Exception) {
                            statusTextView.text = "Estado: Error al procesar la respuesta"
                            responseTextView.text = "Error: ${e.message}\n\nRespuesta cruda:\n$stringBuilder"
                        }

                        progressBar.visibility = View.GONE
                    }
                } else {
                    // Manejar error en el hilo principal
                    withContext(Dispatchers.Main) {
                        statusTextView.text = "Estado: Error en la conexión"
                        responseTextView.text = "Error: Código de respuesta $responseCode"
                        progressBar.visibility = View.GONE
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {
                // Manejar excepción en el hilo principal
                withContext(Dispatchers.Main) {
                    statusTextView.text = "Estado: Error de conexión"
                    responseTextView.text = "Error: ${e.message}"
                    progressBar.visibility = View.GONE

                    Toast.makeText(
                        this@PhpActivity,
                        "Error de conexión: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
