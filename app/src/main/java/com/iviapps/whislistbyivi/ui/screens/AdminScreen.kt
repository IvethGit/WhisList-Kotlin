
package com.iviapps.whislistbyivi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iviapps.whislistbyivi.model.Product
import com.iviapps.whislistbyivi.model.ProductStatus
import com.iviapps.whislistbyivi.services.EmailClient
import com.iviapps.whislistbyivi.services.EmailData

import com.iviapps.whislistbyivi.services.EmailUser
import com.iviapps.whislistbyivi.ui.components.StatusDropdown
import retrofit2.Call

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(ProductStatus.WANT_IT) }
    var isFavorite by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var editingProductId by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf(listOf<Product>()) }

    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                products = snapshot.documents.mapNotNull {
                    it.toObject(Product::class.java)
                }
            }
        }
    }

    fun sendBrevoEmail(productName: String, productLink: String) {
        val email = EmailData(
            sender = EmailUser(email = "admin_tester@gmail.com", name = "WhisList By Ivi"),
            to = listOf(
                EmailUser(email = "client_tester@gmail.com", name = "Cliente"),
                EmailUser(email = "admin_tester@gmail.com", name = "Tester")
            ),
            subject = "Nuevo producto añadido",
            htmlContent = """
            <h2>🎉 ¡Nuevo producto añadido!</h2>
            <h3><strong>$productName</strong></h3>
            <p><a href="$productLink">Ver producto</a></p>
        """.trimIndent()
        )

        val call = EmailClient.service.sendEmail(email)
        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                Toast.makeText(
                    context,
                    if (response.isSuccessful) "Correo enviado con éxito" else "Error ${response.code()}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }



    val clearForm = {
        name = ""
        description = ""
        link = ""
        isFavorite = false
        selectedStatus = ProductStatus.WANT_IT
        editingProductId = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin: Wishlist de Ivi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = if (editingProductId == null) "Agregar Producto" else "Editar Producto",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del producto") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("Link del producto") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                StatusDropdown(selectedStatus) { selectedStatus = it }
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                    Text("¿Favorito?", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotEmpty() && description.isNotEmpty() && link.isNotEmpty()) {
                            isLoading = true

                            if (editingProductId == null) {
                                val productId = db.collection("products").document().id
                                val newProduct = hashMapOf(
                                    "id" to productId,
                                    "name" to name,
                                    "description" to description,
                                    "link" to link,
                                    "status" to selectedStatus.name,
                                    "favorite" to isFavorite,
                                    "dateAchieved" to Timestamp.now()
                                )
                                db.collection("products").document(productId).set(newProduct)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show()
                                        sendBrevoEmail(name, link)
                                        clearForm()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al agregar: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                    .addOnCompleteListener {
                                        isLoading = false
                                    }
                            } else {
                                val updatedProduct = mapOf(
                                    "name" to name,
                                    "description" to description,
                                    "link" to link,
                                    "status" to selectedStatus.name,
                                    "favorite" to isFavorite
                                )
                                db.collection("products").document(editingProductId!!).update(updatedProduct)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                                        clearForm()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al actualizar: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                    .addOnCompleteListener {
                                        isLoading = false
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoading) "Guardando..." else if (editingProductId == null) "Guardar Producto" else "Actualizar Producto")
                }
                Spacer(modifier = Modifier.height(32.dp))

                Text("Productos existentes", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            name = product.name
                            description = product.description
                            link = product.link
                            selectedStatus = product.status
                            isFavorite = product.favorite
                            editingProductId = product.id
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text(product.description, style = MaterialTheme.typography.bodySmall)
                        Text(product.link, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                db.collection("products").document(product.id).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                        if (editingProductId == product.id) clearForm()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al eliminar: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🗑 Eliminar", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }
    }
}
