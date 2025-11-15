package data.network

import com.example.atylsmovies.utils.NetworkUtils
import data.model.Movie
import data.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MoviesApi {
    @GET(NetworkUtils.TRENDING_MOVIES_ENDPOINT)
    suspend fun getTrendingMovies(@Query("api_key") apiKey: String = NetworkUtils.API_KEY): MovieResponse
}
