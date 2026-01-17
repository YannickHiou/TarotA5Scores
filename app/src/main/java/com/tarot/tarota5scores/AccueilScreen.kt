/*
 * TarotA5Scores - Application de gestion des scores de Tarot à 5
 * Copyright (C) 2025  Yannick Hiou
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tarot.tarota5scores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccueilScreen(
    onAproposClick: () -> Unit,
    onJouerClick: () -> Unit,
    onJoueursClick: () -> Unit,
    onHistoriqueClick: () -> Unit,
    onStatistiquesClick: () -> Unit,
    onConstantesClick: () -> Unit,
    onReglesClick: () -> Unit,
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
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onConstantesClick) {
            Text("Constantes")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // NOUVEAU : bouton Règles FFT
        Button(onClick = onReglesClick) {
            Text("Règles")
        }

        // Espace flexible pour pousser la version en bas
        Spacer(modifier = Modifier.weight(1f))

        // Version en bas-centre
        Text(
            text = "v ${`Version`.VERSION}",
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
