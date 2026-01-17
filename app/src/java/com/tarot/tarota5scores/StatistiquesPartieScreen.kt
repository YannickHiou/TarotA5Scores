package com.tarot.tarota5scores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatistiquesPartieScreen(
    historique: Historique,
    partieId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToJoueur: (String) -> Unit,
) {
    val partie = remember(historique, partieId) {
        historique.parties.find { it.id == partieId }
    }

    val analyseur = remember { AnalyseurHistorique() }
    val statistiques = remember(partie, historique) {
        if (partie != null) {
            // Analyser tout l'historique pour obtenir les statistiques
            val (_, partiesStats, _) = analyseur.analyser(historique)
            // Trouver les statistiques de la partie spécifique
            val indexPartie = historique.parties.indexOf(partie)
            if (indexPartie >= 0 && indexPartie < partiesStats.size) {
                partiesStats[indexPartie]
            } else null
        } else null
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Titre centré
        Text(
            text = if (partie != null) {
                "Statistiques - ${dateFormat.format(Date(partie.createdAt))}"
            } else {
                "Partie non trouvée"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (partie == null || statistiques == null) {
            Text(
                text = "Aucune partie trouvée",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Informations générales",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nombre de donnes : ${statistiques.nbDonnes}")

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Classement :", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))

                            // Calculer les gains par joueur dans cette partie
                            val gainsParJoueur = remember(partie) {
                                val gains = mutableMapOf<String, Int>()

                                // Initialiser tous les joueurs à 0
                                partie.joueurs.forEach { joueur ->
                                    gains[joueur] = 0
                                }

                                // Additionner les scores de toutes les donnes
                                partie.donnes.forEach { donne ->
                                    donne.scores.forEachIndexed { index, score ->
                                        val joueur = partie.joueurs[index]
                                        gains[joueur] = gains[joueur]!! + score
                                    }
                                }

                                // Trier par gain décroissant
                                gains.toList().sortedByDescending { it.second }
                            }

                            // Afficher chaque joueur avec son gain
                            gainsParJoueur.forEach { (joueur, gain) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Bouton avec le nom du joueur (largeur fixe)
                                    Button(
                                        onClick = { onNavigateToJoueur(joueur) },
                                        modifier = Modifier
                                            .height(36.dp)
                                            .width(140.dp), // Largeur fixe au lieu de weight(1f)
                                        contentPadding = PaddingValues(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Text(
                                            text = joueur,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f)) // Pousse le score vers la droite

                                    // Score à droite
                                    Text(
                                        text = if (gain >= 0) "+$gain" else "$gain",
                                        color = if (gain >= 0)
                                            androidx.compose.material3.MaterialTheme.colorScheme.primary
                                        else
                                            androidx.compose.material3.MaterialTheme.colorScheme.error,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }


                        }
                    }
                }


                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Scores",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Meilleur score : ${statistiques.meilleurScore}")
                            Text("Pire score : ${statistiques.pireScore}")
                            Text("Points : ${statistiques.pointsGagnes}")
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Contrats",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Petite : ${statistiques.partieContrats[0]}")
                            Text("Garde : ${statistiques.partieContrats[1]}")
                            Text("Garde Sans : ${statistiques.partieContrats[2]}")
                            Text("Garde Contre : ${statistiques.partieContrats[3]}")
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Détails",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bouts de l'attaque : ${statistiques.attaqueNbBouts}")
                            Text("Bouts de la défense : ${statistiques.nbDonnes * 3 - statistiques.attaqueNbBouts}")
                            Text("Petit au bout gagnés : ${statistiques.petitAuBoutGagne}")
                            Text("Petit au bout perdus : ${statistiques.petitAuBoutPerdus}")
                            Text("Misères : ${statistiques.miseres}")
                        }
                    }
                }

                if (statistiques.partiePoignees.sum() > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Poignées",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Simple : ${statistiques.partiePoignees[0]}")
                                Text("Double : ${statistiques.partiePoignees[1]}")
                                Text("Triple : ${statistiques.partiePoignees[2]}")
                            }
                        }
                    }
                }

                if (statistiques.partieChelems.sum() > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Chélems",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Non annoncé : ${statistiques.partieChelems[0]}")
                                Text("Annoncé raté : ${statistiques.partieChelems[1]}")
                                Text("Annoncé réussi : ${statistiques.partieChelems[2]}")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton Retour en bas au centre
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Retour")
        }
    }
}
