package com.tarot.tarota5scores

data class StatistiquesJoueur(
    val contrats: List<Int>, // [PETITE, GARDE, GARDE_SANS, GARDE_CONTRE]
    val poignees: List<Int>, // [SIMPLE, DOUBLE, TRIPLE]
    val chelems: List<Int>, // [NON_ANNONCE_REUSSI, ANNONCE_RATE, ANNONCE_REUSSI]
    val miseres: Int,
    val petitAuBoutGagne: Int,
    val petitAuBoutPerdu: Int,
    val preneur: Int,
    val appele: Int,
    val defense: Int,
    val totalDonnes: Int,
    val totalParties: Int,
    val pointsGagnes: Int,
    val pointsPerdus: Int,
    val meilleurScore: Int,
    val pireScore: Int,
    val gainNet: Int,
    val gainMoyenParDonne: Double,
    val gainMediane: Int,
    val gainMoyenMediane: Double,
    val decile: Int,
    val decileMoyen: Int
)

data class StatistiquesPartie(
    val nbDonnes: Int,
    val attaqueNbBouts: Int,
    val petitAuBoutGagne: Int,
    val petitAuBoutPerdus: Int,
    val miseres: Int,
    val pointsGagnes: Int,
    val pointsPerdus: Int,
    val meilleurScore: Int,
    val pireScore: Int,
    val partieContrats: List<Int>,
    val partiePoignees: List<Int>,
    val partieChelems: List<Int>
)

data class StatistiquesGlobales(
    val nbDonnes: Int,
    val attaqueNbBouts: Int,
    val petitAuBoutGagne: Int,
    val petitAuBoutPerdu: Int,
    val miseres: Int,
    val pointsGagnes: Int,
    val meilleurScore: Int,
    val pireScore: Int,
    val contrats: List<Int>,
    val poignees: List<Int>,
    val chelems: List<Int>,
    val gainMin: Int,
    val gainMax: Int,
    val mediane: Int,
    val gainMoyenMin: Double,
    val gainMoyenMax: Double,
    val medianeMoyenne: Double
)

class AnalyseurHistorique {

    companion object {
        // Format unique et coherent
        private val CONTRATS_TYPE = listOf("PETITE", "GARDE", "GARDE_SANS", "GARDE_CONTRE")
        private val POIGNEES_TYPE = listOf("SIMPLE", "DOUBLE", "TRIPLE")
    }

