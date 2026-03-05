package com.squasre.tap2color.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.squasre.tap2color.svg.SvgRegion
import java.io.File
import java.io.FileOutputStream

object ImageExporter {
    fun exportToBitmap(
        regions: List<SvgRegion>,
        regionColors: Map<String, Color>,
        viewBox: Rect
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            viewBox.width.toInt().coerceAtLeast(1),
            viewBox.height.toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val fillPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            color = android.graphics.Color.BLACK
            strokeWidth = 4f
            isAntiAlias = true
        }

        regions.forEach { region ->
            val color = regionColors[region.id] ?: Color.White
            fillPaint.color = color.toArgb()
            
            val androidPath = region.path.asAndroidPath()
            canvas.drawPath(androidPath, fillPaint)
            canvas.drawPath(androidPath, strokePaint)
        }

        return bitmap
    }

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "colored_image.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share image"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
