package com.squasre.tap2color.ui

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF as AndroidRectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squasre.tap2color.data.DrawingCategory
import com.squasre.tap2color.data.DrawingTemplate
import com.squasre.tap2color.data.SampleDrawings
import com.squasre.tap2color.export.ImageExporter
import com.squasre.tap2color.svg.SvgParser
import com.squasre.tap2color.svg.SvgRegion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(onBack: () -> Unit, onDrawingSelected: (DrawingTemplate) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tap2color_prefs", android.content.Context.MODE_PRIVATE) }
    var selectedCategory by remember { mutableStateOf(DrawingCategory.ALL) }
    
    val coloredDrawings = remember {
        SampleDrawings.all.filter { template ->
            val regions = SvgParser.parse(template.svgContent)
            regions.any { region ->
                val key = "progress_${template.id}_${region.id}"
                prefs.contains(key) && prefs.getInt(key, Color.White.toArgb()) != Color.White.toArgb()
            }
        }
    }

    val filteredDrawings = remember(selectedCategory, coloredDrawings) {
        if (selectedCategory == DrawingCategory.ALL) {
            coloredDrawings
        } else {
            coloredDrawings.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Art Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (filteredDrawings.isNotEmpty()) {
                        IconButton(onClick = {
                            // Only share the top 10 most recent/filtered drawings to keep the image clean
                            val drawingsToShare = filteredDrawings.take(10)
                            val bitmap = captureGalleryBitmap(drawingsToShare, selectedCategory.displayName, prefs)
                            ImageExporter.shareBitmap(context, bitmap)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Gallery")
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (coloredDrawings.isNotEmpty()) {
                CategoryFilterBar(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            if (filteredDrawings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎨", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        val message = if (coloredDrawings.isEmpty()) "Your gallery is empty!" else "No drawings in this category yet!"
                        Text(message, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (coloredDrawings.isEmpty()) {
                            Text("Start coloring to see your art here.", color = Color.Gray)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDrawings) { template ->
                        GalleryCard(template, prefs) {
                            onDrawingSelected(template)
                        }
                    }
                }
            }
        }
    }
}

private fun captureGalleryBitmap(
    drawings: List<DrawingTemplate>, 
    categoryName: String,
    prefs: android.content.SharedPreferences
): Bitmap {
    val cols = 2
    val rows = (drawings.size + 1) / cols
    val itemSize = 500f
    val padding = 40f
    val headerHeight = 150f
    
    val width = (cols * itemSize + (cols + 1) * padding).toInt()
    val height = (headerHeight + rows * itemSize + (rows + 1) * padding).toInt()
    
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(android.graphics.Color.parseColor("#F8F9FA"))
    
    val titlePaint = AndroidPaint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#6200EE")
        textSize = 60f
        textAlign = AndroidPaint.Align.CENTER
        isFakeBoldText = true
    }
    
    canvas.drawText("My Tap2Color Art: $categoryName", width / 2f, headerHeight * 0.7f, titlePaint)

    val fillPaint = AndroidPaint().apply { isAntiAlias = true; style = AndroidPaint.Style.FILL }
    val strokePaint = AndroidPaint().apply { 
        isAntiAlias = true
        style = AndroidPaint.Style.STROKE
        color = android.graphics.Color.BLACK
        strokeWidth = 2f 
    }
    val textPaint = AndroidPaint().apply {
        isAntiAlias = true
        color = android.graphics.Color.BLACK
        textSize = 36f
        textAlign = AndroidPaint.Align.CENTER
        isFakeBoldText = true
    }

    drawings.forEachIndexed { index, template ->
        val col = index % cols
        val row = index / cols
        
        val left = padding + col * (itemSize + padding)
        val top = headerHeight + padding + row * (itemSize + padding)
        
        // Draw card background
        val cardPaint = AndroidPaint().apply { color = android.graphics.Color.WHITE; style = AndroidPaint.Style.FILL }
        canvas.drawRoundRect(left, top, left + itemSize, top + itemSize, 40f, 40f, cardPaint)

        // Draw SVG content
        val regions = SvgParser.parse(template.svgContent)
        val viewBox = SvgParser.parseViewBox(template.svgContent)
        
        val drawingAreaSize = itemSize * 0.7f
        val drawingLeft = left + (itemSize - drawingAreaSize) / 2
        val drawingTop = top + (itemSize - drawingAreaSize) / 2 - 20
        
        val scale = drawingAreaSize / maxOf(viewBox.width, viewBox.height)
        
        regions.forEach { region ->
            val key = "progress_${template.id}_${region.id}"
            val colorArgb = prefs.getInt(key, android.graphics.Color.WHITE)
            fillPaint.color = colorArgb
            
            val path = region.path.asAndroidPath()
            canvas.save()
            canvas.translate(drawingLeft, drawingTop)
            canvas.scale(scale, scale)
            canvas.drawPath(path, fillPaint)
            canvas.drawPath(path, strokePaint)
            canvas.restore()
        }
        
        canvas.drawText(template.name, left + itemSize / 2f, top + itemSize - 40f, textPaint)
    }
    
    return bitmap
}

@Composable
fun GalleryCard(template: DrawingTemplate, prefs: android.content.SharedPreferences, onClick: () -> Unit) {
    val regions = remember(template) { SvgParser.parse(template.svgContent) }
    val viewBox = remember(template) { SvgParser.parseViewBox(template.svgContent) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / viewBox.width
                    val scaleY = size.height / viewBox.height
                    val scale = minOf(scaleX, scaleY)
                    val offsetX = (size.width - viewBox.width * scale) / 2
                    val offsetY = (size.height - viewBox.height * scale) / 2

                    withTransform({
                        translate(offsetX, offsetY)
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        regions.forEach { region ->
                            val key = "progress_${template.id}_${region.id}"
                            val colorArgb = prefs.getInt(key, Color.White.toArgb())
                            val color = Color(colorArgb)
                            
                            drawPath(path = region.path, color = color)
                            drawPath(path = region.path, color = Color.Black, style = Stroke(width = 2f / scale))
                        }
                    }
                }
            }
            Text(
                text = template.name,
                modifier = Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

private fun Color.toArgb(): Int {
    return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
           ((this.red * 255.0f + 0.5f).toInt() shl 16) or
           ((this.green * 255.0f + 0.5f).toInt() shl 8) or
           (this.blue * 255.0f + 0.5f).toInt()
}
