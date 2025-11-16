package com.example.atylsmovies.data.repository

import android.content.Context
import com.example.atylsmovies.data.local.AppDatabase
import com.example.atylsmovies.data.local.MovieEntity
import com.example.atylsmovies.data.model.Movie
import com.example.atylsmovies.data.model.MovieResponse
import com.example.atylsmovies.data.network.ResponseWrapper
import com.example.atylsmovies.data.network.RetrofitInstance
import com.example.atylsmovies.utils.NetworkUtils

class MovieRepository(private val appContext: Context) {
    private val db = AppDatabase.get(appContext)
    private val dao = db.movieDao()

    private fun Movie.toEntity() = MovieEntity(
        id = id,
        title = title ?: "",
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        mediaType = mediaType,
        originalLanguage = originalLanguage,
        popularity = popularity,
        releaseDate = releaseDate,
        video = video,
        voteAverage = voteAverage,
        voteCount = voteCount,
        adult = adult
    )
    private fun MovieEntity.toModel() = Movie(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        mediaType = mediaType,
        originalLanguage = originalLanguage,
        genreIds = emptyList(), // not stored locally
        popularity = popularity,
        releaseDate = releaseDate,
        video = video,
        voteAverage = voteAverage,
        voteCount = voteCount,
        adult = adult
    )

    suspend fun getTrendingMovies(): ResponseWrapper<List<Movie>> {
        val online = NetworkUtils.isOnline(appContext)
        return if (online) {
            // Online path: return network success or error, no cache fallback
            try {
                val response = RetrofitInstance.api.getTrendingMovies(NetworkUtils.apiKey())
                if (response.isSuccessful) {
                    val body: MovieResponse? = response.body()
                    val movies = body?.results.orEmpty()
                    if (movies.isNotEmpty()) {
                        // Persist latest
                        dao.clear()
                        dao.upsertAll(movies.map { it.toEntity() })
                        ResponseWrapper.Success(movies)
                    } else {
                        ResponseWrapper.EmptySuccess
                    }
                } else {
                    ResponseWrapper.Error("HTTP ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                ResponseWrapper.Error(e.localizedMessage ?: "Something went wrong", e)
            }
        } else {
            // Offline path: serve cache if available, else error
            val cached = dao.getAll().map { it.toModel() }
            if (cached.isNotEmpty()) ResponseWrapper.Success(cached)
            else ResponseWrapper.Error("No internet connection and no cached data available")
        }
    }
}
