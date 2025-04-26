package com.example.cocktailsapp

data class Cocktail(
    val name: String,
    val ingredients: List<String>,
    val description: String,

    val timerSeconds: Int?
)
