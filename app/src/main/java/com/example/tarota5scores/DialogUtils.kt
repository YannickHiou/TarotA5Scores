package com.example.tarota5scores

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terminer la partie") },
        //text = { Text("Est-tu sûr de terminer la partie ?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Oui")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Non")
            }
        }
    )
}