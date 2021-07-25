package com.example.appseriespdm

import android.content.ContentValues
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi

/**
 * Actividad que nos permitirá configurar las recomendaciones de
 * nuestra aplicación.
 */
class Configuracion : AppCompatActivity() {
    var plataformas: String = ""
    var criterios: String = ""
    var idusuario: String = ""
    var canceladas: Boolean? = false

    /**
     * Al crear la actividad, se cargan las preferencias guardadas
     * anteriormente
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)
        //Cargar aquí las opciones elegidas por el usuario (si existe)
        idusuario = intent.getStringExtra("IDUSUARIO")!!
        var dbhelper = MyDbHelper(this)
        var sqlite = dbhelper.writableDatabase

        var cursor = sqlite.rawQuery("SELECT * FROM ${UsuarioContract.UsuarioEntry.TABLE_NAME} WHERE ID=${idusuario}",null)
        if(cursor.moveToFirst()){
            plataformas = cursor.getString(1)
            criterios = cursor.getString(2)
            val numCancelada = cursor.getInt(3)
            canceladas = numCancelada != 0
        }

        if(canceladas!!){
            var botonCanceladas = findViewById<CheckBox>(R.id.verCanceladas)
            botonCanceladas.isChecked = true
        }
    }

    /**
     * Al seleccionar alguno de los dos botones, nos aparecerá
     * la lista de opciones para que, posteriormente, podamos
     * interactuar con ellas. Cargamos toda la lógica necesaria
     * para que, a la hora de cerrar la actividad, estas preferencias
     * se guarden en la base de datos local.
     */
    fun seleccionOpcion(view: View){
        var layout = findViewById<LinearLayout>(R.id.listaConfiguracion)

        layout.removeAllViews()
        if(view.id == R.id.botonplataformas){
            var netflix = CheckBox(this)
            netflix.text = "Netflix"
            netflix.textSize = 18.0f
            netflix.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    //Añadir a plataformas
                    plataformas += "Netflix,"
                }
                else{
                    plataformas = plataformas.replace("Netflix,", "")
                }
            }
            if(plataformas.contains("Netflix")){
                netflix.isChecked = true
            }
            layout.addView(netflix)

            var hbo = CheckBox(this)
            hbo.text = "HBO"
            hbo.textSize = 18.0f
            hbo.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    //Añadir a plataformas
                    plataformas += "HBO,"
                }
                else{
                    plataformas = plataformas.replace("HBO,", "")
                }
            }
            if(plataformas.contains("HBO")){
                hbo.isChecked = true
            }
            layout.addView(hbo)

            var prime = CheckBox(this)
            prime.text = "Prime Video"
            prime.textSize = 18.0f
            prime.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    //Añadir a plataformas
                    plataformas += "Amazon Prime Video,"
                }
                else{
                    plataformas = plataformas.replace("Amazon Prime Video,", "")
                }
            }
            if(plataformas.contains("Amazon Prime Video")){
                prime.isChecked = true
            }
            layout.addView(prime)

            var disney = CheckBox(this)
            disney.text = "Disney +"
            disney.textSize = 18.0f
            disney.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    //Añadir a plataformas
                    plataformas += "Disney Plus,"
                }
                else{
                    plataformas = plataformas.replace("Disney Plus,", "")
                }
            }
            if(plataformas.contains("Disney Plus")){
                disney.isChecked = true
            }
            layout.addView(disney)
        }
        if(view.id == R.id.botoncriterios){
            var radioGroup = RadioGroup(this)

            var mostrarPopulares = RadioButton(this)
            mostrarPopulares.text = "Mostrar series populares"
            radioGroup.addView(mostrarPopulares)

            var mostrarMasValoradas = RadioButton(this)
            mostrarMasValoradas.text = "Mostrar series más valoradas"
            radioGroup.addView(mostrarMasValoradas)

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if(checkedId == mostrarMasValoradas.id){
                    criterios = "valoradas"
                }
                else{
                    criterios = "popular"
                }
            }

            if(criterios.contains("valoradas")){
                radioGroup.check(mostrarMasValoradas.id)
            }
            else{
                radioGroup.check(mostrarPopulares.id)
            }
            layout.addView(radioGroup)
        }
    }

    /**
     * Al finalizar la actividad, guardamos toda la información de
     * nuevo en la base de datos, para que los cambios efectuados perduren
     * a lo largo del tiempo.
     */
    override fun onStop() {
        var botonCanceladas = findViewById<CheckBox>(R.id.verCanceladas)
        canceladas = botonCanceladas.isChecked

        // Guardar aquí las opciones del usuario
        // Si no existe crear uno nuevo
        var dbhelper = MyDbHelper(this)
        var sqlite = dbhelper.writableDatabase

        var cursor = sqlite.rawQuery("SELECT * FROM ${UsuarioContract.UsuarioEntry.TABLE_NAME} WHERE ID=${idusuario}",null)
        if(cursor.moveToFirst()){
            //Existe usuario y se edita info
            var cv = ContentValues().apply {
                put(UsuarioContract.UsuarioEntry.COLUMN_PLATAFORMAS, plataformas)
                put(UsuarioContract.UsuarioEntry.COLUMN_PREFERENCIAS, criterios)
                put(UsuarioContract.UsuarioEntry.COLUMN_CANCELADAS, canceladas)
            }

            sqlite.update(UsuarioContract.UsuarioEntry.TABLE_NAME,cv,"ID = $idusuario", null)
        }
        else{
            //Se crea nuevo usuario con datos
            var cv = ContentValues().apply{
                put(UsuarioContract.UsuarioEntry.COLUMN_ID, idusuario)
                put(UsuarioContract.UsuarioEntry.COLUMN_PLATAFORMAS, plataformas)
                put(UsuarioContract.UsuarioEntry.COLUMN_PREFERENCIAS, criterios)
                put(UsuarioContract.UsuarioEntry.COLUMN_CANCELADAS, canceladas)
            }

            sqlite.insert(UsuarioContract.UsuarioEntry.TABLE_NAME,null,cv)
        }
        super.onStop()
    }
}