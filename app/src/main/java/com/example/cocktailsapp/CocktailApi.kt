package com.example.cocktailsapp
import com.example.cocktailsapp.CocktailDto
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.reflect.Type

interface CocktailApi {
    @GET("search.php")
    suspend fun searchCocktails(@Query("s") searchQuery: String): CocktailResponse

    @GET("random.php")
    suspend fun getRandomCocktail(): CocktailResponse

    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): CocktailResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

    fun create(): CocktailApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val gson = GsonBuilder()
            .registerTypeAdapter(CocktailResponse::class.java, CocktailResponseDeserializer())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.thecocktaildb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(CocktailApi::class.java)
    }
}

data class CocktailResponse(
    val drinks: List<CocktailDto>?
)

class CocktailResponseDeserializer : JsonDeserializer<CocktailResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CocktailResponse {
        val obj = json.asJsonObject
        val drinksElement = obj.get("drinks")
        return if (drinksElement == null || drinksElement.isJsonNull || drinksElement.isJsonPrimitive) {
            CocktailResponse(null)
        } else {
            val listType = object : TypeToken<List<CocktailDto>>() {}.type
            val drinks = context.deserialize<List<CocktailDto>>(drinksElement, listType)
            CocktailResponse(drinks)
        }
    }
}