package com.example.conectaalimentos.data

import android.util.Log
import com.example.conectaalimentos.BuildConfig

/**
 * Punto único de registro (logs) de la app — Actividad 3, punto 5
 * "Manejo de errores y registro".
 *
 * Reglas de seguridad:
 *  - Solo escribe en Logcat cuando la app está compilada en modo debug
 *    (BuildConfig.DEBUG). En el build de release (el que se sube a la
 *    tienda) no se genera NINGÚN log, para no dejar rastro de errores
 *    internos ni de datos sensibles en el dispositivo del usuario final.
 *  - Nunca se le pasa a esta clase el token, la contraseña ni el cuerpo
 *    completo de una petición; solo mensajes descriptivos del error.
 */
object AppLogger {
    private const val TAG = "ConectaAlimentos"

    fun d(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message)
    }

    /** Para errores esperados (ej. el servidor respondió 404/401/500). */
    fun w(message: String) {
        if (BuildConfig.DEBUG) Log.w(TAG, message)
    }

    /** Para excepciones reales (fallo de red, parseo, etc.). */
    fun e(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(TAG, message, throwable)
    }
}
