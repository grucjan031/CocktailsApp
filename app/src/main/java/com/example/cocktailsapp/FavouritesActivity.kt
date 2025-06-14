package com.example.cocktailsapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cocktailsapp.ui.theme.CocktailsAppTheme

@Composable
fun FavouritesScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val favoritesManager = remember { FavoritesManager(context) }
    var favoritesList by remember { mutableStateOf<List<Cocktail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Ładowanie ulubionych
    LaunchedEffect(key1 = true) {
        favoritesList = favoritesManager.getFavorites()
        isLoading = false
    }

    Column(modifier = Modifier.padding(paddingValues = paddingValues)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else if (favoritesList.isEmpty()) {
            // Komunikat gdy brak ulubionych
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Nie masz jeszcze ulubionych drinków",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            // Zmieniamy LazyColumn na LazyVerticalGrid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = favoritesList) { cocktail ->
                    CocktailGridItem(
                        cocktail = cocktail,
                        onClick = {
                            // Przejście do szczegółów drinka
                            val intent = Intent(context, DetailsActivity::class.java)
                            intent.putExtra("cocktailName", cocktail.name)
                            context.startActivity(intent)
                        },
                        isFavorite = true,
                        onFavoriteToggle = { cocktailToToggle ->
                            favoritesManager.toggleFavorite(cocktailToToggle)
                            // Odświeżenie listy po usunięciu z ulubionych
                            favoritesList = favoritesManager.getFavorites()
                        }
                    )
                }
            }
        }
    }
}