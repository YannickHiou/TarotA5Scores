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

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import kotlin.random.Random

fun fakeHistorique(filesDir: File, nb_parties: Int) {
    val json = Json { prettyPrint = true }

    // Base de 15 joueurs
    val joueurs = listOf(
        "Yannick", "Christine", "Aurélie", "Alexis", "Camille", "Laïla", "Christophe", "Florence",
        "Clémence", "Arthur", "Eloïse", "Martin", "Valérie", "Benjamin", "Armand"
    )

    // Tous les contrats possibles incluant CHELEM
    val contratsType = listOf("PETITE", "GARDE", "GARDE_SANS", "GARDE_CONTRE", "CHELEM")
    val poigneesType = listOf("SIMPLE", "DOUBLE", "TRIPLE")

    //  NOUVEAU : Calcul des dates réparties sur 5 ans
    val maintenant = System.currentTimeMillis()
    val cinqAnsEnMillis = 5L * 365 * 24 * 60 * 60 * 1000 // 5 ans en millisecondes
    val intervalleEntreParties = if (nb_parties > 1) {
        cinqAnsEnMillis / (nb_parties - 1) // Intervalle entre chaque partie
    } else {
        0L
    }

    val parties = mutableListOf<Partie>()

    repeat(nb_parties) { partieIndex ->
        //  NOUVEAU : Date calculée pour répartir uniformément sur 5 ans
        val datePartie = maintenant - cinqAnsEnMillis + (partieIndex * intervalleEntreParties)

        // Toujours 5 joueurs pour le tarot à 5
        val nbJoueurs = 5
        val joueursPartie = joueurs.shuffled().take(nbJoueurs)

        // Générer entre 5 et 15 donnes par partie
        val nbDonnes = Random.nextInt(5, 16)
        val donnes = mutableListOf<Donne>()

        repeat(nbDonnes) { donneIndex ->
            val preneur = Random.nextInt(nbJoueurs)
            val contrat = contratsType[Random.nextInt(contratsType.size)]

            // Le preneur appelle toujours un roi
            // 25% de chance que le roi soit dans sa main (appele = preneur)
            val appele = if (Random.nextFloat() < 0.25f) {
                preneur // Le roi appelé est dans la main du preneur
            } else {
                // Le roi appelé est chez un autre joueur
                var appelleIndex: Int
                do {
                    appelleIndex = Random.nextInt(nbJoueurs)
                } while (appelleIndex == preneur)
                appelleIndex
            }

            // Générer le nombre de bouts possédés par l'attaque (0 à 3)
            val attaqueNbBouts = Random.nextInt(0, 4)

            // Seuil selon le nombre de bouts (vraies règles)
            val seuil = when (attaqueNbBouts) {
                3 -> 36
                2 -> 41
                1 -> 51
                0 -> 56
                else -> 56
            }

            // Points d'attaque selon le contrat
            val pointsAtq = when (contrat) {
                "PETITE" -> Random.nextInt(seuil - 15, seuil + 20)
                "GARDE" -> Random.nextInt(seuil - 10, seuil + 25)
                "GARDE_SANS" -> Random.nextInt(seuil - 5, seuil + 30)
                "GARDE_CONTRE" -> Random.nextInt(seuil, seuil + 35)
                "CHELEM" -> 91 // Chelem = toujours 91 points (tous les plis)
                else -> Random.nextInt(seuil - 15, seuil + 20)
            }.coerceIn(20, 91) // Limiter entre 20 et 91 points

            // Petit au bout (25% de chance d'être valide, et si valide : 75% gagne=true, 25% gagne=false)
            val petitAuBout = if (Random.nextFloat() < 0.25f) {
                PetitAuBoutIndex(
                    index = Random.nextInt(5), // index dans [0;4]
                    gagne = Random.nextFloat() < 0.75f // 75% de chance que gagne=true
                )
            } else null

            // Poignées (15% de chance par joueur)
            val poigneesList = mutableListOf<PoigneeIndex>()
            repeat(nbJoueurs) { playerIndex ->
                if (Random.nextFloat() < 0.15f) {
                    val typePoignee = poigneesType[Random.nextInt(poigneesType.size)]
                    poigneesList.add(PoigneeIndex(index = playerIndex, type = typePoignee))
                }
            }

            // Misères (10% de chance par joueur)
            val miseres = mutableListOf<Int>()
            repeat(nbJoueurs) { index ->
                if (Random.nextFloat() < 0.1f) {
                    miseres.add(index)
                }
            }

            // Gestion du chelem
            val chelem = if (contrat == "CHELEM") {
                // Si le contrat est CHELEM, il y a obligatoirement un chelem
                Chelem(
                    annonce = Random.nextBoolean(), // 50% annoncé, 50% non annoncé
                    succes = Random.nextFloat() < 0.85f // 85% de réussite pour les chelems tentés
                )
            } else if (Random.nextFloat() < 0.02f) {
                // 2% de chance de chelem non annoncé sur les autres contrats (très rare)
                Chelem(
                    annonce = false, // Toujours non annoncé sur les autres contrats
                    succes = true // S'il y a chelem non annoncé, il est forcément réussi
                )
            } else null

            // Scores calculés selon les vraies règles
            val scores = generateRealisticScores(
                contrat,
                pointsAtq,
                seuil,
                preneur,
                appele,
                petitAuBout,
                poigneesList,
                miseres,
                chelem,
                joueursPartie
            )

            //  NOUVEAU : Date de la donne légèrement décalée par rapport à la partie
            val dateDonne = datePartie + Random.nextLong(0, 3 * 60 * 60 * 1000) // +0 à 3h après le début de partie

            val donne = Donne(
                id = UUID.randomUUID().toString(),
                createdAt = dateDonne, //  MODIFIÉ
                preneur = preneur,
                appele = appele,
                contrat = contrat,
                pointsAtq = pointsAtq,
                attaqueNbBouts = attaqueNbBouts,
                petitAuBout = petitAuBout,
                poignees = poigneesList,
                miseres = miseres,
                chelem = chelem,
                scores = scores
            )

            donnes.add(donne)
        }

        val partie = Partie(
            id = UUID.randomUUID().toString(),
            createdAt = datePartie, //  MODIFIÉ : Utilise la date calculée
            joueurs = joueursPartie,
            donnes = donnes
        )

        parties.add(partie)
    }

    val historique = Historique(parties = parties)
    // Sauvegarder dans historique.json
    val historiqueFile = File(filesDir, "historique.json")
    historiqueFile.writeText(json.encodeToString(historique))
}


