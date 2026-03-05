# Tap2Color 🎨

A simple, clean, and kid-friendly Android coloring app built with **Kotlin** and **Jetpack Compose**. Designed for children ages 3–8 to explore their creativity through interactive SVG coloring.

## ✨ Features

- **Tap-to-Color**: Simple touch interaction to fill SVG regions with vibrant colors.
- **Dynamic Content**: Load all drawings and categories dynamically from a single JSON file (`assets/drawings.json`).
- **Smart SVG Parsing**: Dynamically parses SVG paths, rects, and circles with unique IDs.
- **Magic Color Picker**: An intuitive HSV-based color picker for creating custom colors.
- **Art Gallery**: A personal collection view with live previews of your colored art.
- **Dynamic Category Filtering**: Categories are automatically generated and sorted based on your JSON data.
- **Celebration Animation**: A fun confetti and sparkle effect triggered when a drawing is fully colored.
- **Progress Persistence**: Automatically saves coloring progress and custom colors using SharedPreferences.
- **Undo & Reset**: Easy-to-use buttons to correct mistakes or start over.
- **Sharing**: 
    - Export single drawings as high-quality PNGs.
    - Generate and share a beautiful collage of your top 10 gallery items.

## 🚀 Tech Stack

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Data Serialization**: Kotlinx Serialization (JSON)
- **Graphics**: Android Canvas / Compose Path API
- **Navigation**: Jetpack Navigation Compose
- **Persistence**: SharedPreferences (ARGB state mapping)
- **Image Export**: Android Bitmap & Canvas API / FileProvider

## 📂 Project Structure

- `assets/`: Contains `drawings.json`, the source of truth for all coloring templates.
- `ui/`: Contains all Compose screens (`HomeScreen`, `DrawingScreen`, `GalleryScreen`) and theme.
- `svg/`: Logic for parsing SVG strings into interactive Compose `Path` objects.
- `viewmodel/`: `ColoringViewModel` handling app state, persistence, and history.
- `export/`: `ImageExporter` for generating and sharing PNG images and gallery collages.
- `data/`: `SampleDrawings` state holder that loads and manages dynamic content.

## 🛠️ How to Add New Drawings

Adding new content is as easy as updating the `app/src/main/assets/drawings.json` file. No code changes required!

Example JSON entry:
```json
{
  "id": "new_art",
  "name": "Magic Castle",
  "category": "FANTASY",
  "svgContent": "<svg viewBox=\"0 0 300 300\"><path id=\"tower\" d=\"...\" fill=\"#FFFFFF\" stroke=\"#000\"/></svg>"
}
```

## 📋 Requirements

- Minimum SDK: 24+
- Android Studio Ladybug or newer
- Kotlin 2.2.10

---
Built with ❤️ for the little artists.
