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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun StatistiquesGlobalesScreen(
    historique: Historique,
    onNavigateBack: () -> Unit
) {
    val analyseur = remember { AnalyseurHistorique() }
    val (_, _, statistiquesGlobales) = remember {
        analyseur.analyser(historique)
    }

    val decimalFormat = DecimalFormat("#.##")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Header - seulement le titre
        // Header - titre centré
        Text(
            text = "Statistiques Globales",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )


        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total donnes: ${statistiquesGlobales.nbDonnes}")
                            Text("Bouts attaque: ${statistiquesGlobales.attaqueNbBouts}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Points: ${statistiquesGlobales.pointsGagnes}")
                            Text("Bouts défense: ${3 * statistiquesGlobales.nbDonnes - statistiquesGlobales.attaqueNbBouts}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Misères: ${statistiquesGlobales.miseres}")
                            Text("") // Cellule vide pour l'alignement
                        }

                        // Petit au bout sur deux lignes séparées
                        Text("Petit bout gagnés: ${statistiquesGlobales.petitAuBoutGagne}")

                        Text("Petit bout perdus: ${statistiquesGlobales.petitAuBoutPerdu}")
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Meilleur: ${statistiquesGlobales.meilleurScore}")
                            Text("Pire: ${statistiquesGlobales.pireScore}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gain min: ${statistiquesGlobales.gainMin}")
                            Text("Gain max: ${statistiquesGlobales.gainMax}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Médiane: ${statistiquesGlobales.mediane}")
                            Text("Médiane moy: ${decimalFormat.format(statistiquesGlobales.medianeMoyenne)}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gain moy min: ${decimalFormat.format(statistiquesGlobales.gainMoyenMin)}")
                            Text("Gain moy max: ${decimalFormat.format(statistiquesGlobales.gainMoyenMax)}")
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
                            text = "Contrats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Petite: ${statistiquesGlobales.contrats[0]}")
                            Text("Garde: ${statistiquesGlobales.contrats[1]}")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Garde Sans: ${statistiquesGlobales.contrats[2]}")
                            Text("Garde Contre: ${statistiquesGlobales.contrats[3]}")
                        }
                    }
                }
            }

            if (statistiquesGlobales.poignees.sum() > 0) {
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Simple: ${statistiquesGlobales.poignees[0]}")
                                Text("Double: ${statistiquesGlobales.poignees[1]}")
                                Text("Triple: ${statistiquesGlobales.poignees[2]}")
                            }
                        }
                    }
                }
            }

            if (statistiquesGlobales.chelems.sum() > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Chelems",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Non annoncé: ${statistiquesGlobales.chelems[0]}")
                                Text("Annoncé raté: ${statistiquesGlobales.chelems[1]}")
                                Text("Annoncé réussi: ${statistiquesGlobales.chelems[2]}")
                            }
                        }
                    }
                }
            }
        }

        // Bouton Retour en bas au centre
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onNavigateBack) {
                Text("Retour")
            }
        }
    }
}
