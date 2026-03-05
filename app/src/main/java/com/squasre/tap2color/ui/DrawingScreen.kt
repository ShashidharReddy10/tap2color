package com.squasre.tap2color.ui

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.Region
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.squasre.tap2color.data.DrawingTemplate
import com.squasre.tap2color.export.ImageExporter
import com.squasre.tap2color.svg.SvgParser
import com.squasre.tap2color.viewmodel.ColoringViewModel
import kotlin.random.Random

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
    
    var showColorPicker by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }

    // Observe completion state
    LaunchedEffect(viewModel.isCompleted) {
        if (viewModel.isCompleted) {
            showCelebration = true
        } else {
            showCelebration = false
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    customColors = viewModel.customColors,
                    onColorSelected = { selectedColor = it },
                    onAddColorClick = { showColorPicker = true }
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

        // Celebration Overlay
        AnimatedVisibility(
            visible = showCelebration,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut()
        ) {
            CelebrationOverlay(onDismiss = { showCelebration = false })
        }
    }

    if (showColorPicker) {
        CustomColorPickerDialog(
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                viewModel.addCustomColor(color)
                selectedColor = color
                showColorPicker = false
            }
        )
    }
}

@Composable
fun CelebrationOverlay(onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "AMAZING!",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✨ Beautiful Colors! ✨",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Simple confetti effect using Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = Random(42)
            for (i in 0 until 50) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val color = Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
                
                withTransform({
                    rotate(rotation + i * 10, Offset(x, y))
                }) {
                    drawRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(15.dp.toPx(), 15.dp.toPx())
                    )
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
fun ColorPalette(
    selectedColor: Color,
    customColors: List<Color>,
    onColorSelected: (Color) -> Unit,
    onAddColorClick: () -> Unit
) {
    val presetColors = listOf(
        Color(0xFFFF5252), Color(0xFFFF4081), Color(0xFFE040FB), Color(0xFF7C4DFF),
        Color(0xFF536DFE), Color(0xFF448AFF), Color(0xFF40C4FF), Color(0xFF18FFFF),
        Color(0xFF64FFDA), Color(0xFF69F0AE), Color(0xFFB2FF59), Color(0xFFEEFF41),
        Color(0xFFFFFF00), Color(0xFFFFD740), Color(0xFFFFAB40), Color(0xFFFF6E40),
        Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B), Color.Black
    )

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = Color.White
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                        .border(2.dp, Color(0xFFDDDDDD), CircleShape)
                        .clickable { onAddColorClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Color",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            items(customColors) { color ->
                ColorCircle(color, selectedColor == color) { onColorSelected(color) }
            }

            items(presetColors) { color ->
                ColorCircle(color, selectedColor == color) { onColorSelected(color) }
            }
        }
    }
}

@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val size by animateFloatAsState(if (isSelected) 72f else 64f, label = "circleSize")
    
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 4.dp else 2.dp,
                color = if (isSelected) Color(0xFF333333) else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}

@Composable
fun CustomColorPickerDialog(onDismiss: () -> Unit, onColorSelected: (Color) -> Unit) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }

    val currentColor = remember(hue, saturation, value) {
        Color.hsv(hue, saturation, value)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Magic Color Picker", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(4.dp, Color(0xFFEEEEEE), CircleShape)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                ColorSlider(label = "Color (Hue)", value = hue, valueRange = 0f..360f, onValueChange = { hue = it })
                Spacer(modifier = Modifier.height(16.dp))
                ColorSlider(label = "Vibrancy", value = saturation, valueRange = 0f..1f, onValueChange = { saturation = it })
                Spacer(modifier = Modifier.height(16.dp))
                ColorSlider(label = "Brightness", value = value, valueRange = 0f..1f, onValueChange = { value = it })
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onColorSelected(currentColor) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("Pick Me!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSlider(label: String, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF6200EE),
                activeTrackColor = Color(0xFF6200EE).copy(alpha = 0.3f),
                inactiveTrackColor = Color(0xFFF0F0F0)
            )
        )
    }
}
