package com.example.cocktailsapp

data class Cocktail(
    val name: String,
    val ingredients: List<String>,
    val description: String,
    val timerSeconds: Int?,
    val imagePath: String? = null,
    val imageUrl: String? = null
)

data class CocktailDto(
    val idDrink: String,
    val strDrink: String,
    val strInstructions: String?,
    val strDrinkThumb: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?
) {
    // Metoda zbierająca wszystkie składniki z miarami
    fun collectIngredients(): List<String> {
        val ingredientsList = listOfNotNull(
            strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15
        )

        val measuresList = listOfNotNull(
            strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
            strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
            strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15
        ) + List(15) { "" }

        return ingredientsList.zip(measuresList) { ingredient, measure ->
            if (measure.isNotBlank()) "$measure $ingredient" else ingredient
        }
    }

    // Zaktualizowana metoda toCocktail przyjmująca tłumaczenia
    fun toCocktail(
        translatedDescription: String? = null,
        translatedIngredients: List<String>? = null
    ): Cocktail {
        // Używamy przetłumaczonych składników jeśli są dostępne
        val finalIngredients = translatedIngredients ?: collectIngredients()

        // Używamy przetłumaczonego opisu jeśli jest dostępny
        val finalDescription = translatedDescription ?: strInstructions ?: ""

        return Cocktail(
            name = strDrink,
            ingredients = finalIngredients,
            description = finalDescription,
            timerSeconds = null,
            imageUrl = strDrinkThumb
        )
    }

    // Zachowanie oryginalnej metody dla kompatybilności wstecznej
    fun toCocktail(): Cocktail {
        return toCocktail(
            translatedDescription = null,
            translatedIngredients = null
        )
    }
}