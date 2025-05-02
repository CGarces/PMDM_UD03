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
     * Item de ejemplo con valores por defecto.
     */
    fun addExampleFilms() {
        val film =  Film(
            title = "Película por defecto",
            director = "Director Desconocido",
            imageResId = R.drawable.icono_pelicula,
            comments = "Esta es una película de ejemplo para la aplicación.",
            format = Film.FORMAT_DVD,
            genre = Film.GENRE_ACTION,
            imdbUrl = "http://www.imdb.com",
            year = 2025
        )
        viewModelScope.launch {
            repository.addFilm (film) // Inserta todas de una vez.
        }
    }

    fun deleteSelectedFilms(selectedFilms: List<Film>) {
        viewModelScope.launch {
            repository.deleteMultipleFilms(selectedFilms)
            fetchFilms() // Recarga la lista tras la eliminación.
        }
    }
}