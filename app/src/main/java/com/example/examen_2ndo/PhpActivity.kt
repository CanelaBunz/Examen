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

    // Variables para los elementos de la interfaz de usuario
    private lateinit var nameEditText: EditText      // Campo para el nombre
    private lateinit var ageEditText: EditText       // Campo para la edad
    private lateinit var messageEditText: EditText   // Campo para el mensaje
    private lateinit var sendButton: Button          // Botón para enviar datos
    private lateinit var getAllRecordsButton: Button // Botón para obtener registros
    private lateinit var statusTextView: TextView    // Texto para mostrar el estado
    private lateinit var responseTextView: TextView  // Texto para mostrar la respuesta
    private lateinit var progressBar: ProgressBar    // Barra de progreso

    // URL del servidor PHP
    private val serverUrl = "http:/192.168.10.46/api.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_php)

        // Inicializar los elementos de la interfaz de usuario
        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        getAllRecordsButton = findViewById(R.id.getAllRecordsButton)
        statusTextView = findViewById(R.id.statusTextView)
        responseTextView = findViewById(R.id.responseTextView)
        progressBar = findViewById(R.id.progressBar)

        // Configurar el botón para enviar datos al servidor
        sendButton.setOnClickListener {
            // Primero validamos los datos antes de enviarlos
            if (validarDatos()) {
                enviarDatos()
            }
        }

        // Configurar el botón para obtener todos los registros del servidor
        getAllRecordsButton.setOnClickListener {
            obtenerRegistros()
        }
    }

    // Función para validar que los datos ingresados por el usuario sean correctos
    private fun validarDatos(): Boolean {
        // Obtener y limpiar los datos ingresados
        val nombre = nameEditText.text.toString().trim()
        val edadTexto = ageEditText.text.toString().trim()
        val mensaje = messageEditText.text.toString().trim()

        // Validar que el nombre no esté vacío
        if (nombre.isEmpty()) {
            nameEditText.error = "El nombre es requerido"
            return false
        }

        // Validar que la edad no esté vacía
        if (edadTexto.isEmpty()) {
            ageEditText.error = "La edad es requerida"
            return false
        }

        // Validar que la edad sea un número válido entre 0 y 120
        try {
            val edad = edadTexto.toInt()
            if (edad < 0 || edad > 120) {
                ageEditText.error = "La edad debe estar entre 0 y 120 años"
                return false
            }
        } catch (e: NumberFormatException) {
            // Si no se puede convertir a número, mostramos un error
            ageEditText.error = "La edad debe ser un número"
            return false
        }

        // Validar que el mensaje no esté vacío
        if (mensaje.isEmpty()) {
            messageEditText.error = "El mensaje es requerido"
            return false
        }

        // Si todas las validaciones pasan, retornamos true
        return true
    }

    // Función para enviar los datos del formulario al servidor PHP
    private fun enviarDatos() {
        // Mostrar barra de progreso para indicar que se está procesando
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Estado: Enviando datos..."

        // Crear objeto JSON con los datos del formulario
        val datosJson = JSONObject()
        datosJson.put("name", nameEditText.text.toString().trim())
        datosJson.put("age", ageEditText.text.toString().trim())
        datosJson.put("message", messageEditText.text.toString().trim())

        // Usar corrutina para realizar la operación de red en segundo plano
        // y no bloquear la interfaz de usuario
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Configurar la conexión HTTP para enviar datos al servidor
                val url = URL(serverUrl)
                val conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "POST"
                conexion.setRequestProperty("Content-Type", "application/json")
                conexion.doOutput = true  // Permitir envío de datos
                conexion.doInput = true   // Permitir recepción de datos

                // Enviar los datos JSON al servidor
                val escritor = OutputStreamWriter(conexion.outputStream)
                escritor.write(datosJson.toString())
                escritor.flush()

                // Leer la respuesta del servidor
                val codigoRespuesta = conexion.responseCode
                val respuesta = leerRespuesta(conexion)

                // Actualizar la interfaz de usuario en el hilo principal
                withContext(Dispatchers.Main) {
                    // Ocultar la barra de progreso
                    progressBar.visibility = View.GONE

                    // Procesar la respuesta según el código HTTP
                    if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                        // Parsear la respuesta JSON
                        val jsonRespuesta = JSONObject(respuesta)
                        val exito = jsonRespuesta.optBoolean("success", false)
                        val mensaje = jsonRespuesta.optString("message", "Sin mensaje")

                        if (exito) {
                            // Si la operación fue exitosa, limpiar los campos
                            statusTextView.text = "Estado: Datos enviados correctamente"
                            responseTextView.text = "Respuesta: $mensaje\n\n$respuesta"

                            // Limpiar los campos del formulario
                            nameEditText.text.clear()
                            ageEditText.text.clear()
                            messageEditText.text.clear()
                        } else {
                            // Si hubo un error en el servidor
                            statusTextView.text = "Estado: Error en el servidor"
                            responseTextView.text = "Error: $mensaje\n\n$respuesta"
                        }
                    } else {
                        // Si hubo un error en la conexión HTTP
                        statusTextView.text = "Estado: Error de conexión"
                        responseTextView.text = "Error: Código $codigoRespuesta\n\n$respuesta"
                    }
                }

                // Cerrar la conexión
                conexion.disconnect()

            } catch (e: Exception) {
                // Manejar cualquier excepción que ocurra durante el proceso
                withContext(Dispatchers.Main) {
                    // Actualizar la interfaz para mostrar el error
                    progressBar.visibility = View.GONE
                    statusTextView.text = "Estado: Error de conexión"
                    responseTextView.text = "Error: ${e.message}"

                    // Mostrar un mensaje de error al usuario
                    Toast.makeText(
                        this@PhpActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Función para obtener todos los registros almacenados en el servidor
    private fun obtenerRegistros() {
        // Mostrar barra de progreso para indicar que se está procesando
        progressBar.visibility = View.VISIBLE
        statusTextView.text = "Estado: Obteniendo registros..."

        // Usar corrutina para realizar la operación de red en segundo plano
        // y no bloquear la interfaz de usuario
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Configurar la conexión HTTP para solicitar datos al servidor
                val url = URL(serverUrl)
                val conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "GET"  // Método GET para obtener datos
                conexion.setRequestProperty("Accept", "application/json")
                conexion.doInput = true  // Permitir recepción de datos

                // Leer la respuesta del servidor
                val codigoRespuesta = conexion.responseCode
                val respuesta = leerRespuesta(conexion)

                // Actualizar la interfaz de usuario en el hilo principal
                withContext(Dispatchers.Main) {
                    // Ocultar la barra de progreso
                    progressBar.visibility = View.GONE

                    // Procesar la respuesta según el código HTTP
                    if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                        // Parsear la respuesta JSON
                        val jsonRespuesta = JSONObject(respuesta)
                        val exito = jsonRespuesta.optBoolean("success", false)

                        if (exito) {
                            // Obtener el array de registros
                            val registros = jsonRespuesta.optJSONArray("records")

                            // Verificar si hay registros
                            if (registros != null && registros.length() > 0) {
                                // Construir una representación de texto de los registros
                                val textoRegistros = StringBuilder()
                                textoRegistros.append("REGISTROS DE LA BASE DE DATOS:\n\n")

                                // Recorrer cada registro y añadirlo al texto
                                for (i in 0 until registros.length()) {
                                    val registro = registros.getJSONObject(i)
                                    textoRegistros.append("ID: ${registro.optString("id", "N/A")}\n")
                                    textoRegistros.append("Nombre: ${registro.optString("nombre", "N/A")}\n")
                                    textoRegistros.append("Edad: ${registro.optString("edad", "N/A")}\n")
                                    textoRegistros.append("Mensaje: ${registro.optString("mensaje", "N/A")}\n")
                                    textoRegistros.append("Fecha: ${registro.optString("fecha", "N/A")}\n")
                                    textoRegistros.append("------------------------\n")
                                }

                                // Mostrar los registros en la interfaz
                                statusTextView.text = "Estado: Registros obtenidos correctamente"
                                responseTextView.text = textoRegistros.toString()
                            } else {
                                // No hay registros
                                statusTextView.text = "Estado: No hay registros"
                                responseTextView.text = "No se encontraron registros en la base de datos."
                            }
                        } else {
                            // Si hubo un error en el servidor
                            val mensaje = jsonRespuesta.optString("message", "Sin mensaje")
                            statusTextView.text = "Estado: Error en el servidor"
                            responseTextView.text = "Error: $mensaje\n\n$respuesta"
                        }
                    } else {
                        // Si hubo un error en la conexión HTTP
                        statusTextView.text = "Estado: Error de conexión"
                        responseTextView.text = "Error: Código $codigoRespuesta\n\n$respuesta"
                    }
                }

                // Cerrar la conexión
                conexion.disconnect()

            } catch (e: Exception) {
                // Manejar cualquier excepción que ocurra durante el proceso
                withContext(Dispatchers.Main) {
                    // Actualizar la interfaz para mostrar el error
                    progressBar.visibility = View.GONE
                    statusTextView.text = "Estado: Error de conexión"
                    responseTextView.text = "Error: ${e.message}"

                    // Mostrar un mensaje de error al usuario
                    Toast.makeText(
                        this@PhpActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Función auxiliar para leer la respuesta HTTP y convertirla a String
    private fun leerRespuesta(conexion: HttpURLConnection): String {
        // Obtener el flujo de entrada de la conexión
        val inputStream = conexion.inputStream
        // Crear un lector para leer el flujo de entrada
        val reader = BufferedReader(InputStreamReader(inputStream))
        // StringBuilder para construir la respuesta completa
        val respuesta = StringBuilder()
        var linea: String?

        // Leer línea por línea hasta el final
        while (reader.readLine().also { linea = it } != null) {
            respuesta.append(linea)
        }

        // Cerrar los recursos
        reader.close()
        inputStream.close()

        // Devolver la respuesta como String
        return respuesta.toString()
    }
}
