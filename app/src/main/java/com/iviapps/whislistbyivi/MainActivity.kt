package com.iviapps.whislistbyivi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.iviapps.whislistbyivi.ui.screens.AdminScreen
import com.iviapps.whislistbyivi.ui.screens.LoginScreen
import com.iviapps.whislistbyivi.ui.screens.MainScreen
import com.google.firebase.auth.FirebaseAuth
import com.iviapps.whislistbyivi.ui.theme.WhisListByIviTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WhisListByIviTheme {
                // 🌐 Estado de autenticación
                var isCheckingAuth by remember { mutableStateOf(true) }
                var isUserLoggedIn by remember { mutableStateOf(false) }
                var userEmail by remember { mutableStateOf<String?>(null) }

                val auth = FirebaseAuth.getInstance()

                // 🔍 Comprobar si ya hay sesión iniciada
                LaunchedEffect(Unit) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        isUserLoggedIn = true
                        userEmail = currentUser.email
                    }
                    isCheckingAuth = false
                }

                // ⏳ Pantalla de carga inicial
                if (isCheckingAuth) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cargando...", style = MaterialTheme.typography.headlineMedium)
                    }
                } else {
                    // 🔑 Si no ha iniciado sesión
                    if (!isUserLoggedIn) {
                        LoginScreen(
                            onLoginSuccess = {
                                isUserLoggedIn = true
                                userEmail = auth.currentUser?.email
                            }
                        )
                    } else {
                        // 👩‍💼 Admin o usuario normal
                        if (userEmail == "barrezuetaiveth@gmail.com") {
                            AdminScreen(
                                onLogout = {
                                    isUserLoggedIn = false
                                    userEmail = null
                                }
                            )
                        } else {
                            MainScreen(
                                onLogout = {
                                    isUserLoggedIn = false
                                    userEmail = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
