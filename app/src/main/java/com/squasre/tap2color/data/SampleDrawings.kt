package com.squasre.tap2color.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DrawingTemplate(
    val id: String,
    val name: String,
    val category: String,
    val svgContent: String
)

object SampleDrawings {
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    var all by mutableStateOf<List<DrawingTemplate>>(emptyList())
        private set
        
    var availableCategories by mutableStateOf<List<String>>(listOf("All"))
        private set

    fun load(context: Context) {
        if (all.isNotEmpty()) return
        try {
            val jsonString = context.assets.open("drawings.json").bufferedReader().use { it.readText() }
            val loadedDrawings = json.decodeFromString<List<DrawingTemplate>>(jsonString)
            if (loadedDrawings.isNotEmpty()) {
                all = loadedDrawings
                availableCategories = listOf("All") + loadedDrawings.map { it.category }.distinct().sorted()
            } else {
                loadFallback()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadFallback()
        }
    }

    private fun loadFallback() {
        all = listOf(
            DrawingTemplate("sun", "Sun", "NATURE", "<svg viewBox=\"0 0 300 300\"><circle id=\"core\" cx=\"150\" cy=\"150\" r=\"60\" fill=\"#FFFFFF\" stroke=\"#000\" stroke-width=\"4\"/></svg>")
        )
        availableCategories = listOf("All", "NATURE")
    }
}
