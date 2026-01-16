package com.example.tarota5scores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun StatistiquesJoueursScreen(
    historique: Historique,
    joueurId: String?,
    onNavigateBack: () -> Unit,
    fromPartie: Boolean = false
) {
    val analyseur = remember { AnalyseurHistorique() }
    val (statistiquesJoueurs, _, _) = remember(historique) {
        analyseur.analyser(historique)
    }

    val statistiques = statistiquesJoueurs[joueurId]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Titre centré
        Text(
            text = if (joueurId != null) "Statistiques - $joueurId" else "Joueur non trouvé",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (statistiques == null) {
            Text(
                text = "Aucune statistique trouvée pour ce joueur",
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
                                text = "Résumé général",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Parties jouées : ${statistiques.totalParties}")
                            Text("Donnes jouées : ${statistiques.totalDonnes}")
                            Text("Gain net : ${statistiques.gainNet}")
                            Text("Gain moyen par donne : ${"%.1f".format(statistiques.gainMoyenParDonne)}")
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
                                text = "Rôles",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Preneur : ${statistiques.preneur}")
                            Text("Appelé : ${statistiques.appele}")
                            Text("Défense : ${statistiques.defense}")
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
                            Text("Petite : ${statistiques.contrats[0]}")
                            Text("Garde : ${statistiques.contrats[1]}")
                            Text("Garde Sans : ${statistiques.contrats[2]}")
                            Text("Garde Contre : ${statistiques.contrats[3]}")
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

                if (statistiques.poignees.sum() > 0) {
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
                                Text("Simple : ${statistiques.poignees[0]}")
                                Text("Double : ${statistiques.poignees[1]}")
                                Text("Triple : ${statistiques.poignees[2]}")
                            }
                        }
                    }
                }

                if (statistiques.chelems.sum() > 0) {
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
                                Text("Non annoncé réussi : ${statistiques.chelems[0]}")
                                Text("Annoncé raté : ${statistiques.chelems[1]}")
                                Text("Annoncé réussi : ${statistiques.chelems[2]}")
                            }
                        }
                    }
                }

                if (statistiques.petitAuBoutGagne > 0 || statistiques.petitAuBoutPerdu > 0 || statistiques.miseres > 0) {
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
                                Text("Petit au bout gagnés : ${statistiques.petitAuBoutGagne}")
                                Text("Petit au bout perdus : ${statistiques.petitAuBoutPerdu}")
                                Text("Misères : ${statistiques.miseres}")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton Retour
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (fromPartie) "Retour à la partie" else "Retour")
        }
    }
}
