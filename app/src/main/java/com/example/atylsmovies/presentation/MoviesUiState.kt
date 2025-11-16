package com.example.atylsmovies.presentation

import com.example.atylsmovies.data.model.Movie

sealed class MoviesUiState {
    data object Loading : MoviesUiState()
    data class Success(val movies: List<Movie>) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
    data object Empty : MoviesUiState()
}

