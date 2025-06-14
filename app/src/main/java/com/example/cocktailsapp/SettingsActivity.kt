package com.example.cocktailsapp

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    // Stany ustawień
    var isDarkMode by remember { mutableStateOf(sharedPrefs.getBoolean("dark_mode", false)) }
    var showNonAlcoholicOnly by remember { mutableStateOf(sharedPrefs.getBoolean("non_alcoholic_only", false)) }
    var measurementUnit by remember { mutableStateOf(sharedPrefs.getString("measurement_unit", "ml") ?: "ml") }
    var showUnitSelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Ustawienia aplikacji",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Przełącznik motywu ciemnego
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Tryb ciemny",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Tryb ciemny",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkMode,
                onCheckedChange = { isChecked ->
                    isDarkMode = isChecked
                    // Zapisz zmianę w SharedPreferences
                    sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()

                    // Aplikacja zmiany motywu wymaga restartu aplikacji
                    // lub użycia LocalThemeAware w Theme.kt
                }
            )
        }

        Divider()

        // Filtrowanie bezalkoholowych koktajli
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalBar,
                contentDescription = "Filtr bezalkoholowy",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Tylko koktajle bezalkoholowe",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = showNonAlcoholicOnly,
                onCheckedChange = { isChecked ->
                    showNonAlcoholicOnly = isChecked
                    sharedPrefs.edit().putBoolean("non_alcoholic_only", isChecked).apply()
                }
            )
        }

        Divider()

        // Wybór jednostek miary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showUnitSelector = !showUnitSelector }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Scale,
                contentDescription = "Jednostki miary",
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Jednostki miary",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = measurementUnit,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Rozwiń"
            )

            // Menu rozwijane z jednostkami
            DropdownMenu(
                expanded = showUnitSelector,
                onDismissRequest = { showUnitSelector = false }
            ) {
                listOf("ml", "oz").forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            measurementUnit = unit
                            sharedPrefs.edit().putString("measurement_unit", unit).apply()
                            showUnitSelector = false
                        }
                    )
                }
            }
        }

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        // Wyczyść dane
        Button(
            onClick = {
                // Czyszczenie wszystkich ustawień i ulubionych
                context.getSharedPreferences("app_settings", Context.MODE_PRIVATE).edit().clear().apply()
                context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE).edit().clear().apply()

                // Resetowanie stanów lokalnych
                isDarkMode = false
                showNonAlcoholicOnly = false
                measurementUnit = "ml"
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = "Usuń dane"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Wyczyść wszystkie dane")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Informacje o aplikacji
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "O aplikacji",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "CocktailsApp v1.0 2025",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Aplikacja do przeglądania przepisów na koktajle",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}