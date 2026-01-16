package com.example.tarota5scores


private fun trace(tag: String, msg: String) = println("$tag $msg")

// ---------------------------------------------
// Fonctions utilitaires
// ---------------------------------------------

private fun computeDistributionWeights(
    joueurs: List<String>,
    preneurIdx: Int,
    appelleIdx: Int
): List<Int> {
    val n = joueurs.size
    val weights = MutableList(n) { 0 }
    if (preneurIdx == appelleIdx) {
        // preneur seul : preneur = n-1 (ex: 4), autres = -1
        for (i in 0 until n) weights[i] = -1
        weights[preneurIdx] = n - 1
    } else {
        // standard : preneur=2, appelle=1, autres=-1
        for (i in 0 until n) weights[i] = -1
        weights[preneurIdx] = 2
        weights[appelleIdx] = 1
    }
    return weights
}

private fun applyMiseres(
    miseres: List<String>,
    joueurs: List<String>,
    scores: MutableList<Int>,
    constantes: ConstantesConfig,
    TAG: String
) {
    if (miseres.isEmpty()) {
        trace(TAG, "Pas de miseres déclarées.")
        return
    }

    val n = joueurs.size
    val perOther = constantes.misere_penalite
    val gainDeclarant = perOther * (n - 1) // ex: n=5 -> 10 * 4 = 40

    for (decl in miseres) {
        val idx = joueurs.indexOf(decl)
        if (idx < 0) {
            trace(TAG, "Misere déclarée par '$decl' non trouvée -> ignorée")
            continue
        }

        // appliquons la perte aux autres
        for (i in joueurs.indices) {
            if (i == idx) continue
            scores[i] -= perOther
        }
        // et le gain total au déclarant
        scores[idx] += gainDeclarant

        trace(TAG, "Misere déclarée par ${decl} (index $idx) -> chaque autre -$perOther, declarant +$gainDeclarant")
    }
    trace(TAG, "Scores après application miseres: $scores")
}

///////////////////////////