    fun analyser(historique: Historique): Triple<Map<String, StatistiquesJoueur>, List<StatistiquesPartie>, StatistiquesGlobales> {

        val joueurs = mutableMapOf<String, MutableStatistiquesJoueur>()
        val parties = mutableListOf<StatistiquesPartie>()
        val globales = MutableStatistiquesGlobales()

        // Traitement de chaque partie
        for (partie in historique.parties) {
            // Initialiser les joueurs s'ils n'existent pas
            for (nomJoueur in partie.joueurs) {
                if (!joueurs.containsKey(nomJoueur)) {
                    joueurs[nomJoueur] = MutableStatistiquesJoueur()
                }
                joueurs[nomJoueur]!!.totalParties++

            }

            val partieNbDonnes = partie.donnes.size
            var partieAttaqueNbBouts = 0
            var partieMeilleurScore = 0
            var partiePireScore = 0
            var partiePointsGagnes = 0
            var partiePointsPerdus = 0
            val partieContrats = MutableList(4) { 0 }
            val partiePoignees = MutableList(3) { 0 }
            val partieChelems = MutableList(3) { 0 }
            var partieMiseres = 0
            var partiePetitAuBoutGagne = 0
            var partiePetitAuBoutPerdu = 0

            for (donne in partie.donnes) {
                // PRENEUR
                val idxPreneur = donne.preneur
                val nomPreneur = partie.joueurs[idxPreneur]
                joueurs[nomPreneur]!!.preneur++

                // APPELE
                val idxAppele = donne.appele
                val nomAppele = partie.joueurs[idxAppele]
                joueurs[nomAppele]!!.appele++

                // DEFENSE - Calcul pour les autres joueurs
                for ((idxJoueur, nomJoueur) in partie.joueurs.withIndex()) {
                    if (idxJoueur != idxPreneur && idxJoueur != idxAppele) {
                        joueurs[nomJoueur]!!.defense++
                    }
                }

                // POINTS GAGNES/PERDUS
                for ((idxJoueur, score) in donne.scores.withIndex()) {
                    val nomJoueur = partie.joueurs[idxJoueur]
                    val joueurStats = joueurs[nomJoueur]!!

                    if (score >= 0) {
                        joueurStats.pointsGagnes += score
                        partiePointsGagnes += score
                    }

                    if (score < 0) {
                        joueurStats.pointsPerdus += score
                        partiePointsPerdus += score
                    }

                    if (joueurStats.pireScore > score) {
                        joueurStats.pireScore = score
                    }

                    if (joueurStats.meilleurScore < score) {
                        joueurStats.meilleurScore = score
                    }

                    if (partiePireScore > score) {
                        partiePireScore = score
                    }

                    if (partieMeilleurScore < score) {
                        partieMeilleurScore = score
                    }
                }

                // NOMBRE DE BOUTS
                partieAttaqueNbBouts += donne.attaqueNbBouts

                // PETIT AU BOUT
                donne.petitAuBout?.let { petitAuBout ->
                    val idxJoueur = petitAuBout.index
                    val nomJoueur = partie.joueurs[idxJoueur]

                    if (petitAuBout.gagne) {
                        partiePetitAuBoutGagne++
                        joueurs[nomJoueur]!!.petitAuBoutGagne++
                    } else {
                        partiePetitAuBoutPerdu++
                        joueurs[nomJoueur]!!.petitAuBoutPerdu++
                    }
                }

                // MISERE
                for (idxMisere in donne.miseres) {
                    val nomJoueur = partie.joueurs[idxMisere]
                    joueurs[nomJoueur]!!.miseres++
                    partieMiseres++
                }

                // CONTRAT ET CHELEM
                if (donne.contrat == "CHELEM") {
                    // Gestion speciale pour les chelems
                    donne.chelem?.let { chelemType ->
                        val idxChelem = when {
                            chelemType.annonce && chelemType.succes -> 2 // Annonce reussi
                            chelemType.annonce && !chelemType.succes -> 1 // Annonce rate
                            !chelemType.annonce && chelemType.succes -> 0 // Non annonce reussi
                            else -> 0 // Fallback
                        }

                        // Ajouter aux statistiques du preneur
                        val nomPreneur = partie.joueurs[idxPreneur]
                        joueurs[nomPreneur]!!.chelems[idxChelem]++

                        // Ajouter aux statistiques de l'appele (si different du preneur)
                        if (idxAppele != idxPreneur) {
                            val nomAppele = partie.joueurs[idxAppele]
                            joueurs[nomAppele]!!.chelems[idxChelem]++
                        }

                        partieChelems[idxChelem]++
                    }
                } else {
                    // Contrats normaux
                    for ((idxContrat, contratType) in CONTRATS_TYPE.withIndex()) {
                        if (contratType == donne.contrat) {
                            val nomJoueur = partie.joueurs[idxPreneur]
                            joueurs[nomJoueur]!!.contrats[idxContrat]++
                            partieContrats[idxContrat]++
                            break
                        }
                    }

                    // Chelem non annonce sur contrat normal
                    donne.chelem?.let { chelemType ->
                        if (!chelemType.annonce && chelemType.succes) {
                            val idxChelem = 0 // Non annonce reussi

                            // Ajouter aux statistiques du preneur
                            val nomPreneur = partie.joueurs[idxPreneur]
                            joueurs[nomPreneur]!!.chelems[idxChelem]++

                            // Ajouter aux statistiques de l'appele (si different du preneur)
                            if (idxAppele != idxPreneur) {
                                val nomAppele = partie.joueurs[idxAppele]
                                joueurs[nomAppele]!!.chelems[idxChelem]++
                            }

                            partieChelems[idxChelem]++
                        }
                    }
                }

                // POIGNEES
                for (poignee in donne.poignees) {
                    val idxJoueur = poignee.index
                    val nomJoueur = partie.joueurs[idxJoueur]

                    for ((idxPoignee, poigneeType) in POIGNEES_TYPE.withIndex()) {
                        if (poigneeType == poignee.type) {
                            joueurs[nomJoueur]!!.poignees[idxPoignee]++
                            partiePoignees[idxPoignee]++
                            break
                        }
                    }
                }
            }

            // Ajouter les statistiques de la partie

            parties.add(
                StatistiquesPartie(
                    nbDonnes = partieNbDonnes,
                    attaqueNbBouts = partieAttaqueNbBouts,
                    petitAuBoutGagne = partiePetitAuBoutGagne,
                    petitAuBoutPerdus = partiePetitAuBoutPerdu,
                    miseres = partieMiseres,
                    pointsGagnes = partiePointsGagnes,
                    pointsPerdus = partiePointsPerdus,
                    meilleurScore = partieMeilleurScore,
                    pireScore = partiePireScore,
                    partieContrats = partieContrats.toList(),
                    partiePoignees = partiePoignees.toList(),
                    partieChelems = partieChelems.toList()
                )
            )

            // Mettre a jour les statistiques globales
            globales.nbDonnes += partieNbDonnes
            globales.attaqueNbBouts += partieAttaqueNbBouts
            globales.petitAuBoutGagne += partiePetitAuBoutGagne
            globales.petitAuBoutPerdu += partiePetitAuBoutPerdu
            globales.miseres += partieMiseres
            globales.pointsGagnes += partiePointsGagnes

            for ((i, x) in partieContrats.withIndex()) {
                globales.contrats[i] += x
            }

            for ((i, x) in partiePoignees.withIndex()) {
                globales.poignees[i] += x
            }

            for ((i, x) in partieChelems.withIndex()) {
                globales.chelems[i] += x
            }

            if (globales.meilleurScore < partieMeilleurScore) {
                globales.meilleurScore = partieMeilleurScore
            }

            if (globales.pireScore > partiePireScore) {
                globales.pireScore = partiePireScore
            }
        }

        // CALCUL DES STATISTIQUES NORMALISEES
        val nbJoueurs = joueurs.size
        var gainMax = 0
        var gainMin = 0
        val gainsTotaux = mutableListOf<Int>()
        val gainsMoyensParDonne = mutableListOf<Double>()

        // Calcul des gains nets et normalisation par nombre de donnes
        for ((nomJoueur, dataJoueur) in joueurs) {
            // Gain net total
            val gainNet = dataJoueur.pointsGagnes + dataJoueur.pointsPerdus
            dataJoueur.gainNet = gainNet

            // Nombre total de donnes jouees
            dataJoueur.totalDonnes = dataJoueur.preneur + dataJoueur.appele + dataJoueur.defense

            // Gain moyen par donne (normalisation)
            if (dataJoueur.totalDonnes > 0) {
                dataJoueur.gainMoyenParDonne = gainNet.toDouble() / dataJoueur.totalDonnes
                gainsMoyensParDonne.add(dataJoueur.gainMoyenParDonne)
            } else {
                dataJoueur.gainMoyenParDonne = 0.0
            }

            // Min/Max gains totaux
            if (gainNet > gainMax) {
                gainMax = gainNet
            }

            if (gainNet < gainMin) {
                gainMin = gainNet
            }

            gainsTotaux.add(gainNet)
        }

        // Tri pour les medianes et deciles
        gainsTotaux.sort()
        gainsMoyensParDonne.sort()
        val nbJoueursActifs = gainsMoyensParDonne.size

        // Calcul des medianes
        val medianeTotale = if (gainsTotaux.isNotEmpty()) {
            if (gainsTotaux.size % 2 == 1) {
                gainsTotaux[(gainsTotaux.size - 1) / 2]
            } else {
                gainsTotaux[gainsTotaux.size / 2]
            }
        } else {
            0
        }

        val medianeMoyenne = if (nbJoueursActifs > 0) {
            if (nbJoueursActifs % 2 == 1) {
                gainsMoyensParDonne[(nbJoueursActifs - 1) / 2]
            } else {
                gainsMoyensParDonne[nbJoueursActifs / 2]
            }
        } else {
            0.0
        }

        // Statistiques globales
        globales.gainMin = gainMin
        globales.gainMax = gainMax
        globales.mediane = medianeTotale
        globales.gainMoyenMin = if (gainsMoyensParDonne.isNotEmpty()) gainsMoyensParDonne.minOrNull() ?: 0.0 else 0.0
        globales.gainMoyenMax = if (gainsMoyensParDonne.isNotEmpty()) gainsMoyensParDonne.maxOrNull() ?: 0.0 else 0.0
        globales.medianeMoyenne = medianeMoyenne

        // Calcul des deciles sur les DEUX metriques
        if (nbJoueurs > 1) {
            // Deciles gains totaux
            val aTotal = kotlin.math.abs(gainsTotaux[0])
            val bTotal = kotlin.math.abs(gainsTotaux[gainsTotaux.size - 1])

            // Deciles gains moyens
            val aMoyen = if (gainsMoyensParDonne.isNotEmpty()) kotlin.math.abs(gainsMoyensParDonne[0]) else 0.0
            val bMoyen = if (gainsMoyensParDonne.isNotEmpty()) kotlin.math.abs(gainsMoyensParDonne[gainsMoyensParDonne.size - 1]) else 0.0

            for (dataJoueur in joueurs.values) {
                if (dataJoueur.totalDonnes > 0) {
                    // Medianes
                    dataJoueur.gainMediane = medianeTotale
                    dataJoueur.gainMoyenMediane = medianeMoyenne

                    // Deciles gains totaux
                    if (aTotal + bTotal > 0) {
                        dataJoueur.decile = (10 * (kotlin.math.abs(aTotal + dataJoueur.gainNet).toDouble() / (aTotal + bTotal))).toInt()
                    } else {
                        dataJoueur.decile = 5
                    }

                    // Deciles gains moyens
                    if (aMoyen + bMoyen > 0) {
                        dataJoueur.decileMoyen = (10 * (kotlin.math.abs(aMoyen + dataJoueur.gainMoyenParDonne) / (aMoyen + bMoyen))).toInt()
                    } else {
                        dataJoueur.decileMoyen = 5
                    }
                } else {
                    dataJoueur.gainMediane = medianeTotale
                    dataJoueur.gainMoyenMediane = medianeMoyenne
                    dataJoueur.decile = 5
                    dataJoueur.decileMoyen = 5
                }
            }
        } else {
            // Cas ou il n'y a qu'un seul joueur
            for (dataJoueur in joueurs.values) {
                dataJoueur.gainMediane = medianeTotale
                dataJoueur.gainMoyenMediane = medianeMoyenne
                dataJoueur.decile = 5
                dataJoueur.decileMoyen = 5
            }
        }

        // Conversion vers les classes immutables
        val joueursImmutables = joueurs.mapValues { (_, dataJoueur) ->
            StatistiquesJoueur(
                contrats = dataJoueur.contrats.toList(),
                poignees = dataJoueur.poignees.toList(),
                chelems = dataJoueur.chelems.toList(),
                miseres = dataJoueur.miseres,
                petitAuBoutGagne = dataJoueur.petitAuBoutGagne,
                petitAuBoutPerdu = dataJoueur.petitAuBoutPerdu,
                preneur = dataJoueur.preneur,
                appele = dataJoueur.appele,
                defense = dataJoueur.defense,
                totalDonnes = dataJoueur.totalDonnes,
                totalParties = dataJoueur.totalParties, // NOUVEAU
                pointsGagnes = dataJoueur.pointsGagnes,
                pointsPerdus = dataJoueur.pointsPerdus,
                meilleurScore = dataJoueur.meilleurScore,
                pireScore = dataJoueur.pireScore,
                gainNet = dataJoueur.gainNet,
                gainMoyenParDonne = dataJoueur.gainMoyenParDonne,
                gainMediane = dataJoueur.gainMediane,
                gainMoyenMediane = dataJoueur.gainMoyenMediane,
                decile = dataJoueur.decile,
                decileMoyen = dataJoueur.decileMoyen
            )
        }

        val globalesImmutables = StatistiquesGlobales(
            nbDonnes = globales.nbDonnes,
            attaqueNbBouts = globales.attaqueNbBouts,
            petitAuBoutGagne = globales.petitAuBoutGagne,
            petitAuBoutPerdu = globales.petitAuBoutPerdu,
            miseres = globales.miseres,
            pointsGagnes = globales.pointsGagnes,
            meilleurScore = globales.meilleurScore,
            pireScore = globales.pireScore,
            contrats = globales.contrats.toList(),
            poignees = globales.poignees.toList(),
            chelems = globales.chelems.toList(),
            gainMin = globales.gainMin,
            gainMax = globales.gainMax,
            mediane = globales.mediane,
            gainMoyenMin = globales.gainMoyenMin,
            gainMoyenMax = globales.gainMoyenMax,
            medianeMoyenne = globales.medianeMoyenne
        )

        return Triple(joueursImmutables, parties, globalesImmutables)
    }

