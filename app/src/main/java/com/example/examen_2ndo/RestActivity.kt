package com.example.examen_2ndo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.examen_2ndo.databinding.ActivityRestBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class RestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MarsPhotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MarsPhotosAdapter(emptyList())
        recyclerView.adapter = adapter

        fetchMarsPhotos()
    }

    private fun fetchMarsPhotos() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(NasaApiService::class.java)
        val call = service.getMarsPhotos("DEMO_KEY") // Usa tu propia API key si quieres

        call.enqueue(object : Callback<MarsPhotosResponse> {
            override fun onResponse(call: Call<MarsPhotosResponse>, response: Response<MarsPhotosResponse>) {
                if (response.isSuccessful) {
                    val photos = response.body()?.photos ?: emptyList()
                    adapter.updatePhotos(photos)
                } else {
                    Toast.makeText(this@RestActivity, "Error al obtener fotos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MarsPhotosResponse>, t: Throwable) {
                Toast.makeText(this@RestActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// Interfaz para el servicio API
interface NasaApiService {
    @GET("mars-photos/api/v1/rovers/curiosity/photos?sol=1000")
    fun getMarsPhotos(@retrofit2.http.Query("api_key") apiKey: String): Call<MarsPhotosResponse>
}

// Modelo de datos
data class MarsPhotosResponse(val photos: List<MarsPhoto>)

data class MarsPhoto(
    val id: Int,
    val sol: Int,
    val camera: Camera,
    val img_src: String,
    val earth_date: String,
    val rover: Rover
)

data class Camera(
    val id: Int,
    val name: String,
    val rover_id: Int,
    val full_name: String
)

data class Rover(
    val id: Int,
    val name: String,
    val landing_date: String,
    val launch_date: String,
    val status: String
)