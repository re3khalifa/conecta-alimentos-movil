package com.example.conectaalimentos

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.conectaalimentos.data.ApiClient
import com.example.conectaalimentos.data.ApiResult
import com.example.conectaalimentos.data.AppLogger
import com.example.conectaalimentos.data.Session
import com.example.conectaalimentos.ui.screens.*
import com.example.conectaalimentos.ui.theme.ConectaAlimentosTheme
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Session.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            ConectaAlimentosTheme {
                AppNavigation()
            }
        }
    }
}

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val OFFER_DETAIL = "offer_detail/{offerId}"
    const val PUBLISH = "publish_offer"
    const val MY_OFFERS = "my_offers"
    const val RESERVATIONS = "reservations"
}

/** dd/MM/aaaa (lo que pide el formulario) -> yyyy-MM-dd (lo que espera el backend). */
private fun toIsoDate(ddmmyyyy: String): String? {
    return try {
        val parts = ddmmyyyy.trim().split("/")
        if (parts.size != 3) return null
        val (d, m, y) = parts
        "%04d-%02d-%02d".format(y.toInt(), m.toInt(), d.toInt())
    } catch (e: Exception) {
        null
    }
}

private fun JSONObject.toMerchantOffer(): MerchantOffer {
    val location = optJSONObject("location")
    return MerchantOffer(
        id = optString("_id", optString("id", "")),
        title = optString("title", ""),
        price = "%.2f".format(optDouble("price", 0.0)),
        quantity = optInt("quantity", 0).toString(),
        expiry = optString("expirationDate", "").substringBefore("T"),
        status = if (optInt("quantity", 0) > 0) "Activa" else "Sin stock",
        category = optString("category", "Otros"),
        address = optString("address", "")
    )
}

