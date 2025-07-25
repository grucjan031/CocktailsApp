package com.example.cocktailsapp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon

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

// Dodaj funkcję pobierającą koktajl z API
private suspend fun getCocktailByName(context: Context, name: String): Cocktail? {
    val cocktailRepository = CocktailRepository()
    val cocktails = cocktailRepository.searchCocktails(context, name)
    return cocktails.firstOrNull { it.name == name }
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

        setContent {
            CocktailsAppTheme {
                var selectedCocktail by remember { mutableStateOf<Cocktail?>(null) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(key1 = true) {
                    isLoading = true
                    selectedCocktail = getCocktailByName(this@DetailsActivity, cocktailName)
                    // Jeśli nie znaleziono w API, szukaj lokalnie
                    if (selectedCocktail == null) {
                        val localCocktails = CocktailRepository.getCocktails(this@DetailsActivity)
                        selectedCocktail = localCocktails.firstOrNull { it.name == cocktailName }
                    }

                    if (selectedCocktail == null) {
                        Toast.makeText(this@DetailsActivity,
                            "Nie znaleziono koktajlu: $cocktailName",
                            Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        timerViewModel.initTimer(selectedCocktail?.timerSeconds ?: 0)
                    }
                    isLoading = false
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CocktailLoadingAnimation(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    selectedCocktail?.let {
                        DetailsScreen(it, timerViewModel)
                    }
                }
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
    var useTranslation by remember { mutableStateOf(false) }
    var notes by rememberSaveable { mutableStateOf(getNoteForCocktail(context, selectedCocktail.name)) }
    var isTimerVisible by rememberSaveable { mutableStateOf(false) }

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
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val ingredients = selectedCocktail.getLocalizedIngredients(useTranslation)
                        val message = buildString {
                            append("Składniki koktajlu ${selectedCocktail.name}:\n\n")
                            ingredients.forEachIndexed { index, ingredient ->
                                append("${index + 1}. $ingredient\n")
                            }
                        }

                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:")
                            putExtra("sms_body", message)
                        }

                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                "Brak aplikacji do wysyłania SMS",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Wyślij składniki",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
                        // Dodajemy wyświetlanie obrazka
                        selectedCocktail.imageUrl?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Zdjęcie drinka ${selectedCocktail.name}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text(text = "Nazwa: ${selectedCocktail.name}", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Składniki:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Używamy getLocalizedIngredients zamiast ingredients
                        selectedCocktail.getLocalizedIngredients(useTranslation).forEach { ingr ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = ingr, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Przygotowanie:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))

                        DrinkDescription(
                            cocktail = selectedCocktail,
                            onLanguageChanged = { newUseTranslation ->
                                useTranslation = newUseTranslation
                            }
                        )

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

                // Przycisk przełączający widoczność minutnika
                Button(
                    onClick = { isTimerVisible = !isTimerVisible },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isTimerVisible) "Ukryj minutnik" else "Pokaż minutnik")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wyświetlanie minutnika tylko gdy isTimerVisible jest true
                if (isTimerVisible && selectedCocktail.timerSeconds != null) {
                    TimerComposable(timerViewModel, startValue = selectedCocktail.timerSeconds)
                }
            }
        }
    }
}

@Composable
fun DrinkDescription(cocktail: Cocktail, onLanguageChanged: (Boolean) -> Unit) {
    var useTranslation by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tekst opisu z przyciskiem flagi obok
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Tekst opisu
            Text(
                text = if (useTranslation && !cocktail.translatedDescription.isNullOrBlank())
                    cocktail.translatedDescription!!
                else
                    cocktail.originalDescription,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            // Przycisk flagi tylko gdy jest dostępne tłumaczenie
            if (!cocktail.translatedDescription.isNullOrBlank()) {
                IconButton(
                    onClick = {
                        useTranslation = !useTranslation
                        onLanguageChanged(useTranslation)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = if (useTranslation)
                            ImageVector.vectorResource(R.drawable.ic_flag_pl)
                        else
                            ImageVector.vectorResource(R.drawable.ic_flag_gb),
                        contentDescription = if (useTranslation) "Zmień na angielski" else "Zmień na polski",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

@Composable
fun TimerButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun TimerComposable(
    timerViewModel: TimerViewModel,
    startValue: Int
) {
    val currentTime by timerViewModel.currentTime
    var customTime by remember { mutableStateOf(startValue.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$currentTime",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "s",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pole do wprowadzania własnego czasu
            OutlinedTextField(
                value = customTime,
                onValueChange = { input ->
                    // Akceptujemy tylko cyfry
                    if (input.isEmpty() || input.all { it.isDigit() }) {
                        customTime = input
                    }
                },
                label = { Text("Podaj czas w sekundach") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Przycisk zatwierdzający własny czas
            Button(
                onClick = {
                    customTime.toIntOrNull()?.let { time ->
                        timerViewModel.resetTimer(time)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ustaw czas")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimerButton(
                    icon = Icons.Default.PlayArrow,
                    onClick = { timerViewModel.startTimer() }
                )
                TimerButton(
                    icon = Icons.Default.Pause,
                    onClick = { timerViewModel.pauseTimer() }
                )
                TimerButton(
                    icon = Icons.Default.Refresh,
                    onClick = { timerViewModel.resetTimer(startValue) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}