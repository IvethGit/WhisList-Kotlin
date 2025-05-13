package com.iviapps.whislistbyivi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iviapps.whislistbyivi.model.Product
import com.iviapps.whislistbyivi.ui.components.ProductCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var products by remember { mutableStateOf(listOf<Product>()) }
    var previousList by remember { mutableStateOf(listOf<Product>()) }

    var searchText by remember { mutableStateOf("") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 🔄 Escuchar Firestore en tiempo real
    LaunchedEffect(Unit) {
        db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val updatedList = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }

                    if (updatedList.size > previousList.size && previousList.isNotEmpty()) {
                        val newProduct = updatedList
                            .filterNot { old -> previousList.any { it.id == old.id } }
                            .maxByOrNull { it.dateAchieved ?: com.google.firebase.Timestamp(0, 0) }

                        newProduct?.let {
                            val message = "🎉 Nuevo: ${it.name}\n🔗 ${it.link}"
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    }

                    previousList = products
                    products = updatedList
                }
            }
    }

    val filteredProducts = products.filter {
        it.name.contains(searchText, ignoreCase = true) &&
                (!showOnlyFavorites || it.favorite)
    }

    // UI con colores personalizados
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Wishlist de Ivi", color = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        onLogout()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar producto...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showOnlyFavorites,
                        onCheckedChange = { showOnlyFavorites = it }
                    )
                    Text("Solo favoritos", modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            items(filteredProducts) { product ->
                ProductCard(product)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
