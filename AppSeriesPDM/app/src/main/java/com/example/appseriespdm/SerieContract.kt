package com.example.appseriespdm

import android.provider.BaseColumns

/**
 * Esquema de la tabla serie almacenada
 * en la base de datos local.
 */
class SerieContract {
    object SerieEntry : BaseColumns{
        const val TABLE_NAME = "serie"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_SEASON = "season"
        const val COLUMN_CAP = "chapter"
    }
}