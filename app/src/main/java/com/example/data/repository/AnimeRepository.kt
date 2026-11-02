package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.AnimeDao
import com.example.data.local.TokenUsageState
import com.example.data.local.UserPreferencesManager
import com.example.data.model.AnimeEntity
import com.example.data.model.AnimeSearchResult
import com.example.data.model.EpisodeDetail
import com.example.data.model.EpisodeEntity
import com.example.data.model.ExportArchive
import com.example.data.model.UserProgressSummary
import com.example.data.remote.GeminiApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnimeRepository(
    private val context: Context,
    private val animeDao: AnimeDao,
    private val geminiApiService: GeminiApiService = GeminiApiService(),
    private val userPreferencesManager: UserPreferencesManager = UserPreferencesManager(context)
) {

    val allAnime: Flow<List<AnimeEntity>> = animeDao.getAllAnime()

    val themeMode: StateFlow<String> = userPreferencesManager.themeMode
    val customApiKey: StateFlow<String> = userPreferencesManager.customApiKey
    val selectedApiModel: StateFlow<String> = userPreferencesManager.selectedApiModel
    val tokenUsageState: StateFlow<TokenUsageState> = userPreferencesManager.tokenUsageState

    fun setThemeMode(mode: String) {
        userPreferencesManager.setThemeMode(mode)
    }

    fun setCustomApiKey(key: String) {
        userPreferencesManager.setCustomApiKey(key)
    }

    fun setSelectedApiModel(model: String) {
        userPreferencesManager.setSelectedApiModel(model)
    }

    fun resetTokenUsage() {
        userPreferencesManager.resetTokenUsage()
    }

    fun getAnimeByStatus(status: String): Flow<List<AnimeEntity>> {
        return if (status == "All") allAnime else animeDao.getAnimeByStatus(status)
    }

    fun getAnimeById(id: String): Flow<AnimeEntity?> {
        return animeDao.getAnimeByIdFlow(id)
    }

    fun getEpisodesForAnime(animeId: String): Flow<List<EpisodeEntity>> {
        return animeDao.getEpisodesForAnime(animeId)
    }

    suspend fun toggleEpisodeWatched(episodeId: String, animeId: String, isWatched: Boolean) {
        animeDao.toggleEpisodeWatched(episodeId, animeId, isWatched)
    }

    suspend fun updateAnimeStatus(animeId: String, newStatus: String) {
        val anime = animeDao.getAnimeById(animeId) ?: return
        animeDao.updateAnime(
            anime.copy(
                watchStatus = newStatus,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateAnimeUserScore(animeId: String, score: Float, notes: String = "") {
        val anime = animeDao.getAnimeById(animeId) ?: return
        animeDao.updateAnime(
            anime.copy(
                userScore = score,
                userNotes = notes,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateAnimeCoverUrl(animeId: String, newCoverUrl: String) {
        val anime = animeDao.getAnimeById(animeId) ?: return
        animeDao.updateAnime(
            anime.copy(
                coverUrl = newCoverUrl,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun fetchAndApplyRealTitleCard(animeId: String, title: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val realPosterUrl = geminiApiService.fetchRealTitleCardUrl(title)
            if (!realPosterUrl.isNullOrBlank()) {
                updateAnimeCoverUrl(animeId, realPosterUrl)
                Result.success(realPosterUrl)
            } else {
                Result.failure(Exception("Could not find a high-res title card for '$title'."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnime(animeId: String) {
        animeDao.deleteAnimeById(animeId)
        animeDao.deleteEpisodesForAnime(animeId)
    }

    suspend fun searchAnimeByName(query: String): Result<AnimeSearchResult> {
        val currentApiKey = customApiKey.value
        val model = selectedApiModel.value
        val result = geminiApiService.searchAnimeByName(query, currentApiKey, selectedModel = model)
        if (result.isSuccess) {
            // Record token usage based on model efficiency
            val tokens = if (model.contains("2.5") || model.contains("lite")) 400L else 1200L
            userPreferencesManager.recordApiUsage(tokens)
        }
        return result
    }

    suspend fun searchAnimeByPhotos(bitmaps: List<Bitmap>): Result<AnimeSearchResult> {
        val currentApiKey = customApiKey.value
        val model = selectedApiModel.value
        val result = geminiApiService.searchAnimeByPhotos(bitmaps, currentApiKey, selectedModel = model)
        if (result.isSuccess) {
            // Record token usage (~800 tokens for 2.5 flash vs 2200 for 3.5 pro)
            val baseTokens = if (model.contains("2.5") || model.contains("lite")) 800L else 2200L
            userPreferencesManager.recordApiUsage(baseTokens * bitmaps.size)
        }
        return result
    }

    suspend fun autoFetchMissingTitleCards() = withContext(Dispatchers.IO) {
        try {
            val allList = animeDao.getAllAnimeSync()
            for (anime in allList) {
                if (anime.coverUrl.isBlank() || anime.coverUrl.contains("unsplash.com")) {
                    val realPoster = geminiApiService.fetchRealTitleCardUrl(anime.title)
                    if (!realPoster.isNullOrBlank()) {
                        updateAnimeCoverUrl(anime.id, realPoster)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveSearchResultToLibrary(
        searchResult: AnimeSearchResult,
        initialWatchStatus: String = "Watching"
    ): String = withContext(Dispatchers.IO) {
        val animeId = searchResult.title.lowercase()
            .replace(Regex("[^a-z0-9]"), "-")
            .trim('-')
            .ifEmpty { "anime-${System.currentTimeMillis()}" }

        var coverImage = searchResult.coverUrl
        if (coverImage.isBlank() || coverImage.contains("unsplash.com")) {
            val realPoster = geminiApiService.fetchRealTitleCardUrl(searchResult.title)
            if (!realPoster.isNullOrBlank()) {
                coverImage = realPoster
            } else if (coverImage.isBlank()) {
                coverImage = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600"
            }
        }

        val animeEntity = AnimeEntity(
            id = animeId,
            title = searchResult.title,
            japaneseTitle = searchResult.japaneseTitle,
            synopsis = searchResult.synopsis,
            coverUrl = coverImage,
            watchStatus = initialWatchStatus,
            totalEpisodes = searchResult.totalEpisodes,
            episodesWatched = 0,
            rating = searchResult.rating,
            genres = searchResult.genres.joinToString(", "),
            studio = searchResult.studio,
            releaseYear = searchResult.releaseYear,
            officialWatchUrl = searchResult.officialWatchUrl,
            streamingPlatforms = searchResult.streamingPlatforms.joinToString(", "),
            addedAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        animeDao.insertAnime(animeEntity)

        // Create episode entities
        val epEntities = mutableListOf<EpisodeEntity>()
        val totalEps = searchResult.totalEpisodes.coerceAtLeast(searchResult.episodesList.size)
        for (i in 1..totalEps) {
            val epDetail = searchResult.episodesList.find { it.episodeNumber == i }
            epEntities.add(
                EpisodeEntity(
                    id = "${animeId}_ep_$i",
                    animeId = animeId,
                    episodeNumber = i,
                    title = epDetail?.title ?: "Episode $i",
                    synopsis = epDetail?.synopsis ?: "Episode $i of ${searchResult.title}",
                    durationMinutes = epDetail?.durationMinutes ?: 24,
                    airDate = epDetail?.airDate ?: "",
                    isWatched = false
                )
            )
        }

        animeDao.insertEpisodes(epEntities)
        animeId
    }

    suspend fun getAllEpisodes(): List<EpisodeEntity> = withContext(Dispatchers.IO) {
        animeDao.getAllEpisodesSync()
    }

    suspend fun getUserSummary(): UserProgressSummary = withContext(Dispatchers.IO) {
        val animeList = animeDao.getAllAnimeSync()
        val epList = animeDao.getAllEpisodesSync()
        
        val totalWatchedEps = epList.count { it.isWatched }
        val totalMinutes = totalWatchedEps * 24L
        
        val scoredAnime = animeList.filter { it.userScore > 0 }
        val avgScore = if (scoredAnime.isNotEmpty()) scoredAnime.map { it.userScore }.average().toFloat() else 0.0f

        UserProgressSummary(
            totalAnimeTracked = animeList.size,
            watchingCount = animeList.count { it.watchStatus.equals("Watching", ignoreCase = true) },
            completedCount = animeList.count { it.watchStatus.equals("Completed", ignoreCase = true) },
            planToWatchCount = animeList.count { it.watchStatus.equals("Plan to Watch", ignoreCase = true) },
            onHoldCount = animeList.count { it.watchStatus.equals("On Hold", ignoreCase = true) },
            droppedCount = animeList.count { it.watchStatus.equals("Dropped", ignoreCase = true) },
            totalEpisodesWatched = totalWatchedEps,
            totalMinutesWatched = totalMinutes,
            averageScoreGiven = avgScore
        )
    }

    suspend fun exportJsonArchive(animeList: List<AnimeEntity>, episodesList: List<EpisodeEntity>, summary: UserProgressSummary): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "AnimeArchives")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "anime_progress_backup_$timestamp.json")

            val jsonRoot = JSONObject().apply {
                put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                put("appVersion", "1.0")
                put("totalAnime", animeList.size)
                put("totalEpisodesWatched", episodesList.count { it.isWatched })

                put("animeList", JSONArray().apply {
                    animeList.forEach { anime ->
                        put(JSONObject().apply {
                            put("id", anime.id)
                            put("title", anime.title)
                            put("japaneseTitle", anime.japaneseTitle)
                            put("watchStatus", anime.watchStatus)
                            put("totalEpisodes", anime.totalEpisodes)
                            put("episodesWatched", anime.episodesWatched)
                            put("genres", anime.genres)
                            put("studio", anime.studio)
                            put("userScore", anime.userScore)
                            put("userNotes", anime.userNotes)
                            put("officialWatchUrl", anime.officialWatchUrl)
                        })
                    }
                })

                put("watchedEpisodes", JSONArray().apply {
                    episodesList.filter { it.isWatched }.forEach { ep ->
                        put(JSONObject().apply {
                            put("id", ep.id)
                            put("animeId", ep.animeId)
                            put("episodeNumber", ep.episodeNumber)
                            put("title", ep.title)
                            put("watchedAt", ep.watchedAt)
                        })
                    }
                })
            }

            val outputStream = FileOutputStream(file)
            outputStream.write(jsonRoot.toString(2).toByteArray())
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportMarkdownArchive(animeList: List<AnimeEntity>, summary: UserProgressSummary): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "AnimeArchives")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "anime_archive_summary_$timestamp.md")

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val mdContent = StringBuilder().apply {
                appendLine("# 🎌 Personal Anime Watch Archive & Progress Summary")
                appendLine("> Generated on $dateStr via Anime Tracker")
                appendLine()
                appendLine("## 📊 Overall Progress Statistics")
                appendLine("- **Total Anime Tracked**: ${animeList.size}")
                appendLine("- **Total Episodes Watched**: ${summary.totalEpisodesWatched}")
                appendLine("- **Total Watch Time**: ${summary.totalMinutesWatched / 60} hours ${summary.totalMinutesWatched % 60} minutes")
                appendLine()
                appendLine("## 📺 Anime Library Breakdown")
                appendLine("| Anime Title | Status | Progress | Genres | Rating | Official Watch Link |")
                appendLine("|---|---|---|---|---|---|")

                animeList.forEach { anime ->
                    val link = if (anime.officialWatchUrl.isNotBlank()) "[Watch Online](${anime.officialWatchUrl})" else "N/A"
                    appendLine("| **${anime.title}** | ${anime.watchStatus} | ${anime.episodesWatched}/${anime.totalEpisodes} | ${anime.genres} | ${anime.rating}★ | $link |")
                }

                appendLine()
                appendLine("---")
                appendLine("*Offline Personal Archive — Stored locally on device.*")
            }.toString()

            val outputStream = FileOutputStream(file)
            outputStream.write(mdContent.toByteArray())
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun ensureInitialSampleData() = withContext(Dispatchers.IO) {
        val currentAnime = animeDao.getAnimeById("demon-slayer")
        if (currentAnime == null) {
            val samples = listOf(
                AnimeEntity(
                    id = "demon-slayer",
                    title = "Demon Slayer: Kimetsu no Yaiba",
                    japaneseTitle = "鬼滅の刃",
                    synopsis = "Tanjiro Kamado, a kindhearted boy whose family was slaughtered by demons, sets out to become a demon slayer to turn his transformed sister Nezuko back into a human.",
                    coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600",
                    watchStatus = "Watching",
                    totalEpisodes = 26,
                    episodesWatched = 12,
                    rating = 8.7f,
                    genres = "Action, Supernatural, Fantasy",
                    studio = "ufotable",
                    releaseYear = "2019",
                    officialWatchUrl = "https://www.crunchyroll.com/series/GY5P48356/demon-slayer-kimetsu-no-yaiba",
                    streamingPlatforms = "Crunchyroll, Netflix, Hulu",
                    addedAt = System.currentTimeMillis()
                ),
                AnimeEntity(
                    id = "attack-on-titan",
                    title = "Attack on Titan",
                    japaneseTitle = "進撃の巨人",
                    synopsis = "Humanity lives inside cities surrounded by enormous walls due to the Titans, gigantic humanoid creatures who devour humans without reason.",
                    coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600",
                    watchStatus = "Completed",
                    totalEpisodes = 25,
                    episodesWatched = 25,
                    rating = 9.1f,
                    genres = "Action, Dark Fantasy, Mystery",
                    studio = "WIT Studio / MAPPA",
                    releaseYear = "2013",
                    officialWatchUrl = "https://www.crunchyroll.com/series/GR751KNZY/attack-on-titan",
                    streamingPlatforms = "Crunchyroll, Hulu",
                    addedAt = System.currentTimeMillis() - 86400000
                ),
                AnimeEntity(
                    id = "jujutsu-kaisen",
                    title = "Jujutsu Kaisen",
                    japaneseTitle = "呪術廻戦",
                    synopsis = "Yuji Itadori, a high school student with extraordinary physical strength, swallows a cursed object to save a classmate and becomes host to Ryomen Sukuna.",
                    coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600",
                    watchStatus = "Watching",
                    totalEpisodes = 24,
                    episodesWatched = 18,
                    rating = 8.6f,
                    genres = "Action, Supernatural, School",
                    studio = "MAPPA",
                    releaseYear = "2020",
                    officialWatchUrl = "https://www.crunchyroll.com/series/G6W41098Y/jujutsu-kaisen",
                    streamingPlatforms = "Crunchyroll, Netflix",
                    addedAt = System.currentTimeMillis() - 172800000
                ),
                AnimeEntity(
                    id = "frieren",
                    title = "Frieren: Beyond Journey's End",
                    japaneseTitle = "葬送のフリーレン",
                    synopsis = "An elf mage and her former adventurer companions defeat the Demon King. As time moves on, Frieren reflects on mortality, friendship, and the passage of time.",
                    coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
                    watchStatus = "Plan to Watch",
                    totalEpisodes = 28,
                    episodesWatched = 0,
                    rating = 9.3f,
                    genres = "Adventure, Drama, Fantasy",
                    studio = "Madhouse",
                    releaseYear = "2023",
                    officialWatchUrl = "https://www.crunchyroll.com/series/GG5H5X31E/frieren-beyond-journeys-end",
                    streamingPlatforms = "Crunchyroll",
                    addedAt = System.currentTimeMillis() - 259200000
                )
            )

            for (sample in samples) {
                animeDao.insertAnime(sample)
                val epEntities = (1..sample.totalEpisodes).map { epNum ->
                    EpisodeEntity(
                        id = "${sample.id}_ep_$epNum",
                        animeId = sample.id,
                        episodeNumber = epNum,
                        title = "Episode $epNum: ${getSampleEpTitle(sample.title, epNum)}",
                        synopsis = "Official episode $epNum storyline for ${sample.title}.",
                        durationMinutes = 24,
                        airDate = "Episode $epNum",
                        isWatched = epNum <= sample.episodesWatched
                    )
                }
                animeDao.insertEpisodes(epEntities)
            }
        }

        // Auto fetch real official title cards / posters for any anime missing a real poster
        autoFetchMissingTitleCards()
    }

    private fun getSampleEpTitle(animeTitle: String, epNum: Int): String {
        return when (epNum) {
            1 -> "Cruelty & Awakening"
            2 -> "Trainer Sakonji Urokodaki"
            3 -> "Final Selection"
            4 -> "Sabito & Makomo"
            5 -> "My Own Steel"
            6 -> "Swordsman Accompanying a Demon"
            else -> "Chapter $epNum"
        }
    }
}
