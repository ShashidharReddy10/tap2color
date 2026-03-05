package com.squasre.tap2color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.squasre.tap2color.data.DrawingTemplate
import com.squasre.tap2color.data.SampleDrawings
import com.squasre.tap2color.viewmodel.ColoringViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ColoringViewModel,
    onDrawingSelected: (DrawingTemplate) -> Unit,
    onGalleryClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var premiumTemplateToUnlock by remember { mutableStateOf<DrawingTemplate?>(null) }
    
    val filteredDrawings = remember(selectedCategory, SampleDrawings.all) {
        if (selectedCategory == "All") {
            SampleDrawings.all
        } else {
            SampleDrawings.all.filter { it.category == selectedCategory }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tap2Color",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6200EE)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.isPremiumUnlocked) viewModel.lockPremium() else viewModel.unlockPremium()
                    }) {
                        Icon(
                            if (viewModel.isPremiumUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Toggle Premium",
                            tint = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onGalleryClick) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Art Gallery",
                            tint = Color(0xFF6200EE)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CategoryFilterBar(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            if (filteredDrawings.isEmpty() && SampleDrawings.all.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6200EE))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredDrawings) { template ->
                        DrawingCard(template, viewModel.isPremiumUnlocked) {
                            if (template.isPremium && !viewModel.isPremiumUnlocked) {
                                premiumTemplateToUnlock = template
                            } else {
                                onDrawingSelected(template)
                            }
                        }
                    }
                }
            }
        }
    }

    if (premiumTemplateToUnlock != null) {
        PremiumUnlockDialog(
            template = premiumTemplateToUnlock!!,
            onDismiss = { premiumTemplateToUnlock = null },
            onUnlock = {
                viewModel.unlockPremium()
                premiumTemplateToUnlock = null
            }
        )
    }
}

@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SampleDrawings.availableCategories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { 
                    Text(
                        category,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6200EE),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color.DarkGray
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.LightGray,
                    selectedBorderColor = Color(0xFF6200EE)
                )
            )
        }
    }
}

@Composable
fun DrawingCard(template: DrawingTemplate, isPremiumUnlocked: Boolean, onClick: () -> Unit) {
    val isLocked = template.isPremium && !isPremiumUnlocked
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(if (isLocked) Color(0xFFE8EAF6) else Color(0xFFF1F3F4)),
                    contentAlignment = Alignment.Center
                ) {
                    val placeholder = when(template.category.uppercase()) {
                        "ANIMALS" -> "🐾"
                        "VEHICLES" -> "🚗"
                        "FOOD" -> "🍎"
                        "NATURE" -> "🌳"
                        else -> "🎨"
                    }
                    Text(placeholder, fontSize = 48.sp)
                    
                    if (isLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Premium",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.padding(12.dp).size(24.dp)
                            )
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                template.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            if (template.isPremium) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            template.category,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumUnlockDialog(template: DrawingTemplate, onDismiss: () -> Unit, onUnlock: () -> Unit) {
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
                Text(
                    "✨ Magic Art Pass ✨",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6200EE),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3E5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Unlock \"${template.name}\" and all other premium drawings!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Added Benefits:\n• Unlock ALL Drawings\n• Sparkle Magic Brushes\n• Neon Color Palette\n• No Ads Forever!",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onUnlock,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Get Full Access!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = Color.Gray)
                }
            }
        }
    }
}
