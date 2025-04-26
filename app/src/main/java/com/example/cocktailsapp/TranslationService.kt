// TranslationService.kt
package com.example.cocktailsapp

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object TranslationService {
    private var translator: Translator? = null

    suspend fun prepareTranslator(
        sourceLang: String = TranslateLanguage.ENGLISH,
        targetLang: String = TranslateLanguage.POLISH
    ) = withContext(Dispatchers.IO) {
        translator?.close()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        translator = Translation.getClient(options)
        translator!!.downloadModelIfNeeded(
            DownloadConditions.Builder().requireWifi().build()
        ).await()
    }

    suspend fun translateText(text: String?): String? = withContext(Dispatchers.IO) {
        if (text.isNullOrBlank()) return@withContext text
        try {
            translator?.translate(text)?.await()
        } catch (_: Exception) {
            text
        }
    }
}