package com.example.tarota5scores

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.UUID
import kotlin.math.roundToInt

// Enum pour les types de chelem
enum class ChelemType {
    NONE,
    ANNONCE_REUSSI,
    ANNONCE_RATE,
    NON_ANNONCE_REUSSI
}

// Data class pour la sélection de chelem
data class ChelemSelection(
    val type: ChelemType
) {
    val annonce: Boolean get() = type == ChelemType.ANNONCE_REUSSI || type == ChelemType.ANNONCE_RATE
    val reussi: Boolean get() = type == ChelemType.ANNONCE_REUSSI || type == ChelemType.NON_ANNONCE_REUSSI
}

/**
 * Charge les constantes depuis assets via loadConstantesFromAssets (Utils.kt),
 * puis lit explicitement la clé "poignee_values" dans constantes.json si présente.
 * Retourne paire (ConstantesConfig, poigneeValuesMap).
 */
private fun loadConstantesWithPoigneesSafe(
    context: Context,
    filename: String = "constantes.json"
): Pair<ConstantesConfig, Map<String, Int>> {
    val constantes = try {
        loadConstantesFromAssets(context, filename)
    } catch (e: Exception) {
        e.printStackTrace()
        // fallback minimal — ces valeurs devraient idéalement provenir du fichier JSON
        ConstantesConfig(
            seuils_bouts = listOf(56, 51, 41, 36),
            multiplicateurs = mapOf(
                "Petite" to 1,
                "Garde" to 2,
                "GardeSans" to 4,
                "GardeContre" to 6,
                "Chelem" to 0
            ),
            petit_au_bout = 10,
            chelem = ChelemConfig(
                annonce_reussi = 400,
                non_annonce_reussi = 200,
                annonce_rate = -200
            ),
            misere_penalite = 10,
            base_const = 25,
            poignee_values = mapOf(
                "NONE" to 0,
                "SIMPLE" to 20,
                "DOUBLE" to 30,
                "TRIPLE" to 40
            )
        )
    }

    // Récupère la map des poignees fournie par le JSON ou par le fallback
    val poigneeValues: Map<String, Int> = constantes.poignee_values

    return constantes to poigneeValues
}

// Fonctions de conversion
fun convertContratToSaveFormat(contratUI: String): String {
    return when {
        contratUI == "Petite" -> "PETITE"
        contratUI == "Garde" -> "GARDE"
        contratUI == "Garde Sans" -> "GARDE_SANS"
        contratUI == "Garde Contre" -> "GARDE_CONTRE"

        //  NOUVEAU : Gérer tous les formats de chelem
        contratUI.startsWith("Chelem") -> "CHELEM"

        // Fallback pour les anciens formats
        contratUI == "Chelem" -> "CHELEM"

        else -> {
            Log.w("CONTRAT_CONVERSION", "Format de contrat UI non reconnu: '$contratUI'")
            contratUI // Fallback
        }
    }
}


private fun convertContratFromSaveFormat(saveContrat: String): String {
    return when (saveContrat) {
        "PETITE" -> "Petite"
        "GARDE" -> "Garde"
        "GARDE_SANS" -> "Garde Sans"
        "GARDE_CONTRE" -> "Garde Contre"
        else -> saveContrat
    }
}

// Fonction pour construire l'objet Chelem à partir de la sélection UI
private fun buildChelemFromSelection(chelemSelection: ChelemSelection?): Chelem? {
    return when (chelemSelection?.type) {
        ChelemType.ANNONCE_REUSSI -> Chelem(annonce = true, succes = true)
        ChelemType.ANNONCE_RATE -> Chelem(annonce = true, succes = false)
        ChelemType.NON_ANNONCE_REUSSI -> Chelem(annonce = false, succes = true)
        ChelemType.NONE, null -> null
    }
}

// Fonction pour convertir depuis l'objet Chelem vers ChelemSelection
private fun chelemToChelemSelection(chelem: Chelem?): ChelemSelection? {
    return when {
        chelem == null -> ChelemSelection(ChelemType.NONE)
        chelem.annonce && chelem.succes -> ChelemSelection(ChelemType.ANNONCE_REUSSI)
        chelem.annonce && !chelem.succes -> ChelemSelection(ChelemType.ANNONCE_RATE)
        !chelem.annonce && chelem.succes -> ChelemSelection(ChelemType.NON_ANNONCE_REUSSI)
        else -> ChelemSelection(ChelemType.NONE)
    }
}


