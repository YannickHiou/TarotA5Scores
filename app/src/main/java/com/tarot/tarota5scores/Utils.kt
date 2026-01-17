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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

fun loadOrCreateJoueurs(context: Context): List<Joueur> {
    val file = File(context.filesDir, "joueurs.json")
    return if (file.exists()) {
        try {
            val jsonArray = JSONArray(file.readText())
            List(jsonArray.length()) { index ->
                val obj = jsonArray.getJSONObject(index)
                val id = obj.optString("id", "")
                val nom = obj.optString("nom", "")
                Joueur(id = id, nom = nom)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    } else {
        val defaultJoueurs = getDefaultJoueurs() // contient des UUID explicites
        saveJoueurs(context, defaultJoueurs)
        defaultJoueurs
    }
}

fun saveJoueurs(context: Context, joueurs: List<Joueur>) {
    val file = File(context.filesDir, "joueurs.json")
    try {
        val jsonArray = JSONArray()
        joueurs.forEach { joueur ->
            val obj = JSONObject()
            obj.put("id", joueur.id)
            obj.put("nom", joueur.nom)
            jsonArray.put(obj)
        }
        file.writeText(jsonArray.toString(2))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun getDefaultJoueurs(): List<Joueur> {
    // Liste initiale avec UUID explicites (tu avais validé ces UUID)
    return listOf(
        Joueur("0a1b2c3d-4e5f-4a6b-8c7d-90ab12cd34ef", "Yannick"),
        Joueur("cc6f04e7-c611-4b25-9e3f-2a1b3c4d5e6f", "Christine"),
        Joueur("f47ac10b-58cc-4f15-8a2b-7d6c5b4a3920", "Aurélie"),
        Joueur("a3d9b2e1-77c4-4d99-8f12-0c1d2e3f4a5b", "Alexis"),
        Joueur("9b8a7c6d-1234-4f00-9abc-deadbeef00ff", "Camille"),
        Joueur("1f2e3d4c-5b6a-4c3b-8d7e-6f5a4b3c2d1e", "Laïla"),
        Joueur("e2d3c4b5-a6f7-4b8c-9d0e-112233445566", "Christophe"),
        Joueur("3c2b1a0f-9e8d-4821-8abc-ffeeccddee11", "Florence"),
        Joueur("7a6b5c4d-3e2f-4a1b-9f8e-0a1b2c3d4e5f", "Clémence"),
        Joueur("b1c2d3e4-f5a6-4f7e-8d9c-abcdef012345", "Arthur"),
        Joueur("d4c3b2a1-0f1e-4d2c-8b7a-1234abcd5678", "Eloïse"),
        Joueur("5f6e7d8c-9a0b-4c1d-9b8a-fedcba987654", "Martin"),
        Joueur("c1d2e3f4-5678-4031-8c9d-0f1e2d3c4b5a", "Valérie"),
        Joueur("8f7e6d5c-4b3a-4a9b-9c8d-7e6f5a4b3c2d", "Benjamin"),
        Joueur("2b3c4d5e-6f70-4f81-8a9b-0c0d0e0f1a2b", "Armand"),
        Joueur("6a5b4c3d-2e1f-47b2-9a8b-334455667788", "Alexis G")
    )
}

@Serializable
data class ChelemConfig(
    val annonce_reussi: Int,
    val non_annonce_reussi: Int,
    val annonce_rate: Int
)

@Serializable
data class ConstantesConfig(
    val seuils_bouts: List<Int>,
    val multiplicateurs: Map<String, Int>,
    val petit_au_bout: Int,
    val chelem: ChelemConfig,
    val misere_penalite: Int = 10,
    val base_const: Int,
    val poignee_values: Map<String, Int>,
    val poignee_atouts: Map<String, Int>
)

fun loadConstantesFromAssets(
    context: Context,
    filename: String = "constantes.json"
): ConstantesConfig {
    val jsonText = context.assets.open(filename).bufferedReader().use { it.readText() }
    return Json.decodeFromString(ConstantesConfig.serializer(), jsonText)
}
