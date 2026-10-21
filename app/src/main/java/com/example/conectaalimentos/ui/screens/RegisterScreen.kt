package com.example.conectaalimentos.ui.screens
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conectaalimentos.data.Validators
import com.example.conectaalimentos.ui.components.*
import com.example.conectaalimentos.ui.theme.*

enum class AccountType { USUARIO, COMERCIO }

@Composable
fun RegisterScreen(
    onRegisterClick: (email: String, password: String, type: AccountType) -> Unit,
    onLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.USUARIO) }
    var privacyAccepted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        BrandTitle()
        Spacer(Modifier.height(20.dp))

        Text(
            "Crear cuenta",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = InkDark
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Rescata alimentos.\nConecta con tu comunidad.",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = InkDark
        )
        Spacer(Modifier.height(16.dp))

        AccountTypeSelector(selected = accountType, onSelected = { accountType = it })
        Spacer(Modifier.height(16.dp))

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

        AppTextField(
            label = "Confirmar contraseña",
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true
        )

        Spacer(Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Checkbox(
                checked = privacyAccepted,
                onCheckedChange = { privacyAccepted = it }
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "He leído y acepto el",
                    fontSize = 13.sp,
                    color = InkDark
                )


                Text(
                    text = "Aviso de privacidad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Orange,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        try {
                            val file = java.io.File(
                                context.cacheDir,
                                "aviso_privacidad_conecta_alimentos.pdf"
                            )

                            context.assets.open(
                                "aviso_privacidad_conecta_alimentos.pdf"
                            ).use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }

                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(
                                context,
                                "No se pudo abrir el aviso de privacidad",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )

            }
        }


        Spacer(Modifier.height(20.dp))






        // Validación centralizada en data/Validators.kt (Actividad 3, punto 3),
        // así esta pantalla y cualquier otra futura usan exactamente las
        // mismas reglas de email/contraseña.
        fun isValidEmail(email: String) = Validators.isValidEmail(email)
        fun isStrongPassword(password: String) = Validators.isStrongPassword(password)


        PrimaryButton("Registrarme", onClick = {
            when {
                email.isBlank() -> {
                    android.widget.Toast.makeText(
                        context,
                        "Ingresa tu correo electrónico",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }

                !isValidEmail(email) -> {
                    android.widget.Toast.makeText(
                        context,
                        "Ingresa un correo válido, por ejemplo usuario@gmail.com",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                password.isBlank() -> {
                    android.widget.Toast.makeText(
                        context,
                        "Ingresa tu contraseña",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }

                !isStrongPassword(password) -> {
                    android.widget.Toast.makeText(
                        context,
                        "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                password != confirmPassword -> {
                    android.widget.Toast.makeText(
                        context,
                        "Las contraseñas no coinciden",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                !privacyAccepted -> {
                    android.widget.Toast.makeText(
                        context,
                        "Debes aceptar el aviso de privacidad para registrarte",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    onRegisterClick(email.trim(), password, accountType)
                }
            }
        })


        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes cuenta? ", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
            Text(
                "Inicia sesión",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

/** Selector segmentado Usuario / Comercio. */
@Composable
private fun AccountTypeSelector(selected: AccountType, onSelected: (AccountType) -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .border(BorderStroke(1.dp, FieldBorder), shape)
    ) {
        SegmentItem("Usuario", Icons.Outlined.Person, selected == AccountType.USUARIO,
            Modifier.weight(1f)) { onSelected(AccountType.USUARIO) }
        SegmentItem("Comercio", Icons.Outlined.Store, selected == AccountType.COMERCIO,
            Modifier.weight(1f)) { onSelected(AccountType.COMERCIO) }
    }
}

@Composable
private fun SegmentItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) Color.White else Orange
    Row(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isSelected) Orange else Color.Transparent)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = contentColor)
        Spacer(Modifier.width(8.dp))
        Text(text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
