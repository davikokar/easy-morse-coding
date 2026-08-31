package com.example.easymorsecoding

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {
    private const val PREFERENCES_NAME = "app_settings"
    private const val LANGUAGE_KEY = "app_language"

    val supportedLanguageTags = listOf(
        "en", "ar", "de", "es", "fr", "it", "ja", "ko", "pt-BR", "zh-CN"
    )

    fun getPersistedLanguageTag(context: Context): String =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, "") ?: ""

    fun persistLanguageTag(context: Context, tag: String) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, tag)
            .apply()
    }

    fun wrap(context: Context): Context {
        val tag = getPersistedLanguageTag(context)
        if (tag.isEmpty()) {
            systemLocale()?.let(Locale::setDefault)
            return context
        }

        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun systemLocale(): Locale? = try {
        Resources.getSystem().configuration.locales[0]
    } catch (_: Throwable) {
        null
    }
}