@Composable
fun DonneScreen(
    joueurs: List<String>,
    onScoresSubmit: (List<Int>) -> Unit,
    onCancel: () -> Unit,
    donneToEdit: Donne? = null,
    onDonneSubmit: ((Donne) -> Unit)? = null
) {
    require(joueurs.size >= 5) { "Il faut au moins 5 joueurs" }

    val context = LocalContext.current
    val (constantes, poigneeValues) = remember { loadConstantesWithPoigneesSafe(context) }

    // Initialiser les états à partir de donneToEdit si fourni
    var preneur by remember {
        mutableStateOf(
            donneToEdit?.preneur?.let { index ->
                if (index < joueurs.size) joueurs[index] else null
            }
        )
    }

    var appelle by remember {
        mutableStateOf(
            donneToEdit?.appele?.let { index ->
                if (index < joueurs.size) joueurs[index] else null
            }
        )
    }

    // Conversion du contrat depuis le format sauvegarde vers le format UI
    var contratUI by remember {
        mutableStateOf(
            donneToEdit?.contrat?.let { convertContratFromSaveFormat(it) }
        )
    }

    // NOUVEAU : Gestion du chelem séparé
    var chelemSelection by remember {
        mutableStateOf(
            donneToEdit?.chelem?.let { chelemToChelemSelection(it) } ?: ChelemSelection(ChelemType.NONE)
        )
    }
    var showChelemDialog by remember { mutableStateOf(false) }

    var petitAuBoutSelection by remember { mutableStateOf(donneToEdit?.petitAuBout) }
    var showPetitAuBoutDialog by remember { mutableStateOf(false) }

    var showPreneurDialog by remember { mutableStateOf(false) }
    var showAppelleDialog by remember { mutableStateOf(false) }
    var showContratDialog by remember { mutableStateOf(false) }
    var showMisereDialog by remember { mutableStateOf(false) }

    var showPoigneesDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Points
    var pointsAtq by remember { mutableStateOf(donneToEdit?.pointsAtq ?: 45) }
    var pointsDef by remember { mutableStateOf(91 - pointsAtq) }

    // Bouts (2 sliders liés)
    var attackBoutsCount by remember { mutableStateOf(donneToEdit?.attaqueNbBouts ?: 0) }
    var defenceBoutsCount by remember { mutableStateOf(3 - attackBoutsCount) }

    // Misères : map player -> bool, préremplie si edition
    val misereSelections = remember {
        mutableStateMapOf<String, Boolean>().apply {
            joueurs.forEachIndexed { index, nom ->
                put(nom, donneToEdit?.miseres?.contains(index) == true)
            }
        }
    }

    // Pour l'affichage (noms)
    var misereSelectedNames by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(donneToEdit) {
        if (donneToEdit != null) {
            misereSelectedNames = donneToEdit.miseres.map { index ->
                joueurs.getOrNull(index) ?: "Joueur $index"
            }
        }
    }

    val poignees = remember {
        mutableStateMapOf<String, PoigneeType>().apply {
            joueurs.forEachIndexed { index, name ->
                val poigneeForThisPlayer = donneToEdit?.poignees?.find { it.index == index }
                val value = if (poigneeForThisPlayer != null) {
                    try {
                        PoigneeType.valueOf(poigneeForThisPlayer.type)
                    } catch (e: Exception) {
                        PoigneeType.NONE
                    }
                } else PoigneeType.NONE
                put(name, value)
            }
        }
    }

    // MODIFIER ButtonWithResponse pour afficher le détail du chelem
    val contratDisplayText = if (contratUI?.equals("Chelem", ignoreCase = true) == true) {
        when (chelemSelection.type) {
            ChelemType.NONE -> "Chelem (Non sélectionné)"
            ChelemType.ANNONCE_REUSSI -> "Chelem (Annoncé réussi)"
            ChelemType.ANNONCE_RATE -> "Chelem (Annoncé raté)"
            ChelemType.NON_ANNONCE_REUSSI -> "Chelem (Non annoncé réussi)"
        }
    } else {
        contratUI
    }

    //ButtonWithResponse("Choisir un Contrat", contratDisplayText) { showContratDialog = true }


    // Liste des contrats pour l'UI (AVEC Chelem)
    val contrats = listOf("Petite", "Garde", "Garde Sans", "Garde Contre", "Chelem")

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.Black)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ButtonWithResponse("Choisir un Preneur", preneur) { showPreneurDialog = true }
            ButtonWithResponse("Choisir un Appelé", appelle) { showAppelleDialog = true }
            ButtonWithResponse("Choisir un Contrat", contratDisplayText) { showContratDialog = true }

            ButtonWithResponse(
                "Misère",
                if (misereSelectedNames.isNotEmpty()) misereSelectedNames.joinToString(", ") else "Non sélectionné"
            ) { showMisereDialog = true }

            val petitAuBoutText = petitAuBoutSelection?.let { result ->
                val nom =
                    if (result.index < joueurs.size) joueurs[result.index] else "Index invalide"
                "$nom (${if (result.gagne) "Gagné" else "Perdu"})"
            } ?: "Non sélectionné"

            ButtonWithResponse("Petit au bout", petitAuBoutText) {
                showPetitAuBoutDialog = true
            }

            ButtonWithResponse(
                "Poignées",
                poignees.entries.filter { it.value != PoigneeType.NONE }
                    .joinToString { "${it.key}:${it.value.name}" }
                    .ifBlank { "Non déclarées" }
            ) { showPoigneesDialog = true }

            InlineBox(title = "Points", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Point à l'attaque",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = pointsAtq.toFloat(),
                            onValueChange = { newValue ->
                                pointsAtq = newValue.toInt()
                                pointsDef = 91 - pointsAtq
                            },
                            valueRange = 0f..91f,
                            steps = 90,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = pointsAtq.toString(),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Point à la défense",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = pointsDef.toFloat(),
                            onValueChange = { newValue ->
                                pointsDef = newValue.toInt()
                                pointsAtq = 91 - pointsDef
                            },
                            valueRange = 0f..91f,
                            steps = 90,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = pointsDef.toString(),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }

            InlineBox(title = "Nombre de bouts à l'attaque", modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Bouts en attaque",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = attackBoutsCount.toFloat(),
                            onValueChange = { newValue ->
                                val v = newValue.roundToInt().coerceIn(0, 3)
                                attackBoutsCount = v
                                defenceBoutsCount = 3 - v
                            },
                            valueRange = 0f..3f,
                            steps = 2,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = attackBoutsCount.toString(),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bouts en défense",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = defenceBoutsCount.toFloat(),
                            onValueChange = { newValue ->
                                val v = newValue.roundToInt().coerceIn(0, 3)
                                defenceBoutsCount = v
                                attackBoutsCount = 3 - v
                            },
                            valueRange = 0f..3f,
                            steps = 2,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = defenceBoutsCount.toString(),
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) { Text("Annuler") }
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) { Text("Soumettre") }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogues existants
    if (showPreneurDialog) {
        PlayerSelectionDialogDialog(
            title = "Choisir un joueur (Preneur)",
            players = joueurs,
            onPlayerSelected = { selected ->
                preneur = selected
                showPreneurDialog = false
            },
            onDismiss = { showPreneurDialog = false }
        )
    }

    if (showAppelleDialog) {
        PlayerSelectionDialogDialog(
            title = "Choisir un joueur (Appelé)",
            players = joueurs,
            onPlayerSelected = { selected ->
                appelle = selected
                showAppelleDialog = false
            },
            onDismiss = { showAppelleDialog = false }
        )
    }

    if (showContratDialog) {
        ContractSelectionDialog(
            contracts = contrats,
            onContractSelected = { selectedContract ->
                if (selectedContract == "Chelem") {
                    // Cas spécial : ouvrir le dialogue chelem
                    showChelemDialog = true
                    // Ne pas fermer showContratDialog ici
                } else {
                    // Cas normal
                    contratUI = selectedContract
                    showContratDialog = false
                }
            },
            onDismiss = { showContratDialog = false }
        )
    }