private fun generateRealisticScores(
    contrat: String,
    pointsAtq: Int,
    seuil: Int,
    preneur: Int,
    appele: Int,
    petitAuBout: PetitAuBoutIndex?,
    poignees: List<PoigneeIndex>,
    miseres: List<Int>,
    chelem: Chelem?,
    joueursPartie: List<String>
): List<Int> {
    val scores = MutableList(5) { 0 }

    // Gestion spéciale pour le contrat CHELEM
    if (contrat == "CHELEM") {
        val multiplicateur = 6 // Chelem = multiplicateur x6

        val baseScore = if (chelem?.succes == true) {
            // Chelem réussi
            val bonus = if (chelem.annonce) 400 else 200 // Annoncé = +400, Non annoncé = +200
            (25 + (pointsAtq - seuil)) * multiplicateur + bonus
        } else {
            // Chelem raté (seulement possible si annoncé)
            val malus = if (chelem?.annonce == true) -200 else 0 // Pénalité si annoncé et raté
            (25 + (pointsAtq - seuil)) * multiplicateur + malus
        }

        // Répartition selon si preneur = appelé ou non
        if (appele != preneur) {
            // 2 contre 3 : Preneur + Appelé vs 3 défenseurs
            scores[preneur] = baseScore * 2
            scores[appele] = baseScore
            repeat(5) { i ->
                if (i != preneur && i != appele) scores[i] = -baseScore
            }
        } else {
            // 1 contre 4 : Preneur seul vs 4 défenseurs
            scores[preneur] = baseScore * 4
            repeat(5) { i ->
                if (i != preneur) scores[i] = -baseScore
            }
        }
    } else {
        // Logique normale pour les autres contrats
        val reussi = pointsAtq >= seuil
        val multiplicateur = when (contrat) {
            "PETITE" -> 1
            "GARDE" -> 2
            "GARDE_SANS" -> 4
            "GARDE_CONTRE" -> 6
            else -> 1
        }

        // Score de base du contrat
        val baseScore = (25 + (pointsAtq - seuil)) * multiplicateur
        val finalScore = if (reussi) baseScore else -baseScore

        // Répartition selon si preneur = appelé ou non
        if (appele != preneur) {
            // 2 contre 3 : Preneur + Appelé vs 3 défenseurs
            scores[preneur] = finalScore * 2
            scores[appele] = finalScore
            repeat(5) { i ->
                if (i != preneur && i != appele) scores[i] = -finalScore
            }
        } else {
            // 1 contre 4 : Preneur seul vs 4 défenseurs
            scores[preneur] = finalScore * 4
            repeat(5) { i ->
                if (i != preneur) scores[i] = -finalScore
            }
        }

        // Bonus chelem non annoncé sur contrat normal
        chelem?.let { chelemObj ->
            if (chelemObj.succes && !chelemObj.annonce) {
                val bonusChelem = 200
                if (appele != preneur) {
                    scores[preneur] += bonusChelem * 2
                    scores[appele] += bonusChelem
                    repeat(5) { i ->
                        if (i != preneur && i != appele) scores[i] -= bonusChelem
                    }
                } else {
                    scores[preneur] += bonusChelem * 4
                    repeat(5) { i ->
                        if (i != preneur) scores[i] -= bonusChelem
                    }
                }
            }
        }
    }

    // Ajouter les points du petit au bout
    petitAuBout?.let { pab ->
        val multiplicateurPetit = when (contrat) {
            "PETITE" -> 1
            "GARDE" -> 2
            "GARDE_SANS" -> 4
            "GARDE_CONTRE" -> 6
            "CHELEM" -> 6
            else -> 1
        }
        val pointsPetitAuBout = 10 * multiplicateurPetit

        val attaqueALePetit = (pab.index == preneur || (appele != preneur && pab.index == appele))
        val attaqueGagne = (attaqueALePetit && pab.gagne) || (!attaqueALePetit && !pab.gagne)

        if (attaqueGagne) {
            // L'attaque gagne le petit au bout
            if (appele != preneur) {
                scores[preneur] += pointsPetitAuBout * 2
                scores[appele] += pointsPetitAuBout
                repeat(5) { i ->
                    if (i != preneur && i != appele) scores[i] -= pointsPetitAuBout
                }
            } else {
                scores[preneur] += pointsPetitAuBout * 4
                repeat(5) { i ->
                    if (i != preneur) scores[i] -= pointsPetitAuBout
                }
            }
        } else {
            // L'attaque perd le petit au bout
            if (appele != preneur) {
                scores[preneur] -= pointsPetitAuBout * 2
                scores[appele] -= pointsPetitAuBout
                repeat(5) { i ->
                    if (i != preneur && i != appele) scores[i] += pointsPetitAuBout
                }
            } else {
                scores[preneur] -= pointsPetitAuBout * 4
                repeat(5) { i ->
                    if (i != preneur) scores[i] += pointsPetitAuBout
                }
            }
        }
    }

    // Ajouter les points des poignées
    poignees.forEach { poignee ->
        val pointsPoignee = when (poignee.type) {
            "SIMPLE" -> 20
            "DOUBLE" -> 30
            "TRIPLE" -> 40
            else -> 0
        }

        val attaqueALaPoignee = (poignee.index == preneur || (appele != preneur && poignee.index == appele))

        if (attaqueALaPoignee) {
            // L'attaque a la poignée
            if (appele != preneur) {
                scores[preneur] += pointsPoignee * 2
                scores[appele] += pointsPoignee
                repeat(5) { i ->
                    if (i != preneur && i != appele) scores[i] -= pointsPoignee
                }
            } else {
                scores[preneur] += pointsPoignee * 4
                repeat(5) { i ->
                    if (i != preneur) scores[i] -= pointsPoignee
                }
            }
        } else {
            // La défense a la poignée
            if (appele != preneur) {
                scores[preneur] -= pointsPoignee * 2
                scores[appele] -= pointsPoignee
                repeat(5) { i ->
                    if (i != preneur && i != appele) scores[i] += pointsPoignee
                }
            } else {
                scores[preneur] -= pointsPoignee * 4
                repeat(5) { i ->
                    if (i != preneur) scores[i] += pointsPoignee
                }
            }
        }
    }

    // Ajouter les points des misères
    miseres.forEach { indexJoueur ->
        val pointsMisere = 10

        // La misère rapporte TOUJOURS +10 au joueur qui la fait
        // Les autres joueurs perdent proportionnellement
        scores[indexJoueur] += pointsMisere * 4
        repeat(5) { i ->
            if (i != indexJoueur) scores[i] -= pointsMisere
        }
    }

    // Ajuster pour que la somme soit exactement 0
    val somme = scores.sum()
    if (somme != 0) {
        scores[0] -= somme
    }

    return scores
}