fun calculateScores(
    joueurs: List<String>,
    preneur: String,
    appelle: String,
    contrat: String,
    pointsAtq: Int,
    nbBoutsAttaque: Int,
    miseres: List<String>,
    petitAuBout: PetitAuBoutIndex?,
    chelem: Chelem?,
    poignees: Map<String, PoigneeType>,
    constantes: ConstantesConfig,
    poigneeValues: Map<String, Int>
): List<Int> {
    val TAG = "SCORE_CALC"
    trace(TAG, "🔥 VERSION CORRIGEE - TEST")
    trace(TAG, "--- NOUVEAU CALCUL DE SCORE ---")
    trace(TAG, "Joueurs: $joueurs")
    trace(TAG, "Preneur: $preneur, Appelé: $appelle, Contrat: $contrat")
    trace(TAG, "Points Attaque: $pointsAtq")
    trace(TAG, "Bouts Attaque (entrée): $nbBoutsAttaque")
    trace(TAG, "PetitAuBout (entrée): $petitAuBout")
    trace(TAG, "Chelem (entrée): $chelem")
    trace(TAG, "Poignees (entrée): $poignees")
    trace(TAG, "Miseres (entrée): $miseres")

    require(joueurs.size == 5) { "Il doit y avoir exactement 5 joueurs" }
    require(joueurs.contains(preneur) && joueurs.contains(appelle)) { "Preneur/appelé non trouvés dans la liste des joueurs" }

    val scores = MutableList(joueurs.size) { 0 }
    val idxPreneur = joueurs.indexOf(preneur)
    val idxAppelle = joueurs.indexOf(appelle)

    //  CORRECTION : Normaliser tous les types de chelems
    val contratEffective = when {
        contrat.contains("Chelem", ignoreCase = true) -> {
            trace(TAG, "Chelem détecté dans le contrat '$contrat', normalisation vers 'Chelem'")
            "Chelem"
        }
        else -> contrat
    }

    val multiplicateur = constantes.multiplicateurs[contratEffective]
        ?: constantes.multiplicateurs["Garde"]
        ?: 2

    val nbBouts = nbBoutsAttaque.coerceIn(0, constantes.seuils_bouts.size - 1)
    val seuil = constantes.seuils_bouts[nbBouts]
    val delta = pointsAtq - seuil

    //  CORRECTION : Base fixe pour TOUS les chelems (annoncés ou non)
    val base = if (contratEffective.equals("Chelem", ignoreCase = true)) {
        constantes.base_const  // 25 points fixes pour tous les chelems
    } else {
        constantes.base_const + kotlin.math.abs(delta)  // Logique normale pour autres contrats
    }

    trace(TAG, "nbBouts utilisé: $nbBouts, seuil: $seuil")
    trace(TAG, "delta (pointsAtq - seuil): $delta")
    trace(TAG, "contrat original: $contrat")
    trace(TAG, "contratEffectif: $contratEffective")

    if (contratEffective.equals("Chelem", ignoreCase = true)) {
        trace(TAG, "base (chelem - fixe): ${constantes.base_const}")
    } else {
        trace(TAG, "base (base_const + |delta|): ${constantes.base_const} + ${kotlin.math.abs(delta)} = $base")
    }

    trace(TAG, "multiplicateur (contratEffectif='$contratEffective'): $multiplicateur")

    val valeur = base * multiplicateur
    trace(TAG, "valeur initiale (base * multiplicateur): $valeur")

    //  CORRECTION : Pour chelem, le signe dépend UNIQUEMENT du succès
    val signe = if (contratEffective.equals("Chelem", ignoreCase = true)) {
        if (chelem?.succes == true) 1 else -1
    } else {
        if (delta >= 0) 1 else -1
    }

    val totalManche = valeur * signe
    trace(TAG, "signe: $signe (chelem=${contratEffective.equals("Chelem", ignoreCase = true)}, succes=${chelem?.succes})")
    trace(TAG, "totalManche (valeur * signe): $totalManche")

    // Distribution initiale
    val weights = computeDistributionWeights(joueurs, idxPreneur, idxAppelle)
    for (i in joueurs.indices) {
        scores[i] = weights[i] * totalManche
    }
    trace(TAG, "Distribution initiale: $scores")

    // Petit au bout
    trace(TAG, "Avant petit au bout: $scores")
    applyPetitAuBout(
        joueurs,
        petitAuBout,
        weights,
        constantes,
        multiplicateur,
        idxPreneur,
        idxAppelle,
        scores,
        contratEffective,
        TAG
    )
    trace(TAG, "Après petit au bout: $scores")

    // Poignees
    trace(TAG, "Avant poignees: $scores")
    applyPoignees(
        joueurs = joueurs,
        poignees = poignees,
        idxPreneur = idxPreneur,
        idxAppelle = idxAppelle,
        poigneeValues = poigneeValues,
        weights = weights,
        scores = scores,
        contrat = contratEffective,
        TAG = TAG
    )
    trace(TAG, "Après poignees: $scores")

    // Miseres
    trace(TAG, "Avant miseres: $scores")
    applyMiseres(miseres, joueurs, scores, constantes, TAG)
    trace(TAG, "Après miseres: $scores")

    // Chelem
    trace(TAG, "Avant chelem: $scores")
    applyChelem(joueurs, chelem, idxPreneur, idxAppelle, constantes, weights, scores, TAG)
    trace(TAG, "Après chelem: $scores")

    // Correction finale pour obtenir somme nulle
    val sum = scores.sum()
    trace(TAG, "Somme des scores avant correction: $sum")
    if (sum != 0) {
        scores[idxPreneur] -= sum
        trace(TAG, "Correction appliquée au preneur (index $idxPreneur): nouvelle valeur ${scores[idxPreneur]}")
    }

    trace(TAG, "SCORES FINAUX: $scores")
    trace(TAG, "--- FIN DU CALCUL ---")
    return scores
}