// Dans DonneScreen, le dialogue chelem :
    if (showChelemDialog) {
        ChelemSelectionDialog(
            currentSelection = chelemSelection,
            onSelectionChanged = { newSelection ->
                chelemSelection = newSelection

                // Mise à jour de l'affichage du contrat
                contratUI = when (newSelection.type) {
                    ChelemType.ANNONCE_REUSSI -> "Chelem (Annoncé réussi)"
                    ChelemType.ANNONCE_RATE -> "Chelem (Annoncé raté)"
                    ChelemType.NON_ANNONCE_REUSSI -> "Chelem (Non annoncé réussi)"
                    ChelemType.NONE -> "Chelem" // Ne devrait pas arriver
                }

                // Fermer les deux dialogues
                showChelemDialog = false
                showContratDialog = false
            },
            onDismiss = {
                // Annulation : fermer seulement le dialogue chelem
                showChelemDialog = false
                // Le dialogue contrat reste ouvert
            }
        )
    }

    if (showMisereDialog) {
        MisereSelectionDialog(
            players = joueurs,
            selectedPlayers = misereSelections,
            onFinish = { selectedNames ->
                misereSelectedNames = selectedNames
                showMisereDialog = false
            },
            onDismiss = { showMisereDialog = false }
        )
    }

    if (showPetitAuBoutDialog) {
        PetitAuBoutDialog(
            players = joueurs,
            currentSelection = petitAuBoutSelection,
            onSelectionChanged = { selected -> petitAuBoutSelection = selected },
            onDismiss = { showPetitAuBoutDialog = false }
        )
    }

    if (showPoigneesDialog) {
        PoigneesDialog(
            players = joueurs,
            initial = poignees.toMap(),
            onFinish = { map ->
                poignees.clear()
                poignees.putAll(map)
                showPoigneesDialog = false
            },
            onDismiss = { showPoigneesDialog = false }
        )
    }

    // Confirmation dialog : calcule et renvoie les scores + Donne si onDonneSubmit présent
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmer la donne") },
            text = {
                val pr = preneur ?: "—"
                val ap = appelle ?: "—"
                val ct = contratUI ?: "—"
                val pts = pointsAtq
                val nbBouts = attackBoutsCount

                // Gestion spéciale pour chelem
                val resultatTexte = if (contratUI?.startsWith("Chelem") == true) {
                    when (chelemSelection.type) {
                        ChelemType.ANNONCE_REUSSI -> "Le chelem annoncé est réussi !"
                        ChelemType.ANNONCE_RATE -> "Le chelem annoncé a échoué."
                        ChelemType.NON_ANNONCE_REUSSI -> "Le chelem non annoncé est réussi !"
                        ChelemType.NONE -> "Erreur: type de chelem non défini"
                    }
                } else {
                    val seuil = constantes.seuils_bouts.getOrElse(nbBouts) { constantes.seuils_bouts.first() }
                    val diff = pointsAtq - seuil
                    if (diff >= 0) "La partie est faite de ${diff} point(s)."
                    else "La partie est chutée de ${-diff} point(s)."
                }

                val miseresText =
                    if (misereSelectedNames.isEmpty()) "Personne" else misereSelectedNames.joinToString(", ")
                val poigneesText = poignees.entries.filter { it.value != PoigneeType.NONE }
                    .joinToString { "${it.key}:${it.value.name}" }.ifBlank { "Aucune" }

                val petitAuBoutText = petitAuBoutSelection?.let { result ->
                    val nom = if (result.index < joueurs.size) joueurs[result.index] else "Index invalide"
                    val resultat = if (result.gagne) "gagné" else "perdu"
                    "$nom a mené le petit au bout et a $resultat"
                } ?: "Personne n'a mené le petit au bout"

                Column {
                    Text("$pr a appelé $ap pour réaliser une $ct. Ils font $pts avec $nbBouts bout(s).")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(resultatTexte)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(petitAuBoutText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$miseresText avait une misère")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Poignées: $poigneesText")
                }
            },
            confirmButton = {
                val isButtonEnabled = preneur != null && appelle != null && contratUI != null

                Button(
                    onClick = {
                        Log.d("SCORE_CALC", "Avant calculateScores: preneur=$preneur, appelle=$appelle")
                        Log.d("SCORE_CALC", "Contrat UI: $contratUI")
                        Log.d("SCORE_CALC", "Chelem selection: $chelemSelection")

                        // CONVERSION DU CONTRAT UI VERS FORMAT SAUVEGARDE
                        val contratSave = convertContratToSaveFormat(contratUI!!)
                        Log.d("SCORE_CALC", "Contrat UI: $contratUI -> Contrat Save: $contratSave")

                        // Construire l'objet Chelem à partir de la sélection
                        val chelemArg = buildChelemFromSelection(chelemSelection.takeIf { it.type != ChelemType.NONE })
                        Log.d("SCORE_CALC", "chelemArg construit: $chelemArg")

                        // Normaliser uniquement annonce_rate en mémoire si besoin
                        val safeConstantes = normalizeOnlyAnnonceRate(constantes)

                        // Calculer les scores avec le contrat UI (pour les calculs)
                        val scores = calculateScores(
                            joueurs = joueurs,
                            preneur = preneur!!,
                            appelle = appelle!!,
                            contrat = contratUI!!, // Utilise le format UI pour les calculs
                            pointsAtq = pointsAtq,
                            nbBoutsAttaque = attackBoutsCount,
                            miseres = misereSelectedNames,
                            petitAuBout = petitAuBoutSelection,
                            chelem = chelemArg,
                            poignees = poignees.toMap(),
                            constantes = safeConstantes,
                            poigneeValues = poigneeValues
                        )

                        // Construire l'objet Donne complet
                        val donneId = donneToEdit?.id ?: UUID.randomUUID().toString()
                        val createdAt = donneToEdit?.createdAt ?: System.currentTimeMillis()

                        // Convertir les noms en indices pour la sauvegarde
                        val preneurIndex = joueurs.indexOf(preneur!!)
                        val appeleIndex = appelle.let { joueurs.indexOf(it) }
                        val miseresList = misereSelectedNames.mapNotNull { nom ->
                            joueurs.indexOf(nom).takeIf { it >= 0 }
                        }

                        val poigneesList = poignees
                            .filter { it.value != PoigneeType.NONE }
                            .mapNotNull { (joueurName, poigneeType) ->
                                val index = joueurs.indexOf(joueurName)
                                if (index >= 0) {
                                    PoigneeIndex(index = index, type = poigneeType.name)
                                } else null
                            }
                            .toMutableList()

                        val newDonne = Donne(
                            id = donneId,
                            createdAt = createdAt,
                            preneur = preneurIndex,
                            appele = appeleIndex,
                            contrat = contratSave, // ← UTILISE LE FORMAT SAUVEGARDE
                            pointsAtq = pointsAtq,
                            attaqueNbBouts = attackBoutsCount,
                            petitAuBout = petitAuBoutSelection,
                            poignees = poigneesList,
                            miseres = miseresList,
                            chelem = chelemArg,
                            scores = scores
                        )

                        // Appeler les callbacks
                        if (onDonneSubmit != null) {
                            onDonneSubmit(newDonne)
                        } else {
                            onScoresSubmit(scores)
                        }

                        showConfirmDialog = false
                    },
                    enabled = isButtonEnabled
                ) { Text("Valider") }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ChelemSelectionDialog(
    currentSelection: ChelemSelection,
    onSelectionChanged: (ChelemSelection) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Type de Chelem") },
        text = {
            Column {
                // Seulement les 3 vrais types de chelem
                val options = listOf(
                    ChelemType.ANNONCE_REUSSI to "Chelem annoncé réussi",
                    ChelemType.ANNONCE_RATE to "Chelem annoncé raté",
                    ChelemType.NON_ANNONCE_REUSSI to "Chelem non annoncé réussi"
                )

                options.forEach { (type, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentSelection.type == type,
                                onClick = { onSelectionChanged(ChelemSelection(type)) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSelection.type == type,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/////////////////////////////////
@Composable
fun PoigneesDialog(
    players: List<String>,
    initial: Map<String, PoigneeType> = emptyMap(),
    onFinish: (Map<String, PoigneeType>) -> Unit,
    onDismiss: () -> Unit
) {
    val selections = remember {
        mutableStateMapOf<String, PoigneeType>().apply {
            players.forEach { put(it, initial[it] ?: PoigneeType.NONE) }
        }
    }

    val states =
        listOf(PoigneeType.NONE, PoigneeType.SIMPLE, PoigneeType.DOUBLE, PoigneeType.TRIPLE)
    val labels = mapOf(
        PoigneeType.NONE to "",
        PoigneeType.SIMPLE to "S",
        PoigneeType.DOUBLE to "D",
        PoigneeType.TRIPLE to "T"
    )

    val valueOf = mapOf(
        PoigneeType.NONE to 0,
        PoigneeType.SIMPLE to 8,
        PoigneeType.DOUBLE to 10,
        PoigneeType.TRIPLE to 13
    )

    val total = remember(selections.toMap()) {
        selections.values.sumOf { valueOf[it] ?: 0 }
    }
    val maxAllowed = 22
    val overLimit = total > maxAllowed

    val standardButtonColor = Color(0xFFB0BEC5)
    val standardTextColor = Color.Black

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Déclarer les poignées") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "S / D / T",
                        fontSize = 14.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.widthIn(min = 60.dp)
                    )
                }

                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(players) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = player,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )

                            val current = selections[player] ?: PoigneeType.NONE
                            val currentIndex = states.indexOf(current).coerceAtLeast(0)
                            val nextIndex = (currentIndex + 1) % states.size

                            Button(
                                onClick = { selections[player] = states[nextIndex] },
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (current == PoigneeType.NONE) Color(
                                        0xFF424242
                                    ) else Color(0xFF212121)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = labels[current] ?: "",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onFinish(selections.toMap()) },
                enabled = !overLimit,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = standardButtonColor,
                    contentColor = standardTextColor,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.Gray
                )
            ) {
                Text("Terminer")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = standardButtonColor,
                    contentColor = standardTextColor
                )
            ) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun PoigneeButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color.Green else Color.LightGray),
        modifier = Modifier.height(36.dp)
    ) { Text(label, color = Color.Black, fontSize = 12.sp) }
}

