package com.example.appseriespdm

import android.provider.BaseColumns

/**
 * Esquema de la tabla de preferencias
 * almacenada en la base de datos local.
 */
class UsuarioContract {
    object UsuarioEntry : BaseColumns{
        const val TABLE_NAME = "usuario"
        const val COLUMN_ID = "id"
        const val COLUMN_PLATAFORMAS = "plataformas"
        const val COLUMN_PREFERENCIAS = "preferencias"
        const val COLUMN_CANCELADAS = "seriecancelada"
    }
}