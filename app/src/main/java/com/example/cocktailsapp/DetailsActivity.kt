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

        timerViewModel.initTimer(selectedCocktail.timerSeconds)

        setContent {
            CocktailsAppTheme {
                DetailsScreen(selectedCocktail, timerViewModel)
            }
        }
    }
}

@Composable
fun DetailsScreen(
    selectedCocktail: Cocktail,
    timerViewModel: TimerViewModel
) {
    var notes by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = { Text(text = "CocktailsApp\uD83C\uDF79") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Nazwa: ${selectedCocktail.name}")
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Składniki:")
            selectedCocktail.ingredients.forEach { ingr ->
                Text(text = "- $ingr")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = selectedCocktail.description)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { newValue -> notes = newValue },
                label = { Text("Uwagi") },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TimerComposable(timerViewModel, startValue = selectedCocktail.timerSeconds)
        }
    }
}
@Composable
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