//  Fonction applyPetitAuBout corrigée
private fun applyPetitAuBout(
    joueurs: List<String>,
    petitAuBout: PetitAuBoutIndex?,
    weights: List<Int>,
    constantes: ConstantesConfig,
    multiplicateur: Int,
    idxPreneur: Int,
    idxAppelle: Int,
    scores: MutableList<Int>,
    contrat: String,
    TAG: String
) {
    if (petitAuBout == null) {
        trace(TAG, "Pas de PetitAuBout.")
        return
    }

    val holderIdx = petitAuBout.index
    if (holderIdx < 0 || holderIdx >= joueurs.size) {
        trace(TAG, "PetitAuBout holder index '$holderIdx' invalide.")
        return
    }

    val bonusBase = constantes.petit_au_bout
    val bonusValue = bonusBase * multiplicateur
    val succesSign = if (petitAuBout.gagne) 1 else -1

    val holderIsAttaque = (holderIdx == idxPreneur || holderIdx == idxAppelle)

    //  CORRECTION : Logique différente selon le type de contrat
    val appliedWeights = if (contrat.equals("Chelem", ignoreCase = true)) {
        // Pour chelem : appliquer le signe du succès du petit au bout
        if (holderIsAttaque) {
            weights.map { it * succesSign }
        } else {
            weights.map { -it * succesSign }
        }
    } else {
        // Pour contrats normaux : logique originale
        val attaqueALePetit = holderIsAttaque
        val attaqueGagne = (attaqueALePetit && petitAuBout.gagne) || (!attaqueALePetit && !petitAuBout.gagne)

        if (attaqueGagne) weights else weights.map { -it }
    }

    trace(TAG, "PetitAuBout holder: '${joueurs[holderIdx]}' (index $holderIdx), succes=${petitAuBout.gagne}")
    trace(TAG, "PetitAuBout - bonusBase: $bonusBase, multiplicateur: $multiplicateur, bonusValue: $bonusValue")

    for (i in joueurs.indices) {
        scores[i] += appliedWeights[i] * bonusValue
    }
    trace(TAG, "Scores après application petit au bout: $scores")
}

//  Fonction applyPoignees corrigée
private fun applyPoignees(
    joueurs: List<String>,
    poignees: Map<String, PoigneeType>,
    idxPreneur: Int,
    idxAppelle: Int,
    poigneeValues: Map<String, Int>,
    weights: List<Int>,
    scores: MutableList<Int>,
    contrat: String,
    TAG: String
) {
    if (poignees.isEmpty()) {
        trace(TAG, "Pas de poignees déclarées.")
        return
    }

    for ((nom, type) in poignees) {
        val idx = joueurs.indexOf(nom)
        if (idx < 0) continue

        val base = poigneeValues[type.name] ?: continue

        //  CORRECTION : Ignorer les poignées NONE
        if (base == 0 || type.name == "NONE") {
            trace(TAG, "Poignee ${type.name} de $nom ignorée (valeur 0)")
            continue
        }

        val holderIsAttaque = (idx == idxPreneur || idx == idxAppelle)
        val appliedWeights = if (holderIsAttaque) weights else weights.map { -it }

        trace(TAG, "Poignee ${type.name} déclarée par ${nom} (index $idx): base=$base")

        for (i in joueurs.indices) {
            scores[i] += appliedWeights[i] * base
        }

        trace(TAG, "Scores après poignee ${type.name} de $nom: $scores")
    }
}

//  Fonction applyChelem corrigée
private fun applyChelem(
    joueurs: List<String>,
    chelem: Chelem?,
    idxPreneur: Int,
    idxAppelle: Int,
    constantes: ConstantesConfig,
    weights: List<Int>,
    scores: MutableList<Int>,
    TAG: String
) {
    if (chelem == null) {
        trace(TAG, "Pas de chelem déclaré.")
        return
    }

    val bonusBase = when {
        chelem.annonce && chelem.succes -> constantes.chelem.annonce_reussi
        !chelem.annonce && chelem.succes -> constantes.chelem.non_annonce_reussi
        chelem.annonce && !chelem.succes -> kotlin.math.abs(constantes.chelem.annonce_rate)
        else -> 0
    }

    if (bonusBase == 0) {
        trace(TAG, "Chelem déclaré mais montant calculé à 0 -> ignoré.")
        return
    }

    //  CORRECTION : Utiliser le succès du chelem pour déterminer le signe
    val finalWeights = if (chelem.succes) weights else weights.map { -it }

    trace(TAG, "Chelem: annonce=${chelem.annonce}, succes=${chelem.succes}, bonusBase=$bonusBase")

    for (i in joueurs.indices) {
        scores[i] += finalWeights[i] * bonusBase
    }

    trace(TAG, "Scores après application chelem: $scores")
}

fun normalizeOnlyAnnonceRate(constantes: ConstantesConfig): ConstantesConfig {
    val c = constantes.chelem
    val newAnnonceRate = if (c.annonce_rate < 0) -c.annonce_rate else c.annonce_rate
    val newChelem = c.copy(annonce_rate = newAnnonceRate)
    return constantes.copy(chelem = newChelem)
}