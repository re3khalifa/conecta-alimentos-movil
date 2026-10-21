package com.example.conectaalimentos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conectaalimentos.ui.theme.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OfferStatus(val label: String) { ACTIVA("Activa"), VENCE_HOY("Vence hoy"), VENCIDA("Vencida") }

/** Oferta real, tal como la regresa `GET /api/ofertas` del backend. */
data class Offer(
    val id: String,
    val title: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val expirationDate: String?,
    val address: String,
    val lat: Double?,
    val lng: Double?,
    val status: OfferStatus
)

private val isoDay = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

/** Convierte el JSON de una oferta (tal como lo manda Mongoose) a nuestro modelo. */
fun JSONObject.toOffer(): Offer {
    val id = optString("_id", optString("id", ""))
    val expiration = optString("expirationDate", null)
    val location = optJSONObject("location")

    val status = try {
        if (expiration.isNullOrBlank()) {
            OfferStatus.ACTIVA
        } else {
            val expDate: Date = isoDay.parse(expiration) ?: Date(Long.MAX_VALUE)
            val now = Date()
            val oneDayMillis = 24L * 60 * 60 * 1000
            when {
                expDate.before(now) -> OfferStatus.VENCIDA
                expDate.time - now.time <= oneDayMillis -> OfferStatus.VENCE_HOY
                else -> OfferStatus.ACTIVA
            }
        }
    } catch (e: Exception) {
        OfferStatus.ACTIVA
    }

    return Offer(
        id = id,
        title = optString("title", ""),
        category = optString("category", "Otros"),
        price = optDouble("price", 0.0),
        quantity = optInt("quantity", 0),
        expirationDate = expiration,
        address = optString("address", ""),
        lat = location?.optDouble("lat")?.takeIf { !it.isNaN() },
        lng = location?.optDouble("lng")?.takeIf { !it.isNaN() },
        status = status
    )
}

private val categories = listOf("Todo", "Panadería", "Frutas", "Comida", "Lácteos", "Otros")

@Composable
fun HomeScreen(
    offers: List<Offer>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onOfferClick: (Offer) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReservasClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todo") }

    val filtered = offers.filter {
        (selectedCategory == "Todo" || it.category == selectedCategory) &&
                (query.isBlank() || it.title.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true))
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            BottomBar(
                selected = 0,
                onInicio = {},
                onReservas = onReservasClick,
                onPerfil = onProfileClick
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ofertas cerca de ti",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = InkDark
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Buscar pan, fruta, comida...", color = MutedText) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MutedText) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FieldBg,
                    unfocusedContainerColor = FieldBg,
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = FieldBorder
                )
            )

            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = cat == selectedCategory,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontWeight = FontWeight.Medium) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Orange,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = Color(0xFF6B4A2B)
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = MutedText)
                }
                filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay ofertas disponibles todavía", color = MutedText)
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filtered) { offer -> OfferCard(offer) { onOfferClick(offer) } }
                }
            }
        }
    }
}

@Composable
private fun OfferCard(offer: Offer, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FieldBg),
        border = BorderStroke(1.dp, FieldBorder)
    ) {
        Column(Modifier.padding(12.dp)) {
            StatusBadge(offer.status, Modifier.align(Alignment.End))
            Spacer(Modifier.height(8.dp))
            Text(
                offer.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = InkDark,
                minLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(offer.category, fontSize = 12.sp, color = InkDark)
            if (offer.address.isNotBlank()) {
                Text(offer.address, fontSize = 12.sp, color = MutedText, maxLines = 1)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = FieldBorder)
            Text(
                "$%.2f".format(offer.price),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Orange
            )
        }
    }
}

@Composable
private fun StatusBadge(status: OfferStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        OfferStatus.ACTIVA -> GreenBadgeBg to LeafGreen
        OfferStatus.VENCE_HOY -> YellowBadgeBg to YellowBadgeText
        OfferStatus.VENCIDA -> YellowBadgeBg to Color(0xFF8A3B1F)
    }
    Box(
        modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status.label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BottomBar(selected: Int, onInicio: () -> Unit, onReservas: () -> Unit, onPerfil: () -> Unit) {
    NavigationBar(containerColor = Cream) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Orange,
            selectedTextColor = Orange,
            indicatorColor = Color.Transparent,
            unselectedIconColor = Color(0xFF555555),
            unselectedTextColor = Color(0xFF555555)
        )
        NavigationBarItem(selected == 0, onInicio, { Icon(Icons.Outlined.Home, null) }, label = { Text("Inicio") }, colors = colors)
        NavigationBarItem(selected == 1, onReservas, { Icon(Icons.Outlined.CalendarMonth, null) }, label = { Text("Reservas") }, colors = colors)
        NavigationBarItem(selected == 2, onPerfil, { Icon(Icons.Outlined.Person, null) }, label = { Text("Perfil") }, colors = colors)
    }
}
