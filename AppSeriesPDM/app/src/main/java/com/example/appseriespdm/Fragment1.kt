package com.example.appseriespdm

import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.huxq17.swipecardsview.SwipeCardsView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Fragmento donde se verán las opciones relacionadas
 * con el seguimiento de series.
 */
class Fragment1 : Fragment() {
    var seriesSeguimiento: String? = null
    var cartas: SwipeCardsView? = null
    var listaSeries: List<Serie> = arrayListOf()
    var capituloSerie: String? = null
    var requestToken: String? = null
    var idsesion: String? = null
    var infousuario: String? = null
    var serieInfo: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_1, container, false)
    }

    /**
     * Creación de la vista del fragmento. Se cargarán los capítulos que tenemos
     * pendientes de las series que estemos siguiendo, configurando el comportamiento
     * de las cartas cuando se realiza un deslizamiento a la derecha sobre una de ellas.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestToken = requireArguments().get("REQUESTOKEN") as String
        idsesion = requireArguments().get("IDSESION") as String
        infousuario = requireArguments().get("INFOCUENTA") as String

        var jsonInfoUsuario = JSONObject(infousuario)
        var idcuenta = jsonInfoUsuario.getString("id")

        peticionSeriesEnSeguimiento(idcuenta)
        while (seriesSeguimiento==null){

        }

        var jsonSeries = JSONArray(seriesSeguimiento)

        var dbHelper = MyDbHelper(context)
        cartas = view.findViewById(R.id.SwipeCardsView2)
        cartas!!.retainLastCard(false)
        cartas!!.enableSwipe(true)
        cartas!!.setCardsSlideListener(object : SwipeCardsView.CardsSlideListener {
            override fun onShow(index: Int) {
                //nada
            }

            override fun onCardVanish(index: Int, type: SwipeCardsView.SlideType?) {
                if(type == SwipeCardsView.SlideType.RIGHT){
                    //Obtener capitulo y temporada actual
                    var idSerie = jsonSeries.getJSONObject(index).getString("id")
                    var numCapitulo = 0
                    var numTemporada = 0

                    var sqliteBD = dbHelper.writableDatabase
                    var cursor = sqliteBD.rawQuery("SELECT * FROM ${SerieContract.SerieEntry.TABLE_NAME} WHERE ID=${idSerie}", null)
                    if(cursor.moveToFirst()){
                        do {
                            numCapitulo = cursor.getInt(3)
                            numTemporada = cursor.getInt(2)
                        } while (cursor.moveToNext())
                    }

                    //Obtener datos serie
                    serieInfo = null
                    peticionInfoSerie(idSerie)
                    while (serieInfo == null){

                    }
                    val jsonSerieInfo = JSONObject(serieInfo)
                    val temporadasJSON = jsonSerieInfo.getJSONArray("seasons")
                    val numeroTemporadas = temporadasJSON.length()
                    val capitulosTemporadaActual = temporadasJSON.getJSONObject(numTemporada-1).getInt("episode_count")

                    //Si es último capitulo de temporada, siguiente temporada
                    if(capitulosTemporadaActual == numCapitulo){
                        if(numeroTemporadas != numTemporada){
                            //Actualizamos temporada y capítulo
                            var cv1 = ContentValues()
                            cv1.put(SerieContract.SerieEntry.COLUMN_SEASON, numTemporada+1)
                            cv1.put(SerieContract.SerieEntry.COLUMN_CAP, 1)

                            sqliteBD.update(SerieContract.SerieEntry.TABLE_NAME, cv1, SerieContract.SerieEntry.COLUMN_ID + "=" + idSerie, null)
                        }
                    }

                    //Si no, avanzar capitulo y dejar igual temporada
                    else{
                        var cv1 = ContentValues()
                        cv1.put(SerieContract.SerieEntry.COLUMN_CAP, numCapitulo + 1)

                        sqliteBD.update(SerieContract.SerieEntry.TABLE_NAME, cv1, SerieContract.SerieEntry.COLUMN_ID + "=" + idSerie, null)
                    }
                }
            }

            override fun onItemClick(cardImageView: View?, index: Int) {
                //nada
            }

        })

        for(i in 0 until jsonSeries.length()){
            capituloSerie = null

            var imagenSerie = jsonSeries.getJSONObject(i).getString("poster_path")
            var urlImagenSeleccionada = "https://image.tmdb.org/t/p/w500$imagenSerie"

            var idSerie = jsonSeries.getJSONObject(i).getString("id")

            var numCapitulo = ""
            var numTemporada = ""
            var dbHelper = MyDbHelper(context)
            var sqliteBD = dbHelper.readableDatabase

            var cursor = sqliteBD.rawQuery("SELECT * FROM ${SerieContract.SerieEntry.TABLE_NAME} WHERE ID=${jsonSeries.getJSONObject(i).getString("id")}", null)
            if(cursor.moveToFirst()){
                do {
                    numCapitulo = cursor.getString(3)
                    numTemporada = cursor.getString(2)
                } while (cursor.moveToNext())
            }

            peticionCapituloActual(numTemporada, idSerie, numCapitulo)
            while (capituloSerie == null){

            }

            insertarSerieLista(capituloSerie!!,
                urlImagenSeleccionada,
                jsonSeries.getJSONObject(i).getString("id"))
        }

        estableceAdaptador()
    }

    /**
     * Función para solicitar las series que estamos siguiendo a la base
     * de datos de TheMovieDB
     */
    fun peticionSeriesEnSeguimiento(idcuenta: String){
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/account/$idcuenta/watchlist/tv?api_key=ecfe4f06a0f028c3618838df92bfea77&session_id=$idsesion")
            .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response) {
                    seriesSeguimiento = JSONObject(response.body()!!.string()).getJSONArray("results").toString()
                }
            })
    }

    /**
     * Función para obtener la información del capítulo donde dejamos la serie
     * que estamos siguiendo
     */
    fun peticionCapituloActual(temporada: String, id: String, capitulo: String){
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/$id/season/$temporada/episode/$capitulo?api_key=ecfe4f06a0f028c3618838df92bfea77&session_id=$idsesion")
            .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response) {
                    capituloSerie = JSONObject(response.body()!!.string()).getString("name")
                }
            })
    }

    /**
     * Función para insertar una serie dentro de la lista que se pasará
     * posteriormente al adaptador para la creación de cartas.
     */
    fun insertarSerieLista(titulo: String, imagen: String, idPeli: String){
        listaSeries += Serie(titulo, imagen, idPeli)
    }

    /**
     * Función para establecer el adaptador de las cartas de las
     * series
     */
    fun estableceAdaptador(){
        var adaptador = Adaptador(listaSeries, requireContext())
        cartas!!.setAdapter(adaptador)
    }

    /**
     * Función para solicitar información acerca de una serie.
     */
    fun peticionInfoSerie(id: String){
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/tv/$id?api_key=ecfe4f06a0f028c3618838df92bfea77&session_id=$idsesion")
            .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response) {
                    serieInfo = response.body()!!.string()
                }
            })
    }
}