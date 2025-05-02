package com.campusdigitalfp.filmotecav2.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.filmotecav2.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.identity.Identity

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    // Variables de estado para almacenar el email, la contraseña y los mensajes de error
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Manejador del resultado del login con Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // Verifica si la autenticación con Google fue exitosa
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // Obtiene las credenciales de inicio de sesión desde la respuesta de Google
            val credential = Identity.getSignInClient(context)
                .getSignInCredentialFromIntent(result.data)

            // Lanza una corrutina para procesar el resultado del inicio de sesión
            scope.launch {
                // Intenta autenticar al usuario en Firebase con las credenciales obtenidas
                val success = viewModel.handleGoogleSignInResult(credential)
                if (success) {
                    // Si la autenticación es exitosa, navega a la pantalla principal
                    navController.navigate("list")
                } else {
                    // En caso de error, muestra un mensaje de error
                    errorMessage = "Error al autenticar con Google"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de la pantalla de inicio de sesión
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de entrada de email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de entrada de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para iniciar sesión con email y contraseña
        Button(
            onClick = {
                isLoading = true
                viewModel.loginUser(email, password) { success, error ->
                    isLoading = false
                    if (success) {
                        navController.navigate("list")
                    } else {
                        errorMessage = error
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Iniciar Sesión")
        }

        // Botón para iniciar sesión con Google
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val signInRequest = viewModel.signInWithGoogle()

                    if (signInRequest != null) {
                        googleSignInLauncher.launch(signInRequest)
                    } else {
                        errorMessage = "No se pudo iniciar sesión con Google"
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text("Iniciar Sesión con Google")
        }

        // Indicador de carga mientras se realiza el proceso de autenticación
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }

        // Mostrar mensaje de error en caso de fallo en la autenticación
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para ir a la pantalla de registro
        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
