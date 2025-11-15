package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import data.model.Movie
import data.repository.MovieRepository
import data.network.ResponseWrapper
import kotlinx.coroutines.Dispatchers

class MainViewModel : ViewModel() {
    private val repository = MovieRepository()
    private val _movies = MutableStateFlow<ResponseWrapper<List<Movie>>>(ResponseWrapper.EmptySuccess)
    val movies = _movies.asStateFlow()

    fun fetchMovies() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getTrendingMovies()
            _movies.value = response
        }
    }
}
