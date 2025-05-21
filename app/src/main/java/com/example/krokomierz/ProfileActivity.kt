package com.example.krokomierz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.krokomierz.ui.theme.BottomNavBar
import com.example.krokomierz.ui.theme.KrokomierzTheme
import com.example.krokomierz.ui.theme.NavItem
import com.example.krokomierz.util.ThemeController

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        ThemeController.load(this)

        setContent {
            KrokomierzTheme(darkTheme = ThemeController.isDark.value) {
                ProfileScaffold { ProfileScreen() }
            }
        }
    }
}

/* ----------  UI scaffold ---------- */

@Composable
private fun ProfileScaffold(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selected = NavItem.PROFIL,
                onKrokomierzClick = {
                    ctx.startActivity(
                        Intent(ctx, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    ); (ctx as Activity).overridePendingTransition(0, 0)
                },
                onHistoriaClick = {
                    ctx.startActivity(
                        Intent(ctx, HistoryActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    ); (ctx as Activity).overridePendingTransition(0, 0)
                },
                onProfilClick = { }
            )
        }
    ) { padding -> Box(Modifier.padding(padding)) { content() } }
}

/* ----------  karta-sekcja ---------- */
@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.06f)
        )
    ) { Column(Modifier.padding(20.dp), content = content) }
}

@Composable
private fun ProfileScreen() {
    val ctx = LocalContext.current
    val isDark by ThemeController.isDark

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* -------- Sekcja motywu -------- */
        SectionCard {
            Text("Motyw", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ciemny motyw")
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = isDark,
                    onCheckedChange = { ThemeController.setDark(ctx, it) }
                )
            }
        }

        /* -------- Sekcja placeholder -------- */
        SectionCard {
            Text("Miejsce na kolejne\nustawienia profilu",
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}
