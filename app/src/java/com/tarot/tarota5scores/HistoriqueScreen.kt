package com.tarot.tarota5scores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// États de navigation
sealed class NavigationState {
    object Years : NavigationState()
    data class Months(val year: Int) : NavigationState()
    data class Days(val year: Int, val month: Int) : NavigationState()
    data class Parties(val year: Int, val month: Int, val day: Int) : NavigationState()
}

data class HistoriqueContext(
    val year: Int,
    val month: Int,
    val day: Int
)


//  Fonction utilitaire AVANT les composables
private fun getMonthName(month: Int): String {
    return when (month) {
        Calendar.JANUARY -> "Janvier"
        Calendar.FEBRUARY -> "Février"
        Calendar.MARCH -> "Mars"
        Calendar.APRIL -> "Avril"
        Calendar.MAY -> "Mai"
        Calendar.JUNE -> "Juin"
        Calendar.JULY -> "Juillet"
        Calendar.AUGUST -> "Août"
        Calendar.SEPTEMBER -> "Septembre"
        Calendar.OCTOBER -> "Octobre"
        Calendar.NOVEMBER -> "Novembre"
        Calendar.DECEMBER -> "Décembre"
        else -> "Mois $month"
    }
}

/** calcule la somme des scores par joueur pour une liste de donnes */
private fun computeTotals(joueursCount: Int, donnes: List<Donne>): List<Int> {
    if (joueursCount <= 0) return emptyList()
    val acc = MutableList(joueursCount) { 0 }
    donnes.forEach { d ->
        d.scores?.let { scores ->
            for (i in 0 until minOf(scores.size, joueursCount)) {
                acc[i] += scores[i]
            }
        }
    }
    return acc.toList()
}

//  Composables correctement structurés
@Composable
private fun YearsView(
    parties: List<Partie>,
    calendar: Calendar,
    onYearSelected: (Int) -> Unit,
    onBack: () -> Unit  //  Ajout du paramètre onBack
) {
    val years = remember(parties) {
        parties.map { partie ->
            calendar.timeInMillis = partie.createdAt
            calendar.get(Calendar.YEAR)
        }.distinct().sortedDescending()  //  Déjà correct (plus récent d'abord)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (years.isEmpty()) {
            //  Cas où il n'y a pas d'historique
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucune partie enregistrée", color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)  //  Utilise weight dans une Column
            ) {
                items(years) { year ->
                    Button(
                        onClick = { onYearSelected(year) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Année $year")
                    }
                }
            }
        }

        //  Bouton Retour ajouté
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}

