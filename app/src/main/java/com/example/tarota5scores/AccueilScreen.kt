package com.example.tarota5scores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun AccueilScreen(
    onAproposClick: () -> Unit,
    onJouerClick: () -> Unit,
    onJoueursClick: () -> Unit,
    onHistoriqueClick: () -> Unit,
    onStatistiquesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Titre
        Text(
            text = "Tarot à 5",
            color = Color.White,
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Scores",
            color = Color.White,
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Boutons
        Button(onClick = onAproposClick) {
            Text("A propos")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onJouerClick) {
            Text("Nouvelle partie")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onJoueursClick) {
            Text("Gestion des joueurs")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onHistoriqueClick) {
            Text("Historique")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onStatistiquesClick) {
            Text("Statistiques")
        }

        // Espace flexible pour pousser la version en bas
        Spacer(modifier = Modifier.weight(1f))

        // Version en bas-centre
        Text(
            text = "v ${Version.VERSION}",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}
