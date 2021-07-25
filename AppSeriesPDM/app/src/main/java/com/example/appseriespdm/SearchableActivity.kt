package com.example.appseriespdm

import android.app.SearchManager
import android.content.ContentValues
import android.content.Intent
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
import org.json.JSONObject
import java.io.IOException

/**
 * Actividad de búsqueda de nuevas series
 */
class SearchableActivity : AppCompatActivity() {
    var resultadosBusqueda: String? = null
    var idcuenta: String? = null
    var idsesion: String? = null

    /**
     * Método donde la actividad recibe la petición de búsqueda, la
     * procesa, y muestra los resultados, que aparecerán en forma de
     * lista donde para cada serie aparecerá su nombre y un botón
     * que nos permitirá añadirla a la lista de seguimiento.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                peticionBusqueda(query)
            }
        }

        idcuenta = intent.getStringExtra("IDCUENTA")
        idsesion = intent.getStringExtra("IDSESION")

        while (resultadosBusqueda == null){
            Log.i("ESPERA", "A")
        }

        var resultadoSeries = JSONObject(resultadosBusqueda).getJSONArray("results")

        for(i in 0 until resultadoSeries.length()){
            var layoutLista = findViewById<LinearLayout>(R.id.listapelis)
            var serieJSON = resultadoSeries.getJSONObject(i)

            var serie = LinearLayout(this)
            serie.orientation = LinearLayout.HORIZONTAL
            val parametros_layout = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            parametros_layout.setMargins(20,20,20,0)

            var botonAñadirSeguimiento = Button(this)
            botonAñadirSeguimiento.setOnClickListener {
                var idserie = serieJSON.getString("id")
                val requestBody = FormBody.Builder()
                    .add("media_type", "tv")
                    .add("media_id", idserie)
                    .add("watchlist", "true")
                    .build()

                val request = Request.Builder()
                    .url("https://api.themoviedb.org/3/account/$idcuenta/watchlist?api_key=ecfe4f06a0f028c3618838df92bfea77&session_id=$idsesion")
                    .method("POST",requestBody)
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
                            Log.i("STATUS", res.getString("status_message"))
                        }
                    })

                var dbHelper = MyDbHelper(this)
                var sqliteBD = dbHelper.writableDatabase

                var contenido = ContentValues().apply {
                    put(SerieContract.SerieEntry.COLUMN_ID, serieJSON.getString("id"))
                    put(SerieContract.SerieEntry.COLUMN_NAME, serieJSON.getString("name"))
                    put(SerieContract.SerieEntry.COLUMN_SEASON, 1)
                    put(SerieContract.SerieEntry.COLUMN_CAP, 1)
                }

                var nuevaFila = sqliteBD.insert(SerieContract.SerieEntry.TABLE_NAME, null, contenido)
                Log.i("FILA", nuevaFila.toString())
            }
            botonAñadirSeguimiento.setText("Seguir")
            botonAñadirSeguimiento.textSize = 12.0f
            botonAñadirSeguimiento.layoutParams = parametros_layout
            serie.addView(botonAñadirSeguimiento)

            var datosSerie = resultadoSeries.getJSONObject(i)
            var nombreSerie = TextView(this)
            nombreSerie.text = datosSerie.getString("name")
            nombreSerie.layoutParams = parametros_layout
            nombreSerie.textSize = 16.0f
            nombreSerie.setTextColor(getColor(R.color.black))
            nombreSerie.layoutParams = parametros_layout
            serie.addView(nombreSerie)

            layoutLista.addView(serie)
        }
    }

    /**
     * Petición de busqueda realizada a la base de datos, con
     * la que obtendremos series relacionadas con dicha petición.
     */
    fun peticionBusqueda(query: String){
        val request = Request.Builder()
                .url("https://api.themoviedb.org/3/search/tv?api_key=ecfe4f06a0f028c3618838df92bfea77&query=$query")
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
                        resultadosBusqueda = response.body()!!.string()
                        Log.i("RESULTADOS", resultadosBusqueda!!)
                    }
                })
    }
}