@Composable
private fun MonthsView(
    parties: List<Partie>,
    year: Int,
    calendar: Calendar,
    onMonthSelected: (Int) -> Unit,
    onBack: () -> Unit  //  Ajout du paramètre onBack
) {
    val months = remember(parties, year) {
        parties.mapNotNull { partie ->
            calendar.timeInMillis = partie.createdAt
            if (calendar.get(Calendar.YEAR) == year) {
                calendar.get(Calendar.MONTH)
            } else null
        }.distinct().sortedDescending()  //  Tri décroissant (plus récent d'abord)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  //  Utilise weight dans une Column
        ) {
            items(months) { month ->
                Button(
                    onClick = { onMonthSelected(month) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(getMonthName(month))
                }
            }
        }

        //  Bouton Retour ajouté
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}

@Composable
private fun DaysView(
    parties: List<Partie>,
    year: Int,
    month: Int,
    calendar: Calendar,
    onDaySelected: (Int) -> Unit,
    onBack: () -> Unit  //  Ajout du paramètre onBack
) {
    val days = remember(parties, year, month) {
        parties.mapNotNull { partie ->
            calendar.timeInMillis = partie.createdAt
            if (calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month) {
                calendar.get(Calendar.DAY_OF_MONTH)
            } else null
        }.distinct().sortedDescending()  //  Tri décroissant (plus récent d'abord)
    }

    val monthName = getMonthName(month)

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  //  Utilise weight dans une Column
        ) {
            items(days) { day ->
                Button(
                    onClick = { onDaySelected(day) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("$day $monthName")
                }
            }
        }

        //  Bouton Retour ajouté
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}

@Composable
private fun PartiesView(
    parties: List<Partie>,
    year: Int,
    month: Int,
    day: Int,
    calendar: Calendar,
    sdf: SimpleDateFormat,
    onPartieSelected: (Partie) -> Unit,
    onBack: () -> Unit  //  Ajout du paramètre onBack
) {
    val dayParties = remember(parties, year, month, day) {
        parties.filter { partie ->
            calendar.timeInMillis = partie.createdAt
            calendar.get(Calendar.YEAR) == year &&
                    calendar.get(Calendar.MONTH) == month &&
                    calendar.get(Calendar.DAY_OF_MONTH) == day
        }.sortedByDescending { it.createdAt }  //  Déjà correct (plus récent d'abord)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)  //  Utilise weight dans une Column
        ) {
            items(dayParties) { partie ->
                Button(
                    onClick = { onPartieSelected(partie) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    val timeStr = try {
                        SimpleDateFormat(
                            "HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date(partie.createdAt))
                    } catch (e: Exception) {
                        partie.createdAt.toString()
                    }
                    Text(timeStr)
                }
            }
        }

        //  Bouton Retour ajouté
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}

@Composable
fun HistoriqueScreen(
    historique: Historique?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onReprendrePartie: (Partie) -> Unit,
    onStatistiquesPartie: (Partie, HistoriqueContext) -> Unit,
    onSupprimerPartie: (Partie) -> Unit = {},
    initialContext: HistoriqueContext? = null
)
{
    var navigationState by remember(initialContext) {
        mutableStateOf<NavigationState>(
            if (initialContext != null) {
                NavigationState.Parties(
                    year = initialContext.year,
                    month = initialContext.month,
                    day = initialContext.day
                )
            } else {
                NavigationState.Years
            }
        )
    }

    var selectedPartie by remember { mutableStateOf<Partie?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    // Filtrer les parties avec au moins une donne
    val validParties = remember(historique) {
        historique?.parties?.filter { it.donnes.isNotEmpty() } ?: emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()  //  Protection contre la barre de statut
            .padding(12.dp)
    ) {
        // Titre dynamique
        val title = when (val state = navigationState) {
            is NavigationState.Years -> "Historique des parties"
            is NavigationState.Months -> "Année ${state.year}"
            is NavigationState.Days -> {
                val monthName = getMonthName(state.month)
                "$monthName ${state.year}"
            }

            is NavigationState.Parties -> {
                val monthName = getMonthName(state.month)
                "${state.day} $monthName ${state.year}"
            }
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Contenu selon l'état de navigation
        when (val state = navigationState) {
            is NavigationState.Years -> {
                YearsView(
                    parties = validParties,
                    calendar = calendar,
                    onYearSelected = { year ->
                        navigationState = NavigationState.Months(year)
                    },
                    onBack = onBack  //  Passe la fonction onBack
                )
            }

            is NavigationState.Months -> {
                MonthsView(
                    parties = validParties,
                    year = state.year,
                    calendar = calendar,
                    onMonthSelected = { month ->
                        navigationState = NavigationState.Days(state.year, month)
                    },
                    onBack = { navigationState = NavigationState.Years }  //  Retour vers Years
                )
            }

            is NavigationState.Days -> {
                DaysView(
                    parties = validParties,
                    year = state.year,
                    month = state.month,
                    calendar = calendar,
                    onDaySelected = { day ->
                        navigationState = NavigationState.Parties(state.year, state.month, day)
                    },
                    onBack = {
                        navigationState = NavigationState.Months(state.year)
                    }  //  Retour vers Months
                )
            }

            is NavigationState.Parties -> {
                PartiesView(
                    parties = validParties,
                    year = state.year,
                    month = state.month,
                    day = state.day,
                    calendar = calendar,
                    sdf = sdf,
                    onPartieSelected = { partie ->
                        selectedPartie = partie
                        showActionDialog = true
                    },
                    onBack = {
                        navigationState = NavigationState.Days(state.year, state.month)
                    }  //  Retour vers Days
                )
            }
        }

        //  SUPPRIMÉ le bouton Retour global car maintenant chaque vue a son propre bouton
    }

    // Dialog de détail de la partie (inchangé)
    if (showActionDialog && selectedPartie != null) {
        val p = selectedPartie!!
        val totals = computeTotals(p.joueurs.size, p.donnes)

        AlertDialog(
            onDismissRequest = {
                showActionDialog = false
                selectedPartie = null
            },
            title = {
                Text(
                    text = try {
                        sdf.format(Date(p.createdAt))
                    } catch (_: Exception) {
                        p.createdAt.toString()
                    },
                    color = Color.White
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(vertical = 8.dp)
                ) {
                    p.joueurs.forEachIndexed { idx, nom ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nom,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = totals.getOrNull(idx)?.toString() ?: "0",
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        showActionDialog = false
                        val partieToResume = selectedPartie
                        selectedPartie = null
                        if (partieToResume != null) onReprendrePartie(partieToResume)
                    }) {
                        Text("Reprendre", color = Color.White)
                    }

                    TextButton(onClick = {
                        showActionDialog = false
                        val partieForStats = selectedPartie
                        // on NE remet pas forcément selectedPartie à null ici
                        val state = navigationState
                        if (partieForStats != null && state is NavigationState.Parties) {
                            val ctx = HistoriqueContext(
                                year = state.year,
                                month = state.month,
                                day = state.day
                            )
                            onStatistiquesPartie(partieForStats, ctx)
                        } else {
                            selectedPartie = null
                        }
                    }) {
                        Text("Stats", color = Color.White)
                    }


                    TextButton(onClick = {
                        showActionDialog = false
                        selectedPartie = null
                    }) {
                        Text("Retour", color = Color.White)
                    }
                }
            },

            dismissButton = {},
            containerColor = Color.Black
        )
    }
}
