package com.example.conectaalimentos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conectaalimentos.ui.theme.Cream
import com.example.conectaalimentos.ui.theme.InkDark
import com.example.conectaalimentos.ui.theme.Orange

@Composable
fun ProfileScreen(
    accountType: AccountType,
    onBack: () -> Unit,
    onMyOffers: () -> Unit,
    onReservations: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Mi perfil",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = if (accountType == AccountType.COMERCIO) {
                "Tipo de cuenta: Comercio"
            } else {
                "Tipo de cuenta: Usuario"
            },
            fontSize = 16.sp,
            color = InkDark
        )

        Spacer(Modifier.height(28.dp))

        if (accountType == AccountType.COMERCIO) {
            Button(
                onClick = onMyOffers,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange
                )
            ) {
                Text("Mis ofertas")
            }
        }

        if (accountType == AccountType.USUARIO) {
            Button(
                onClick = onReservations,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange
                )
            ) {
                Text("Mis reservas")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange
            )
        ) {
            Text("Volver")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange
            )
        ) {
            Text("Cerrar sesión")
        }
    }
}