
package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.PendingPayment
import com.example.finconapp.ui.viewmodel.PendingPaymentViewModel
import com.example.finconapp.ui.components.formatCurrency
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PendingPaymentScreen(
    paddingValues: PaddingValues,
    viewModel: PendingPaymentViewModel,
    categories: List<Category>,
    onCreateCategory: (String, (Int?) -> Unit) -> Unit
) {
    val payments by viewModel.pendingPayments.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var paymentToManage by remember { mutableStateOf<PendingPayment?>(null) }
    var paymentToEdit by remember { mutableStateOf<PendingPayment?>(null) }
    var paymentToDelete by remember { mutableStateOf<PendingPayment?>(null) }

    // Solo se consideran compromisos que todavía tienen saldo.
    val activePayments = payments.filter {
        it.status != "PAID"
    }

    val totalAmount = activePayments.sumOf { it.amount }
    val totalPaid = activePayments.sumOf { it.paidAmount }
    val totalRemaining = activePayments.sumOf {
        (it.amount - it.paidAmount).coerceAtLeast(0.0)
    }

    val progress = if (totalAmount > 0.0) {
        (totalPaid / totalAmount).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pagos pendientes",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(onClick = { showAddDialog = true }) {
                Text("Agregar")
            }
        }

        Spacer(Modifier.height(16.dp))

        // RESUMEN
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "Total por pagar",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = formatCurrency(totalRemaining),
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "${(progress * 100).toInt()}% liquidado",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Abonado: ${formatCurrency(totalPaid)} de ${formatCurrency(totalAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Compromisos registrados",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        if (payments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay pagos pendientes registrados.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = payments,
                    key = { it.id }
                ) { payment ->
                    PendingPaymentCard(
                        payment = payment,
                        categories = categories,
                        onManage = { paymentToManage = payment },
                        onEdit = { paymentToEdit = payment },
                        onDelete = { paymentToDelete = payment }
                    )
                }
            }
        }
    }

    // AGREGAR
    if (showAddDialog) {
        PendingPaymentFormDialog(
            payment = null,
            categories = categories,
            onDismiss = { showAddDialog = false },
            onCreateCategory = onCreateCategory,
            onConfirm = { newPayment ->
                viewModel.insert(newPayment)
                showAddDialog = false
            }
        )
    }

    // REGISTRAR ABONO
    paymentToManage?.let { payment ->
        RegisterPaymentDialog(
            payment = payment,
            onDismiss = { paymentToManage = null },
            onConfirm = { amount ->
                viewModel.registerPayment(payment, amount)
                paymentToManage = null
            }
        )
    }

    // EDITAR
    paymentToEdit?.let { payment ->
        PendingPaymentFormDialog(
            payment = payment,
            categories = categories,
            onDismiss = { paymentToEdit = null },
            onCreateCategory = onCreateCategory,
            onConfirm = { updatedPayment ->
                viewModel.update(updatedPayment)
                paymentToEdit = null
            }
        )
    }

    // ELIMINAR
    paymentToDelete?.let { payment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Eliminar pago") },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar \"${payment.name}\"? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(payment)
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PendingPaymentCard(
    payment: PendingPayment,
    categories: List<Category>,
    onManage: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryName = categories
        .find { it.id == payment.categoryId }
        ?.name ?: "Categoría desconocida"

    val remaining = (payment.amount - payment.paidAmount)
        .coerceAtLeast(0.0)

    val progress = if (payment.amount > 0.0) {
        (payment.paidAmount / payment.amount)
            .coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    val isPaid = payment.status == "PAID"
    val isOverdue = !isPaid && payment.dueDate < startOfToday()

    val statusText = when {
        isPaid -> "Pagado"
        isOverdue -> "Vencido"
        else -> "Pendiente"
    }

    val statusColor = when {
        isPaid -> MaterialTheme.colorScheme.primary
        isOverdue -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = payment.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar pago"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar pago",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Abonado",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        formatCurrency(payment.paidAmount),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Saldo pendiente",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        formatCurrency(remaining),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${(progress * 100).toInt()}% liquidado",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Vencimiento: ${formatPaymentDate(payment.dueDate)}",
                style = MaterialTheme.typography.bodySmall
            )

            if (!isPaid) {
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onManage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar abono")
                }
            }
        }
    }
}

