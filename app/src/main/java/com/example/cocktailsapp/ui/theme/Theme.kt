// W pliku `app/src/main/java/com/example/cocktailsapp/ui/theme/Theme.kt`
package com.example.cocktailsapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}