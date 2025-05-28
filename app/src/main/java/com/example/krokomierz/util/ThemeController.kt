package com.example.krokomierz.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb


object ThemeController {
    private const val PREFS      = "theme_prefs"
    private const val KEY_DARK   = "pref_dark_theme"
    private const val KEY_ACCENT = "pref_accent_color"

    val isDark      = mutableStateOf(false)
    val accentColor = mutableStateOf(Color(0xFF6650A4))

    fun load(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        isDark.value = prefs.getBoolean(KEY_DARK, false)
        val savedArgb = prefs.getInt(KEY_ACCENT, accentColor.value.toArgb())
        accentColor.value = Color(savedArgb)
    }

    fun setDark(ctx: Context, dark: Boolean) {
        isDark.value = dark
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, dark).apply()
    }

    fun setAccent(ctx: Context, color: Color) {
        accentColor.value = color
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_ACCENT, color.toArgb()).apply()
    }
}