@Composable
private fun RegisterPaymentDialog(
    payment: PendingPayment,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember(payment.id) {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val remaining = (payment.amount - payment.paidAmount)
        .coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar abono") },
        text = {
            Column {
                Text("Pago: ${payment.name}")

                Spacer(Modifier.height(8.dp))

                Text("Importe total: ${formatCurrency(payment.amount)}")
                Text("Abonado: ${formatCurrency(payment.paidAmount)}")
                Text("Restante: ${formatCurrency(remaining)}")

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Importe del abono") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { Text(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.trim()
                        .replace(",", ".")
                        .toDoubleOrNull()

                    when {
                        amount == null ||
                                !amount.isFinite() ||
                                amount <= 0.0 -> {
                            errorMessage = "Introduce un importe válido."
                        }

                        amount > remaining -> {
                            errorMessage = "El abono supera el saldo pendiente."
                        }

                        else -> onConfirm(amount)
                    }
                }
            ) {
                Text("Guardar abono")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingPaymentFormDialog(
    payment: PendingPayment?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCreateCategory: (String, (Int?) -> Unit) -> Unit,
    onConfirm: (PendingPayment) -> Unit
) {
    var name by remember(payment?.id) {
        mutableStateOf(payment?.name ?: "")
    }

    var amountText by remember(payment?.id) {
        mutableStateOf(payment?.amount?.toString() ?: "")
    }

    var categoryId by remember(payment?.id) {
        mutableIntStateOf(payment?.categoryId ?: categories.firstOrNull()?.id ?: 0)
    }

    var dueDate by remember(payment?.id) {
        mutableStateOf(payment?.dueDate ?: startOfToday())
    }

    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedCategory = categories.find { it.id == categoryId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (payment == null) "Nuevo pago" else "Editar pago")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nombre del pago") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name
                            ?: "Selecciona una categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    categoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { showNewCategory = !showNewCategory }
                ) {
                    Text(
                        if (showNewCategory) "Cancelar nueva categoría"
                        else "Crear categoría"
                    )
                }

                if (showNewCategory) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Nombre de la categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(
                        onClick = {
                            val value = newCategoryName.trim()
                            if (value.isNotBlank()) {
                                onCreateCategory(value) { createdId ->
                                    if (createdId != null) {
                                        categoryId = createdId
                                        newCategoryName = ""
                                        showNewCategory = false
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Guardar categoría")
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Importe total") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (payment != null) {
                    Text(
                        text = "Importe abonado: ${formatCurrency(payment.paidAmount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vencimiento: ${formatPaymentDate(dueDate)}")
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleanName = name.trim()
                    val amount = amountText.trim()
                        .replace(",", ".")
                        .toDoubleOrNull()

                    when {
                        cleanName.isBlank() -> {
                            errorMessage = "Introduce el nombre del pago."
                        }

                        selectedCategory == null -> {
                            errorMessage = "Selecciona una categoría válida."
                        }

                        amount == null ||
                                !amount.isFinite() ||
                                amount <= 0.0 -> {
                            errorMessage = "El importe debe ser mayor que cero."
                        }

                        payment != null && amount < payment.paidAmount -> {
                            errorMessage = "El importe no puede ser menor que lo ya abonado."
                        }

                        else -> {
                            val paid = payment?.paidAmount ?: 0.0

                            onConfirm(
                                PendingPayment(
                                    id = payment?.id ?: 0,
                                    name = cleanName,
                                    categoryId = categoryId,
                                    amount = amount,
                                    paidAmount = paid,
                                    dueDate = dueDate,
                                    reminderDate = payment?.reminderDate,
                                    status = if (paid >= amount) "PAID" else "PENDING",
                                    description = payment?.description
                                )
                            )
                        }
                    }
                }
            ) {
                Text(if (payment == null) "Guardar pago" else "Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun startOfToday(): Long {
    return Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun formatPaymentDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "MX")
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(timestamp))
}