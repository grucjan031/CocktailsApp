package com.example.cocktailsapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cocktailsapp.ui.theme.CocktailsAppTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontFamily

class MainActivity : ComponentActivity() {

    private lateinit var cocktails: List<Cocktail>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cocktails = CocktailRepository.getCocktails(this)

        setContent {
            CocktailsAppTheme {
                MainScreen(cocktails) { selectedCocktail ->
                    val intent = Intent(this, DetailsActivity::class.java)
                    intent.putExtra("cocktailName", selectedCocktail.name)
                    startActivity(intent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    cocktailList: List<Cocktail>,
    onCocktailClick: (Cocktail) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CocktailsApp\ud83c\udf79",style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif
                    // Można użyć innego fontu, np. FontFamily.Monospace
                ), color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            items(cocktailList) { cocktail ->
                CocktailItem(
                    cocktail = cocktail,
                    onClick = { onCocktailClick(cocktail) }
                )
            }
        }
    }
}
@Composable
fun CocktailItem(
    cocktail: Cocktail,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()              // Szerokość na pełną dostępność
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = cocktail.name)
        }
    }
}