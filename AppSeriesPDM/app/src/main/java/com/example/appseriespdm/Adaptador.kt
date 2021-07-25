package com.example.appseriespdm

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.appseriespdm.R
import com.example.appseriespdm.Serie
import com.huxq17.swipecardsview.BaseCardAdapter
import com.squareup.picasso.Picasso

/**
 * Adaptador para las cartas deslizables correspondientes
 * a capítulos de series o series.
 */
class Adaptador : BaseCardAdapter<CardView> {
    var listaSeries: List<Serie> = listOf()
    var contexto: Context? = null

    constructor(listaSeries: List<Serie>, contexto: Context){
        this.listaSeries = listaSeries
        this.contexto = contexto
    }

    /**
     * Obtiene el numero de series que maneja el adaptador
     */
    override fun getCount(): Int{
        return listaSeries.size
    }

    /**
     * Obtiene el ID del layout donde se mostrará la información
     * correspondiente a una serie o capítulo (el diseño de la carta)
     */
    override fun getCardLayoutId(): Int{
        return R.layout.carta_serie
    }

    /**
     * En esta función se carga la información de una serie o capítulo
     * en el view correspondiente a la carta.
     */
    override fun onBindData(position: Int, cardview: View){
        if(listaSeries.isEmpty()){
            return
        }

        val imagen = cardview.findViewById<ImageView>(R.id.imagencarta)
        val texto = cardview.findViewById<TextView>(R.id.tituloserie)
        var pelicula = listaSeries[position]
        texto.text = pelicula.titulo
        Picasso.with(contexto).load(pelicula.imagen).into(imagen)
    }
}