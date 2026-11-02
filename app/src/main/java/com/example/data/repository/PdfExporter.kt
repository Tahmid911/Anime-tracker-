package com.example.data.repository

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.RectF
import android.graphics.Typeface
import com.example.data.model.AnimeEntity
import com.example.data.model.EpisodeEntity
import com.example.data.model.UserProgressSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateProgressPdf(
        context: Context,
        summary: UserProgressSummary,
        animeList: List<AnimeEntity>,
        episodesList: List<EpisodeEntity>
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 width in points
            val pageHeight = 842 // Standard A4 height in points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            var currentY = 40f

            // Background Header
            paint.color = Color.rgb(15, 23, 42) // IndigoDark
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 120f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("ANIME WATCH PROGRESS REPORT", 30f, 50f, paint)

            // Subtitle / Date
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(203, 213, 225)
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            canvas.drawText("Generated on: $dateStr | Anime Tracker Archives", 30f, 75f, paint)

            currentY = 145f

            // Section 1: Progress Summary Box
            paint.color = Color.rgb(241, 245, 249)
            val summaryRect = RectF(30f, currentY, pageWidth - 30f, currentY + 110f)
            canvas.drawRoundRect(summaryRect, 8f, 8f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("TRACKING OVERVIEW", 45f, currentY + 25f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(51, 65, 85)

            val col1X = 45f
            val col2X = 220f
            val col3X = 400f

            val totalMins = summary.totalEpisodesWatched * 24L
            val hours = totalMins / 60
            val mins = totalMins % 60

            canvas.drawText("Total Anime Tracked: ${summary.totalAnimeTracked}", col1X, currentY + 50f, paint)
            canvas.drawText("Episodes Watched: ${summary.totalEpisodesWatched}", col1X, currentY + 70f, paint)
            canvas.drawText("Total Watch Time: ${hours}h ${mins}m", col1X, currentY + 90f, paint)

            canvas.drawText("Watching: ${summary.watchingCount}", col2X, currentY + 50f, paint)
            canvas.drawText("Completed: ${summary.completedCount}", col2X, currentY + 70f, paint)
            canvas.drawText("Plan to Watch: ${summary.planToWatchCount}", col2X, currentY + 90f, paint)

            canvas.drawText("On Hold: ${summary.onHoldCount}", col3X, currentY + 50f, paint)
            canvas.drawText("Dropped: ${summary.droppedCount}", col3X, currentY + 70f, paint)

            currentY += 135f

            // Section 2: Anime Table Header
            paint.color = Color.rgb(139, 92, 246) // NeonPurple
            canvas.drawRect(30f, currentY, pageWidth - 30f, currentY + 25f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val tTitleX = 40f
            val tStatusX = 280f
            val tProgressX = 380f
            val tRatingX = 480f

            canvas.drawText("ANIME TITLE", tTitleX, currentY + 17f, paint)
            canvas.drawText("STATUS", tStatusX, currentY + 17f, paint)
            canvas.drawText("EPISODES", tProgressX, currentY + 17f, paint)
            canvas.drawText("SCORE", tRatingX, currentY + 17f, paint)

            currentY += 30f

            // Table Rows
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for ((index, anime) in animeList.withIndex()) {
                if (currentY > pageHeight - 60f) break

                // Alternate row background
                if (index % 2 == 0) {
                    paint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(30f, currentY - 5f, pageWidth - 30f, currentY + 22f, paint)
                }

                paint.color = Color.rgb(15, 23, 42)
                paint.textSize = 10f

                val truncatedTitle = if (anime.title.length > 32) anime.title.take(30) + "..." else anime.title
                canvas.drawText(truncatedTitle, tTitleX, currentY + 12f, paint)
                canvas.drawText(anime.watchStatus, tStatusX, currentY + 12f, paint)
                canvas.drawText("${anime.episodesWatched} / ${anime.totalEpisodes}", tProgressX, currentY + 12f, paint)

                val scoreText = if (anime.userScore > 0) "${anime.userScore}/10" else "${anime.rating}★"
                canvas.drawText(scoreText, tRatingX, currentY + 12f, paint)

                currentY += 25f
            }

            // Footer
            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 9f
            canvas.drawText("Generated by Anime Tracker App | Confidential Personal Records", 30f, pageHeight - 30f, paint)

            pdfDocument.finishPage(page)

            // Save PDF to file
            val exportDir = File(context.getExternalFilesDir(null), "AnimeArchives")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(exportDir, "Anime_Progress_Report_$timestamp.pdf")

            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
