package com.example.atylsmovies.data.repository

import com.example.atylsmovies.data.model.Movie
import com.example.atylsmovies.data.model.MovieResponse
import com.example.atylsmovies.data.network.ResponseWrapper
import com.example.atylsmovies.data.network.RetrofitInstance

class MovieRepository {
    suspend fun getTrendingMovies(): ResponseWrapper<List<Movie>> {
        return try {
            val response = RetrofitInstance.api.getTrendingMovies()
            if (response.isSuccessful) {
                val body: MovieResponse? = response.body()
                val movies = body?.results.orEmpty()
                if (movies.isNotEmpty()) {
                    ResponseWrapper.Success(movies)
                } else {
                    ResponseWrapper.EmptySuccess
                }
            } else {
                ResponseWrapper.Error("HTTP ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ResponseWrapper.Error( "Something went wrong", e)
        }
    }
}
