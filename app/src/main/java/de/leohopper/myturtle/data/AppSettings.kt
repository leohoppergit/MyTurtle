package de.leohopper.myturtle.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HomeCardLayout(
    val label: String,
    val description: String,
) {
    COMPACT(
        label = "Kompakt",
        description = "Nur Name und Art in sehr kompakter Form.",
    ),
    STANDARD(
        label = "Standard",
        description = "Die bisherige Kartengröße mit den wichtigsten Eckdaten.",
    ),
    LARGE(
        label = "Groß",
        description = "Mehr Platz und mehr Informationen direkt auf der Startkarte.",
    ),
}

class AppSettings(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _homeCardLayout = MutableStateFlow(loadHomeCardLayout())
    val homeCardLayout: StateFlow<HomeCardLayout> = _homeCardLayout.asStateFlow()

    fun currentHomeCardLayout(): HomeCardLayout = _homeCardLayout.value

    fun updateHomeCardLayout(layout: HomeCardLayout) {
        if (_homeCardLayout.value == layout) return

        sharedPreferences.edit()
            .putString(KEY_HOME_CARD_LAYOUT, layout.name)
            .apply()

        _homeCardLayout.value = layout
    }

    private fun loadHomeCardLayout(): HomeCardLayout {
        val rawValue = sharedPreferences.getString(KEY_HOME_CARD_LAYOUT, HomeCardLayout.STANDARD.name)
        return HomeCardLayout.entries.firstOrNull { it.name == rawValue } ?: HomeCardLayout.STANDARD
    }

    private companion object {
        const val PREFS_NAME = "myturtle_settings"
        const val KEY_HOME_CARD_LAYOUT = "home_card_layout"
    }
}
