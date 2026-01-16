package com.example.tarota5scores

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
