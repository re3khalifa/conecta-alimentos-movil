package com.example.conectaalimentos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFF3EFE8)
val Orange = Color(0xFFE03C08)
val InkDark = Color(0xFF1F1F1F)
val FieldBg = Color(0xFFF7F5F1)
val FieldBorder = Color(0xFFD9D4CB)
val LeafGreen = Color(0xFF4F7A4A)
val GreenBadgeBg = Color(0xFFDDEBD6)
val YellowBadgeBg = Color(0xFFFBE9A8)
val YellowBadgeText = Color(0xFF6B4E00)
val MutedText = Color(0xFF8A857C)

private val ColorsConecta = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    background = Cream,
    surface = Cream,
    onBackground = InkDark,
    onSurface = InkDark
)

@Composable
fun ConectaAlimentosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorsConecta, content = content)
}
