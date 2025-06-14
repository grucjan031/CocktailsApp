package com.example.cocktailsapp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.cocktailsapp.ui.theme.CocktailsAppTheme
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            var cocktails by remember { mutableStateOf<List<Cocktail>>(emptyList()) }
            var isLoading by remember { mutableStateOf(true) }
            var hasError by remember { mutableStateOf(false) }
            val pagerState = rememberPagerState(pageCount = { 3 })
            val navItems = listOf(
                BottomNavItem.Home,
                BottomNavItem.Favorites,
                BottomNavItem.Settings
            )
            val favoritesManager = remember { FavoritesManager(this) }
            var searchQuery by remember { mutableStateOf("") }

            // Funkcja do ładowania danych
            fun loadData() {
                isLoading = true
                hasError = false
                lifecycleScope.launch {
                    try {
                        val results = if (searchQuery.isNotBlank()) {
                            cocktailRepository.searchCocktails(this@MainActivity, searchQuery)
                        } else {
                            cocktailRepository.getRandomCocktails(10)
                        }

                        // Sprawdzamy czy lista jest pusta
                        if (results.isEmpty()) {
                            hasError = true
                        } else {
                            cocktails = results
                        }
                        isLoading = false
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Błąd pobierania danych: ${e.message}")
                        hasError = true
                        isLoading = false
                    }
                }
            }

            // Początkowe ładowanie danych
            LaunchedEffect(key1 = true) {
                loadData()
            }

            CocktailsAppTheme {
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
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) },
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> { // Home
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                ) {
                                    // Pole wyszukiwania
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        placeholder = { Text("Szukaj koktajli...") },
                                        trailingIcon = {
                                            Row {
                                                IconButton(onClick = {
                                                    if (searchQuery.isNotBlank()) {
                                                        loadData()
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Search, contentDescription = "Szukaj")
                                                }
                                                if (searchQuery.isNotBlank()) {
                                                    IconButton(onClick = {
                                                        searchQuery = ""
                                                        loadData()
                                                    }) {
                                                        Icon(Icons.Default.Refresh, contentDescription = "Wyczyść")
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        ),
                                        singleLine = true
                                    )

                                    when {
                                        isLoading -> {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                CocktailLoadingAnimation(modifier = Modifier.align(Alignment.Center))
                                            }
                                        }
                                        hasError -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.WifiOff,
                                                        contentDescription = "Brak połączenia",
                                                        modifier = Modifier.size(100.dp),
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = "Błąd pobierania danych",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Sprawdź swoje połączenie z internetem i spróbuj ponownie",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    androidx.compose.material3.Button(
                                                        onClick = { loadData() },
                                                        modifier = Modifier.padding(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Refresh,
                                                            contentDescription = "Odśwież"
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Spróbuj ponownie")
                                                    }
                                                }
                                            }
                                        }
                                        else -> {
                                            // Zawartość koktajli
                                            LazyVerticalGrid(
                                                columns = GridCells.Fixed(2),
                                                contentPadding = PaddingValues(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (searchQuery.isNotBlank()) {
                                                    item(span = { GridItemSpan(2) }) {
                                                        Text(
                                                            text = "Wyniki dla: \"$searchQuery\"",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            modifier = Modifier.padding(vertical = 8.dp)
                                                        )
                                                    }
                                                }

                                                items(items = cocktails) { cocktail ->
                                                    CocktailGridItem(
                                                        cocktail = cocktail,
                                                        onClick = {
                                                            val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                                                            intent.putExtra("cocktailName", cocktail.name)
                                                            startActivity(intent)
                                                        },
                                                        isFavorite = favoritesManager.isFavorite(cocktail),
                                                        onFavoriteToggle = { favoritesManager.toggleFavorite(it) }
                                                    )
                                                }
                                                item(span = { GridItemSpan(2) }) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        androidx.compose.material3.Button(
                                                            onClick = {
                                                                searchQuery = ""
                                                                loadData()
                                                            },
                                                            modifier = Modifier.fillMaxWidth(0.7f)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Refresh,
                                                                contentDescription = "Odśwież"
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("Losowe koktajle")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> { // Favorites
                                FavouritesScreen(paddingValues)
                            }
                            2 -> { // Settings
                                SettingsScreen(paddingValues)
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
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CocktailLoadingAnimation(
                    modifier = Modifier.size(80.dp),
                    size = 80.dp
                )
            }
        }

        // Zmieniamy LazyColumn na LazyVerticalGrid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items = if (searchQuery.isEmpty()) cocktailList else searchResults) { cocktail ->
                val isFavorite = favoritesManager.isFavorite(cocktail)
                CocktailGridItem(
                    cocktail = cocktail,
                    onClick = { onCocktailClick(cocktail) },
                    isFavorite = isFavorite,
                    onFavoriteToggle = { cocktail ->
                        favoritesManager.toggleFavorite(cocktail)
                        favoritesList = favoritesManager.getFavorites()
                    }
                )
            }
            // Przycisk odświeżania jako ostatni element listy
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
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

@Composable
fun CocktailGridItem(
    cocktail: Cocktail,
    onClick: (Cocktail) -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: (Cocktail) -> Unit
) {
    var currentFavoriteState by remember(cocktail.name, isFavorite) { mutableStateOf(isFavorite) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f) // Proporcje karty
            .clickable { onClick(cocktail) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Obrazek na górze karty, zajmujący większość miejsca
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CocktailImage(
                    cocktail = cocktail,
                    modifier = Modifier.fillMaxSize()
                )

                // Ikona ulubionych w rogu obrazka
                IconButton(
                    onClick = {
                        onFavoriteToggle(cocktail)
                        currentFavoriteState = !currentFavoriteState  },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (currentFavoriteState) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (currentFavoriteState) "Usuń z ulubionych" else "Dodaj do ulubionych",
                        tint = if (currentFavoriteState) Color.Red else Color.Gray
                    )
                }
            }

            // Tekst na dole karty
            Text(
                text = cocktail.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}