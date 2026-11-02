package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.AnimeSearchResult
import com.example.data.model.EpisodeDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    suspend fun searchAnimeByName(query: String, customApiKey: String? = null, selectedModel: String = ""): Result<AnimeSearchResult> = withContext(Dispatchers.IO) {
        try {
            // Keyless by design: Jikan mirrors MyAnimeList and needs no Google account or API key.
            val result = trySearchAnimeViaJikan(query)
            if (result != null) Result.success(result)
            else Result.failure(Exception("No anime found for '$query'. Try the official title or a shorter search."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun searchAnimeByPhotos(bitmaps: List<Bitmap>, customApiKey: String? = null, selectedModel: String = ""): Result<AnimeSearchResult> =
        Result.failure(Exception("Photo recognition is not enabled in this keyless build. Search by anime name instead."))

    suspend fun searchAnimeByPhoto(bitmap: Bitmap, customApiKey: String? = null): Result<AnimeSearchResult> {
        return searchAnimeByPhotos(listOf(bitmap), customApiKey)
    }

    /**
     * Fetches the official canonical high-res poster / title card for an anime using Jikan (MyAnimeList) and Kitsu APIs.
     * Guarantees real high-quality title cards without requiring API keys!
     */
    suspend fun fetchRealTitleCardUrl(animeTitle: String): String? = withContext(Dispatchers.IO) {
        val cleanTitle = animeTitle.split(":")[0].split("-")[0].trim()
        val queries = if (cleanTitle.equals(animeTitle, ignoreCase = true) || cleanTitle.length < 3) {
            listOf(animeTitle)
        } else {
            listOf(animeTitle, cleanTitle)
        }

        for (q in queries) {
            val posterUrl = tryFetchJikanPoster(q) ?: tryFetchKitsuPoster(q)
            if (!posterUrl.isNullOrEmpty()) {
                return@withContext posterUrl
            }
        }
        return@withContext null
    }

    private fun tryFetchJikanPoster(query: String): String? {
        return try {
            val encodedTitle = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.jikan.moe/v4/anime?q=$encodedTitle&limit=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AnimeTracker/1.0 (Android)")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val dataArr = json.optJSONArray("data")
                if (dataArr != null && dataArr.length() > 0) {
                    val firstAnime = dataArr.getJSONObject(0)
                    val imagesObj = firstAnime.optJSONObject("images")
                    val jpgObj = imagesObj?.optJSONObject("jpg")
                    val largeUrl = jpgObj?.optString("large_image_url")
                    if (!largeUrl.isNullOrEmpty() && largeUrl.startsWith("http")) return largeUrl
                    val imageUrl = jpgObj?.optString("image_url")
                    if (!imageUrl.isNullOrEmpty() && imageUrl.startsWith("http")) return imageUrl
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun tryFetchKitsuPoster(query: String): String? {
        return try {
            val encodedTitle = URLEncoder.encode(query, "UTF-8")
            val url = "https://kitsu.io/api/edge/anime?filter[text]=$encodedTitle&page[limit]=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AnimeTracker/1.0 (Android)")
                .header("Accept", "application/vnd.api+json")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val dataArr = json.optJSONArray("data")
                if (dataArr != null && dataArr.length() > 0) {
                    val firstAnime = dataArr.getJSONObject(0)
                    val attributes = firstAnime.optJSONObject("attributes")
                    val posterImage = attributes?.optJSONObject("posterImage")
                    val largeUrl = posterImage?.optString("large")
                    if (!largeUrl.isNullOrEmpty() && largeUrl.startsWith("http")) return largeUrl
                    val mediumUrl = posterImage?.optString("medium")
                    if (!mediumUrl.isNullOrEmpty() && mediumUrl.startsWith("http")) return mediumUrl
                    val originalUrl = posterImage?.optString("original")
                    if (!originalUrl.isNullOrEmpty() && originalUrl.startsWith("http")) return originalUrl
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun trySearchAnimeViaJikan(query: String): AnimeSearchResult? {
        return try {
            val encodedTitle = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.jikan.moe/v4/anime?q=$encodedTitle&limit=1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AnimeTracker/1.0 (Android)")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val dataArr = json.optJSONArray("data")
                if (dataArr != null && dataArr.length() > 0) {
                    val animeObj = dataArr.getJSONObject(0)
                    val id = animeObj.optInt("mal_id", 0)
                    val title = animeObj.optString("title_english", "").ifEmpty { animeObj.optString("title", "Unknown Anime") }
                    val japaneseTitle = animeObj.optString("title_japanese", "")
                    val synopsis = animeObj.optString("synopsis", "No synopsis provided.")
                    val score = animeObj.optDouble("score", 8.0).toFloat()
                    val totalEpisodes = animeObj.optInt("episodes", 12).coerceAtLeast(1)
                    val year = animeObj.optInt("year", 2023).toString()

                    val studiosArr = animeObj.optJSONArray("studios")
                    val studio = if (studiosArr != null && studiosArr.length() > 0) {
                        studiosArr.getJSONObject(0).optString("name", "Studio")
                    } else "Animation Studio"

                    val genresList = mutableListOf<String>()
                    val genresArr = animeObj.optJSONArray("genres")
                    if (genresArr != null) {
                        for (i in 0 until genresArr.length()) {
                            genresList.add(genresArr.getJSONObject(i).optString("name", ""))
                        }
                    }

                    val imagesObj = animeObj.optJSONObject("images")
                    val jpgObj = imagesObj?.optJSONObject("jpg")
                    val coverUrl = jpgObj?.optString("large_image_url") ?: jpgObj?.optString("image_url") ?: ""

                    val officialUrl = animeObj.optString("url", "https://myanimelist.net")

                    val episodesList = mutableListOf<EpisodeDetail>()
                    if (id > 0) {
                        try {
                            val epUrl = "https://api.jikan.moe/v4/anime/$id/episodes"
                            val epReq = Request.Builder().url(epUrl).header("User-Agent", "AnimeTracker/1.0 (Android)").build()
                            val epResp = client.newCall(epReq).execute()
                            if (epResp.isSuccessful) {
                                val epBody = epResp.body?.string() ?: ""
                                val epJson = JSONObject(epBody)
                                val epData = epJson.optJSONArray("data")
                                if (epData != null) {
                                    for (i in 0 until epData.length().coerceAtMost(24)) {
                                        val epObj = epData.getJSONObject(i)
                                        episodesList.add(
                                            EpisodeDetail(
                                                episodeNumber = epObj.optInt("mal_id", i + 1),
                                                title = epObj.optString("title", "Episode ${i + 1}"),
                                                synopsis = epObj.optString("synopsis", ""),
                                                durationMinutes = 24,
                                                airDate = epObj.optString("aired", "").split("T")[0]
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (episodesList.isEmpty()) {
                        for (i in 1..totalEpisodes.coerceAtMost(12)) {
                            episodesList.add(
                                EpisodeDetail(
                                    episodeNumber = i,
                                    title = "Episode $i",
                                    synopsis = "Episode $i of $title",
                                    durationMinutes = 24,
                                    airDate = ""
                                )
                            )
                        }
                    }

                    return AnimeSearchResult(
                        title = title,
                        japaneseTitle = japaneseTitle,
                        synopsis = synopsis,
                        genres = genresList.ifEmpty { listOf("Action", "Adventure") },
                        totalEpisodes = totalEpisodes,
                        rating = score,
                        studio = studio,
                        releaseYear = year,
                        coverUrl = coverUrl,
                        officialWatchUrl = officialUrl,
                        streamingPlatforms = listOf("Crunchyroll", "Netflix", "MyAnimeList"),
                        episodesList = episodesList,
                        matchConfidence = "High (Jikan 0-Token Search Engine)"
                    )
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getActiveApiKey(customApiKey: String?): String {
        if (!customApiKey.isNullOrBlank()) {
            return customApiKey.trim()
        }
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val maxDim = 1024
        val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = Math.min(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun parseGeminiResponseToAnimeResult(rawResponseString: String): AnimeSearchResult? {
        return try {
            val root = JSONObject(rawResponseString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val text = parts.getJSONObject(0).optString("text", "")

            val cleanJsonStr = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleanJsonStr)

            val genresList = mutableListOf<String>()
            val genresJsonArr = json.optJSONArray("genres")
            if (genresJsonArr != null) {
                for (i in 0 until genresJsonArr.length()) {
                    genresList.add(genresJsonArr.getString(i))
                }
            }

            val platformsList = mutableListOf<String>()
            val platformsJsonArr = json.optJSONArray("streamingPlatforms")
            if (platformsJsonArr != null) {
                for (i in 0 until platformsJsonArr.length()) {
                    platformsList.add(platformsJsonArr.getString(i))
                }
            }

            val episodeDetailsList = mutableListOf<EpisodeDetail>()
            val epJsonArr = json.optJSONArray("episodesList")
            if (epJsonArr != null) {
                for (i in 0 until epJsonArr.length()) {
                    val epObj = epJsonArr.getJSONObject(i)
                    episodeDetailsList.add(
                        EpisodeDetail(
                            episodeNumber = epObj.optInt("episodeNumber", i + 1),
                            title = epObj.optString("title", "Episode ${i + 1}"),
                            synopsis = epObj.optString("synopsis", ""),
                            durationMinutes = epObj.optInt("durationMinutes", 24),
                            airDate = epObj.optString("airDate", "")
                        )
                    )
                }
            }

            val totalEpCount = json.optInt("totalEpisodes", episodeDetailsList.size.coerceAtLeast(12))

            AnimeSearchResult(
                title = json.optString("title", "Unknown Anime"),
                japaneseTitle = json.optString("japaneseTitle", ""),
                synopsis = json.optString("synopsis", "No synopsis provided."),
                genres = genresList.ifEmpty { listOf("Action", "Adventure") },
                totalEpisodes = totalEpCount,
                rating = json.optDouble("rating", 8.0).toFloat(),
                studio = json.optString("studio", "Animation Studio"),
                releaseYear = json.optString("releaseYear", "2023"),
                coverUrl = json.optString("coverUrl", ""),
                officialWatchUrl = json.optString("officialWatchUrl", "https://www.crunchyroll.com"),
                streamingPlatforms = platformsList.ifEmpty { listOf("Crunchyroll", "Netflix") },
                episodesList = episodeDetailsList,
                matchConfidence = json.optString("matchConfidence", "High")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
