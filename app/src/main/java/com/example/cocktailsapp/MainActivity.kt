package com.example.cocktailsapp

import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Search
import com.google.gson.Gson
import com.example.cocktailsapp.Cocktail
import android.util.Log
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.res.vectorResource


sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Koktajle")
    object Favorites : BottomNavItem("favorites", Icons.Default.Favorite, "Ulubione")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Ustawienia")
}
class FavoritesManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)

    fun addFavorite(cocktail: Cocktail) {
        val gson = Gson()
        val editor = sharedPreferences.edit()
        editor.putString(cocktail.name, gson.toJson(cocktail))
        editor.apply()
    }

    fun removeFavorite(cocktail: Cocktail) {
        val editor = sharedPreferences.edit()
        editor.remove(cocktail.name)
        editor.apply()
    }

    fun getFavorites(): List<Cocktail> {
        val gson = Gson()
        val favorites = mutableListOf<Cocktail>()

        sharedPreferences.all.forEach { (_, value) ->
            if (value is String) {
                try {
                    val cocktail = gson.fromJson(value, Cocktail::class.java)
                    favorites.add(cocktail)
                } catch (e: Exception) {
                    Log.e("FavoritesManager", "Error parsing favorite", e)
                }
            }
        }

        return favorites
    }

    fun isFavorite(cocktail: Cocktail): Boolean {
        return sharedPreferences.contains(cocktail.name)
    }

    fun toggleFavorite(cocktail: Cocktail): Boolean {
        val isFavorite = isFavorite(cocktail)
        if (isFavorite) {
            removeFavorite(cocktail)
        } else {
            addFavorite(cocktail)
        }
        return !isFavorite
    }
}
class MainActivity : ComponentActivity() {
    private val cocktailRepository = CocktailRepository()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CocktailsAppTheme {
                var selectedNavItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Home) }
                var cocktails by remember { mutableStateOf<List<Cocktail>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                val refreshCocktails = {
                    isLoading = true
                    CoroutineScope(Dispatchers.Main).launch {
                        cocktails = cocktailRepository.getRandomCocktails()
                        isLoading = false
                    }
                    Unit
                }
                LaunchedEffect(key1 = true) {
                    isLoading = true
                    cocktails = cocktailRepository.getRandomCocktails()
                    isLoading = false
                }

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
                    bottomBar = {
                        NavigationBar {
                            val navItems = listOf(
                                BottomNavItem.Home,
                                BottomNavItem.Favorites,
                                BottomNavItem.Settings
                            )
                            navItems.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) },
                                    selected = selectedNavItem == item,
                                    onClick = { selectedNavItem = item }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    when (selectedNavItem) {
                        BottomNavItem.Home -> {
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                                }
                            } else {
                                MainScreen(
                                    cocktailList = cocktails,
                                    paddingValues = paddingValues,
                                    onCocktailClick = { selectedCocktail ->
                                        val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                                        intent.putExtra("cocktailName", selectedCocktail.name)
                                        startActivity(intent)
                                    },
                                    onRefresh = refreshCocktails
                                )
                            }
                        }
                        BottomNavItem.Favorites -> {
                            FavouritesScreen(paddingValues)

                        }
                        BottomNavItem.Settings -> {
                            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                                Text(
                                    text = "Tutaj będą ustawienia",
                                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                                )
                            }
                        }
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
    paddingValues: PaddingValues,
    onCocktailClick: (Cocktail) -> Unit,
    onRefresh: () -> Unit
) {
    val cocktailRepository = remember { CocktailRepository() }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Cocktail>>(cocktailList) }
    var isSearching by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val favoritesManager = remember { FavoritesManager(context) }
    var favoritesList by remember { mutableStateOf<List<Cocktail>>(emptyList()) }

    LaunchedEffect(key1 = true) {
        favoritesList = favoritesManager.getFavorites()
    }

    Column(
        modifier = Modifier.padding(paddingValues)
    ) {
        // Wyszukiwarka
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                if (newQuery.isNotEmpty()) {
                    isSearching = true

                    // Uruchomienie wyszukiwania z użyciem rememberCoroutineScope
                    coroutineScope.launch(Dispatchers.IO) {
                        val results = cocktailRepository.searchCocktails(context, newQuery)
                        withContext(Dispatchers.Main) {
                            searchResults = results
                            isSearching = false
                        }
                    }
                } else {
                    searchResults = cocktailList
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Wyszukaj drinka...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Wskaźnik ładowania podczas wyszukiwania
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        LazyColumn {
            items(if (searchQuery.isEmpty()) cocktailList else searchResults) { cocktail ->
                val isFavorite = favoritesManager.isFavorite(cocktail)
                CocktailListItem(
                    cocktail = cocktail,
                    onClick = { onCocktailClick(cocktail) },
                    isFavorite = isFavorite,
                    onFavoriteToggle = { cocktail ->
                        favoritesManager.toggleFavorite(cocktail)
                        // Odświeżenie listy ulubionych
                        favoritesList = favoritesManager.getFavorites()
                    }
                )
            }

            // Przycisk odświeżania na końcu listy
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onRefresh() },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Odśwież listę",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Odśwież listę drinków",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                // Dodajemy padding na końcu listy
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CocktailListItem(
    cocktail: Cocktail,
    onClick: (Cocktail) -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: (Cocktail) -> Unit
) {
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
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f)
            )

            // Gwiazdka do oznaczania ulubionych
            var currentFavoriteState by remember(cocktail.name) { mutableStateOf(isFavorite) }

            IconButton(onClick = {
                onFavoriteToggle(cocktail)
                // Aktualizujemy stan lokalny natychmiast
                currentFavoriteState = !currentFavoriteState
            }) {
                Icon(
                    imageVector = if (currentFavoriteState) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (currentFavoriteState) "Usuń z ulubionych" else "Dodaj do ulubionych",
                    tint = if (currentFavoriteState) Color.Red else Color.Gray
                )
            }
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