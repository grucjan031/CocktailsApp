// W pliku `app/src/main/java/com/example/cocktailsapp/ui/theme/Theme.kt`
package com.example.cocktailsapp.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFFD81B60),
    onPrimary = Color.White,
    secondary = Color(0xFF5E35B1),
    onSecondary = Color.White,
    tertiary = Color(0xFFFFB300)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF06292),
    onPrimary = Color.Black,
    secondary = Color(0xFF9575CD),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFD54F)
)

@Composable
fun CocktailsAppTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // Odczytaj wartość z SharedPreferences bez używania remember
    // dzięki czemu każde renderowanie sprawdzi aktualny stan
    val isDarkTheme = sharedPrefs.getBoolean("dark_mode", isSystemInDarkTheme())

    val colorScheme = if (isDarkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}