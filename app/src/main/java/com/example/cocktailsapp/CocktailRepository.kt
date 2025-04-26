// CocktailRepository.kt
package com.example.cocktailsapp

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CocktailRepository {
    private val api = RetrofitClient.create()

    suspend fun searchCocktails(context: Context, query: String = ""): List<Cocktail> =
        withContext(Dispatchers.IO) {
            try {
                val searchQuery = if (query.isBlank()) "margarita" else query
                val response = api.searchCocktails(searchQuery)

                TranslationService.prepareTranslator()
                response.drinks?.map { dto ->
                    Cocktail(
                        name         = dto.strDrink,
                        description  = TranslationService.translateText(dto.strInstructions)
                            ?: dto.strInstructions.orEmpty(),
                        ingredients  = dto.collectIngredients().map { ing ->
                            TranslationService.translateText(ing) ?: ing
                        },
                        timerSeconds = 0,
                        imageUrl     = dto.strDrinkThumb
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                Log.e("CocktailRepository", "API error: ${e.message}")
                getCocktails(context)
            }
        }

    suspend fun getRandomCocktails(count: Int = 30): List<Cocktail> =
        withContext(Dispatchers.IO) {
            val cocktails = mutableListOf<Cocktail>()
            val seenIds = mutableSetOf<String>()

            TranslationService.prepareTranslator()
            repeat(count * 2) {
                if (cocktails.size >= count) return@repeat
                runCatching {
                    api.getRandomCocktail().drinks
                        ?.firstOrNull { it.idDrink !in seenIds }
                        ?.also { dto ->
                            seenIds += dto.idDrink
                            val desc = TranslationService.translateText(dto.strInstructions)
                                ?: dto.strInstructions.orEmpty()
                            val ings = dto.collectIngredients().map { ing ->
                                TranslationService.translateText(ing) ?: ing
                            }
                            cocktails += Cocktail(
                                name         = dto.strDrink,
                                description  = desc,
                                ingredients  = ings,
                                timerSeconds = 0,
                                imageUrl     = dto.strDrinkThumb
                            )
                        }
                }.onFailure {
                    Log.e("CocktailRepository", "fetch error: ${it.message}")
                }
            }
            cocktails.take(count)
        }

    companion object {
        suspend fun getCocktails(context: Context): List<Cocktail> =
            withContext(Dispatchers.IO) {
                val json = context.assets.open("cocktails.json")
                    .bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<Cocktail>>() {}.type
                Gson().fromJson(json, type)
            }
    }
}