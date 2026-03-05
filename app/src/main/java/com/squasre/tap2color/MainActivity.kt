package com.squasre.tap2color

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.squasre.tap2color.data.SampleDrawings
import com.squasre.tap2color.ui.DrawingScreen
import com.squasre.tap2color.ui.GalleryScreen
import com.squasre.tap2color.ui.HomeScreen
import com.squasre.tap2color.ui.theme.Tap2colorTheme
import com.squasre.tap2color.viewmodel.ColoringViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tap2colorTheme {
                val navController = rememberNavController()
                val coloringViewModel: ColoringViewModel = viewModel()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onDrawingSelected = { template ->
                                coloringViewModel.loadTemplate(template)
                                navController.navigate("drawing/${template.id}")
                            },
                            onGalleryClick = {
                                navController.navigate("gallery")
                            }
                        )
                    }
                    composable("gallery") {
                        GalleryScreen(
                            onBack = { navController.popBackStack() },
                            onDrawingSelected = { template ->
                                coloringViewModel.loadTemplate(template)
                                navController.navigate("drawing/${template.id}")
                            }
                        )
                    }
                    composable("drawing/{drawingId}") { backStackEntry ->
                        val drawingId = backStackEntry.arguments?.getString("drawingId")
                        val template = SampleDrawings.all.find { it.id == drawingId }
                        
                        if (template != null) {
                            DrawingScreen(
                                template = template,
                                viewModel = coloringViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
