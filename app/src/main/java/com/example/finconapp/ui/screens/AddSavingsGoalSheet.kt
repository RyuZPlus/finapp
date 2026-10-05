package com.example.finconapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.SavingsGoal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (SavingsGoal) -> Unit,
    onCreateCategory: (String, (Int?) -> Unit) -> Unit
) {

    val context = LocalContext.current

    val scrollState = rememberScrollState()

    // --------------------------------
    // CAMPOS
    // --------------------------------

    var name by remember {
        mutableStateOf("")
    }

    var selectedCategoryId by remember {
        mutableStateOf<Int?>(null)
    }

    var targetAmount by remember {
        mutableStateOf("")
    }

    var currentAmount by remember {
        mutableStateOf("")
    }

    var selectedDate by remember {
        mutableStateOf<Long?>(null)
    }

    // --------------------------------
    // NUEVA CATEGORÍA
    // --------------------------------

    var createNewCategory by remember {
        mutableStateOf(false)
    }

    var newCategory by remember {
        mutableStateOf("")
    }

    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var categoryError by remember {
        mutableStateOf(false)
    }

    var newCategoryError by remember {
        mutableStateOf(false)
    }

    // --------------------------------
    // ERRORES
    // --------------------------------

    var nameError by remember {
        mutableStateOf(false)
    }

    var targetError by remember {
        mutableStateOf(false)
    }

    var currentAmountError by remember {
        mutableStateOf(false)
    }

    // --------------------------------
    // FORMATO FECHA
    // --------------------------------

    val dateFormatter = remember {
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("es", "MX")
        )
    }

    // --------------------------------
    // BOTTOM SHEET
    // --------------------------------

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding()
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
                text = "Nueva meta de ahorro",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // --------------------------------
            // NOMBRE
            // --------------------------------

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = {
                    Text("Nombre")
                },
                isError = nameError,
                supportingText = {

                    if (nameError) {
                        Text("Ingresa un nombre")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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

            // --------------------------------
            // CREAR NUEVA CATEGORÍA
            // --------------------------------

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Checkbox(
                    checked = createNewCategory,
                    onCheckedChange = {

                        createNewCategory = it
                        categoryError = false
                        newCategoryError = false

                        if (it) {
                            selectedCategoryId = null
                        } else {
                            newCategory = ""
                        }
                    }
                )

                Text(
                    text = "Crear nueva categoría",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // --------------------------------
            // CATEGORÍA NUEVA
            // --------------------------------

            if (createNewCategory) {

                OutlinedTextField(
                    value = newCategory,
                    onValueChange = {
                        newCategory = it
                        newCategoryError = false
                    },
                    label = {
                        Text("Nueva categoría")
                    },
                    isError = newCategoryError,
                    supportingText = {

                        if (newCategoryError) {
                            Text("Ingresa el nombre de la categoría")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

            } else {

                // --------------------------------
                // CATEGORÍAS EXISTENTES
                // --------------------------------

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = {
                        categoryExpanded = !categoryExpanded
                    }
                ) {

                    val selectedCategory =
                        categories.find {
                            it.id == selectedCategoryId
                        }

                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Seleccionar categoría")
                        },
                        isError = categoryError,
                        supportingText = {

                            if (categoryError) {
                                Text("Selecciona una categoría")
                            }
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
                                    Text("No hay categorías registradas")
                                },
                                onClick = {
                                    categoryExpanded = false
                                }
                            )

                        } else {

                            categories.forEach { category ->

                                DropdownMenuItem(
                                    text = {
                                        Text(category.name)
                                    },
                                    onClick = {

                                        selectedCategoryId =
                                            category.id

                                        categoryError = false
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // --------------------------------
            // OBJETIVO
            // --------------------------------

            OutlinedTextField(
                value = targetAmount,
                onValueChange = {
                    targetAmount = it
                    targetError = false
                    currentAmountError = false
                },
                label = {
                    Text("Objetivo")
                },
                isError = targetError,
                supportingText = {

                    if (targetError) {
                        Text("Ingresa un objetivo mayor a $0")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // --------------------------------
            // AHORRO ACTUAL
            // --------------------------------

            OutlinedTextField(
                value = currentAmount,
                onValueChange = {
                    currentAmount = it
                    currentAmountError = false
                },
                label = {
                    Text("Ahorro actual (opcional)")
                },
                isError = currentAmountError,
                supportingText = {

                    if (currentAmountError) {
                        Text(
                            "El ahorro actual no puede superar el objetivo"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // --------------------------------
            // FECHA LÍMITE
            // --------------------------------

            OutlinedButton(
                onClick = {

                    val calendar = Calendar.getInstance()

                    selectedDate?.let {
                        calendar.timeInMillis = it
                    }

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->

                            calendar.set(
                                year,
                                month,
                                dayOfMonth,
                                0,
                                0,
                                0
                            )

                            calendar.set(
                                Calendar.MILLISECOND,
                                0
                            )

                            selectedDate =
                                calendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector =
                        Icons.Default.CalendarToday,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = selectedDate?.let {
                        "Fecha límite: ${dateFormatter.format(it)}"
                    } ?: "Agregar fecha límite"
                )
            }

            if (selectedDate != null) {

                TextButton(
                    onClick = {
                        selectedDate = null
                    }
                ) {

                    Text("Quitar fecha")
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            // --------------------------------
            // GUARDAR
            // --------------------------------

            Button(
                onClick = {

                    // -------------------------
                    // CONVERSIONES
                    // -------------------------

                    val target =
                        targetAmount
                            .replace(",", ".")
                            .toDoubleOrNull()

                    val current =
                        currentAmount
                            .ifBlank { "0" }
                            .replace(",", ".")
                            .toDoubleOrNull()

                    // -------------------------
                    // VALIDACIONES
                    // -------------------------

                    nameError =
                        name.trim().isBlank()

                    categoryError =
                        !createNewCategory &&
                                selectedCategoryId == null

                    newCategoryError =
                        createNewCategory &&
                                newCategory.trim().isBlank()

                    targetError =
                        target == null ||
                                target <= 0

                    currentAmountError =
                        current == null ||
                                current < 0 ||
                                (
                                        target != null &&
                                                current > target
                                        )

                    if (
                        nameError ||
                        categoryError ||
                        newCategoryError ||
                        targetError ||
                        currentAmountError
                    ) {
                        return@Button
                    }

                    // -------------------------
                    // GUARDAR META
                    // -------------------------

                    if (createNewCategory) {

                        /*
                         * Primero creamos la categoría.
                         *
                         * El callback devolverá el ID
                         * generado por Room.
                         */
                        onCreateCategory(
                            newCategory.trim()
                        ) { categoryId ->

                            if (categoryId != null) {

                                onSave(
                                    SavingsGoal(
                                        name = name.trim(),
                                        categoryId = categoryId,
                                        targetAmount = target!!,
                                        initialAmount = current!!,
                                        deadline = selectedDate
                                    )
                                )
                            }
                        }

                    } else {

                        /*
                         * La categoría ya existe,
                         * por lo que usamos directamente
                         * su ID.
                         */
                        onSave(
                            SavingsGoal(
                                name = name.trim(),
                                categoryId =
                                    selectedCategoryId!!,
                                targetAmount = target!!,
                                initialAmount = current!!,
                                deadline = selectedDate
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Guardar meta")
            }
        }
    }
}