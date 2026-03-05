# Tap2Color 🎨

A simple, clean, and kid-friendly Android coloring app built with **Kotlin** and **Jetpack Compose**. Designed for children ages 3–8 to explore their creativity through interactive SVG coloring.

## ✨ Features

- **Tap-to-Color**: Simple touch interaction to fill SVG regions with vibrant colors.
- **Smart SVG Parsing**: Dynamically parses SVG paths, rects, and circles with unique IDs.
- **Magic Color Picker**: An intuitive HSV-based color picker for creating custom colors.
- **Art Gallery**: A personal collection view where kids can see their half-colored and completed masterpieces.
- **Category Filtering**: Easily browse drawings by categories: Animals, Vehicles, Food, and Nature.
- **Celebration Animation**: A fun confetti and sparkle effect triggered when a drawing is fully colored.
- **Progress Persistence**: Automatically saves coloring progress so children can pick up exactly where they left off.
- **Undo & Reset**: Easy-to-use buttons to correct mistakes or start over.
- **Sharing**: 
    - Export single drawings as PNG.
    - Generate and share a beautiful collage of the top 10 gallery items.

## 🚀 Tech Stack

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Graphics**: Android Canvas / Compose Path API
- **Navigation**: Jetpack Navigation Compose
- **Persistence**: SharedPreferences (ARGB color state mapping)
- **Image Export**: Android Bitmap & Canvas API / FileProvider

## 📂 Project Structure

- `ui/`: Contains all Compose screens (`HomeScreen`, `DrawingScreen`, `GalleryScreen`) and the app theme.
- `svg/`: Logic for parsing SVG strings into interactive Compose `Path` objects.
- `viewmodel/`: `ColoringViewModel` handling the app state, logic, and persistence.
- `export/`: `ImageExporter` for generating and sharing PNG images.
- `data/`: `SampleDrawings` containing the SVG templates and category definitions.

## 🛠️ Requirements

- Minimum SDK: 24+
- Android Studio Ladybug or newer
- Kotlin 2.2.10

## 🎨 Sample Drawings Included

1. **Animals**: Cat, Dog, Elephant
2. **Vehicles**: Car, Truck
3. **Food**: Apple, Banana
4. **Nature**: Tree, Sun (Fixed for 1-tap completion!)

---
Built with ❤️ for the little artists.
