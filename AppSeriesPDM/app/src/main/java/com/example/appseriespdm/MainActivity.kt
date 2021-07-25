package com.example.appseriespdm

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    var idsesion: String? = null
    var infousuario: String? = null
    var requestToken: String? = null

    /**
     * Creación de la actividad. Aquí se carga tanto la vista
     * tabular con los dos fragmentos, como el buscador de series.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestToken = intent.getStringExtra("RequestToken")

        peticionIdSesion()
        while (idsesion==null){

        }

        peticionInformacionCuenta()
        while (infousuario == null){

        }

        var adapter = ViewPagerAdapter(this, requestToken!!, idsesion!!, infousuario!!)
        var pager = findViewById<ViewPager2>(R.id.pager)
        pager.adapter = adapter
        // Con la siguiente línea desactivamos el deslizamiento
        // entre fragments. Esto lo hacemos para no solapar la
        // funcionalidad de las cartas deslizantes.
        pager.isUserInputEnabled = false
        val tabLayoutMediator = TabLayoutMediator(
            findViewById(R.id.tabLayout),
            findViewById(R.id.pager),
            TabLayoutMediator.TabConfigurationStrategy{ tab, position ->  
                if(position == 0){
                    tab.text = "Mi seguimiento"
                }
                else{
                    tab.text = "Recomendaciones"
                }
            })
        tabLayoutMediator.attach()

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val buscador = findViewById<SearchView>(R.id.searchView)
        buscador.setSearchableInfo(searchManager.getSearchableInfo(componentName))
    }

    override fun onSearchRequested(): Boolean {
        return super.onSearchRequested()
    }

    /**
     * Sobreescritura del método startActivity para que, cuando
     * se llame a la función de busqueda, se pasen los argumentos
     * necesarios para introducir cualquier serie que coincida
     * con la búsqueda en la lista de seguimiento
     */
    override fun startActivity(intent: Intent) {
        // check if search intent
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.putExtra("IDSESION", idsesion)
            val idcuenta = JSONObject(infousuario).getString("id")
            intent.putExtra("IDCUENTA", idcuenta)
        }
        super.startActivity(intent)
    }

    /**
     * Función para ver la configuración del usuario
     */
    fun verConfiguracion(view: View){
        var intentUsuariosSeguidos = Intent(this, Configuracion::class.java)
        var infoJSON = JSONObject(infousuario)
        var idcuenta = infoJSON.getString("id")
        intentUsuariosSeguidos.putExtra("IDUSUARIO", idcuenta)

        startActivity(intentUsuariosSeguidos)
    }

    /**
     * Función para solicitar infomación acerca de nuestra cuenta de usuario
     *
     * Nos servirá para obtener nuestro ID de usuario dentro de la base de datos,
     * con lo que podremos realizar acciones sobre nuestra propia cuenta
     */
    fun peticionInformacionCuenta(){
        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/account?api_key=ecfe4f06a0f028c3618838df92bfea77&session_id=$idsesion")
            .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response) {
                    infousuario = response.body()!!.string()
                }
            })
    }

    /**
     * Función para solicitar un ID de sesión a través de un Request Token
     *
     * Este ID nos servirá para peticiones que realizaremos en un futuro.
     */
    fun peticionIdSesion(){
        val requestBody = FormBody.Builder()
            .add("request_token", requestToken)
            .build()

        val request = Request.Builder()
            .url("https://api.themoviedb.org/3/authentication/session/new?api_key=ecfe4f06a0f028c3618838df92bfea77")
            .method("POST", requestBody)
            .build()
        val cliente = OkHttpClient()

        cliente.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response) {
                    val res = JSONObject(response.body()!!.string())

                    if (res.getBoolean("success")) {
                        idsesion = res.getString("session_id")
                    }
                }
            })
    }

    /**
     * Función para iniciar la actividad donde podremos ver el
     * listado de series que estamos siguiendo.
     */
    fun verMisSeries(v: View){
        val intentListaSeries = Intent(this, MisSeries::class.java)
        intentListaSeries.putExtra("IDSESION", idsesion)

        var infoJSON = JSONObject(infousuario)
        var idcuenta = infoJSON.getString("id")
        intentListaSeries.putExtra("IDCUENTA", idcuenta)

        startActivity(intentListaSeries)
    }
}