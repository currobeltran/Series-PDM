package com.example.appseriespdm

import android.content.ContentValues
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Actividad donde se listarán las series.
 */
class MisSeries : AppCompatActivity() {
    var idcuenta: String? = null
    var idsesion: String? = null
    var seriesSeguimiento: String? = null

    /**
     * En la creación de la lista, podemos tanto ver su nombre como
     * dejar de seguir esa serie, eliminándola tanto de la lista de
     * seguimiento como de la base de datos local.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_series)
        idcuenta = intent.getStringExtra("IDCUENTA")
        idsesion = intent.getStringExtra("IDSESION")

        peticionSeriesWatchlist()
        while(seriesSeguimiento == null){
            Log.i("ESPERA", "A")
        }

        var seriesSeguimientoJSON = JSONArray(seriesSeguimiento)
        var misseries = findViewById<LinearLayout>(R.id.misseries)

        for(i in 0 until seriesSeguimientoJSON.length()){
            var serie = LinearLayout(this)
            serie.orientation = LinearLayout.HORIZONTAL
            val parametros_layout = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            parametros_layout.setMargins(20,20,20,0)

            var botonCancelaSeguimiento = Button(this)
            botonCancelaSeguimiento.setOnClickListener {
                var idserie = seriesSeguimientoJSON.getJSONObject(i).getString("id")

                val request = Request.Builder()
                        .header("Content-Type", "application/json")
                        .url("https://api.themoviedb.org/3/account/$idcuenta/watchlist?api_key=ecfe4f06a0f028c3618838df92bfea77&session_id=$idsesion")
                        .post(RequestBody.create(
                                MediaType.parse("application/json"),
                                "{\"media_type\":\"tv\",\"media_id\":\"$idserie\",\"watchlist\":false}"
                        ))
                        .build()
                val cliente = OkHttpClient()

                cliente.newCall(request)
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call?, e: IOException?) {
                                // Error
                                runOnUiThread {
                                    // For the example, you can show an error dialog or a toast
                                    // on the main UI thread
                                }
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call?, response: Response) {
                                val res = JSONObject(response.body()!!.string())
                                Log.i("STATUS", res.getString("status_code") + " " + res.getString("status_message"))
                            }
                        })

                var dbHelper = MyDbHelper(this)
                var sqliteBD = dbHelper.writableDatabase

                var filaEliminada = sqliteBD.delete(SerieContract.SerieEntry.TABLE_NAME, SerieContract.SerieEntry.COLUMN_ID + "=" + seriesSeguimientoJSON.getJSONObject(i).getString("id"), null)
                Log.i("FILA", filaEliminada.toString())

                recreate()
            }
            botonCancelaSeguimiento.setText("Dejar de seguir")
            botonCancelaSeguimiento.textSize = 12.0f
            botonCancelaSeguimiento.layoutParams = parametros_layout
            serie.addView(botonCancelaSeguimiento)

            var datosSerie = seriesSeguimientoJSON.getJSONObject(i)
            var nombreSerie = TextView(this)
            nombreSerie.text = datosSerie.getString("name")
            nombreSerie.layoutParams = parametros_layout
            nombreSerie.textSize = 16.0f
            nombreSerie.setTextColor(getColor(R.color.black))
            nombreSerie.layoutParams = parametros_layout
            serie.addView(nombreSerie)

            misseries.addView(serie)
        }
    }

    /**
     * Función para solicitar las series en nuestra lista de seguimiento.
     */
    fun peticionSeriesWatchlist(){
        val request = Request.Builder()
                .url("https://api.themoviedb.org/3/account/$idcuenta/watchlist/tv?api_key=ecfe4f06a0f028c3618838df92bfea77&session_id=$idsesion")
                .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        // Error
                        runOnUiThread {
                            // For the example, you can show an error dialog or a toast
                            // on the main UI thread
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call?, response: Response) {
                        seriesSeguimiento = JSONObject(response.body()!!.string()).getJSONArray("results").toString()
                    }
                })
    }
}