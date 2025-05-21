package com.example.krokomierz

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.krokomierz.ui.theme.*
import com.example.krokomierz.util.PrefKeys

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContent { KrokomierzTheme { ProfileScaffold { ProfileScreen() } } }
    }
}

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
                    )
                    (ctx as Activity).overridePendingTransition(0, 0)
                },
                onHistoriaClick = {
                    ctx.startActivity(
                        Intent(ctx, HistoryActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    )
                    (ctx as Activity).overridePendingTransition(0, 0)
                },
                onProfilClick = {}
            )
        }
    ) { padding -> Box(Modifier.padding(padding)) { content() } }
}

@Composable
private fun ProfileScreen() = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) { Text("Profil", style = MaterialTheme.typography.headlineMedium) }
