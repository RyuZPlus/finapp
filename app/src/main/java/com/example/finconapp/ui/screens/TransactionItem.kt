package com.example.finconapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "$%.2f".format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = transaction.type,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar movimiento"
                )
            }
        }
    }
}