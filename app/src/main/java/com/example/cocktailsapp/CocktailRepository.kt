package com.example.cocktailsapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CocktailRepository {

    fun getCocktails(context: Context): List<Cocktail> {
        val jsonString = context.assets.open("cocktails.json")
            .bufferedReader()
            .use { it.readText() }

        val listType = object : TypeToken<List<Cocktail>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
}