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
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

@Composable
fun AproposScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Titre centré sur deux lignes, gras et grand
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tarot à 5",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Scores",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Avantages (label à gauche)
        Text(
            text = "Avantages",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Puces indentées
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp)
        ) {
            Text(text = "• Gratuité", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(vertical = 2.dp))
            Text(text = "• Pas de publicité", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(vertical = 2.dp))
            Text(text = "• Pas de traceur", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(vertical = 2.dp))

            val url = "https://github.com/YannickHiou/TarotA5Scores"

            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("• Code ouvert publié sur ")
                }
                withLink(
                    LinkAnnotation.Url(
                        url = url,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = Color.White,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("github")
                }
            }

            Text(
                text = annotatedString,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }


        Spacer(modifier = Modifier.height(18.dp))

        // Inconvénient (label à gauche)
        Text(
            text = "Inconvénients",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp)
        ) {
            Text(text = "• Pauvre en graphisme", color = Color.White, fontSize = 16.sp)
            Text(text = "• Maintenance épisodique", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bouton Retour centré en bas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
