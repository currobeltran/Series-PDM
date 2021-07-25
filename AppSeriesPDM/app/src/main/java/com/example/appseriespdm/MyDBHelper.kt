package com.example.appseriespdm

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Clase que nos servirá para interactuar con la base de datos
 * local que pueden poseer todas las aplicaciones Android.
 */
class MyDbHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    /**
     * Si no existe la base de datos, se crea con esta información
     * Se crean dos tablas, una para las preferencias del usuario
     * y otra, que estará asociada con cada serie, donde se almacenará
     * el capítulo por donde el usuario lleva dicha serie.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE " + SerieContract.SerieEntry.TABLE_NAME + " (" +
                SerieContract.SerieEntry.COLUMN_ID + " VARCHAR(30) PRIMARY KEY," +
                SerieContract.SerieEntry.COLUMN_NAME + " TEXT," +
                SerieContract.SerieEntry.COLUMN_SEASON + " INT," +
                SerieContract.SerieEntry.COLUMN_CAP + " INT)")

        db.execSQL("CREATE TABLE " + UsuarioContract.UsuarioEntry.TABLE_NAME + " (" +
                UsuarioContract.UsuarioEntry.COLUMN_ID + " VARCHAR(30) PRIMARY KEY," +
                UsuarioContract.UsuarioEntry.COLUMN_PLATAFORMAS + " TEXT," +
                UsuarioContract.UsuarioEntry.COLUMN_PREFERENCIAS + " TEXT," +
                UsuarioContract.UsuarioEntry.COLUMN_CANCELADAS + " INT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + SerieContract.SerieEntry.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS " + UsuarioContract.UsuarioEntry.TABLE_NAME)
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "pym.db"
    }
}
