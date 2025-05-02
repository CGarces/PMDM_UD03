package com.campusdigitalfp.filmotecav2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.filmotecav2.model.Film
import com.campusdigitalfp.filmotecav2.repository.FilmRepository
import com.campusdigitalfp.filmotecav2.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar películas.
 * Usa el repositorio para interactuar con Firestore.
 */
class FilmViewModel : ViewModel() {

    // Repositorio para operaciones de Firestore.
    private val repository = FilmRepository()

    // Lista interna de películas.
    private val _films = MutableStateFlow<List<Film>>(emptyList())

    // Lista de películas para la UI.
    val films: StateFlow<List<Film>> get() = _films

    // Inicia escucha de cambios al crear el ViewModel.
    init {
        listenToFilms()
    }

    /**
     * Escucha cambios en la colección de películas.
     * Actualiza la UI automáticamente.
     */
    private fun listenToFilms() {
        repository.listenToFilmsUpdates { updatedFilms ->
            _films.value = updatedFilms
        }
    }

    /**
     * Obtiene películas de Firestore.
     * Usa corrutinas para no bloquear la UI.
     */
    private fun fetchFilms() {
        viewModelScope.launch {
            _films.value = repository.getFilms()
        }
    }

    /**
     * Añade una película a Firestore.
     */
    fun addFilm(film: Film) {
        viewModelScope.launch {
            repository.addFilm(film)
            fetchFilms() // Recarga la lista.
        }
    }

    /**
     * Actualiza una película en Firestore.
     */
    fun updateFilm(film: Film) {
        viewModelScope.launch {
            repository.updateFilm(film)
            fetchFilms()
        }
    }

    /**
     * Borra una película por su ID.
     */
    fun deleteFilm(filmId: String) {
        viewModelScope.launch {
            repository.deleteFilm(filmId)
            fetchFilms()
        }
    }

    /**
     * Funcion temporal, se usa una unica vez para poder probar la conexion.
     */
    fun addExampleFilms() {
        val films = listOf(
            Film(
                title = "Harry Potter y la piedra filosofal",
                director = "Chris Columbus",
                imageResId = R.drawable.harry_potter_y_la_piedra_filosofal,
                comments = "Una aventura mágica en Hogwarts.",
                format = Film.FORMAT_DVD,
                genre = Film.GENRE_ACTION,
                imdbUrl = "http://www.imdb.com/title/tt0241527",
                year = 2001,
            ),
        )

        viewModelScope.launch {
            repository.addMultipleFilms(films) // Inserta todas de una vez.
        }
    }
}