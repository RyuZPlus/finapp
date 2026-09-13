package com.example.finconapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilters(
    selectedType: String,
    onTypeSelected: (String) -> Unit,

    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        /*
         * TIPO
         */
        FilterChip(
            selected = selectedType == "Todos",
            onClick = {
                onTypeSelected("Todos")
            },
            label = {
                Text("Todos")
            }
        )

        FilterChip(
            selected = selectedType == "Ingreso",
            onClick = {
                onTypeSelected("Ingreso")
            },
            label = {
                Text("Ingresos")
            }
        )

        FilterChip(
            selected = selectedType == "Gasto",
            onClick = {
                onTypeSelected("Gasto")
            },
            label = {
                Text("Gastos")
            }
        )

        /*
         * CATEGORÍA
         */
        Box {

            FilterChip(
                selected = selectedCategory != null,
                onClick = {
                    categoryExpanded = true
                },
                label = {
                    Text(
                        selectedCategory
                            ?: "Categoría"
                    )
                }
            )

            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = {
                    categoryExpanded = false
                }
            ) {

                DropdownMenuItem(
                    text = {
                        Text("Todas")
                    },
                    onClick = {

                        onCategorySelected(null)

                        categoryExpanded = false
                    }
                )

                categories.forEach { category ->

                    DropdownMenuItem(
                        text = {
                            Text(category)
                        },
                        onClick = {

                            onCategorySelected(
                                category
                            )

                            categoryExpanded = false
                        }
                    )
                }
            }
        }
    }
}