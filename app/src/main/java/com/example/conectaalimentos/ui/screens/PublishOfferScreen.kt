package com.example.conectaalimentos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.conectaalimentos.data.Validators
import com.example.conectaalimentos.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishOfferScreen(
    onBack: () -> Unit,
    onPublish: (String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Panadería") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var photoMessage by remember {
        mutableStateOf("Sin fotografía seleccionada")
    }
    // Validación de entradas (Actividad 3, punto 3): en vez de un simple
    // true/false, guardamos el motivo exacto para que el usuario sepa qué
    // corregir, y para no gastar una petición al servidor con datos que
    // ya sabemos que van a ser rechazados.
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = Validators.OFFER_CATEGORIES

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Text("Publicar oferta")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Orange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Sección de fotografía
            Card(
                border = BorderStroke(
                    1.dp,
                    Orange.copy(alpha = 0.5f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = FieldBg
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Fotografía",
                        tint = Orange,
                        modifier = Modifier.size(38.dp)
                    )

                    Text(
                        text = "Sube una foto de tu producto",
                        color = Orange,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Muestra lo que ofreces para generar confianza",
                        color = MutedText
                    )

                    Text(
                        text = photoMessage,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoMessage =
                                    "La cámara estará disponible al integrar el permiso de cámara"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Cámara")
                        }

                        OutlinedButton(
                            onClick = {
                                photoMessage =
                                    "La galería estará disponible al integrar el selector de imágenes"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Galería")
                        }
                    }
                }
            }

            // Título
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Título de la oferta") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocalOffer,
                        contentDescription = null
                    )
                },
                singleLine = true
            )

            // Selección de categoría
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.labelLarge,
                color = Orange
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { option ->
                    OutlinedButton(
                        onClick = {
                            category = option
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (category == option) {
                                Orange.copy(alpha = 0.15f)
                            } else {
                                FieldBg
                            }
                        )
                    ) {
                        Text(
                            text = if (category == option) {
                                "✓ $option"
                            } else {
                                option
                            },
                            color = Orange
                        )
                    }
                }
            }

            // Precio
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    price = input.filter {
                        it.isDigit() || it == '.'
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Precio ($)") },
                singleLine = true
            )

            // Cantidad
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cantidad disponible") },
                singleLine = true
            )

            // Fecha de caducidad
            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Fecha de caducidad (DD/MM/AAAA)")
                },
                singleLine = true
            )

            // Dirección
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Zona o dirección del comercio") },
                placeholder = {
                    Text("Ej. Centro, Monterrey, Nuevo León")
                },
                supportingText = {
                    Text("Indica dónde recogerán los alimentos.")
                },
                singleLine = true
            )

            errorMessage?.let {
                Text(text = it, color = Orange)
            }

            Text(
                text = "Comparte tus excedentes y ayuda a reducir el desperdicio.",
                color = LeafGreen
            )

            // Botón para publicar
            Button(
                onClick = {
                    val cleanTitle = Validators.sanitizeText(title)
                    val cleanAddress = Validators.sanitizeText(address, maxLength = 200)

                    errorMessage = when {
                        title.isBlank() || price.isBlank() || quantity.isBlank() ||
                            expiry.isBlank() || address.isBlank() ->
                            "Completa todos los campos para publicar."
                        !Validators.isValidOfferTitle(cleanTitle) ->
                            "El título es muy largo (máximo 80 caracteres)."
                        !Validators.isValidCategory(category) ->
                            "Selecciona una categoría válida."
                        Validators.parsePrice(price) == null ->
                            "El precio debe ser un número válido mayor o igual a 0."
                        Validators.parseQuantity(quantity) == null ->
                            "La cantidad debe ser un número entero válido mayor o igual a 0."
                        !Validators.isValidAddress(cleanAddress) ->
                            "La dirección es muy larga (máximo 200 caracteres)."
                        else -> null
                    }

                    if (errorMessage == null) {
                        onPublish(
                            cleanTitle,
                            category,
                            price,
                            quantity,
                            expiry,
                            cleanAddress
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Publicar oferta")
            }
        }
    }
}