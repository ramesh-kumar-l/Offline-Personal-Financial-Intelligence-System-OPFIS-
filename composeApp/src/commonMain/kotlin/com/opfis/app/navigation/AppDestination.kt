package com.opfis.app.navigation

/** The app's top-level destinations, switched by the bottom navigation bar in `App.kt`. */
enum class AppDestination(
    val label: String,
    val glyph: String,
) {
    Dashboard("Home", "⌂"),
    Search("Search", "🔍"),
}
