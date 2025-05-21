package com.example.krokomierz.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf


object ThemeController {
    private const val PREFS    = "theme_prefs"
    private const val KEY_DARK = "pref_dark_theme"

    val isDark = mutableStateOf(false)

    fun load(ctx: Context) {
        isDark.value = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)
    }

    fun setDark(ctx: Context, dark: Boolean) {
        isDark.value = dark
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, dark).apply()
    }
}