@Composable
fun InlineBox(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(6.dp))
            .padding(8.dp)
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            content()
        }
    }
}

@Composable
fun ButtonWithResponse(buttonText: String, responseText: String?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(BorderStroke(1.dp, Color.White), shape = RoundedCornerShape(4.dp))
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Button(onClick = onClick) { Text(buttonText) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = responseText ?: "Non sélectionné", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun PlayerSelectionDialogDialog(
    title: String,
    players: List<String>,
    onPlayerSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                players.forEach { p ->
                    Button(
                        onClick = { onPlayerSelected(p) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) { Text(p) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) { Text("Annuler") }
                }
            }
        }
    }
}

@Composable
fun ContractSelectionDialog(
    contracts: List<String>,
    onContractSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Choisir un contrat") },
        confirmButton = { Button(onClick = onDismiss) { Text("Annuler") } },
        text = {
            Column {
                contracts.forEach { contract ->
                    Button(
                        onClick = {
                            onContractSelected(contract)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) { Text(contract) }
                }
            }
        }
    )
}

@Composable
fun MisereSelectionDialog(
    players: List<String>,
    selectedPlayers: MutableMap<String, Boolean>,
    onFinish: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sélectionner les joueurs pour la Misère") },
        confirmButton = {
            Button(onClick = {
                val selected = players.filter { selectedPlayers[it] == true }
                onFinish(selected)
            }) { Text("Terminer") }
        },
        text = {
            LazyColumn {
                items(players) { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPlayers[player] = !(selectedPlayers[player] ?: false)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player,
                            color = Color.Black,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (selectedPlayers[player] == true) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (selectedPlayers[player] == true) Color.Green else Color.Gray
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun PetitAuBoutDialog(
    players: List<String>,
    currentSelection: PetitAuBoutIndex?,
    onSelectionChanged: (PetitAuBoutIndex?) -> Unit,
    onDismiss: () -> Unit
) {
    var localSelection by remember(currentSelection) { mutableStateOf(currentSelection) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Qui a mené le petit au bout ?") },
        text = {
            Column {
                players.forEachIndexed { index, name ->
                    Button(
                        onClick = {
                            // toggle selection locale : si on reclique sur le même index -> deselect
                            localSelection = if (localSelection?.index == index) null
                            else PetitAuBoutIndex(
                                index = index,
                                gagne = true,
                            )
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.Text(
                                text = name,
                                modifier = Modifier.weight(1f)
                            )
                            if (localSelection?.index == index) {
                                androidx.compose.material3.Text("✔️")
                            }
                        }
                    }
                }

                // Si un joueur est sélectionné localement, afficher le contrôle bi-stable
                if (localSelection != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Text(
                        "Ce dernier pli a été :",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Bouton bi-stable unique
                    val isGagne = localSelection!!.gagne
                    val containerColor =
                        if (isGagne) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    val contentColor = MaterialTheme.colorScheme.onPrimary

                    Button(
                        onClick = {
                            localSelection = localSelection!!.copy(gagne = !isGagne)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = containerColor,
                            contentColor = contentColor
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isGagne) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Gagné",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gagné")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Perdu",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Perdu")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSelectionChanged(localSelection)
                onDismiss()
            }) { Text("Valider") }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) { Text("Annuler") }
        }
    )
}
