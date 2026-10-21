package com.example.conectaalimentos.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda localmente, en el propio celular, el token de sesión (JWT) que
 * regresa el backend al iniciar sesión, junto con el rol y el correo.
 *
 * Actividad 3, punto 1 "Almacenamiento de Datos Seguros": en vez de
 * SharedPreferences normal (que guarda todo en un XML plano, legible por
 * cualquiera con acceso root al celular), se usa EncryptedSharedPreferences
 * de Android (androidx.security.crypto). La llave de cifrado (MasterKey)
 * se genera y se guarda en el Android Keystore del propio sistema
 * operativo, protegida por hardware cuando el equipo lo soporta — nunca
 * queda en texto plano en el código ni en el archivo.
 *
 * No es una base de datos ni una copia de MongoDB: solo recuerda quién
 * eres mientras tienes la app instalada, igual que hace la cookie
 * httpOnly en el navegador cuando usas la versión web.
 */
object Session {
    private const val PREFS_NAME = "conecta_alimentos_session_secure"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return

        prefs = try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Si el Keystore del equipo fallara (muy raro), no tumbamos la
            // app: seguimos con SharedPreferences normal como respaldo,
            // mejor que dejar la sesión sin guardar.
            AppLogger.e("No se pudo inicializar el almacenamiento cifrado, usando respaldo", e)
            context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit().putString("token", value).apply()

    var role: String?
        get() = prefs.getString("role", null)
        set(value) = prefs.edit().putString("role", value).apply()

    var email: String?
        get() = prefs.getString("email", null)
        set(value) = prefs.edit().putString("email", value).apply()

    var userId: String?
        get() = prefs.getString("userId", null)
        set(value) = prefs.edit().putString("userId", value).apply()

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    /** Cierra la sesión local: borra token, rol y correo guardados. */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
