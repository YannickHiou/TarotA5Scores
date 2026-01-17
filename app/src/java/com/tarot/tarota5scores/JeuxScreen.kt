package com.tarot.tarota5scores

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.drawBehind


import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment

@Composable
fun JeuxScreen(
    joueurs: List<String>,
    donnes: List<Donne>, // liste des donnes / scores par donne
    totals: List<Int>, // totaux par joueur
    modifier: Modifier = Modifier,
    onRetour: () -> Unit = {},
    onNouvelleDonne: () -> Unit = {},
    onEditDonne: (Donne) -> Unit = {}
) {
    // Couleur / épaisseur des séparateurs (noir vif)
    val separatorColor = Color.Black
    val separatorStrokeDp = 2.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF00BCD4)) // bleu cyan
    ) {
        // Zone principale : on applique statusBarsPadding pour ne pas écrire sous la status bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Table area (header + donnes + totals)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .drawBehind {
                        // Draw vertical separators across the entire table area
                        if (joueurs.isNotEmpty()) {
                            val n = joueurs.size
                            val columnW = size.width / n
                            val stroke = separatorStrokeDp.toPx()
                            for (i in 1 until n) {
                                val x = columnW * i
                                drawLine(
                                    color = separatorColor,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = stroke
                                )
                            }
                        }
                    }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // HEADER : noms des joueurs (texte en noir)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        joueurs.forEach { nom ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(text = nom, fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }

                    // Ligne noire entre header et scores
                    HorizontalDivider(color = separatorColor, thickness = separatorStrokeDp)

                    // LISTE DES DONNES : prend l'espace restant et scroll si nécessaire
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(donnes) { _, donne ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .combinedClickable(
                                        onClick = { onEditDonne(donne) },
                                        onLongClick = { /* optionnel : autre action */ }
                                    )
                            ) {
                                val scores = donne.scores ?: List(joueurs.size) { 0 }
                                scores.forEach { s ->
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        Text(text = s.toString(), color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Divider bien visible (noir) au-dessus des totaux
                    HorizontalDivider(color = separatorColor, thickness = separatorStrokeDp)

                    // TOTAUX
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        totals.forEach { total ->
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(text = total.toString(), fontSize = 14.sp, color = Color.Yellow)
                            }
                        }
                    }
                }
            }

            // BOUTONS en bas : Retour et Nouvelle Donne (fonctionnels via callbacks)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onRetour, modifier = Modifier.weight(1f)) {
                    Text(text = "Retour")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(onClick = onNouvelleDonne, modifier = Modifier.weight(1f)) {
                    Text(text = "Nouvelle Donne")
                }
            }
        }
    }
}

@Composable
fun HeaderRow(headers: List<String>) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(30.dp)) {
        val columnWidth = size.width / headers.size

        headers.forEachIndexed { index, header ->
            // lignes verticales
            drawLine(
                color = Color.Black,
                start = Offset(x = index * columnWidth, y = 0f),
                end = Offset(x = index * columnWidth, y = size.height)
            )
            // texte
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                canvas.nativeCanvas.drawText(
                    header,
                    (index + 1) * columnWidth - 10.dp.toPx(),
                    20.dp.toPx(),
                    paint
                )
            }
        }

        // ligne horizontale sous headers
        drawLine(
            color = Color.Black,
            start = Offset(0f, 30.dp.toPx()),
            end = Offset(size.width, 30.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun ScoreRow(
    headers: List<String>,
    scores: List<Int>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        val columnWidth = size.width / headers.size

        headers.forEachIndexed { index, _ ->
            drawLine(
                color = Color.Black,
                start = Offset(x = index * columnWidth, y = 0f),
                end = Offset(x = index * columnWidth, y = size.height)
            )
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                canvas.nativeCanvas.drawText(
                    scores.getOrNull(index)?.toString() ?: "0",
                    (index + 1) * columnWidth - 10.dp.toPx(),
                    20.dp.toPx(),
                    paint
                )
            }
        }
    }
}

@Composable
fun TotalRow(headers: List<String>, totalScores: List<Int>) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(30.dp)) {
        val columnWidth = size.width / headers.size

        headers.forEachIndexed { index, _ ->
            drawLine(
                color = Color.Black,
                start = Offset(x = index * columnWidth, y = 0f),
                end = Offset(x = index * columnWidth, y = size.height)
            )
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    totalScores.getOrNull(index)?.toString() ?: "0",
                    (index + 1) * columnWidth - 10.dp.toPx(),
                    20.dp.toPx(),
                    paint
                )
            }
        }
    }
}

fun generateHeaders(joueurs: Set<String>, nbChars: Int): List<String> {
    // conserve l'ordre stable : transforme en liste
    return joueurs.toList().map { joueur ->
        joueur.take(nbChars).padEnd(nbChars, ' ')
    }
}

/**
 * allScores : liste de lignes (List<Int>) ; size : nombre de colonnes attendues
 */
fun calculateTotalScores(allScores: List<List<Int>>, size: Int): List<Int> {
    val totals = MutableList(size) { 0 }
    allScores.forEach { scoreRow ->
        scoreRow.forEachIndexed { index, score ->
            if (index in 0 until size) totals[index] += score
        }
    }
    return totals
}
