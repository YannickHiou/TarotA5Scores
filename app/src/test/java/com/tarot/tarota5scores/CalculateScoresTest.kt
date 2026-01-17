package com.tarot.tarota5scores

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateScoresTest {
    val constantes = ConstantesConfig(
        seuils_bouts = listOf(56, 51, 41, 36),
        multiplicateurs = mapOf(
            "Petite" to 1,
            "Garde" to 2,
            "GardeSans" to 4,
            "GardeContre" to 6,
            "Chelem" to 6
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
        ),
        poignee_atouts = mapOf(
            "SIMPLE" to 8,
            "DOUBLE" to 10,
            "TRIPLE" to 13
        )

    )

    val joueurs = listOf("A", "B", "C", "D", "E")


    @Test
    fun A_BCDE_paspetitaubout_poignee_misere_gagnee() {
        val preneur = "A"
        val appelle = "A"
        val contrat = "Garde"
        val nbBoutsAttaque = 3
        val pointsAtq = 82
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = mapOf("C" to PoigneeType.SIMPLE)


        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(638, -172, -122, -172, -172)
        assertEquals(expected, result)
    }


    @Test
    fun AB_CDE_paspetitaubout_paspoignee_pasmisere_gagnee() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(136, 68, -68, -68, -68)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_paspetitaubout_paspoignee_pasmisere_perdue() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-144, -72, 72, 72, 72)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_petitauboutgagne_paspoignee_pasmisere_gagne() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = true)
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(176, 88, -88, -88, -88)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_petitauboutperdu_paspoignee_pasmisere_gagne() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = false)
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(96, 48, -48, -48, -48)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_petitauboutgagne_paspoignee_pasmisere_perdu() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = true)
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-104, -52, 52, 52, 52)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_petitauboutperdu_paspoignee_pasmisere_perdu() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = false)
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-184, -92, 92, 92, 92)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_paspetitaubout_poigneeattaque_pasmisere_gagnee() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = mapOf("A" to PoigneeType.SIMPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(176, 88, -88, -88, -88)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_paspetitaubout_poigneedefence_pasmisere_gagnee() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = mapOf("C" to PoigneeType.SIMPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(176, 88, -88, -88, -88)
        assertEquals(expected, result)
    }

    ///
    @Test
    fun AB_CDE_paspetitaubout_poigneeattaque_pasmisere_perdue() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = mapOf("A" to PoigneeType.SIMPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-184, -92, 92, 92, 92)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_paspetitaubout_poigneedefence_pasmisere_perdue() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = emptyList<String>()
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = mapOf("C" to PoigneeType.SIMPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-184, -92, 92, 92, 92)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_paspetitaubout_paspoignee_misere_gagnee() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = listOf("B", "E")
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(116, 98, -88, -88, -38)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_paspetitaubout_paspoignee_misere_perdue() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = null
        val poignees = emptyMap<String, PoigneeType>()

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-154, -82, 112, 62, 62)
        assertEquals(expected, result)
    }

    @Test
    fun A_BCDE_petitauboutgagne_poignee_misere_gagnee() {
        val preneur = "A"
        val appelle = "A"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = true)
        val poignees = mapOf("A" to PoigneeType.TRIPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(502, -138, -88, -138, -138)
        assertEquals(expected, result)
    }

    @Test
    fun A_BCDE_petitauboutgagne_poignee_misere_perdue() {
        val preneur = "A"
        val appelle = "A"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = true)
        val poignees = mapOf("A" to PoigneeType.TRIPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-378, 82, 132, 82, 82)
        assertEquals(expected, result)
    }

    @Test
    fun A_BCDE_petitauboutperdu_poignee_misere_gagnee() {
        val preneur = "A"
        val appelle = "A"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 50
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = false)
        val poignees = mapOf("A" to PoigneeType.TRIPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(342, -98, -48, -98, -98)
        assertEquals(expected, result)
    }

    @Test
    fun A_BCDE_petitauboutperdu_poignee_misere_perdue() {
        val preneur = "A"
        val appelle = "A"
        val contrat = "Garde"
        val nbBoutsAttaque = 2
        val pointsAtq = 30
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = false)
        val poignees = mapOf("A" to PoigneeType.TRIPLE)

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = null,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-538, 122, 172, 122, 122)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_chelem_pasannonce_petitauboutgagne_poignee_misere() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "GardeSans"
        val nbBoutsAttaque = 3
        val pointsAtq = 91
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = true)
        val poignees = mapOf("A" to PoigneeType.TRIPLE)

        val chelemArg = Chelem(
            annonce = false,
            succes = true,
        )

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(1190, 590, -560, -610, -610)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_chelem_annoncegagne_petitauboutgagne_poignee_misere() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Chelem"
        val nbBoutsAttaque = 3
        val pointsAtq = 91
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = true)
        val poignees = mapOf("A" to PoigneeType.TRIPLE)

        val chelemArg = Chelem(
            annonce = true,
            succes = true,
        )

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(1290, 640, -610, -660, -660)
        assertEquals(expected, result)
    }

    @Test
    fun AB_CDE_chelem_annonceperdu_petitauboutperdu_poignee_misere_perdu() {
        val preneur = "A"
        val appelle = "B"
        val contrat = "Chelem"
        val nbBoutsAttaque = 3
        val pointsAtq = 35
        val miseres = listOf("C")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = false)
        val poignees = mapOf("A" to PoigneeType.SIMPLE)

        val chelemArg = Chelem(
            annonce = true,
            succes = false,
        )

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(-870, -440, 470, 420, 420)
        assertEquals(expected, result)
    }

    @Test
    fun A_BCDE_chelem_annoncegagne_petitauboutgagne_poignee_misere() {
        val preneur = "A"
        val appelle = "A"
        val contrat = "Chelem"
        val nbBoutsAttaque = 3
        val pointsAtq = 91
        val miseres = listOf("C", "D", "E")
        val petitAuBout: PetitAuBoutIndex? = PetitAuBoutIndex(index = 0, gagne = true)
        val poignees = mapOf("A" to PoigneeType.TRIPLE)

        val chelemArg = Chelem(
            annonce = true,
            succes = true,
        )

        val result = calculateScores(
            joueurs = joueurs,
            preneur = preneur,
            appelle = appelle,
            contrat = contrat,
            pointsAtq = pointsAtq,
            nbBoutsAttaque = nbBoutsAttaque,
            miseres = miseres,
            petitAuBout = petitAuBout,
            poignees = poignees,
            chelem = chelemArg,
            constantes = constantes,
            poigneeValues = constantes.poignee_values
        )

        val expected = listOf(2570, -680, -630, -630, -630)
        assertEquals(expected, result)
    }
}
