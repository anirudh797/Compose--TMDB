package presentation

sealed class ScreenName(val route: String) {
    object MainScreen : ScreenName("MovieList")
    object DetailScreen : ScreenName("detail_screen/{movieId}") {
        fun createRoute(
            movieId: Int,
        ) = "detail_screen/$movieId"
    }
}