private fun JSONObject.toReservation(): Reservation {
    val offer = optJSONObject("offer")
    return Reservation(
        id = optString("_id", optString("id", "")),
        title = offer?.optString("title", "Oferta") ?: "Oferta",
        store = offer?.optString("address", "") ?: "",
        price = "%.2f".format(offer?.optDouble("price", 0.0) ?: 0.0),
        code = optString("_id", "").takeLast(6).uppercase(),
        date = optString("createdAt", "").substringBefore("T"),
        pickup = "Consultar con el comercio",
        status = when (optString("status", "activa")) {
            "cancelada" -> "Cancelada"
            else -> "Confirmada"
        }
    )
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var accountType by remember {
        mutableStateOf(if (Session.role == "commerce") AccountType.COMERCIO else AccountType.USUARIO)
    }

    var offers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var offersLoading by remember { mutableStateOf(false) }
    var offersError by remember { mutableStateOf<String?>(null) }

    val merchantOffers = remember { mutableStateListOf<MerchantOffer>() }
    val reservations = remember { mutableStateListOf<Reservation>() }

    // Manejo centralizado de errores y control de sesión (Actividad 3,
    // puntos 4 y 5): un solo lugar que decide qué hacer con cada error de
    // la API, en vez de repetir la lógica en cada pantalla. ApiClient ya
    // registra el detalle técnico en Logcat (solo en debug) y ya limpió
    // Session si el código es 401; aquí solo nos encargamos de avisarle
    // al usuario y, si la sesión expiró, regresarlo al login.
    fun handleApiError(error: ApiResult.Error) {
        AppLogger.w("Error mostrado al usuario: ${error.code} ${error.message}")
        Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()

        if (error.code == 401) {
            accountType = AccountType.USUARIO
            nav.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun loadOffers() {
        scope.launch {
            offersLoading = true
            offersError = null
            when (val result = ApiClient.getOffers()) {
                is ApiResult.Success -> {
                    val arr = result.data as? JSONArray
                    offers = arr?.let { a -> (0 until a.length()).map { a.getJSONObject(it).toOffer() } } ?: emptyList()
                }
                is ApiResult.Error -> {
                    offersError = result.message
                    if (result.code == 401) handleApiError(result)
                }
            }
            offersLoading = false
        }
    }

    fun loadMyOffers() {
        scope.launch {
            when (val result = ApiClient.getMyOffers()) {
                is ApiResult.Success -> {
                    val arr = result.data as? JSONArray
                    merchantOffers.clear()
                    arr?.let { a -> (0 until a.length()).forEach { merchantOffers.add(a.getJSONObject(it).toMerchantOffer()) } }
                }
                is ApiResult.Error -> handleApiError(result)
            }
        }
    }

    fun loadReservations() {
        scope.launch {
            when (val result = ApiClient.getMyReservations()) {
                is ApiResult.Success -> {
                    val arr = result.data as? JSONArray
                    reservations.clear()
                    arr?.let { a -> (0 until a.length()).forEach { reservations.add(a.getJSONObject(it).toReservation()) } }
                }
                is ApiResult.Error -> handleApiError(result)
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = if (Session.isLoggedIn) Routes.HOME else Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = { email, password ->
                    scope.launch {
                        when (val result = ApiClient.login(email, password)) {
                            is ApiResult.Success -> {
                                val json = result.data as? JSONObject
                                val token = json?.optString("token")
                                val user = json?.optJSONObject("user")
                                if (token.isNullOrBlank()) {
                                    Toast.makeText(
                                        context,
                                        "El backend no regresó token. Revisa el parche de auth.controller.js (ver instrucciones).",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Session.token = token
                                    Session.role = user?.optString("role")
                                    Session.email = user?.optString("email")
                                    Session.userId = user?.optString("id")
                                    accountType = if (Session.role == "commerce") AccountType.COMERCIO else AccountType.USUARIO
                                    nav.navigate(Routes.HOME) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            }
                            is ApiResult.Error -> Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            // Nota: aquí NO usamos handleApiError porque un
                            // intento de login fallido (ej. contraseña
                            // incorrecta) también regresa 401, y sería raro
                            // "redirigir al login" cuando ya estamos ahí.
                        }
                    }
                },
                onRegisterClick = { nav.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterClick = { email, password, selectedType ->
                    val roleStr = if (selectedType == AccountType.COMERCIO) "commerce" else "user"
                    scope.launch {
                        when (val regResult = ApiClient.register(email, password, roleStr)) {
                            is ApiResult.Success -> {
                                // El registro no inicia sesión solo; pedimos el login
                                // justo después para obtener el token.
                                when (val loginResult = ApiClient.login(email, password)) {
                                    is ApiResult.Success -> {
                                        val json = loginResult.data as? JSONObject
                                        val token = json?.optString("token")
                                        val user = json?.optJSONObject("user")
                                        Session.token = token
                                        Session.role = user?.optString("role") ?: roleStr
                                        Session.email = user?.optString("email") ?: email
                                        Session.userId = user?.optString("id")
                                        accountType = selectedType
                                        nav.navigate(Routes.HOME) {
                                            popUpTo(Routes.LOGIN) { inclusive = true }
                                        }
                                    }
                                    is ApiResult.Error -> Toast.makeText(context, "Cuenta creada, pero falló el inicio de sesión: ${loginResult.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            is ApiResult.Error -> Toast.makeText(context, regResult.message, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onLoginClick = { nav.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            LaunchedEffect(Unit) { loadOffers() }

            HomeScreen(
                offers = offers,
                isLoading = offersLoading,
                errorMessage = offersError,
                onOfferClick = { offer ->
                    nav.navigate("offer_detail/${Uri.encode(offer.id)}")
                },
                onProfileClick = { nav.navigate(Routes.PROFILE) },
                onReservasClick = { nav.navigate(Routes.RESERVATIONS) }
            )
        }

        composable(
            route = Routes.OFFER_DETAIL,
            arguments = listOf(navArgument("offerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""

            OfferDetailScreen(
                offerId = offerId,
                onBack = { nav.popBackStack() },
                onReserved = { loadOffers() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                accountType = accountType,
                onBack = { nav.popBackStack() },
                onMyOffers = { nav.navigate(Routes.MY_OFFERS) },
                onReservations = { nav.navigate(Routes.RESERVATIONS) },
                onLogout = {
                    scope.launch { ApiClient.logout() }
                    Session.clear()
                    accountType = AccountType.USUARIO

                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // PUBLICAR OFERTAS
        composable(Routes.PUBLISH) {
            if (accountType == AccountType.COMERCIO) {
                PublishOfferScreen(
                    onBack = { nav.popBackStack() },
                    onPublish = { title, category, price, quantity, expiry, address ->
                        scope.launch {
                            val priceNum = price.toDoubleOrNull() ?: 0.0
                            val qtyNum = quantity.toIntOrNull() ?: 0
                            val isoExpiry = toIsoDate(expiry)

                            when (val result = ApiClient.createOffer(
                                title = title,
                                category = category,
                                price = priceNum,
                                quantity = qtyNum,
                                expirationDateIso = isoExpiry,
                                address = address,
                                lat = null,
                                lng = null
                            )) {
                                is ApiResult.Success -> {
                                    Toast.makeText(context, "Oferta publicada", Toast.LENGTH_SHORT).show()
                                    loadMyOffers()
                                    nav.popBackStack()
                                }
                                is ApiResult.Error -> handleApiError(result)
                            }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Solo los comercios pueden publicar ofertas", Toast.LENGTH_SHORT).show()
                    nav.popBackStack()
                }
            }
        }

        // MIS OFERTAS
        composable(Routes.MY_OFFERS) {
            LaunchedEffect(Unit) { loadMyOffers() }

            MyOffersScreen(
                offers = merchantOffers,
                onBack = { nav.popBackStack() },
                onNewOffer = { nav.navigate(Routes.PUBLISH) },
                onDelete = { offer ->
                    scope.launch {
                        when (val result = ApiClient.deleteOffer(offer.id)) {
                            is ApiResult.Success -> {
                                merchantOffers.remove(offer)
                                Toast.makeText(context, "Oferta eliminada", Toast.LENGTH_SHORT).show()
                            }
                            is ApiResult.Error -> handleApiError(result)
                        }
                    }
                },
                onEdit = {
                    Toast.makeText(context, "Para editar, publica una nueva versión de esta oferta", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // RESERVAS
        composable(Routes.RESERVATIONS) {
            LaunchedEffect(Unit) { loadReservations() }

            ReservationsScreen(
                reservations = reservations,
                onBack = { nav.popBackStack() },
                onDetails = { reservation ->
                    Toast.makeText(context, "${reservation.title} · ${reservation.pickup}", Toast.LENGTH_LONG).show()
                },
                onCancel = {
                    // El backend todavía no expone un endpoint para cancelar una
                    // reserva (solo crear y listar), así que por ahora solo avisamos.
                    Toast.makeText(context, "Cancelar reserva aún no está disponible en el backend", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
