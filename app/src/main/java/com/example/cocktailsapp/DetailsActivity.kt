package com.example.cocktailsapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cocktailsapp.Cocktail
import com.example.cocktailsapp.CocktailRepository
import com.example.cocktailsapp.ui.theme.CocktailsAppTheme
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily

private const val PREFS_NAME = "CocktailNotes"
private const val NOTE_KEY_PREFIX = "note_"

fun saveNoteForCocktail(context: Context, cocktailName: String, note: String) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    sharedPreferences.edit().putString(NOTE_KEY_PREFIX + cocktailName, note).apply()
}

fun getNoteForCocktail(context: Context, cocktailName: String): String {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getString(NOTE_KEY_PREFIX + cocktailName, "") ?: ""
}

class DetailsActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cocktailName = intent.getStringExtra("cocktailName") ?: ""
        if (cocktailName.isEmpty()) {
            Toast.makeText(this, "Brak przekazanego koktajlu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val cocktails = CocktailRepository.getCocktails(this)
        val selectedCocktail: Cocktail? = cocktails.firstOrNull { it.name == cocktailName }

        if (selectedCocktail == null) {
            Toast.makeText(this, "Nie znaleziono koktajlu: $cocktailName", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        timerViewModel.initTimer(selectedCocktail.timerSeconds?: 0)

        setContent {
            CocktailsAppTheme {
                DetailsScreen(selectedCocktail, timerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    selectedCocktail: Cocktail,
    timerViewModel: TimerViewModel
) {
    val context = LocalContext.current
    var notes by rememberSaveable { mutableStateOf(getNoteForCocktail(context, selectedCocktail.name)) }
    if (selectedCocktail.timerSeconds != null) {
        TimerComposable(timerViewModel, startValue = selectedCocktail.timerSeconds ?: 0)
    }
    Surface(color = MaterialTheme.colorScheme.background) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("CocktailsApp\ud83c\udf79", style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif
                    ), color = MaterialTheme.colorScheme.onPrimary) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Nazwa: ${selectedCocktail.name}", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Składniki:", style = MaterialTheme.typography.titleMedium)
                        selectedCocktail.ingredients.forEach { ingr ->
                            Text(text = "- $ingr", style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = selectedCocktail.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { newValue ->
                                notes = newValue
                                saveNoteForCocktail(context, selectedCocktail.name, newValue)
                            },
                            label = { Text("Uwagi") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}@Composable
fun TimerComposable(
    timerViewModel: TimerViewModel,
    startValue: Int
) {
    // Po prostu odczytujemy timerViewModel.currentTime.value, który jest mutableState
    val currentTime = timerViewModel.currentTime.value

    Column {
        Text(text = "Timer: $currentTime s")
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            timerViewModel.initTimer(startValue)
            timerViewModel.startTimer()
        }) {
            Text("Start")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { timerViewModel.pauseTimer() }) {
            Text("Pauza")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { timerViewModel.resumeTimer() }) {
            Text("Wznów")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { timerViewModel.resetTimer(startValue) }) {
            Text("Zatrzymaj i resetuj")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { timerViewModel.setTo60() }) {
            Text("Ustaw 60s")
        }
    }
}