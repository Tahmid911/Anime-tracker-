package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AnimeEntity
import com.example.data.model.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime_table ORDER BY lastUpdated DESC")
    fun getAllAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime_table")
    suspend fun getAllAnimeSync(): List<AnimeEntity>

    @Query("SELECT * FROM anime_table WHERE watchStatus = :status ORDER BY lastUpdated DESC")
    fun getAnimeByStatus(status: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime_table WHERE id = :id")
    fun getAnimeByIdFlow(id: String): Flow<AnimeEntity?>

    @Query("SELECT * FROM anime_table WHERE id = :id")
    suspend fun getAnimeById(id: String): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity)

    @Update
    suspend fun updateAnime(anime: AnimeEntity)

    @Query("DELETE FROM anime_table WHERE id = :id")
    suspend fun deleteAnimeById(id: String)

    // Episode DAO operations
    @Query("SELECT * FROM episode_table WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    fun getEpisodesForAnime(animeId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episode_table WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    suspend fun getEpisodesForAnimeSync(animeId: String): List<EpisodeEntity>

    @Query("SELECT * FROM episode_table")
    suspend fun getAllEpisodesSync(): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("UPDATE episode_table SET isWatched = :isWatched, watchedAt = :watchedAt WHERE id = :episodeId")
    suspend fun updateEpisodeWatchedState(episodeId: String, isWatched: Boolean, watchedAt: Long?)

    @Query("SELECT COUNT(*) FROM episode_table WHERE animeId = :animeId AND isWatched = 1")
    suspend fun getWatchedEpisodeCount(animeId: String): Int

    @Query("SELECT COUNT(*) FROM episode_table WHERE isWatched = 1")
    suspend fun getTotalWatchedEpisodeCount(): Int

    @Transaction
    suspend fun toggleEpisodeWatched(episodeId: String, animeId: String, isWatched: Boolean) {
        val now = if (isWatched) System.currentTimeMillis() else null
        updateEpisodeWatchedState(episodeId, isWatched, now)
        val count = getWatchedEpisodeCount(animeId)
        val anime = getAnimeById(animeId)
        if (anime != null) {
            val updatedStatus = if (count >= anime.totalEpisodes && anime.totalEpisodes > 0) {
                "Completed"
            } else if (count > 0 && anime.watchStatus == "Plan to Watch") {
                "Watching"
            } else {
                anime.watchStatus
            }
            updateAnime(
                anime.copy(
                    episodesWatched = count,
                    watchStatus = updatedStatus,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    @Query("DELETE FROM episode_table WHERE animeId = :animeId")
    suspend fun deleteEpisodesForAnime(animeId: String)
}
