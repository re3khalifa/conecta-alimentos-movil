package com.example.conectaalimentos.data

import android.util.Patterns

/**
 * Validación de entradas del lado del cliente — Actividad 3, punto 3.
 *
 * Esto NO sustituye la validación del backend (que sigue siendo la que de
 * verdad protege la base de datos, con express-validator); es una segunda
 * capa para que el usuario vea el error al instante, sin gastar una
 * petición de red, y para no mandar nunca datos obviamente mal formados.
 */
object Validators {

    /** Categorías válidas: deben coincidir EXACTO con el enum del backend (Offer/Commerce). */
    val OFFER_CATEGORIES = listOf(
        "Panadería", "Frutas", "Comida", "Bebidas", "Lácteos", "Verduras", "Otro"
    )

    private const val MAX_TITLE_LENGTH = 80
    private const val MAX_ADDRESS_LENGTH = 200
    private const val MAX_PRICE = 100_000.0
    private const val MAX_QUANTITY = 10_000

    fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    /** Mínimo 8 caracteres, mayúscula, minúscula, número y símbolo (igual que la web). */
    fun isStrongPassword(password: String): Boolean =
        password.length >= 8 &&
            password.any { it.isUpperCase() } &&
            password.any { it.isLowerCase() } &&
            password.any { it.isDigit() } &&
            password.any { !it.isLetterOrDigit() }

    /** Quita espacios de más y recorta a una longitud máxima razonable. */
    fun sanitizeText(input: String, maxLength: Int = MAX_TITLE_LENGTH): String =
        input.trim().take(maxLength)

    fun isValidOfferTitle(title: String): Boolean =
        title.isNotBlank() && title.trim().length <= MAX_TITLE_LENGTH

    fun isValidAddress(address: String): Boolean =
        address.trim().length <= MAX_ADDRESS_LENGTH

    fun isValidCategory(category: String): Boolean =
        category in OFFER_CATEGORIES

    /** Acepta "45" o "45.50"; rechaza negativos, texto o precios absurdos. */
    fun parsePrice(raw: String): Double? {
        val value = raw.trim().toDoubleOrNull() ?: return null
        return if (value in 0.0..MAX_PRICE) value else null
    }

    /** Acepta enteros no negativos dentro de un rango razonable. */
    fun parseQuantity(raw: String): Int? {
        val value = raw.trim().toIntOrNull() ?: return null
        return if (value in 0..MAX_QUANTITY) value else null
    }
}