    // Classes mutables pour les calculs intermediaires
    private data class MutableStatistiquesJoueur(
        val contrats: MutableList<Int> = MutableList(4) { 0 },
        val poignees: MutableList<Int> = MutableList(3) { 0 },
        val chelems: MutableList<Int> = MutableList(3) { 0 },
        var miseres: Int = 0,
        var petitAuBoutGagne: Int = 0,
        var petitAuBoutPerdu: Int = 0,
        var preneur: Int = 0,
        var appele: Int = 0,
        var defense: Int = 0,
        var totalDonnes: Int = 0,
        var totalParties: Int = 0,
        var pointsGagnes: Int = 0,
        var pointsPerdus: Int = 0,
        var meilleurScore: Int = 0,
        var pireScore: Int = 0,
        var gainNet: Int = 0,
        var gainMoyenParDonne: Double = 0.0,
        var gainMediane: Int = 0,
        var gainMoyenMediane: Double = 0.0,
        var decile: Int = 5,
        var decileMoyen: Int = 5
    )

    private data class MutableStatistiquesGlobales(
        var nbDonnes: Int = 0,
        var attaqueNbBouts: Int = 0,
        var petitAuBoutGagne: Int = 0,
        var petitAuBoutPerdu: Int = 0,
        var miseres: Int = 0,
        var pointsGagnes: Int = 0,
        var meilleurScore: Int = 0,
        var pireScore: Int = 0,
        val contrats: MutableList<Int> = MutableList(4) { 0 },
        val poignees: MutableList<Int> = MutableList(3) { 0 },
        val chelems: MutableList<Int> = MutableList(3) { 0 },
        var gainMin: Int = 0,
        var gainMax: Int = 0,
        var mediane: Int = 0,
        var gainMoyenMin: Double = 0.0,
        var gainMoyenMax: Double = 0.0,
        var medianeMoyenne: Double = 0.0
    )
}

            // Calcul des deciles sur les DEUX metriques
