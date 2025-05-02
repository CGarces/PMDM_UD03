package com.campusdigitalfp.filmotecav2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.campusdigitalfp.filmotecav2.navigation.Navigation
import com.campusdigitalfp.filmotecav2.ui.theme.FilmotecaV2Theme
import com.campusdigitalfp.filmotecav2.viewmodel.FilmViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = FirebaseFirestore.getInstance() // Inicializa Firestore
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                MemoryCacheSettings.newBuilder().build() // Usa solo memoria RAM, sin guardar datos en disco.
            ).build()

        enableEdgeToEdge()
        setContent {
            FilmotecaV2Theme {
                val viewModel: FilmViewModel = viewModel()
                Navigation(viewModel)
            }
        }
    }
}
