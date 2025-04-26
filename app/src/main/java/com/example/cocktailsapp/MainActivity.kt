package com.example.cocktailsapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cocktailsapp.ui.theme.CocktailsAppTheme
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    private val cocktailRepository = CocktailRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CocktailsAppTheme {
                var cocktails by remember { mutableStateOf<List<Cocktail>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(key1 = true) {
                    isLoading = true
                    // Pobierz drinki tylko z API, fallback do lokalnych tylko przy błędzie
                    cocktails = cocktailRepository.getRandomCocktails()
                    isLoading = false
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    MainScreen(cocktails) { selectedCocktail ->
                        val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                        intent.putExtra("cocktailName", selectedCocktail.name)
                        // Możesz dodać więcej danych do przesłania do Details
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun AssetImage(imagePath: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(imagePath) {
        try {
            val inputStream = context.assets.open(imagePath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Color.LightGray))
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
                title = { Text("CocktailsApp\ud83c\udf79", style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif
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
                CocktailListItem(
                    cocktail = cocktail,
                    onClick = { onCocktailClick(cocktail) }
                )
            }
        }
    }
}

@Composable
fun CocktailListItem(cocktail: Cocktail, onClick: (Cocktail) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(cocktail) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CocktailImage(
                cocktail = cocktail,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = cocktail.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
@Composable
fun CocktailImage(cocktail: Cocktail, modifier: Modifier = Modifier) {
    if (!cocktail.imageUrl.isNullOrEmpty()) {
        // Używamy biblioteki Coil do obrazków z URL
        AsyncImage(
            model = cocktail.imageUrl,
            contentDescription = cocktail.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else if (!cocktail.imagePath.isNullOrEmpty()) {
        // Istniejący kod dla obrazków z assets
        AssetImage(
            imagePath = cocktail.imagePath!!,
            contentDescription = cocktail.name,
            modifier = modifier
        )
    } else {
        // Placeholder
        Box(modifier.background(Color.LightGray))
    }
}