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

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

@Composable
fun JoueursScreen(
    onRetour: () -> Unit,
    onStatistiquesJoueur: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // États principaux
    var joueurs by remember { mutableStateOf<List<Joueur>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var joueursAvecHistorique by remember { mutableStateOf<Set<String>>(emptySet()) }

    // États pour le tri
    var triAlphabetique by remember { mutableStateOf(false) }

    // États pour les dialogues
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedJoueurId by remember { mutableStateOf<String?>(null) }
    var editNom by remember { mutableStateOf(TextFieldValue("")) }
    var newNom by remember { mutableStateOf(TextFieldValue("")) }

    // Chargement initial
    LaunchedEffect(Unit) {
        try {
            val loaded = withContext(Dispatchers.IO) { loadOrCreateJoueurs(context) }
            joueurs = loaded

            val historique = withContext(Dispatchers.IO) { loadHistorique(context) }
            val joueursHistorique = mutableSetOf<String>()

            historique.parties.forEach { partie ->
                joueursHistorique.addAll(partie.joueurs)
            }

            joueursAvecHistorique = joueursHistorique

        } catch (e: Exception) {
            Log.e("JoueursScreen", "Erreur chargement joueurs", e)
            loadError = e.message ?: "Erreur inconnue"
        } finally {
            isLoading = false
        }
    }

    // Fonction de tri
    val joueursTries = remember(joueurs, triAlphabetique) {
        if (triAlphabetique) {
            joueurs.sortedBy { it.nom.lowercase() }
        } else {
            joueurs // Ordre chronologique = ordre d'ajout
        }
    }

    // UI principale
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(top = 24.dp)
            .padding(16.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            loadError != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Erreur: $loadError", color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        isLoading = true
                        loadError = null
                        scope.launch {
                            try {
                                val loaded =
                                    withContext(Dispatchers.IO) { loadOrCreateJoueurs(context) }
                                joueurs = loaded
                            } catch (e: Exception) {
                                loadError = e.message ?: "Erreur inconnue"
                            } finally {
                                isLoading = false
                            }
                        }
                    }) {
                        Text("Réessayer")
                    }
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Titre
                    Text(
                        text = "Gestion des joueurs",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )


                    // Liste des joueurs
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(joueursTries) { joueur ->
                            val aHistorique = joueursAvecHistorique.contains(joueur.nom)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFF333333),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            8.dp
                                        )
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Nom du joueur (cliquable ou non selon l'historique)
                                Text(
                                    text = joueur.nom,
                                    fontSize = 18.sp,
                                    color = if (aHistorique) Color.Gray else Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (!aHistorique) {
                                                Modifier.clickable {
                                                    selectedJoueurId = joueur.id
                                                    editNom = TextFieldValue(joueur.nom)
                                                    showEditDialog = true
                                                }
                                            } else Modifier
                                        )
                                )

                                // Icônes d'action
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Icône Stats
                                    IconButton(
                                        onClick = { onStatistiquesJoueur(joueur.nom) },
                                        enabled = aHistorique
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BarChart,
                                            contentDescription = "Statistiques",
                                            tint = if (aHistorique)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                Color.Gray
                                        )
                                    }

                                    // Icône Supprimer
                                    IconButton(
                                        onClick = {
                                            selectedJoueurId = joueur.id
                                            showDeleteDialog = true
                                        },
                                        enabled = !aHistorique
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            tint = if (!aHistorique)
                                                MaterialTheme.colorScheme.error
                                            else
                                                Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Boutons du bas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRetour,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Retour")
                        }

                        Button(
                            onClick = { triAlphabetique = !triAlphabetique },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF795548),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (triAlphabetique) Icons.Default.SortByAlpha else Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trier")
                        }

                        Button(
                            onClick = {
                                newNom = TextFieldValue("")
                                showAddDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Ajouter")
                        }
                    }
                }
            }
        }
    }

    // Dialog d'édition (simplifié)
    if (showEditDialog && selectedJoueurId != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                TextField(
                    value = editNom,
                    onValueChange = { editNom = it },
                    singleLine = true,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    label = { Text("Nom du joueur") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Annuler")
                    }

                    TextButton(
                        onClick = {
                            val raw = editNom.text.trim()
                            if (raw.isNotEmpty() && selectedJoueurId != null) {
                                val formatted = raw.lowercase(Locale.getDefault())
                                    .replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                        else it.toString()
                                    }
                                val updated = joueurs.map { j ->
                                    if (j.id == selectedJoueurId) j.copy(nom = formatted) else j
                                }
                                joueurs = updated

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        saveJoueurs(context, updated)
                                        Log.d(
                                            "JoueursScreen",
                                            "Joueur renommé: $selectedJoueurId -> $formatted"
                                        )
                                    } catch (e: Exception) {
                                        Log.e("JoueursScreen", "Erreur saveJoueurs", e)
                                    }
                                }
                            }
                            showEditDialog = false
                        },
                        enabled = editNom.text.trim().isNotEmpty()
                    ) {
                        Text("Valider")
                    }
                }
            },
            dismissButton = {}
        )
    }

    // Dialog de suppression
    if (showDeleteDialog && selectedJoueurId != null) {
        val joueurASupprimer = joueurs.find { it.id == selectedJoueurId }

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = {
                Text("\"${joueurASupprimer?.nom}\" sera supprimé")
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler")
                    }

                    TextButton(
                        onClick = {
                            selectedJoueurId?.let { joueurId ->
                                val updated = joueurs.filter { it.id != joueurId }
                                joueurs = updated

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        saveJoueurs(context, updated)
                                        Log.d("JoueursScreen", "Joueur supprimé: $joueurId")
                                    } catch (e: Exception) {
                                        Log.e("JoueursScreen", "Erreur saveJoueurs", e)
                                    }
                                }
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Supprimer")
                    }
                }
            },
            dismissButton = {}
        )
    }

    // Dialog d'ajout
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nouveau joueur") },
            text = {
                TextField(
                    value = newNom,
                    onValueChange = { newNom = it },
                    singleLine = true,
                    label = { Text("Nom du joueur") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Annuler")
                    }

                    TextButton(
                        onClick = {
                            val raw = newNom.text.trim()
                            if (raw.isNotEmpty()) {
                                val formatted = raw.lowercase(Locale.getDefault())
                                    .replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                        else it.toString()
                                    }
                                val nouveau =
                                    Joueur(id = UUID.randomUUID().toString(), nom = formatted)
                                val updated = joueurs + nouveau
                                joueurs = updated

                                scope.launch(Dispatchers.IO) {
                                    try {
                                        saveJoueurs(context, updated)
                                        Log.d(
                                            "JoueursScreen",
                                            "Joueur ajouté: ${nouveau.id} ${nouveau.nom}"
                                        )
                                    } catch (e: Exception) {
                                        Log.e("JoueursScreen", "Erreur saveJoueurs", e)
                                    }
                                }
                            }
                            showAddDialog = false
                        },
                        enabled = newNom.text.trim().isNotEmpty()
                    ) {
                        Text("Valider")
                    }
                }
            },
            dismissButton = {}
        )
    }
}
