package com.tarot.tarota5scores

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.UUID
//import com.android.identity.util.UUID
import com.tarot.tarota5scores.ui.theme.TarotA5ScoresTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TarotA5ScoresTheme {
                TarotA5ScoressApp()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }

        // Pour générer aléatoiremnt un fichier Historique.json pour les tests
        // fakeHistorique(filesDir, 300)
    }
}

sealed class Screen {
    object Accueil : Screen()
    object Jouer : Screen()
    object Joueurs : Screen()
    object Jeux : Screen()
    object Donne : Screen()
    object Historique : Screen()
    object Apropos : Screen()
    object StatistiquesGlobales : Screen()
    object StatistiquesJoueurs : Screen()
    object StatistiquesPartie : Screen()
    object Constantes : Screen()
}

val ScreenSaver = Saver<Screen, String>(
    save = { screen ->
        when (screen) {
            is Screen.Accueil -> "Accueil"
            is Screen.Jouer -> "Jouer"
            is Screen.Joueurs -> "Joueurs"
            is Screen.Jeux -> "Jeux"
            is Screen.Donne -> "Donne"
            is Screen.Historique -> "Historique"
            is Screen.Apropos -> "A propos"
            is Screen.StatistiquesGlobales -> "StatistiquesGlobales"
            is Screen.StatistiquesJoueurs -> "StatistiquesJoueurs"
            is Screen.StatistiquesPartie -> "StatistiquesPartie"
            is Screen.Constantes -> "Constantes"
        }
    },
    restore = { name ->
        when (name) {
            "Accueil" -> Screen.Accueil
            "Jouer" -> Screen.Jouer
            "Joueurs" -> Screen.Joueurs
            "Jeux" -> Screen.Jeux
            "Donne" -> Screen.Donne
            "Historique" -> Screen.Historique
            "A propos" -> Screen.Apropos
            "StatistiquesGlobales" -> Screen.StatistiquesGlobales
            "StatistiquesJoueurs" -> Screen.StatistiquesJoueurs
            "StatistiquesPartie" -> Screen.StatistiquesPartie
            "Constantes" -> Screen.Constantes
            else -> throw IllegalArgumentException("Unknown screen")
        }
    }
)

