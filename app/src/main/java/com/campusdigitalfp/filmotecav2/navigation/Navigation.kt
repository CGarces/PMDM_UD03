package com.campusdigitalfp.filmotecav2.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.filmotecav2.screens.AboutScreen
import com.campusdigitalfp.filmotecav2.screens.FilmDataScreen
import com.campusdigitalfp.filmotecav2.screens.FilmEditScreen
import com.campusdigitalfp.filmotecav2.screens.FilmListScreen
import com.campusdigitalfp.filmotecav2.viewmodel.FilmViewModel

@Composable
fun Navigation(viewModel: FilmViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") { FilmListScreen(navController, viewModel) }
        composable("data/{filmIndex}") { backStackEntry ->
            val filmIndex = backStackEntry.arguments?.getString("filmIndex")
            filmIndex?.let {
                FilmDataScreen(navController, filmIndex = it, viewModel)
            }
        }
        composable("edit/{filmIndex}") { backStackEntry ->
            val filmIndex = backStackEntry.arguments?.getString("filmIndex")
            filmIndex?.let {
                FilmEditScreen(navController, filmIndex = it)
            }
        }
        composable("about") { AboutScreen(navController) }
    }
}
