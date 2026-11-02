package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.TokenUsageState
import com.example.data.model.AnimeEntity
import com.example.data.model.AnimeSearchResult
import com.example.data.model.EpisodeEntity
import com.example.data.model.UserProgressSummary
import com.example.data.repository.AnimeRepository
import com.example.data.repository.PdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val result: AnimeSearchResult, val isPhotoSearch: Boolean = false) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

sealed interface ExportUiState {
    object Idle : ExportUiState
    object Exporting : ExportUiState
    data class Success(val file: File, val message: String, val type: String) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

class AnimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnimeRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = AnimeRepository(application, database.animeDao())
        viewModelScope.launch {
            repository.ensureInitialSampleData()
        }
    }

    // Settings & Theme
    val themeMode: StateFlow<String> = repository.themeMode
    val customApiKey: StateFlow<String> = repository.customApiKey
    val selectedApiModel: StateFlow<String> = repository.selectedApiModel
    val tokenUsageState: StateFlow<TokenUsageState> = repository.tokenUsageState

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
    }

    fun setCustomApiKey(key: String) {
        repository.setCustomApiKey(key)
    }

    fun setSelectedApiModel(model: String) {
        repository.setSelectedApiModel(model)
    }

    fun resetTokenUsage() {
        repository.resetTokenUsage()
    }

    // Status Filter
    private val _selectedStatusFilter = MutableStateFlow("All")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    // Library Query Search
    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    // Anime List
    val animeList: StateFlow<List<AnimeEntity>> = combine(
        repository.allAnime,
        _selectedStatusFilter,
        _librarySearchQuery
    ) { list, statusFilter, query ->
        list.filter { anime ->
            val matchesStatus = if (statusFilter == "All") true else anime.watchStatus.equals(statusFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() || anime.title.contains(query, ignoreCase = true) || anime.genres.contains(query, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Anime Detail State
    private val _selectedAnimeId = MutableStateFlow<String?>(null)
    val selectedAnimeId: StateFlow<String?> = _selectedAnimeId.asStateFlow()

    private val _selectedAnime = MutableStateFlow<AnimeEntity?>(null)
    val selectedAnime: StateFlow<AnimeEntity?> = _selectedAnime.asStateFlow()

    private val _episodesForSelectedAnime = MutableStateFlow<List<EpisodeEntity>>(emptyList())
    val episodesForSelectedAnime: StateFlow<List<EpisodeEntity>> = _episodesForSelectedAnime.asStateFlow()

    // Search Screen State
    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    // Poster Update State
    private val _posterUpdateMessage = MutableStateFlow<String?>(null)
    val posterUpdateMessage: StateFlow<String?> = _posterUpdateMessage.asStateFlow()

    // Export State
    private val _exportUiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportUiState: StateFlow<ExportUiState> = _exportUiState.asStateFlow()

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    fun selectAnime(animeId: String?) {
        _selectedAnimeId.value = animeId
        if (animeId != null) {
            viewModelScope.launch {
                repository.getAnimeById(animeId).collect { anime ->
                    _selectedAnime.value = anime
                }
            }
            viewModelScope.launch {
                repository.getEpisodesForAnime(animeId).collect { epList ->
                    _episodesForSelectedAnime.value = epList
                }
            }
        } else {
            _selectedAnime.value = null
            _episodesForSelectedAnime.value = emptyList()
        }
    }

    fun toggleEpisodeWatched(episodeId: String, animeId: String, currentWatched: Boolean) {
        viewModelScope.launch {
            repository.toggleEpisodeWatched(episodeId, animeId, !currentWatched)
        }
    }

    fun updateAnimeWatchStatus(animeId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateAnimeStatus(animeId, newStatus)
        }
    }

    fun updateAnimeScore(animeId: String, score: Float, notes: String) {
        viewModelScope.launch {
            repository.updateAnimeUserScore(animeId, score, notes)
        }
    }

    fun updateAnimeCoverUrl(animeId: String, newCoverUrl: String) {
        viewModelScope.launch {
            repository.updateAnimeCoverUrl(animeId, newCoverUrl)
            _posterUpdateMessage.value = "Anime poster cover updated!"
        }
    }

    fun fetchAndApplyRealTitleCard(animeId: String, title: String) {
        viewModelScope.launch {
            _posterUpdateMessage.value = "Searching official title card for '$title'..."
            val result = repository.fetchAndApplyRealTitleCard(animeId, title)
            result.fold(
                onSuccess = {
                    _posterUpdateMessage.value = "Updated title card from MyAnimeList / Kitsu!"
                },
                onFailure = { error ->
                    _posterUpdateMessage.value = error.localizedMessage ?: "Failed to fetch title card."
                }
            )
        }
    }

    fun autoFetchAllMissingTitleCards() {
        viewModelScope.launch {
            _posterUpdateMessage.value = "Auto-downloading title cards for watchlist..."
            repository.autoFetchMissingTitleCards()
            _posterUpdateMessage.value = "Auto title cards updated!"
        }
    }

    fun clearPosterUpdateMessage() {
        _posterUpdateMessage.value = null
    }

    fun deleteAnime(animeId: String) {
        viewModelScope.launch {
            repository.deleteAnime(animeId)
            if (_selectedAnimeId.value == animeId) {
                selectAnime(null)
            }
        }
    }

    fun searchAnimeByName(query: String) {
        if (query.isBlank()) return
        _searchUiState.value = SearchUiState.Loading
        viewModelScope.launch {
            val result = repository.searchAnimeByName(query)
            result.fold(
                onSuccess = { animeResult ->
                    _searchUiState.value = SearchUiState.Success(animeResult, isPhotoSearch = false)
                },
                onFailure = { error ->
                    _searchUiState.value = SearchUiState.Error(error.localizedMessage ?: "Search failed")
                }
            )
        }
    }

    fun searchAnimeByPhotos(bitmaps: List<Bitmap>) {
        if (bitmaps.isEmpty()) return
        _searchUiState.value = SearchUiState.Loading
        viewModelScope.launch {
            val result = repository.searchAnimeByPhotos(bitmaps)
            result.fold(
                onSuccess = { animeResult ->
                    _searchUiState.value = SearchUiState.Success(animeResult, isPhotoSearch = true)
                },
                onFailure = { error ->
                    _searchUiState.value = SearchUiState.Error(error.localizedMessage ?: "Photo recognition failed")
                }
            )
        }
    }

    fun addSearchResultToLibrary(searchResult: AnimeSearchResult, initialWatchStatus: String = "Watching") {
        viewModelScope.launch {
            val animeId = repository.saveSearchResultToLibrary(searchResult, initialWatchStatus)
            _searchUiState.value = SearchUiState.Idle
            selectAnime(animeId)
        }
    }

    fun resetSearchState() {
        _searchUiState.value = SearchUiState.Idle
    }

    fun generatePdfReport() {
        _exportUiState.value = ExportUiState.Exporting
        viewModelScope.launch {
            try {
                val currentAnimeList = repository.allAnime.first()
                val summary = repository.getUserSummary()
                val allEps = repository.getAllEpisodes()
                val file = PdfExporter.generateProgressPdf(
                    getApplication(),
                    summary,
                    currentAnimeList,
                    allEps
                )
                if (file != null && file.exists()) {
                    _exportUiState.value = ExportUiState.Success(file, "PDF generated successfully!", "PDF")
                } else {
                    _exportUiState.value = ExportUiState.Error("Failed to generate PDF document.")
                }
            } catch (e: Exception) {
                _exportUiState.value = ExportUiState.Error(e.localizedMessage ?: "PDF generation error")
            }
        }
    }

    fun exportJsonArchive() {
        _exportUiState.value = ExportUiState.Exporting
        viewModelScope.launch {
            try {
                val currentAnimeList = repository.allAnime.first()
                val summary = repository.getUserSummary()
                val allEps = repository.getAllEpisodes()
                val file = repository.exportJsonArchive(currentAnimeList, allEps, summary)
                if (file != null && file.exists()) {
                    _exportUiState.value = ExportUiState.Success(file, "JSON Archive saved to device storage!", "JSON")
                } else {
                    _exportUiState.value = ExportUiState.Error("Failed to export JSON archive.")
                }
            } catch (e: Exception) {
                _exportUiState.value = ExportUiState.Error(e.localizedMessage ?: "JSON export error")
            }
        }
    }

    fun exportMarkdownArchive() {
        _exportUiState.value = ExportUiState.Exporting
        viewModelScope.launch {
            try {
                val currentAnimeList = repository.allAnime.first()
                val summary = repository.getUserSummary()
                val file = repository.exportMarkdownArchive(currentAnimeList, summary)
                if (file != null && file.exists()) {
                    _exportUiState.value = ExportUiState.Success(file, "Markdown summary report exported!", "Markdown")
                } else {
                    _exportUiState.value = ExportUiState.Error("Failed to export Markdown report.")
                }
            } catch (e: Exception) {
                _exportUiState.value = ExportUiState.Error(e.localizedMessage ?: "Markdown export error")
            }
        }
    }

    fun resetExportState() {
        _exportUiState.value = ExportUiState.Idle
    }

    private fun computeUserSummary(animeList: List<AnimeEntity>): UserProgressSummary {
        val totalTracked = animeList.size
        val watching = animeList.count { it.watchStatus.equals("Watching", ignoreCase = true) }
        val completed = animeList.count { it.watchStatus.equals("Completed", ignoreCase = true) }
        val planToWatch = animeList.count { it.watchStatus.equals("Plan to Watch", ignoreCase = true) }
        val onHold = animeList.count { it.watchStatus.equals("On Hold", ignoreCase = true) }
        val dropped = animeList.count { it.watchStatus.equals("Dropped", ignoreCase = true) }

        val totalEpsWatched = animeList.sumOf { it.episodesWatched }
        val totalMins = totalEpsWatched * 24L

        val scoredAnime = animeList.filter { it.userScore > 0 }
        val avgScore = if (scoredAnime.isNotEmpty()) scoredAnime.map { it.userScore }.average().toFloat() else 8.5f

        return UserProgressSummary(
            totalAnimeTracked = totalTracked,
            watchingCount = watching,
            completedCount = completed,
            planToWatchCount = planToWatch,
            onHoldCount = onHold,
            droppedCount = dropped,
            totalEpisodesWatched = totalEpsWatched,
            totalMinutesWatched = totalMins,
            averageScoreGiven = avgScore
        )
    }
}
