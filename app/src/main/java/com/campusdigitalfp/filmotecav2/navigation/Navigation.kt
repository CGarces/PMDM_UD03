package com.campusdigitalfp.filmotecav2.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.filmotecav2.screens.AboutScreen
import com.campusdigitalfp.filmotecav2.screens.FilmDataScreen
import com.campusdigitalfp.filmotecav2.screens.FilmEditScreen
import com.campusdigitalfp.filmotecav2.screens.FilmListScreen
import com.campusdigitalfp.filmotecav2.screens.LoginScreen
import com.campusdigitalfp.filmotecav2.viewmodel.AuthViewModel
import com.campusdigitalfp.filmotecav2.viewmodel.FilmViewModel
import com.google.firebase.auth.FirebaseAuth

fun comprobarUsuario(): Boolean {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    return currentUser != null
}

@Composable
fun Navigation(viewModel: FilmViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    // Define la pantalla de inicio dependiendo de si el usuario está autenticado o no.
    val startDestination =
        if (comprobarUsuario())
            "list"  // Si está autenticado, ir a la lista de hábitos
        else
            "login" // Si no está autenticado, ir a la pantalla de login


    NavHost(navController = navController, startDestination = startDestination) {
        // Ruta para la pantalla de registro
        //composable("register") {
        //    RegisterScreen(navController, authViewModel)
        //}
        composable("list") {
            if (comprobarUsuario())
                FilmListScreen(navController, viewModel)
            else
                LoginScreen(navController, authViewModel)
        }
        composable("data/{filmIndex}") {
            backStackEntry ->
            val filmIndex = backStackEntry.arguments?.getString("filmIndex")
            filmIndex?.let {
                if (comprobarUsuario())
                    FilmDataScreen(navController, filmIndex = it, viewModel)
                else
                    LoginScreen(navController, authViewModel)
            }
        }
        composable("edit/{filmIndex}") { backStackEntry ->
            val filmIndex = backStackEntry.arguments?.getString("filmIndex")
            filmIndex?.let {
                if (comprobarUsuario())
                    FilmDataScreen(navController, filmIndex = it, viewModel)
                else
                    FilmEditScreen(navController, filmIndex = it)
            }
        }
        composable("about") { AboutScreen(navController) }
    }
}
