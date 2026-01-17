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

import kotlinx.serialization.Serializable

@Serializable
data class PoigneeIndex(
    val index: Int,
    val type: String
)

@Serializable
data class PetitAuBoutIndex(
    val index: Int,      // index du joueur
    val gagne: Boolean // peut être gagné ou perdu
)

@Serializable
enum class PoigneeType { NONE, SIMPLE, DOUBLE, TRIPLE }

@Serializable
enum class ContratsType {PETITE, GARDE, GARDE_SANS, GARDE_CONTRE, CHELEM}

@Serializable
data class Joueur(val id: String, val nom: String)

@Serializable
data class Partie(
    val id: String,
    val createdAt: Long,
    val joueurs: List<String>, // ordre fixe pour toute la partie
    val donnes: List<Donne>
)

@Serializable
data class Donne(
    val id: String,
    val createdAt: Long,
    val preneur: Int,                    // index 0-based dans joueurs
    val appele: Int,             // index 0-based ou null
    val contrat: String,
    val pointsAtq: Int,
    val attaqueNbBouts: Int,
    val petitAuBout: PetitAuBoutIndex? = null,
    val poignees: List<PoigneeIndex>,
    val miseres: List<Int> = emptyList(), // liste d'indices
    val chelem: Chelem? = null,
    val scores: List<Int>        // aligné sur joueurs
)


@Serializable
data class Chelem(
    val annonce: Boolean,
    val succes: Boolean
)

@Serializable
data class Historique(val parties: MutableList<Partie> = mutableListOf())
