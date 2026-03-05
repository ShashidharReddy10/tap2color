package com.squasre.tap2color.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.squasre.tap2color.data.DrawingTemplate
import com.squasre.tap2color.svg.SvgParser
import com.squasre.tap2color.svg.SvgRegion

enum class BrushType {
    NORMAL, SPARKLE
}

class ColoringViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("tap2color_prefs", Context.MODE_PRIVATE)
    
    var currentTemplate: DrawingTemplate? = null
        private set

    var regions = mutableListOf<SvgRegion>()
        private set

    val regionColors = mutableStateMapOf<String, Color>()
    val regionBrushes = mutableStateMapOf<String, BrushType>()
    
    val customColors = mutableStateListOf<Color>()
    
    private val history = mutableStateListOf<Pair<Map<String, Color>, Map<String, BrushType>>>()

    var selectedBrushByPremium by mutableStateOf(BrushType.NORMAL)

    var isPremiumUnlocked by mutableStateOf(false)
        private set

    val isCompleted by derivedStateOf {
        regionColors.isNotEmpty() && regionColors.values.all { it != Color.White }
    }

    init {
        loadCustomColors()
        isPremiumUnlocked = prefs.getBoolean("premium_unlocked", false)
    }

    fun loadTemplate(template: DrawingTemplate) {
        if (currentTemplate?.id == template.id) return
        
        currentTemplate = template
        regions = SvgParser.parse(template.svgContent).toMutableList()
        regionColors.clear()
        regionBrushes.clear()
        history.clear()
        
        // Try to load saved progress
        val savedProgress = loadProgress(template.id)
        if (savedProgress.first.isNotEmpty()) {
            regionColors.putAll(savedProgress.first)
            regionBrushes.putAll(savedProgress.second)
        } else {
            regions.forEach { 
                regionColors[it.id] = Color.White
                regionBrushes[it.id] = BrushType.NORMAL
            }
        }
    }

    fun isTemplateCompleted(template: DrawingTemplate): Boolean {
        val regions = SvgParser.parse(template.svgContent)
        if (regions.isEmpty()) return false
        return regions.all { region ->
            val key = "progress_${template.id}_${region.id}"
            prefs.contains(key) && prefs.getInt(key, Color.White.toArgb()) != Color.White.toArgb()
        }
    }

    fun colorRegion(regionId: String, color: Color, brushType: BrushType) {
        if (regionColors[regionId] == color && regionBrushes[regionId] == brushType) return
        
        saveToHistory()
        regionColors[regionId] = color
        regionBrushes[regionId] = brushType
        saveProgress()
    }

    private fun saveToHistory() {
        history.add(regionColors.toMap() to regionBrushes.toMap())
        if (history.size > 20) {
            history.removeAt(0)
        }
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val (lastColors, lastBrushes) = history.removeAt(history.size - 1)
            regionColors.clear()
            regionColors.putAll(lastColors)
            regionBrushes.clear()
            regionBrushes.putAll(lastBrushes)
            saveProgress()
        }
    }

    fun reset() {
        saveToHistory()
        regionColors.keys.forEach { 
            regionColors[it] = Color.White 
            regionBrushes[it] = BrushType.NORMAL
        }
        saveProgress()
    }

    fun addCustomColor(color: Color) {
        if (color !in customColors) {
            customColors.add(0, color)
            saveCustomColors()
        }
    }

    fun unlockPremium() {
        isPremiumUnlocked = true
        prefs.edit { putBoolean("premium_unlocked", true) }
    }

    fun lockPremium() {
        isPremiumUnlocked = false
        prefs.edit { putBoolean("premium_unlocked", false) }
    }

    private fun saveProgress() {
        val templateId = currentTemplate?.id ?: return
        prefs.edit {
            regionColors.forEach { (id, color) ->
                putInt("progress_${templateId}_$id", color.toArgb())
            }
            regionBrushes.forEach { (id, brush) ->
                putString("brush_${templateId}_$id", brush.name)
            }
        }
    }

    private fun loadProgress(templateId: String): Pair<Map<String, Color>, Map<String, BrushType>> {
        val colors = mutableMapOf<String, Color>()
        val brushes = mutableMapOf<String, BrushType>()
        regions.forEach { region ->
            val colorKey = "progress_${templateId}_${region.id}"
            if (prefs.contains(colorKey)) {
                val argb = prefs.getInt(colorKey, Color.White.toArgb())
                colors[region.id] = Color(argb)
            }
            val brushKey = "brush_${templateId}_${region.id}"
            if (prefs.contains(brushKey)) {
                val brushName = prefs.getString(brushKey, BrushType.NORMAL.name)
                brushes[region.id] = BrushType.valueOf(brushName ?: BrushType.NORMAL.name)
            }
        }
        return colors to brushes
    }

    private fun saveCustomColors() {
        val colorStrings = customColors.map { it.toArgb().toString() }.toSet()
        prefs.edit {
            putStringSet("custom_colors", colorStrings)
        }
    }

    private fun loadCustomColors() {
        val colorStrings = prefs.getStringSet("custom_colors", emptySet()) ?: emptySet()
        customColors.clear()
        colorStrings.forEach { 
            try {
                customColors.add(Color(it.toLong().toInt()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
