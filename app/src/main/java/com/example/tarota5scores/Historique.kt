package com.example.tarota5scores

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.UUID

private const val HISTO_FILENAME = "historique.json"

// Config Json : prettyPrint utile pour débogage
private val json = Json { prettyPrint = true; encodeDefaults = true }

// Chargement depuis le fichier historique.json
fun loadHistorique(context: Context): Historique {
    val file = File(context.filesDir, HISTO_FILENAME)
    if (!file.exists()) return Historique()
    return try {
        val content = file.readText()
        json.decodeFromString<Historique>(content)
    } catch (e: Exception) {
        e.printStackTrace()
        Historique()
    }
}

// Sauvegarde dans le fichier historique.json
fun saveHistorique(context: Context, historique: Historique) {
    val file = File(context.filesDir, HISTO_FILENAME)
    try {
        val text = json.encodeToString(historique)
        file.writeText(text)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Crée une nouvelle partie (persistée immédiatement) et la retourne
fun createPartie(context: Context, joueurs: List<String>): Partie {
    val hist = loadHistorique(context)
    val partie = Partie(
        id = UUID.randomUUID().toString(),
        createdAt = System.currentTimeMillis(),
        joueurs = joueurs,
        donnes = emptyList()
    )
    hist.parties.add(partie)
    saveHistorique(context, hist)
    return partie
}


// Supprime une partie entière et sauvegarde
fun deletePartie(context: Context, partieId: String) {
    val hist = loadHistorique(context)
    val removed = hist.parties.removeAll { it.id == partieId }
    if (removed) saveHistorique(context, hist)
}

// Utilitaires d'accès rapide
fun getPartie(context: Context, partieId: String): Partie? {
    val hist = loadHistorique(context)
    return hist.parties.find { it.id == partieId }
}

fun getDonne(context: Context, partieId: String, donneId: String): Donne? {
    val partie = getPartie(context, partieId) ?: return null
    return partie.donnes.find { it.id == donneId }
}

suspend fun deleteDonneInHistorique(context: Context, partieId: String, donneId: String): Historique {
    val hist = loadHistorique(context)
    val updatedParties = hist.parties.map { p ->
        if (p.id == partieId) {
            p.copy(donnes = p.donnes.filterNot { it.id == donneId } as MutableList<Donne>)
        } else p
    }
    val newHist = hist.copy(parties = updatedParties as MutableList<Partie>)
    saveHistorique(context, newHist)
    return newHist
}

suspend fun editDonneInHistorique(context: Context, partieId: String, updatedDonne: Donne): Historique {
    val hist = loadHistorique(context)
    val updatedParties = hist.parties.map { p ->
        if (p.id == partieId) {
            val newDonnes = p.donnes.map { if (it.id == updatedDonne.id) updatedDonne else it }
            p.copy(donnes = newDonnes as MutableList<Donne>)
        } else p
    }
    val newHist = hist.copy(parties = updatedParties as MutableList<Partie>)
    saveHistorique(context, newHist)
    return newHist
}

suspend fun addDonneInHistorique(context: Context, partieId: String, newDonne: Donne): Historique {
    val hist = loadHistorique(context)
    val updatedParties = hist.parties.map { p ->
        if (p.id == partieId) {
            p.copy(donnes = (p.donnes + newDonne) as MutableList<Donne>)
        } else p
    }
    val newHist = hist.copy(parties = updatedParties as MutableList<Partie>)
    saveHistorique(context, newHist)
    return newHist
}
