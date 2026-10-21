# Actividad 3, punto 6 "Protección de Código fuente".
#
# Con isMinifyEnabled = true (ver app/build.gradle.kts), R8 ofusca los
# nombres de clases/métodos/variables del código Kotlin compilado y
# elimina el código muerto antes de generar el .apk de release. Esto
# dificulta mucho hacer ingeniería inversa del APK para entender la
# lógica interna o encontrar rutas de ataque.
#
# Reglas mínimas necesarias para que la app siga funcionando ofuscada:

# org.json (usado en ApiClient/Validators) es parte del propio Android,
# no necesita regla.

# Mantiene los nombres de los modelos de datos usados por Jetpack Compose
# y la navegación (evita romper la serialización de argumentos de rutas).
-keepclassmembers class com.example.conectaalimentos.ui.screens.** {
    <fields>;
}

# EncryptedSharedPreferences / Tink (androidx.security.crypto) usa
# reflexión internamente para elegir su proveedor de cifrado.
-keep class com.google.crypto.tink.** { *; }
-keep class androidx.security.crypto.** { *; }
-dontwarn com.google.crypto.tink.**
