package presentation

import android.os.Bundle
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.atylsmovies.ui.theme.AtylsMoviesTheme
import com.example.atylsmovies.ui.theme.Typography
import data.model.Movie
import data.network.ResponseWrapper

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
    val moviesState by viewModel.movies.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.fetchMovies()
    }
    when (moviesState) {
        is ResponseWrapper.Success -> {
            val movies = (moviesState as ResponseWrapper.Success<List<Movie>>).data
            ShowMovieList(modifier = modifier, movies){
                navController.currentBackStackEntry?.savedStateHandle?.set("movie", it)
                navController.navigate(
                    ScreenName.DetailScreen.createRoute(
                        movieId = it.id,
                    )
                )
            }
        }
        is ResponseWrapper.Error -> {
            val error = (moviesState as ResponseWrapper.Error).message
            Text(text = "Error: $error", modifier = modifier)
        }
        is ResponseWrapper.EmptySuccess -> {
            Text(text = "No movies found.", modifier = modifier)
        }
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