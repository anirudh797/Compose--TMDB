package com.example.atylsmovies.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.atylsmovies.presentation.theme.AtylsMoviesTheme
import com.example.atylsmovies.presentation.theme.Typography
import com.example.atylsmovies.data.model.Movie
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mainViewModel: MainViewModel by viewModels()
        setContent {
            AtylsMoviesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   NavHostComposable(mainViewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NavHostComposable(viewModel: MainViewModel,modifier: Modifier){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ScreenName.MainScreen.route,
        modifier = modifier
    ) {
        composable(ScreenName.MainScreen.route) {
            Movies(viewModel = viewModel, modifier = modifier.padding(horizontal = 8.dp) , navController)
        }

        composable(
            route = ScreenName.DetailScreen.route
        )
        { backStackEntry ->
            val movie = navController.previousBackStackEntry?.savedStateHandle?.get<Movie>("movie")
            val movieId = movie?.id ?: 0
            val backdropPath = movie?.backdropPath ?: ""
            val title = movie?.title ?: ""
            val overview = movie?.overview ?: ""
            val releaseDate = movie?.releaseDate ?: ""
            DetailScreenComposable(
                backdropPath = backdropPath,
                title = title,
                overview = overview,
                releaseDate = releaseDate,
                onBack = { navController.popBackStack() },
                modifier = Modifier
            )
        }

    }
}

@Composable
fun Movies(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    var retry by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Initial fetch
    LaunchedEffect(Unit) { viewModel.fetchMovies() }

    // Re-fetch when retry toggles true, then reset to false
    LaunchedEffect(retry) {
        if (retry) {
            viewModel.fetchMovies()
            retry = false
        }
    }


    var filteredMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }

    LaunchedEffect(uiState, searchQuery) {
        if (uiState is MoviesUiState.Success) {
            snapshotFlow { searchQuery }
                .debounce(300)
                .collect { debouncedQuery ->
                    Log.d("MoviesComposable", "Debounced Query: $debouncedQuery")
                    filteredMovies = if (debouncedQuery.isBlank()) {
                        (uiState as MoviesUiState.Success).movies
                    } else {
                        (uiState as MoviesUiState.Success).movies.filter { movie ->
                            movie.title.contains(debouncedQuery, ignoreCase = true)
                        }
                    }
                }
        }
    }

    when (val state = uiState) {
        is MoviesUiState.Loading -> LoadingState(modifier)
        is MoviesUiState.Success -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    placeholder = { Text("Search movies") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 22.dp, top = 16.dp)
                )


                val showEmptyState = filteredMovies.isEmpty() && searchQuery.isNotBlank()

                // Content based on filtered results
                if (showEmptyState) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found.",
                            style = Typography.bodyMedium
                        )
                    }
                } else {
                    ShowMovieList(
                        modifier = Modifier.weight(1f),
                        movies = filteredMovies
                    ) { movie ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("movie", movie)
                        navController.navigate(
                            ScreenName.DetailScreen.createRoute(
                                movieId = movie.id,
                            )
                        )
                    }
                }
            }
        }
        is MoviesUiState.Error -> {
            ErrorState(modifier = modifier, message = state.message, onRetry = { retry = true })
        }
        is MoviesUiState.Empty -> EmptyState(modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(modifier: Modifier = Modifier, message: String, onRetry: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Error: $message", style = Typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // Retry button
            androidx.compose.material3.Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No movies found.", style = Typography.bodyMedium)
    }
}



@Composable
fun DetailScreenComposable(
    backdropPath: String,
    title: String,
    overview: String,
    releaseDate: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        // Top back button row
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            val imageUrl = "https://image.tmdb.org/t/p/w500$backdropPath"
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .height(360.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = title, style = Typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = overview, style = Typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Release Date: $releaseDate", style = Typography.bodyMedium)
        }
    }

}

@Composable
fun ShowMovieList(
    modifier: Modifier = Modifier,
    movies: List<Movie>,
    navigateToDetailScreen : (movie : Movie) -> Unit
) {
    LazyColumn(modifier = modifier) {
        val rows = movies.chunked(2)
        items(rows) { rowMovies ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (movie in rowMovies) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clickable {
                                navigateToDetailScreen(movie)
                            }
                    ) {
                        val imageUrl = movie.backdropPath?.let { "https://image.tmdb.org/t/p/w500$it" }
                        if (imageUrl != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Transparent)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = movie.title,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .height(172.dp)
                                        .fillMaxWidth(),
                                )
                            }

                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = movie.title,
                            style = Typography.bodyMedium
                        )
                    }
                }
                if (rowMovies.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AtylsMoviesTheme {

    }
}