package com.iviapps.whislistbyivi.ui.components

import androidx.compose.foundation.layout.Box // 👈 ESTE IMPORT FALTABA
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.iviapps.whislistbyivi.model.ProductStatus

@Composable
fun StatusDropdown(
    selectedStatus: ProductStatus,
    onStatusSelected: (ProductStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(text = when (selectedStatus) {
                ProductStatus.HAVE_IT -> "Lo tengo"
                ProductStatus.IN_PROGRESS -> "En progreso"
                ProductStatus.WANT_IT -> "Lo quiero"
            })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Lo tengo") },
                onClick = {
                    onStatusSelected(ProductStatus.HAVE_IT)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("En progreso") },
                onClick = {
                    onStatusSelected(ProductStatus.IN_PROGRESS)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Lo quiero") },
                onClick = {
                    onStatusSelected(ProductStatus.WANT_IT)
                    expanded = false
                }
            )
        }
    }
}
