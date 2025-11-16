package com.example.atylsmovies.data.network

import com.example.atylsmovies.data.model.MovieResponse
import com.example.atylsmovies.utils.NetworkUtils
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MoviesApi {
    @GET(NetworkUtils.TRENDING_MOVIES_ENDPOINT)
    suspend fun getTrendingMovies(@Query("api_key") apiKey: String): Response<MovieResponse>
}
