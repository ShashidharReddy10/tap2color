package com.squasre.tap2color.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.squasre.tap2color.data.DrawingTemplate
import com.squasre.tap2color.svg.SvgParser
import com.squasre.tap2color.svg.SvgRegion

class ColoringViewModel : ViewModel() {
    var currentTemplate: DrawingTemplate? = null
        private set

    var regions = mutableListOf<SvgRegion>()
        private set

    val regionColors = mutableStateMapOf<String, Color>()
    
    val customColors = mutableStateListOf<Color>()
    
    private val history = mutableStateListOf<Map<String, Color>>()

    val isCompleted by derivedStateOf {
        regionColors.isNotEmpty() && regionColors.values.all { it != Color.White }
    }

    fun loadTemplate(template: DrawingTemplate) {
        if (currentTemplate?.id == template.id) return
        
        currentTemplate = template
        regions = SvgParser.parse(template.svgContent).toMutableList()
        regionColors.clear()
        history.clear()
        regions.forEach { 
            regionColors[it.id] = Color.White 
        }
    }

    fun colorRegion(regionId: String, color: Color) {
        if (regionColors[regionId] == color) return
        
        saveToHistory()
        regionColors[regionId] = color
    }

    private fun saveToHistory() {
        history.add(regionColors.toMap())
        if (history.size > 20) {
            history.removeAt(0)
        }
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val lastState = history.removeAt(history.size - 1)
            regionColors.clear()
            regionColors.putAll(lastState)
        }
    }

    fun reset() {
        saveToHistory()
        regionColors.keys.forEach { 
            regionColors[it] = Color.White 
        }
    }

    fun addCustomColor(color: Color) {
        if (color !in customColors) {
            customColors.add(0, color)
        }
    }
}
