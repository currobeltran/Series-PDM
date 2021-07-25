package com.example.appseriespdm

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.huxq17.swipecardsview.SwipeCardsView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

/**
 * Fragmento donde nos aparecerán las recomendaciones
 * para nuestro perfil.
 */
class Fragment2 : Fragment() {
    var requestToken: String? = null
    var idsesion: String? = null
    var infousuario: String? = null
    var proveedoresBD: String? = null
    var recomendaciones: String? = null
    var serie: String? = null
    var cartas: SwipeCardsView? = null
    var listaSeries: MutableList<Serie> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_2, container, false)
    }

    /**
     * Método donde se crea la vista del fragmento. Aquí se realiza una consulta
     * sobre las series que se pueden recomendar al usuario según los parámetros de
     * configuración que se hayan seleccionado.
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestToken = requireArguments().get("REQUESTOKEN") as String
        idsesion = requireArguments().get("IDSESION") as String
        infousuario = requireArguments().get("INFOCUENTA") as String

        var dbHelper = MyDbHelper(context)
        var sqlite = dbHelper.readableDatabase
        var idcuenta = JSONObject(infousuario).getString("id")

        //Obtenemos preferencias de usuario
        var cursor = sqlite.rawQuery("SELECT * FROM ${UsuarioContract.UsuarioEntry.TABLE_NAME} WHERE ID=$idcuenta", null)
        var preferencias = ""
        var plataformas = ""
        var canceladas = false

        if(cursor.moveToFirst()){
            preferencias = cursor.getString(2)
            plataformas = cursor.getString(1)

            val numCancelada = cursor.getInt(3)
            if(numCancelada == 1){
                canceladas = true
            }
        }

        //Obtener ids de proveedores
        obtenerProveedoresTV()
        while (proveedoresBD == null){

        }

        //Seleccionar una de las plataformas del usuario al azar
        var vectorPlataformas = plataformas.split(",")
        vectorPlataformas = vectorPlataformas.distinct()
        val vectorPlataformasInt: MutableList<Int> = mutableListOf()
        var proveedoresBDJSON = JSONArray(proveedoresBD)

        Log.i("VECTOR", vectorPlataformas.toString() )

        for(i in 0 until proveedoresBDJSON.length()){
            for(j in 0 until vectorPlataformas.size){
                if(proveedoresBDJSON.getJSONObject(i).getString("provider_name") == vectorPlataformas[j] && vectorPlataformas[j] != ""){
                    vectorPlataformasInt += proveedoresBDJSON.getJSONObject(i).getInt("provider_id")
                    Log.i("ENTRA", proveedoresBDJSON.getJSONObject(i).getString("provider_name"))
                }
            }
        }

        var numRandom = Random(System.currentTimeMillis())
        if(vectorPlataformasInt.size != 0){
            //Pedir recomendaciones de plataforma elegida
            var ind = kotlin.math.abs(numRandom.nextInt() % vectorPlataformasInt.size)
            var plataformaRandom = vectorPlataformasInt[ind]

            obtenerRecomendaciones(plataformaRandom.toString(),preferencias)
        }

        while (recomendaciones == null){
            Log.i("ESPERA", "A")
        }

        //Eliminar series canceladas
        var recomendacionesJSON = JSONArray(recomendaciones)
        if(!canceladas){
            var i = 0
            do{
                serie = null
                var JSONSerie = recomendacionesJSON.getJSONObject(i)
                var idSerie = JSONSerie.getString("id")
                obtenerDetalleSerie(idSerie)

                while (serie == null){

                }
                var jsonDetallesSerie = JSONObject(serie)
                var estaCancelada = jsonDetallesSerie.getString("status")
                if(estaCancelada == "Canceled" || estaCancelada == "Ended"){
                    recomendacionesJSON.remove(i)
                }
                else{
                    i++
                }
            } while(i<recomendacionesJSON.length())
        }

        //Crear lista de series
        for(i in 0 until recomendacionesJSON.length()){
            var nombreSerie = recomendacionesJSON.getJSONObject(i).getString("name")

            var imagenSerie = recomendacionesJSON.getJSONObject(i).getString("poster_path")
            var urlImagenSeleccionada = "https://image.tmdb.org/t/p/w500$imagenSerie"

            var idNuevaSerie = recomendacionesJSON.getJSONObject(i).getString("id")

            var nuevaSerie = Serie(nombreSerie,urlImagenSeleccionada,idNuevaSerie)

            listaSeries!! += nuevaSerie
        }

        //Crear el objeto cartas
        cartas = view.findViewById(R.id.SwipeRecomendados)
        cartas!!.retainLastCard(false)
        cartas!!.enableSwipe(true)
        cartas!!.setCardsSlideListener(object : SwipeCardsView.CardsSlideListener{
            override fun onShow(index: Int) {
                //Nada
            }

            override fun onCardVanish(index: Int, type: SwipeCardsView.SlideType?) {
                if(type == SwipeCardsView.SlideType.RIGHT){
                    var serieCarta = listaSeries[index]
                    var idSerieCarta = serieCarta.id
                    var nombreSerie = serieCarta.titulo

                    peticionAnadeListaSeguimiento(idSerieCarta, nombreSerie, idcuenta)
                }
            }

            override fun onItemClick(cardImageView: View?, index: Int) {
                //Nada
            }

        })

        //Establecer lista de series en objeto cartas
        estableceAdaptador()
    }

    /**
     * Función para obtener los identificadores de los
     * proveedores de contenido que soporta la API
     */
    fun obtenerProveedoresTV(){
        val request = Request.Builder()
                .url("https://api.themoviedb.org/3/watch/providers/tv?api_key=ecfe4f06a0f028c3618838df92bfea77")
                .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call?, response: Response) {
                        proveedoresBD = JSONObject(response.body()!!.string()).getJSONArray("results").toString()
                    }
                })
    }

    /**
     * Función para obtener la lista de series recomendadadas.
     */
    fun obtenerRecomendaciones(proveedor: String, criterio: String){
        var request: Request
        if (criterio == "valoradas"){
            request = Request.Builder()
                    .url("https://api.themoviedb.org/3/discover/tv?api_key=ecfe4f06a0f028c3618838df92bfea77&with_watch_providers=$proveedor" +
                            "&watch_region=ES&sort_by=vote_average.desc&vote_count.gte=100")
                    .build()
        }
        else{
            request = Request.Builder()
                    .url("https://api.themoviedb.org/3/discover/tv?api_key=ecfe4f06a0f028c3618838df92bfea77&with_watch_providers=$proveedor&watch_region=ES&sort_by=popularity.desc")
                    .build()
        }
        val cliente = OkHttpClient()

        cliente.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call?, response: Response) {
                        recomendaciones = JSONObject(response.body()!!.string()).getJSONArray("results").toString()
                    }
                })
    }

    /**
     * Función para obtener los detalles de una serie. Nos servirá
     * para comprobar si una serie está o no cancelada.
     */
    fun obtenerDetalleSerie(idSerie: String){
        val request = Request.Builder()
                .url("https://api.themoviedb.org/3/tv/$idSerie?api_key=ecfe4f06a0f028c3618838df92bfea77")
                .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call?, response: Response) {
                        serie = response.body()!!.string()
                    }
                })
    }

    /**
     * Función para añadir una serie a la lista de seguimiento. Se
     * añade tanto a la lista que maneja la base de datos remota como
     * a nuestra base de datos local.
     */
    fun peticionAnadeListaSeguimiento(idSerie: String, nombreSerie: String, idcuenta: String){
        val requestBody = FormBody.Builder()
                .add("media_type", "tv")
                .add("media_id", idSerie)
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

                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call?, response: Response) {
                        val res = JSONObject(response.body()!!.string())
                        Log.i("STATUS", res.getString("status_message"))
                    }
                })

        var dbHelper = MyDbHelper(context)
        var sqliteBD = dbHelper.writableDatabase

        var contenido = ContentValues().apply {
            put(SerieContract.SerieEntry.COLUMN_ID, idSerie)
            put(SerieContract.SerieEntry.COLUMN_NAME, nombreSerie)
            put(SerieContract.SerieEntry.COLUMN_SEASON, 1)
            put(SerieContract.SerieEntry.COLUMN_CAP, 1)
        }

        var nuevaFila = sqliteBD.insert(SerieContract.SerieEntry.TABLE_NAME, null, contenido)
        Log.i("FILA", nuevaFila.toString())
    }

    /**
     * Función para establecer el adaptador de las cartas de las
     * series
     */
    fun estableceAdaptador(){
        var adaptador = Adaptador(listaSeries, requireContext())
        cartas!!.setAdapter(adaptador)
    }
}