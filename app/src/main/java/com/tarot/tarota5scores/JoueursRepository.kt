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

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class JoueursScreen(private val context: Context) {
    private val fileName = "joueurs.json"
    private val json = Json { prettyPrint = true }

    fun lireJoueurs(): List<Joueur> {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            try {
                val jsonString = file.readText()
                json.decodeFromString<List<Joueur>>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun sauvegarderJoueurs(joueurs: List<Joueur>) {
        val file = File(context.filesDir, fileName)
        val jsonString = json.encodeToString(joueurs)
        file.writeText(jsonString)
    }

    fun ajouterJoueur(nom: String) {
        val joueurs = lireJoueurs().toMutableList()
        val nouveau = Joueur(
            id = UUID.randomUUID().toString(), // génération EXPLICITE d'un UUID
            nom = nom
        )
        joueurs.add(nouveau)
        sauvegarderJoueurs(joueurs)
    }

    fun supprimerJoueur(joueur: Joueur) {
        val joueurs = lireJoueurs().toMutableList()
        // Supprimer par id pour être sûr qu'on supprime le bon joueur
        joueurs.removeAll { it.id == joueur.id }
        sauvegarderJoueurs(joueurs)
    }

    fun renommerJoueur(ancienJoueur: Joueur, nouveauNom: String) {
        val joueurs = lireJoueurs().toMutableList()
        val index = joueurs.indexOfFirst { it.id == ancienJoueur.id }
        if (index != -1) {
            // préserver l'id, ne pas recréer un joueur avec un nouvel id
            joueurs[index] = joueurs[index].copy(nom = nouveauNom)
            sauvegarderJoueurs(joueurs)
        }
    }
}
