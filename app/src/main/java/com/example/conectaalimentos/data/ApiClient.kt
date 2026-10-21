package com.example.conectaalimentos.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Dirección del backend ya desplegado (Node/Express + MongoDB Atlas),
 * el MISMO que usa la app web. La app móvil nunca habla directo con
 * MongoDB Atlas: le pide los datos a esta API, igual que lo hace el
 * navegador con la página web.
 */
object Config {
    const val BASE_URL = "https://conecta-alimentos.onrender.com"

    init {
        // Actividad 3, punto 2 "Comunicación de red segura": si alguien
        // cambia esta constante por una URL http:// sin darse cuenta, la
        // app se niega a arrancar peticiones en vez de mandar credenciales
        // sin cifrar. Se complementa con network_security_config.xml, que
        // bloquea tráfico sin HTTPS a nivel del sistema operativo.
        require(BASE_URL.startsWith("https://")) {
            "Config.BASE_URL debe usar HTTPS, nunca HTTP sin cifrar"
        }
    }
}

/** Resultado simple de una llamada a la API: éxito con datos, o error con mensaje. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
}

/**
 * Cliente HTTP simple, sin Retrofit ni MVVM: una conexión por llamada con
 * HttpURLConnection (ya incluido en Android) y JSON a mano con org.json
 * (también incluido). Cada función es "suspend" para llamarse desde una
 * coroutine (rememberCoroutineScope) sin congelar la interfaz.
 */
object ApiClient {

    private fun openConnection(path: String, method: String): HttpsURLConnection {
        val url = URL(Config.BASE_URL + path)
        // HttpsURLConnection en vez de HttpURLConnection: si alguna vez la
        // URL deja de ser https por error, esto truena al castear en vez
        // de mandar la petición sin cifrar en silencio.
        val conn = url.openConnection() as HttpsURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")

        // Mismo JWT que usa la web, pero mandado como header en vez de
        // cookie (una app nativa no tiene cookie de navegador), ver el
        // parche de auth.middleware.js en el backend.
        Session.token?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
        return conn
    }

    private fun readBody(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return stream?.use { s ->
            BufferedReader(InputStreamReader(s, Charsets.UTF_8)).readText()
        } ?: ""
    }

    private suspend fun request(
        path: String,
        method: String,
        body: JSONObject? = null
    ): ApiResult<Any?> = withContext(Dispatchers.IO) {
        try {
            val conn = openConnection(path, method)
            if (body != null) {
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
            }

            val code = conn.responseCode
            val text = readBody(conn)
            conn.disconnect()

            AppLogger.d("$method $path -> $code")

            if (code in 200..299) {
                val parsed: Any? = when {
                    text.isBlank() -> null
                    text.trim().startsWith("[") -> JSONArray(text)
                    else -> JSONObject(text)
                }
                ApiResult.Success(parsed)
            } else {
                val message = try {
                    JSONObject(text).optString("message", "Error del servidor ($code)")
                } catch (e: Exception) {
                    "Error del servidor ($code)"
                }

                // Actividad 3, punto 4 "Autenticación y control de sesiones":
                // si el token ya no es válido (expiró, se revocó, o nunca
                // existió), se borra la sesión guardada en el celular de
                // inmediato. MainActivity revisa este código y manda de
                // vuelta a la pantalla de login.
                if (code == 401) {
                    AppLogger.w("Sesión expirada o token inválido, se limpia la sesión local")
                    Session.clear()
                }

                AppLogger.w("$method $path -> $code: $message")
                ApiResult.Error(message, code)
            }
        } catch (e: Exception) {
            // Nunca se expone e.message del stacktrace completo al usuario
            // ni al log en release; solo un mensaje genérico. El detalle
            // real solo se escribe en Logcat y solo en builds de debug.
            AppLogger.e("Fallo de red en $method $path", e)
            ApiResult.Error("No se pudo conectar con el servidor. Revisa tu conexión a internet.")
        }
    }

    // ── Autenticación ───────────────────────────────────────────────────
    suspend fun register(email: String, password: String, role: String): ApiResult<Any?> {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("role", role)
            put("privacyAccepted", true)
        }
        return request("/api/auth/register", "POST", body)
    }

    suspend fun login(email: String, password: String): ApiResult<Any?> {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        return request("/api/auth/login", "POST", body)
    }

    suspend fun logout(): ApiResult<Any?> = request("/api/auth/logout", "POST")

    // ── Ofertas ─────────────────────────────────────────────────────────
    suspend fun getOffers(): ApiResult<Any?> = request("/api/ofertas", "GET")

    suspend fun getOfferById(id: String): ApiResult<Any?> = request("/api/ofertas/$id", "GET")

    suspend fun getMyOffers(): ApiResult<Any?> = request("/api/ofertas/mine", "GET")

    suspend fun createOffer(
        title: String,
        category: String,
        price: Double,
        quantity: Int,
        expirationDateIso: String?,
        address: String,
        lat: Double?,
        lng: Double?
    ): ApiResult<Any?> {
        val body = JSONObject().apply {
            put("title", title)
            put("category", category)
            put("price", price)
            put("quantity", quantity)
            if (expirationDateIso != null) put("expirationDate", expirationDateIso)
            put("address", address)
            if (lat != null && lng != null) {
                put("location", JSONObject().apply {
                    put("lat", lat)
                    put("lng", lng)
                })
            }
        }
        return request("/api/ofertas", "POST", body)
    }

    suspend fun deleteOffer(id: String): ApiResult<Any?> = request("/api/ofertas/$id", "DELETE")

    // ── Reservas ────────────────────────────────────────────────────────
    suspend fun createReservation(offerId: String): ApiResult<Any?> =
        request("/api/reservas/$offerId", "POST")

    suspend fun getMyReservations(): ApiResult<Any?> = request("/api/reservas/mine", "GET")
}
