package com.campusdigitalfp.filmotecav2.viewmodel

import android.app.Application
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

// Se crea una instancia de BeginSignInRequest para iniciar el proceso de autenticación con Google
private val signInRequest = BeginSignInRequest.builder()
    .setGoogleIdTokenRequestOptions( // Configuración de la solicitud de token de ID de Google
        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true) // Habilita el soporte para autenticación con Google
            .setServerClientId("1072231720699-4vfvg9oa9c6bhnlkusfov7eq138qg10j.apps.googleusercontent.com")
            // Establece el Client ID del servidor, necesario para verificar el token de ID en el backend

            .setFilterByAuthorizedAccounts(false)
            // Permite mostrar todas las cuentas de Google disponibles en el dispositivo,
            // en lugar de restringirse solo a cuentas que ya han iniciado sesión previamente en la app

            .build() // Construye la configuración de solicitud de token de Google
    )
    .build() // Construye el objeto BeginSignInRequest con las opciones configuradas

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        // Inicia el proceso de creación de un usuario con email y contraseña en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task -> // Se añade un listener para detectar cuando el proceso ha finalizado
                if (task.isSuccessful) {
                    // Si el registro es exitoso, se llama a la función de resultado con 'true' y sin mensaje de error
                    onResult(true, null)
                } else {
                    // Si ocurre un error, se traduce el mensaje de error de Firebase a un formato más comprensible
                    val errorMessage = translateErrorFirebase(task.exception)
                    // Se llama a la función de resultado con 'false' y se pasa el mensaje de error
                    onResult(false, errorMessage)
                }
            }
    }

    private fun translateErrorFirebase(exception: Exception?): String {
        // Se verifica si la excepción es de tipo FirebaseAuthException y se obtiene su código de error
        return when ((exception as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo electrónico no tiene un formato válido."
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta o el usuario no tiene contraseña."
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo."
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada."
            "ERROR_TOO_MANY_REQUESTS" -> "Has realizado demasiados intentos, intenta más tarde."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya está registrado en otra cuenta."
            "ERROR_NETWORK_REQUEST_FAILED" -> "No se pudo conectar a la red. Verifica tu conexión."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil. Usa una más segura."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Este correo ya está registrado con otro método de inicio de sesión."
            else -> "Ocurrió un error desconocido. Intenta nuevamente."
        }
    }

    fun loginUser(email: String?, password: String?, onResult: (Boolean, String?) -> Unit) {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            onResult(false, "El correo y la contraseña no pueden estar vacíos.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = translateErrorFirebase(task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    // Inicializa el cliente de One Tap Sign-In de Google
    private val oneTapClient: SignInClient = Identity.getSignInClient(application)

    // Función suspendida que inicia el flujo de autenticación con Google y devuelve un IntentSenderRequest
    suspend fun signInWithGoogle(): IntentSenderRequest? {
        return try {
            // Llama al cliente de One Tap para iniciar el proceso de autenticación con Google
            val result = oneTapClient.beginSignIn(signInRequest).await()

            // Construye y devuelve el IntentSenderRequest a partir del pendingIntent obtenido
            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        } catch (e: Exception) {
            // Si ocurre un error durante el proceso, se devuelve null
            null
        }
    }
    suspend fun handleGoogleSignInResult(credential: SignInCredential): Boolean {
        // Obtiene el ID Token de Google desde las credenciales
        val googleIdToken = credential.googleIdToken ?: return false

        // Crea las credenciales de Firebase basadas en el ID Token de Google
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

        return try {
            // Intenta autenticar al usuario en Firebase con las credenciales de Google
            auth.signInWithCredential(firebaseCredential).await()
            true // Retorna true si la autenticación es exitosa
        } catch (e: Exception) {
            false // Retorna false si ocurre un error durante la autenticación
        }
    }
}