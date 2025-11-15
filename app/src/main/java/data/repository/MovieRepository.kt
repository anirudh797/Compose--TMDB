package data.repository

import data.model.Movie
import data.model.MovieResponse
import data.network.ResponseWrapper
import data.network.RetrofitInstance

class MovieRepository {
    suspend fun getTrendingMovies(): ResponseWrapper<List<Movie>> {
        return try {
            val response: MovieResponse = RetrofitInstance.api.getTrendingMovies() // should return MovieResponse
            val movies = response.results
            if (movies.isNotEmpty()) {
                ResponseWrapper.Success(movies)
            } else {
                ResponseWrapper.EmptySuccess
            }
        } catch (e: Exception) {
            ResponseWrapper.Error(e.localizedMessage ?: "Unknown error", e)
        }
    }
}

