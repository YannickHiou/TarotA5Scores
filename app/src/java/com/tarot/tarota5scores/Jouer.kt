package com.tarot.tarota5scores

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun JouerScreen(onNavigateToGame: (Set<String>) -> Unit, onRetour: () -> Unit) {
    var selected by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Sélectionne 5 joueurs",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier.weight(1f)
        ) {
            choixJoueurs { newSelection ->
                selected = newSelection
                println("DEBUG: Joueurs sélectionnés - $selected")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onRetour,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red, contentColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Retour")
            }

            Button(
                onClick = {
                    if (selected.size == 5) {
                        onNavigateToGame(selected)
                    } else {
                        println("DEBUG: Sélectionnez 5 joueurs.")
                    }
                },
                enabled = selected.size == 5,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF424242), // Gris foncé
                    disabledContentColor = Color(0xFF9E9E9E) // Gris clair
                ),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text(text = "Valide")
            }

        }
    }
}

@Composable
fun choixJoueurs(onSelectedChanged: (Set<String>) -> Unit) {
    val context = LocalContext.current
    val joueurs = loadOrCreateJoueurs(context)
    var selectedJoueurs by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(joueurs) { joueur ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedJoueurs = if (selectedJoueurs.contains(joueur.nom)) {
                            selectedJoueurs - joueur.nom
                        } else {
                            if (selectedJoueurs.size < 5) selectedJoueurs + joueur.nom else selectedJoueurs
                        }
                        println("DEBUG: Joueurs sélectionnés après clic - $selectedJoueurs")
                        onSelectedChanged(selectedJoueurs)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = joueur.nom,
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (selectedJoueurs.contains(joueur.nom)) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = if (selectedJoueurs.contains(joueur.nom)) Color.Green else Color.Gray
                )
            }
        }
    }
}
