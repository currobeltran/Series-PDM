package com.example.appseriespdm

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Clase que nos permitirá controlar la vista tabular de la
 * actividad principal, controlando cuando se carga un
 * fragmento u otro.
 */
class ViewPagerAdapter(var fr: FragmentActivity, var requestToken: String, var idSesion: String, var infoCuenta: String) : FragmentStateAdapter(fr) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragmento: Fragment?

        if(position == 0){
            fragmento = Fragment1()
        }
        else{
            fragmento = Fragment2()
        }

        fragmento.arguments = Bundle().apply {
            putString("REQUESTOKEN", requestToken)
            putString("IDSESION", idSesion)
            putString("INFOCUENTA", infoCuenta)
        }

        return fragmento
    }
}