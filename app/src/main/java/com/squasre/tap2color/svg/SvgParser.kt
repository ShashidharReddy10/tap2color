package com.squasre.tap2color.svg

import android.util.Xml
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.geometry.Rect
import androidx.core.graphics.PathParser
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

object SvgParser {
    fun parse(svgContent: String): List<SvgRegion> {
        val regions = mutableListOf<SvgRegion>()
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(svgContent))

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                val tagName = parser.name
                val id = parser.getAttributeValue(null, "id")
                
                if (id != null) {
                    when (tagName) {
                        "path" -> {
                            val d = parser.getAttributeValue(null, "d")
                            if (d != null) {
                                try {
                                    val androidPath = PathParser.createPathFromPathData(d)
                                    regions.add(SvgRegion(id, androidPath.asComposePath()))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        "rect" -> {
                            val x = parser.getAttributeValue(null, "x")?.toFloat() ?: 0f
                            val y = parser.getAttributeValue(null, "y")?.toFloat() ?: 0f
                            val width = parser.getAttributeValue(null, "width")?.toFloat() ?: 0f
                            val height = parser.getAttributeValue(null, "height")?.toFloat() ?: 0f
                            val path = Path().apply {
                                addRect(androidx.compose.ui.geometry.Rect(x, y, x + width, y + height))
                            }
                            regions.add(SvgRegion(id, path))
                        }
                        "circle" -> {
                            val cx = parser.getAttributeValue(null, "cx")?.toFloat() ?: 0f
                            val cy = parser.getAttributeValue(null, "cy")?.toFloat() ?: 0f
                            val r = parser.getAttributeValue(null, "r")?.toFloat() ?: 0f
                            val path = Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
                            }
                            regions.add(SvgRegion(id, path))
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return regions
    }

    fun parseViewBox(svgContent: String): Rect {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(svgContent))
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "svg") {
                val viewBox = parser.getAttributeValue(null, "viewBox")
                if (viewBox != null) {
                    val parts = viewBox.split(Regex("\\s+")).filter { it.isNotEmpty() }
                    if (parts.size == 4) {
                        return Rect(
                            parts[0].toFloat(),
                            parts[1].toFloat(),
                            parts[2].toFloat(),
                            parts[3].toFloat()
                        )
                    }
                }
            }
            eventType = parser.next()
        }
        return Rect(0f, 0f, 300f, 300f) // Default
    }
}