@Composable
fun TarotA5ScoressApp() {
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Accueil) }
    var showExitDialog by remember { mutableStateOf(false) }

    val joueursSelectionnes = remember { mutableStateOf(setOf<String>()) }

    // Source de vérité en mémoire : donnes de la partie courante
    val allDonnes = remember { mutableStateOf(listOf<Donne>()) }
    var currentPartieId by remember { mutableStateOf<String?>(null) }

    // Historique chargé depuis fichier
    val historique = remember { mutableStateOf<Historique?>(null) }

    // états temporaires pour transmission vers DonneScreen
    var pendingDonneToEdit by remember { mutableStateOf<Donne?>(null) }
    var pendingDonneSubmit by remember { mutableStateOf<((Donne) -> Unit)?>(null) }

    // Dialog pour choix Edit / Delete / Cancel sur une donne
    var showDonneActionDialog by remember { mutableStateOf(false) }
    var selectedDonneForAction by remember { mutableStateOf<Donne?>(null) }

    var selectedPartieId by remember { mutableStateOf<String?>(null) }
    var selectedJoueurNom by remember { mutableStateOf<String?>(null) }

    var historiqueContext by remember { mutableStateOf<HistoriqueContext?>(null) }

    val scope = rememberCoroutineScope()

    // Initialisation : charger historique.json et initialiser état
    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) {
            loadHistorique(context)
        }
        // s'assure que le fichier existe physiquement
        if (loaded.parties.isEmpty()) {
            withContext(Dispatchers.IO) {
                saveHistorique(context, loaded)
            }
        }
        historique.value = loaded

        // charger la dernière partie si présente
        if (loaded.parties.isNotEmpty()) {
            val last = loaded.parties.last()
            currentPartieId = last.id
            allDonnes.value = last.donnes.toList()
        }
    }

    // calcule les totaux par joueur (somme des scores dans allDonnes)
    val joueursList = joueursSelectionnes.value.toList()
    val totals: List<Int> = remember(allDonnes.value, joueursList) {
        val n = joueursList.size
        if (n == 0) return@remember emptyList()
        val acc = MutableList(n) { 0 }
        allDonnes.value.forEach { d ->
            d.scores?.let { scores ->
                for (i in 0 until minOf(scores.size, n)) {
                    acc[i] = acc[i] + scores[i]
                }
            }
        }
        acc.toList()
    }

    when (currentScreen) {
        is Screen.Accueil -> AccueilScreen(
            onJouerClick = {
                currentScreen = Screen.Jouer
                allDonnes.value = listOf()
                currentPartieId = null
            },
            onAproposClick = { currentScreen = Screen.Apropos },
            onJoueursClick = { currentScreen = Screen.Joueurs },
            onHistoriqueClick = { currentScreen = Screen.Historique },
            onStatistiquesClick = { currentScreen = Screen.StatistiquesGlobales },
            onConstantesClick ={ currentScreen = Screen.Constantes },
        )

        is Screen.Jouer -> JouerScreen(
            onRetour = { currentScreen = Screen.Accueil },
            onNavigateToGame = { selectedPlayers ->
                joueursSelectionnes.value = selectedPlayers

                scope.launch {
                    val partie = withContext(Dispatchers.IO) {
                        createPartie(context, selectedPlayers.toList())
                    }
                    currentPartieId = partie.id
                    allDonnes.value = partie.donnes.toList()

                    historique.value = withContext(Dispatchers.IO) { loadHistorique(context) }

                    currentScreen = Screen.Jeux
                }
            }
        )

        is Screen.Apropos -> AproposScreen(onBack = { currentScreen = Screen.Accueil })

        is Screen.Joueurs -> JoueursScreen(
            onRetour = {
                selectedPartieId = null // AJOUTÉ : réinitialiser pour éviter la confusion
                currentScreen = Screen.Accueil
            },
            onStatistiquesJoueur = { joueurNom ->
                selectedJoueurNom = joueurNom
                selectedPartieId = null // AJOUTÉ : s'assurer qu'on ne vient pas d'une partie
                currentScreen = Screen.StatistiquesJoueurs
            }
        )


        is Screen.Jeux -> {
            JeuxScreen(
                joueurs = joueursList,
                donnes = allDonnes.value,
                totals = totals,
                modifier = Modifier.fillMaxSize(),

                onRetour = { showExitDialog = true },

                onNouvelleDonne = {
                    // préparer persistence pour la nouvelle donne via pendingDonneSubmit
                    pendingDonneToEdit = null
                    pendingDonneSubmit = { newDonne ->
                        val pid = currentPartieId
                        if (pid != null) {
                            scope.launch {
                                val newHist = withContext(Dispatchers.IO) {
                                    addDonneInHistorique(context, pid, newDonne)
                                }
                                historique.value = newHist
                                val partie = newHist.parties.firstOrNull { it.id == pid }
                                allDonnes.value = partie?.donnes?.toList() ?: listOf()
                            }
                        } else {
                            // fallback mémoire
                            allDonnes.value = allDonnes.value + newDonne
                        }
                    }
                    currentScreen = Screen.Donne
                },

                onEditDonne = { donneToEdit ->
                    // ouvre la dialog d'action (Edit / Delete / Cancel)
                    selectedDonneForAction = donneToEdit
                    showDonneActionDialog = true
                }
            )
        }

        is Screen.Donne -> {
            DonneScreen(
                joueurs = joueursList,
                onScoresSubmit = { newScores ->
                    val donne = Donne(
                        id = UUID.randomUUID().toString(),
                        createdAt = System.currentTimeMillis(),
                        preneur = 0,
                        appele = 0,
                        contrat = "Inconnu",
                        pointsAtq = 0,
                        attaqueNbBouts = 0,
                        petitAuBout = null,
                        poignees = emptyList(),
                        miseres = emptyList(),
                        scores = newScores
                    )

                    val pid = currentPartieId
                    if (pid != null) {
                        scope.launch {
                            val newHist = withContext(Dispatchers.IO) {
                                addDonneInHistorique(context, pid, donne)
                            }
                            historique.value = newHist
                            val partie = newHist.parties.firstOrNull { it.id == pid }
                            allDonnes.value = partie?.donnes?.toList() ?: listOf()

                            pendingDonneToEdit = null
                            pendingDonneSubmit = null
                            currentScreen = Screen.Jeux
                        }
                    } else {
                        allDonnes.value = allDonnes.value + donne
                        pendingDonneToEdit = null
                        pendingDonneSubmit = null
                        currentScreen = Screen.Jeux
                    }
                },

                onCancel = {
                    pendingDonneToEdit = null
                    pendingDonneSubmit = null
                    currentScreen = Screen.Jeux
                },

                donneToEdit = pendingDonneToEdit,
                onDonneSubmit = { newDonne ->
                    val delegate = pendingDonneSubmit
                    if (delegate != null) {
                        // déléguer la persistance (déjà configurée)
                        delegate(newDonne)
                        pendingDonneToEdit = null
                        pendingDonneSubmit = null
                        currentScreen = Screen.Jeux
                    } else {
                        // pas de delegate : on persiste ici de façon robuste via historique
                        val pid = currentPartieId
                        if (pid != null) {
                            scope.launch {
                                val newHist = withContext(Dispatchers.IO) {
                                    addDonneInHistorique(context, pid, newDonne)
                                }
                                historique.value = newHist
                                val partie = newHist.parties.firstOrNull { it.id == pid }
                                allDonnes.value = partie?.donnes?.toList() ?: listOf()
                                pendingDonneToEdit = null
                                pendingDonneSubmit = null
                                currentScreen = Screen.Jeux
                            }
                        } else {
                            allDonnes.value = allDonnes.value + newDonne
                            pendingDonneToEdit = null
                            pendingDonneSubmit = null
                            currentScreen = Screen.Jeux
                        }
                    }
                }
            )
        }

        is Screen.Historique -> {
            val hist = remember { mutableStateOf(historique.value ?: loadHistorique(context)) }

            HistoriqueScreen(
                historique = hist.value,
                onBack = {
                    historiqueContext = null
                    currentScreen = Screen.Accueil
                },
                onReprendrePartie = { partie ->
                    historiqueContext = null
                    currentPartieId = partie.id
                    joueursSelectionnes.value = partie.joueurs.toSet()
                    allDonnes.value = partie.donnes.toList()
                    scope.launch {
                        historique.value = withContext(Dispatchers.IO) { loadHistorique(context) }
                    }
                    currentScreen = Screen.Jeux
                },
                onStatistiquesPartie = { partie, ctx ->
                    selectedPartieId = partie.id
                    historiqueContext = ctx              // on mémorise où on était
                    currentScreen = Screen.StatistiquesPartie
                },
                onSupprimerPartie = { partie ->
                    scope.launch {
                        withContext(Dispatchers.IO) { deletePartie(context, partie.id) }
                        val newHist = withContext(Dispatchers.IO) { loadHistorique(context) }
                        historique.value = newHist
                    }
                    historiqueContext = null
                    currentScreen = Screen.Accueil
                },
                initialContext = historiqueContext
            )
        }

        is Screen.StatistiquesGlobales -> StatistiquesGlobalesScreen(
            historique = historique.value ?: Historique(mutableListOf()),
            onNavigateBack = { currentScreen = Screen.Accueil }
        )

        is Screen.StatistiquesPartie -> StatistiquesPartieScreen(
            historique = historique.value ?: Historique(mutableListOf()),
            partieId = selectedPartieId,
            onNavigateBack = { currentScreen = Screen.Historique },
            onNavigateToJoueur = { nomJoueur ->
                selectedJoueurNom = nomJoueur
                currentScreen = Screen.StatistiquesJoueurs
            }
        )

        is Screen.StatistiquesJoueurs -> StatistiquesJoueursScreen(
            historique = historique.value ?: Historique(mutableListOf()),
            joueurId = selectedJoueurNom,
            onNavigateBack = {
                if (selectedPartieId != null) { // Si on vient d'une partie
                    currentScreen = Screen.StatistiquesPartie
                } else { // Si on vient de l'écran Joueurs
                    currentScreen = Screen.Joueurs // CHANGÉ de Screen.Historique à Screen.Joueurs
                }
            },
            fromPartie = selectedPartieId != null
        )

        is Screen.Constantes ->ConstantesScreen (onBack = { currentScreen = Screen.Accueil })
    }

    // Dialog d'action pour une donne (Edit / Delete / Cancel)
    if (showDonneActionDialog && selectedDonneForAction != null) {
        val target = selectedDonneForAction!!
        AlertDialog(
            onDismissRequest = { showDonneActionDialog = false; selectedDonneForAction = null },
            title = { Text("Action sur la donne") },
            //text = { Text("Que souhaites‑tu faire  ?") },
            confirmButton = {
                // ÉDITER
                TextButton(onClick = {
                    //  CORRECTION : Normaliser le contrat avant édition
                    val normalizedTarget = target.copy(
                        contrat = when {
                            target.contrat.contains(
                                "Chelem",
                                ignoreCase = true
                            ) && target.contrat.contains("Annoncé", ignoreCase = true) -> "Chelem"

                            target.contrat.contains(
                                "Chelem",
                                ignoreCase = true
                            ) && target.contrat.contains(
                                "Non annoncé",
                                ignoreCase = true
                            ) -> "GardeSans"

                            else -> target.contrat
                        }
                    )

                    // Préparer pendingDonneToEdit avec le contrat normalisé
                    pendingDonneToEdit = normalizedTarget

                    //  AJOUT : Forcer la réinitialisation de l'écran de saisie
                    // Réinitialiser les états liés au contrat (à adapter selon tes variables d'état)
                    // Par exemple, si tu as des variables comme selectedContrat, chelemState, etc.

                    pendingDonneSubmit = { updatedDonne ->
                        val pid = currentPartieId
                        if (pid != null) {
                            scope.launch {
                                val newHist = withContext(Dispatchers.IO) {
                                    editDonneInHistorique(context, pid, updatedDonne)
                                }
                                historique.value = newHist
                                val partie = newHist.parties.firstOrNull { it.id == pid }
                                allDonnes.value = partie?.donnes?.toList() ?: listOf()
                            }
                        } else {
                            // fallback mémoire
                            allDonnes.value =
                                allDonnes.value.map { if (it.id == updatedDonne.id) updatedDonne else it }
                        }
                    }
                    showDonneActionDialog = false
                    selectedDonneForAction = null

                    //  AJOUT : Réinitialiser l'écran avant de naviguer
                    // Ceci force l'écran de saisie à se réinitialiser complètement
                    currentScreen = Screen.Historique  // Transition temporaire

                    // Puis naviguer vers l'écran de saisie après un court délai
                    scope.launch {
                        delay(50) // Court délai pour forcer la recomposition
                        currentScreen = Screen.Donne
                    }
                }) {
                    Text("Éditer")
                }


            },
            dismissButton = {
                Row {
                    // SUPPRIMER
                    TextButton(onClick = {
                        val pid = currentPartieId
                        if (pid != null) {
                            scope.launch {
                                val newHist = withContext(Dispatchers.IO) {
                                    deleteDonneInHistorique(context, pid, target.id)
                                }
                                historique.value = newHist
                                val partie = newHist.parties.firstOrNull { it.id == pid }
                                allDonnes.value = partie?.donnes?.toList() ?: listOf()
                            }
                        } else {
                            allDonnes.value = allDonnes.value.filterNot { it.id == target.id }
                        }
                        showDonneActionDialog = false
                        selectedDonneForAction = null
                    }) {
                        Text("Supprimer")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ANNULER
                    TextButton(onClick = {
                        showDonneActionDialog = false
                        selectedDonneForAction = null
                    }) {
                        Text("Annuler")
                    }
                }
            }
        )
    }

    // Dialog de confirmation de sortie (Retour à l'accueil)
    if (showExitDialog) {
        ConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                currentScreen = Screen.Accueil
            }
        )
    }
}
