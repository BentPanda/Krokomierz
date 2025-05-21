package com.example.krokomierz.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

enum class NavItem { KROKI, HISTORIA, PROFIL }

@Composable
fun BottomNavBar(
    selected: NavItem,
    onKrokomierzClick: () -> Unit,
    onHistoriaClick: () -> Unit,
    onProfilClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == NavItem.KROKI,
            onClick   = onKrokomierzClick,
            icon      = { Icon(Icons.Filled.DirectionsWalk, null) },
            label     = { Text("Krokomierz") }
        )

        NavigationBarItem(
            selected = selected == NavItem.HISTORIA,
            onClick   = onHistoriaClick,
            icon      = { Icon(Icons.Filled.History, null) },
            label     = { Text("Historia") }
        )

        NavigationBarItem(
            selected = selected == NavItem.PROFIL,
            onClick   = onProfilClick,
            icon      = { Icon(Icons.Filled.Person, null) },
            label     = { Text("Profil") }
        )
    }
}
