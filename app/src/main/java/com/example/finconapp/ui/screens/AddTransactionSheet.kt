package com.example.finconapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onCreateCategory: (String) -> Unit
) {

    val context = LocalContext.current

    // --------------------------------
    // SCROLL
    // --------------------------------

    val scrollState = rememberScrollState()

    // --------------------------------
    // CAMPOS
    // --------------------------------

    var title by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var type by remember {
        mutableStateOf("Gasto")
    }

    var category by remember {
        mutableStateOf("")
    }

    var subcategory by remember {
        mutableStateOf("")
    }

    // --------------------------------
    // CATEGORÍA NUEVA
    // --------------------------------

    var createNewCategory by remember {
        mutableStateOf(false)
    }

    var newCategory by remember {
        mutableStateOf("")
    }

    // --------------------------------
    // FECHA
    // --------------------------------

    var selectedDate by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    // --------------------------------
    // DROPDOWN
    // --------------------------------

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    // --------------------------------
    // CATEGORÍAS
    // --------------------------------

    // --------------------------------
    // FORMATO DE FECHA
    // --------------------------------

    val dateFormatter = remember {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )
    }

    // --------------------------------
    // BOTTOM SHEET
    // --------------------------------

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .imePadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 32.dp
                )
        ) {

            // --------------------------------
            // TÍTULO
            // --------------------------------

            Text(
                text = "Nuevo movimiento",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // --------------------------------
            // TÍTULO DEL MOVIMIENTO
            // --------------------------------

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                label = {
                    Text("Título")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // --------------------------------
            // MONTO
            // --------------------------------

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                },
                label = {
                    Text("Monto")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // --------------------------------
            // TIPO
            // --------------------------------

            Text(
                text = "Tipo",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected = type == "Gasto",
                    onClick = {
                        type = "Gasto"
                    },
                    label = {
                        Text("Gasto")
                    }
                )

                FilterChip(
                    selected = type == "Ingreso",
                    onClick = {
                        type = "Ingreso"
                    },
                    label = {
                        Text("Ingreso")
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // --------------------------------
            // FECHA
            // --------------------------------

            Text(
                text = "Fecha",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            OutlinedButton(
                onClick = {

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                    }

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->

                            val newCalendar =
                                Calendar.getInstance().apply {

                                    set(
                                        year,
                                        month,
                                        dayOfMonth,
                                        0,
                                        0,
                                        0
                                    )

                                    set(
                                        Calendar.MILLISECOND,
                                        0
                                    )
                                }

                            selectedDate =
                                newCalendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = dateFormatter.format(
                        Date(selectedDate)
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // --------------------------------
            // CATEGORÍA
            // --------------------------------

            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Checkbox(
                    checked = createNewCategory,
                    onCheckedChange = {

                        createNewCategory = it

                        if (it) {
                            category = ""
                        } else {
                            newCategory = ""
                        }
                    }
                )

                Text(
                    text = "Crear nueva categoría",
                    modifier = Modifier.padding(
                        top = 12.dp
                    )
                )
            }

            // --------------------------------
            // NUEVA CATEGORÍA
            // --------------------------------

            if (createNewCategory) {

                OutlinedTextField(
                    value = newCategory,
                    onValueChange = {
                        newCategory = it
                    },
                    label = {
                        Text("Nueva categoría")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

            } else {

                // --------------------------------
                // DROPDOWN CATEGORÍA
                // --------------------------------

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = {
                        categoryExpanded = !categoryExpanded
                    }
                ) {

                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Seleccionar categoría")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = categoryExpanded
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = {
                            categoryExpanded = false
                        }
                    ) {

                        if (categories.isEmpty()) {

                            DropdownMenuItem(
                                text = {
                                    Text("No hay categorías")
                                },
                                onClick = {
                                    categoryExpanded = false
                                }
                            )

                        } else {

                            categories.forEach { item ->

                                DropdownMenuItem(
                                    text = {
                                        Text(item.name)
                                    },
                                    onClick = {

                                        category = item.name

                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // --------------------------------
            // SUBCATEGORÍA
            // --------------------------------

            OutlinedTextField(
                value = subcategory,
                onValueChange = {
                    subcategory = it
                },
                label = {
                    Text("Subcategoría (opcional)")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // --------------------------------
            // DESCRIPCIÓN
            // --------------------------------

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                label = {
                    Text("Descripción (opcional)")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // --------------------------------
            // GUARDAR
            // --------------------------------

            Button(
                onClick = {

                    val finalCategory =
                        if (createNewCategory) {
                            newCategory.trim()
                        } else {
                            category.trim()
                        }

                    // Si es una categoría nueva,
                    // primero la guardamos en Room.
                    if (
                        createNewCategory &&
                        finalCategory.isNotBlank()
                    ) {

                        onCreateCategory(finalCategory)
                    }

                    val transaction = Transaction(

                        title = title.trim(),

                        amount = amount
                            .toDoubleOrNull()
                            ?: 0.0,

                        description = description
                            .trim()
                            .ifBlank {
                                null
                            },

                        type = type,

                        category = finalCategory,

                        subcategory = subcategory
                            .trim()
                            .ifBlank {
                                null
                            },

                        date = selectedDate
                    )

                    onSave(transaction)
                },

                modifier = Modifier.fillMaxWidth(),

                enabled =
                    title.isNotBlank() &&
                            amount.toDoubleOrNull() != null &&
                            amount.toDoubleOrNull()!! > 0 &&
                            (
                                    category.isNotBlank() ||
                                            newCategory.isNotBlank()
                                    )
            ) {

                Text("Guardar")
            }

            // --------------------------------
            // ESPACIO EXTRA
            // --------------------------------

            Spacer(
                modifier = Modifier.height(80.dp)
            )
        }
    }
}