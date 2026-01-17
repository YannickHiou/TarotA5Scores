
package com.tarot.tarota5scores

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ConstantesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val (constantes, poigneeValues) = remember { loadConstantesWithPoigneesSafe(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Titre centré en haut
        Text(
            text = "Constantes",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        )

        // Zone scrollable pour le contenu
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            InlineBox(title = "Seuils par nombre de bouts", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    constantes.seuils_bouts.forEachIndexed { index, seuil ->
                        Text(
                            text = "${index} bout(s) : $seuil points",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            InlineBox(title = "Multiplicateurs des contrats", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    constantes.multiplicateurs.forEach { (contrat, mult) ->
                        Text(
                            text = "$contrat : x$mult",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            InlineBox(title = "Valeurs de base", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Base de calcul : ${constantes.base_const}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Petit au bout : ${constantes.petit_au_bout} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Pénalité de misère : ${constantes.misere_penalite} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            InlineBox(title = "Chelem", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Chelem annoncé réussi : ${constantes.chelem.annonce_reussi} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Chelem non annoncé réussi : ${constantes.chelem.non_annonce_reussi} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Chelem annoncé raté : ${constantes.chelem.annonce_rate} points",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            InlineBox(title = "Poignées (valeur en points)", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    poigneeValues.forEach { (type, value) ->
                        Text(
                            text = "$type : $value points",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            InlineBox(title = "Poignées (nombre d'atouts)", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    constantes.poignee_atouts.forEach { (type, nbAtouts) ->
                        Text(
                            text = "$type : $nbAtouts atouts",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bouton Retour en bas, hors de la zone scrollable
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onBack) {
                Text("Retour")
            }
        }
    }
}
