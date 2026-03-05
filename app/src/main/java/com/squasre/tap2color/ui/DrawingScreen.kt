package com.squasre.tap2color.ui

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.Region
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.squasre.tap2color.data.DrawingTemplate
import com.squasre.tap2color.export.ImageExporter
import com.squasre.tap2color.svg.SvgParser
import com.squasre.tap2color.viewmodel.ColoringViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    template: DrawingTemplate,
    viewModel: ColoringViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedColor by remember { mutableStateOf(Color.Red) }
    val viewBox = remember(template) { SvgParser.parseViewBox(template.svgContent) }
    
    var lastTappedRegion by remember { mutableStateOf<String?>(null) }
    var popTrigger by remember { mutableStateOf(0) }
    
    val popScale by animateFloatAsState(
        targetValue = if (popTrigger % 2 == 1) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        finishedListener = {
            if (popTrigger % 2 == 1) {
                popTrigger++
            }
        },
        label = "popScale"
    )

    LaunchedEffect(template) {
        viewModel.loadTemplate(template)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(template.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                    IconButton(onClick = {
                        val bitmap = ImageExporter.exportToBitmap(
                            viewModel.regions,
                            viewModel.regionColors,
                            viewBox
                        )
                        ImageExporter.shareBitmap(context, bitmap)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            ColorPalette(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            val regions = viewModel.regions
            val regionColors = viewModel.regionColors

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .pointerInput(template) {
                        detectTapGestures { offset ->
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val scaleX = canvasWidth / viewBox.width
                            val scaleY = canvasHeight / viewBox.height
                            val scale = minOf(scaleX, scaleY)
                            
                            val offsetX = (canvasWidth - viewBox.width * scale) / 2
                            val offsetY = (canvasHeight - viewBox.height * scale) / 2
                            
                            val svgX = (offset.x - offsetX) / scale
                            val svgY = (offset.y - offsetY) / scale

                            for (region in regions.reversed()) {
                                if (isPointInPath(svgX, svgY, region.path)) {
                                    viewModel.colorRegion(region.id, selectedColor)
                                    lastTappedRegion = region.id
                                    popTrigger++
                                    break
                                }
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val scaleX = canvasWidth / viewBox.width
                val scaleY = canvasHeight / viewBox.height
                val scale = minOf(scaleX, scaleY)

                val offsetX = (canvasWidth - viewBox.width * scale) / 2
                val offsetY = (canvasHeight - viewBox.height * scale) / 2

                withTransform({
                    translate(offsetX, offsetY)
                    scale(scale, scale, pivot = Offset.Zero)
                }) {
                    regions.forEach { region ->
                        val color = regionColors[region.id] ?: Color.White
                        val isLastTapped = lastTappedRegion == region.id
                        
                        if (isLastTapped && popScale != 1.0f) {
                            val rectF = RectF()
                            region.path.asAndroidPath().computeBounds(rectF, true)
                            val pivot = Offset(rectF.centerX(), rectF.centerY())
                            
                            withTransform({
                                scale(popScale, popScale, pivot = pivot)
                            }) {
                                drawRegion(region, color, scale)
                            }
                        } else {
                            drawRegion(region, color, scale)
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRegion(
    region: com.squasre.tap2color.svg.SvgRegion,
    color: Color,
    baseScale: Float
) {
    drawPath(
        path = region.path,
        color = color
    )
    drawPath(
        path = region.path,
        color = Color.Black,
        style = Stroke(width = 4f / baseScale)
    )
}

private fun isPointInPath(x: Float, y: Float, path: androidx.compose.ui.graphics.Path): Boolean {
    val androidPath = path.asAndroidPath()
    val rectF = RectF()
    androidPath.computeBounds(rectF, true)
    val region = Region()
    region.setPath(androidPath, Region(
        rectF.left.toInt(), rectF.top.toInt(),
        rectF.right.toInt(), rectF.bottom.toInt()
    ))
    return region.contains(x.toInt(), y.toInt())
}

@Composable
fun ColorPalette(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    val colors = listOf(
        Color.Red, Color.Blue, Color.Yellow, Color.Green,
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF9800), // Orange
        Color(0xFFFFC0CB), // Pink
        Color(0xFF795548), // Brown
        Color.Black, Color.Gray,
        Color.Cyan, Color(0xFFCDDC39) // Lime
    )

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == color) 4.dp else 2.dp,
                            color = if (selectedColor == color) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}
