
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
import com.example.finconapp.data.local.entity.PendingPayment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPendingPaymentSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (PendingPayment) -> Unit,
    onCreateCategory: (String, (Int?) -> Unit) -> Unit
) {

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --------------------------------
    // CAMPOS
    // --------------------------------

    var name by remember { mutableStateOf("") }

    var selectedCategoryId by remember {
        mutableStateOf<Int?>(null)
    }

    var amount by remember { mutableStateOf("") }

    var paidAmount by remember {
        mutableStateOf("")
    }

    var description by remember {
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

    var amountError by remember {
        mutableStateOf(false)
    }

    var paidAmountError by remember {
        mutableStateOf(false)
    }

    var dateError by remember {
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
                text = "Nuevo pago pendiente",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                    Text("Nombre del pago")
                },
                placeholder = {
                    Text("Ej. Servicio de internet")
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

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------
            // CATEGORÍA
            // --------------------------------

            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                            .menuAnchor(
                                ExposedDropdownMenuAnchorType.PrimaryNotEditable
                            )
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

                                        selectedCategoryId = category.id
                                        categoryError = false
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------
            // IMPORTE TOTAL
            // --------------------------------

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it
                    amountError = false
                    paidAmountError = false
                },
                label = {
                    Text("Importe total")
                },
                placeholder = {
                    Text("0.00")
                },
                isError = amountError,
                supportingText = {
                    if (amountError) {
                        Text("Ingresa un importe mayor a $0")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------
            // IMPORTE ABONADO
            // --------------------------------

            OutlinedTextField(
                value = paidAmount,
                onValueChange = {
                    paidAmount = it
                    paidAmountError = false
                },
                label = {
                    Text("Importe abonado (opcional)")
                },
                placeholder = {
                    Text("0.00")
                },
                isError = paidAmountError,
                supportingText = {
                    if (paidAmountError) {
                        Text(
                            "El abono no puede ser negativo ni superar el importe total"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------
            // FECHA DE VENCIMIENTO
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

                            selectedDate = calendar.timeInMillis
                            dateError = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = selectedDate?.let {
                        "Vencimiento: ${dateFormatter.format(it)}"
                    } ?: "Seleccionar vencimiento"
                )
            }

            if (dateError) {
                Text(
                    text = "Selecciona una fecha de vencimiento",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
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

            Spacer(modifier = Modifier.height(12.dp))

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
                placeholder = {
                    Text("Detalles adicionales del pago")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --------------------------------
            // GUARDAR
            // --------------------------------

            Button(
                onClick = {

                    val total =
                        amount
                            .replace(",", ".")
                            .toDoubleOrNull()

                    val paid =
                        paidAmount
                            .ifBlank { "0" }
                            .replace(",", ".")
                            .toDoubleOrNull()

                    // -------------------------
                    // VALIDACIONES
                    // -------------------------

                    nameError = name.trim().isBlank()

                    categoryError =
                        !createNewCategory &&
                                selectedCategoryId == null

                    newCategoryError =
                        createNewCategory &&
                                newCategory.trim().isBlank()

                    amountError =
                        total == null ||
                                !total.isFinite() ||
                                total <= 0.0

                    paidAmountError =
                        paid == null ||
                                !paid.isFinite() ||
                                paid < 0.0 ||
                                (total != null && paid > total)

                    dateError = selectedDate == null

                    if (
                        nameError ||
                        categoryError ||
                        newCategoryError ||
                        amountError ||
                        paidAmountError ||
                        dateError
                    ) {
                        return@Button
                    }

                    // -------------------------
                    // CREAR PAGO
                    // -------------------------

                    val totalAmount = total!!
                    val initialPaidAmount = paid!!

                    val paymentStatus =
                        if (initialPaidAmount >= totalAmount) {
                            "PAID"
                        } else {
                            "PENDING"
                        }

                    // -------------------------
                    // GUARDAR CON NUEVA CATEGORÍA
                    // -------------------------

                    if (createNewCategory) {

                        onCreateCategory(
                            newCategory.trim()
                        ) { categoryId ->

                            if (categoryId != null) {

                                onSave(
                                    PendingPayment(
                                        name = name.trim(),
                                        categoryId = categoryId,
                                        amount = totalAmount,
                                        paidAmount = initialPaidAmount,
                                        dueDate = selectedDate!!,
                                        status = paymentStatus,
                                        description = description
                                            .trim()
                                            .ifBlank { null }
                                    )
                                )
                            }
                        }

                    } else {

                        // -------------------------
                        // CATEGORÍA EXISTENTE
                        // -------------------------

                        onSave(
                            PendingPayment(
                                name = name.trim(),
                                categoryId = selectedCategoryId!!,
                                amount = totalAmount,
                                paidAmount = initialPaidAmount,
                                dueDate = selectedDate!!,
                                status = paymentStatus,
                                description = description
                                    .trim()
                                    .ifBlank { null }
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar pago")
            }
        }
    }
}
