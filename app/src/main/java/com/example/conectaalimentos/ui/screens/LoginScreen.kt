package com.example.conectaalimentos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conectaalimentos.ui.components.*
import com.example.conectaalimentos.ui.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp)
    ) {
        BrandTitle()
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Conectamos comercios con personas para reservar excedentes de comida antes de que caduquen a buen precio.",
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = InkDark
        )
        Spacer(Modifier.height(28.dp))

        AppTextField(
            label = "Correo electrónico",
            value = email,
            onValueChange = { email = it },
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(16.dp))
        AppTextField(
            label = "Contraseña",
            value = password,
            onValueChange = { password = it },
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true
        )
        Spacer(Modifier.height(20.dp))


        val context = androidx.compose.ui.platform.LocalContext.current

        PrimaryButton("Iniciar sesión", onClick = {
            when {
                email.isBlank() -> {
                    android.widget.Toast.makeText(
                        context,
                        "Ingresa tu correo electrónico",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                password.isBlank() -> {
                    android.widget.Toast.makeText(
                        context,
                        "Ingresa tu contraseña",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    onLoginClick(email.trim(), password)
                }
            }
        })

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿No tienes cuenta? ", fontSize = 14.sp, color = InkDark)
            Text(
                "Regístrate",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
    }
}
