package com.example.conectaalimentos.ui.screens

import android.content.Intent
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.conectaalimentos.data.ApiClient
import com.example.conectaalimentos.data.AppLogger
import com.example.conectaalimentos.data.ApiResult
import com.example.conectaalimentos.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offerId: String,
    onBack: () -> Unit,
    onReserved: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var offer by remember { mutableStateOf<Offer?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reserving by remember { mutableStateOf(false) }
    var showReservationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(offerId) {
        loading = true
        error = null
        when (val result = ApiClient.getOfferById(offerId)) {
            is ApiResult.Success -> {
                val json = result.data as? org.json.JSONObject
                offer = json?.toOffer()
                if (json == null) error = "No se pudo leer la oferta"
            }
            is ApiResult.Error -> error = result.message
        }
        loading = false
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Text("Detalle de oferta", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Volver", tint = Orange)
                    }
                },
                actions = {
                    if (offer != null) {
                        IconButton(onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${offer!!.title} - $${offer!!.price} en Conecta Alimentos"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartir oferta"))
                        }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Compartir oferta", tint = Orange)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        },
        bottomBar = {
            if (offer != null) {
                Surface(color = Cream, shadowElevation = 8.dp) {
                    Button(
                        onClick = { showReservationDialog = true },
                        enabled = !reserving && offer!!.quantity > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) {
                        Text(
                            if (offer!!.quantity > 0) "Reservar" else "Sin stock",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            loading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
            error != null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Ocurrió un error", color = MutedText)
            }
            offer != null -> {
                val o = offer!!
                val emoji = when (o.category) {
                    "Frutas" -> "🍎  🍌  🍊"
                    "Comida" -> "🍲"
                    "Lácteos" -> "🥛"
                    else -> "🥖  🥐  🍞"
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color(0xFF795438)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 66.sp)

                        Surface(
                            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                            color = GreenBadgeBg,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                o.status.label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = LeafGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Column(Modifier.padding(horizontal = 18.dp, vertical = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    o.title,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 27.sp,
                                    lineHeight = 31.sp,
                                    color = InkDark
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(o.category, color = InkDark, fontSize = 14.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "$%.2f".format(o.price),
                                    color = Orange,
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${o.quantity} disponibles",
                                    color = LeafGreen,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = FieldBg),
                            border = BorderStroke(1.dp, FieldBorder)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Orange, modifier = Modifier.size(25.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    o.expirationDate?.let { "Vence: ${it.substringBefore("T")}" } ?: "Sin fecha de caducidad",
                                    fontSize = 16.sp,
                                    color = InkDark
                                )
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Orange)
                            Spacer(Modifier.width(6.dp))
                            Text("Ubicación", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        }

                        Spacer(Modifier.height(10.dp))

                        val lat = o.lat
                        val lng = o.lng

                        if (lat != null && lng != null) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true

                                        webChromeClient = object : WebChromeClient() {
                                            override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                                                AppLogger.w("WebView JS: ${m.message()} (${m.sourceId()}:${m.lineNumber()})")
                                                return true
                                            }
                                        }
                                        webViewClient = object : WebViewClient() {
                                            override fun onReceivedError(v: WebView?, r: WebResourceRequest?, e: WebResourceError?) {
                                                AppLogger.e("WebView error: ${e?.description} -> ${r?.url}")
                                            }
                                            override fun onReceivedHttpError(v: WebView?, r: WebResourceRequest?, e: WebResourceResponse?) {
                                                AppLogger.e("WebView HTTP ${e?.statusCode} -> ${r?.url}")
                                            }
                                        }

                                        // Mismo enfoque que la app web: un iframe embebido de
                                        // Google Maps con las coordenadas de la oferta, sin
                                        // necesidad de API key ni SDK nativo.
                                        val html = """
                                            <!DOCTYPE html>
                                            <html>
                                            <head>
                                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                                <style>
                                                    html, body { margin:0; padding:0; width:100%; height:100%; overflow:hidden; }
                                                    iframe { width:100%; height:100%; border:0; }
                                                </style>
                                            </head>
                                            <body>
                                                <iframe src="https://www.google.com/maps?q=$lat,$lng&output=embed"></iframe>
                                            </body>
                                            </html>
                                        """.trimIndent()
                                        loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(14.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(125.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE9E2CE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sin ubicación disponible", color = Color(0xFF655D4D), fontWeight = FontWeight.Medium)
                            }
                        }

                        if (o.address.isNotBlank()) {
                            Text(
                                o.address,
                                modifier = Modifier.padding(top = 10.dp, bottom = 12.dp),
                                color = InkDark,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                if (showReservationDialog) {
                    AlertDialog(
                        onDismissRequest = { showReservationDialog = false },
                        title = { Text("Confirmar reserva") },
                        text = {
                            Text("¿Quieres reservar '${o.title}' por $%.2f?".format(o.price))
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showReservationDialog = false
                                reserving = true
                                scope.launch {
                                    when (val result = ApiClient.createReservation(offerId)) {
                                        is ApiResult.Success -> {
                                            Toast.makeText(context, "Reserva confirmada", Toast.LENGTH_SHORT).show()
                                            onReserved()
                                        }
                                        is ApiResult.Error -> {
                                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    reserving = false
                                }
                            }) {
                                Text("Confirmar", color = Orange)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showReservationDialog = false }) {
                                Text("Cancelar", color = MutedText)
                            }
                        },
                        containerColor = Cream
                    )
                }
            }
        }
    }
}
