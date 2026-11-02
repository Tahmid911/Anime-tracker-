package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_table")
data class AnimeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val japaneseTitle: String = "",
    val synopsis: String = "",
    val coverUrl: String = "",
    val bannerUrl: String = "",
    val watchStatus: String = "Watching", // Watching, Completed, Plan to Watch, On Hold, Dropped
    val totalEpisodes: Int = 12,
    val episodesWatched: Int = 0,
    val rating: Float = 8.5f,
    val userScore: Float = 0.0f,
    val genres: String = "Action, Fantasy", // Comma separated
    val studio: String = "Unknown Studio",
    val releaseYear: String = "2024",
    val officialWatchUrl: String = "",
    val streamingPlatforms: String = "Crunchyroll, Netflix", // Comma separated
    val addedAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val userNotes: String = ""
)

@Entity(tableName = "episode_table")
data class EpisodeEntity(
    @PrimaryKey val id: String, // format: "${animeId}_ep_${episodeNumber}"
    val animeId: String,
    val episodeNumber: Int,
    val title: String,
    val synopsis: String = "",
    val durationMinutes: Int = 24,
    val airDate: String = "",
    val isWatched: Boolean = false,
    val watchedAt: Long? = null
)

data class AnimeWithEpisodes(
    val anime: AnimeEntity,
    val episodes: List<EpisodeEntity>
)

data class StreamingPlatform(
    val name: String,
    val url: String,
    val isAvailable: Boolean = true
)

data class AnimeSearchResult(
    val title: String,
    val japaneseTitle: String = "",
    val synopsis: String,
    val genres: List<String>,
    val totalEpisodes: Int,
    val rating: Float,
    val studio: String,
    val releaseYear: String,
    val coverUrl: String = "",
    val officialWatchUrl: String = "",
    val streamingPlatforms: List<String> = emptyList(),
    val episodesList: List<EpisodeDetail> = emptyList(),
    val matchConfidence: String = "High" // High, Medium, Low for photo recognition
)

data class EpisodeDetail(
    val episodeNumber: Int,
    val title: String,
    val synopsis: String = "",
    val durationMinutes: Int = 24,
    val airDate: String = ""
)

data class UserProgressSummary(
    val totalAnimeTracked: Int = 0,
    val watchingCount: Int = 0,
    val completedCount: Int = 0,
    val planToWatchCount: Int = 0,
    val onHoldCount: Int = 0,
    val droppedCount: Int = 0,
    val totalEpisodesWatched: Int = 0,
    val totalMinutesWatched: Long = 0L,
    val averageScoreGiven: Float = 0.0f,
    val topGenres: List<Pair<String, Int>> = emptyList()
)

data class ExportArchive(
    val exportDate: String,
    val appVersion: String = "1.0",
    val userSummary: UserProgressSummary,
    val animeList: List<AnimeEntity>,
    val episodesList: List<EpisodeEntity>
)
