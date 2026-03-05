package com.squasre.tap2color.viewmodel

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
    
    private val history = mutableStateListOf<Map<String, Color>>()

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
        
        // Save current state to history before changing
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
}
