package com.squasre.tap2color.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.squasre.tap2color.data.DrawingTemplate
import com.squasre.tap2color.data.SampleDrawings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDrawingSelected: (DrawingTemplate) -> Unit,
    onGalleryClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    
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

            if (filteredDrawings.isEmpty()) {
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
                        DrawingCard(template) {
                            onDrawingSelected(template)
                        }
                    }
                }
            }
        }
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
fun DrawingCard(template: DrawingTemplate, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF1F3F4)),
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
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        template.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
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
