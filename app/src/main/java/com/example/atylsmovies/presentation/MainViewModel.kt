package com.example.atylsmovies.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.atylsmovies.data.model.Movie
import com.example.atylsmovies.data.repository.MovieRepository
import com.example.atylsmovies.data.network.ResponseWrapper
import kotlinx.coroutines.Dispatchers

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application.applicationContext)

    // UI state exposed to the composables
    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun fetchMovies() {
        // Show loading first
        _uiState.value = MoviesUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            when (val response: ResponseWrapper<List<Movie>> = repository.getTrendingMovies()) {
                is ResponseWrapper.Success -> {
                    val data = response.data
                    _uiState.value = if (data.isNotEmpty()) MoviesUiState.Success(data) else MoviesUiState.Empty
                }
                is ResponseWrapper.EmptySuccess -> {
                    _uiState.value = MoviesUiState.Empty
                }
                is ResponseWrapper.Error -> {
                    _uiState.value = MoviesUiState.Error(response.message)
                }
            }
        }
    }
}
