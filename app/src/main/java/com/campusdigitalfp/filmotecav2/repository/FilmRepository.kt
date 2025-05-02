package com.campusdigitalfp.filmotecav2.repository
import android.util.Log
import com.campusdigitalfp.filmotecav2.model.Film
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Conecta con Firestore para gestionar películas.
 */
class FilmRepository {

    // Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Referencia a la colección "films" donde guardamos las películas
    private val filmsCollection = db.collection("films")

    /**
     * Guarda una película nueva en Firestore.
     * Usa corrutina para no bloquear la interfaz.
     */
    suspend fun addFilm(film: Film) {
        filmsCollection.add(film).await()
    }

    /**
     * Obtiene todas las películas guardadas.
     * Si hay error, devuelve lista vacía.
     */
    suspend fun getFilms(): List<Film> {
        return try {
            val snapshot = filmsCollection.get().await() // Obtiene los documentos
            snapshot.documents.mapNotNull { it.toObject(Film::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList() // Lista vacía si hay error
        }
    }

    /**
     * Actualiza una película existente.
     */
    suspend fun updateFilm(film: Film) {
        filmsCollection.document(film.id).set(film).await()
    }

    /**
     * Borra una película por ID.
     */
    suspend fun deleteFilm(filmId: String) {
        filmsCollection.document(filmId).delete().await()
    }

    /**
     * Escucha cambios en tiempo real de las películas.
     * Avisa cuando hay cambios para actualizar la interfaz.
     */
    fun listenToFilmsUpdates(onUpdate: (List<Film>) -> Unit) {
        filmsCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("HS_error", "Error al obtener películas: ${exception.message}")
                return@addSnapshotListener
            }

            // Convierte documentos a objetos Film
            val films = snapshot?.documents?.mapNotNull { it.toObject(Film::class.java)?.copy(id = it.id) } ?: emptyList()
            onUpdate(films) // Avisa con la lista actualizada
        }
    }

    /**
     * Añade varias películas de una vez.
     * Más rápido que añadir una a una.
     */
    suspend fun addMultipleFilms(films: List<Film>) {
        val batch = db.batch() // Agrupa operaciones juntas

        films.forEach { film ->
            val newDocRef = filmsCollection.document() // Nuevo ID para cada película
            batch.set(newDocRef, film.copy(id = newDocRef.id)) // Guarda la película
        }

        try {
            batch.commit().await() // Ejecuta todo a la vez
            Log.i("HS_info", "Películas añadidas correctamente")
        } catch (e: Exception) {
            Log.e("HS_error", "Error al añadir películas: ${e.message}")
        }
    }

    /**
     * Borra varias películas a la vez.
     * Ahorra tiempo y recursos.
     */
    suspend fun deleteMultipleFilms(films: List<Film>) {
        val batch = db.batch() // Agrupa operaciones

        films.forEach { film ->
            film.id.let { filmId ->
                batch.delete(filmsCollection.document(filmId)) // Marca para borrar
            }
        }

        try {
            batch.commit().await() // Borra todo junto
            Log.i("HS_info", "Películas borradas correctamente")
        } catch (e: Exception) {
            Log.e("HS_error", "Error al borrar películas: ${e.message}")
        }
    }
}