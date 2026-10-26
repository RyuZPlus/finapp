package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.SavingsGoal
import com.example.finconapp.ui.viewmodel.SavingsGoalViewModel
import com.example.finconapp.ui.components.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Edit

@Composable
fun SavingsScreen(
    paddingValues: PaddingValues,
    viewModel: SavingsGoalViewModel,
    categories: List<Category>,
    onCreateCategory: (String, (Int?) -> Unit) -> Unit
) {

    val savingsGoals by
    viewModel.savingsGoals.collectAsState()

    var showAddGoalSheet by remember {
        mutableStateOf(false)
    }

    var selectedGoal by remember {
        mutableStateOf<SavingsGoal?>(null)
    }

    var goalToEdit by remember {
        mutableStateOf<SavingsGoal?>(null)
    }

    var goalToDelete by remember {
        mutableStateOf<SavingsGoal?>(null)
    }

    // --------------------------------
    // AHORROS TOTALES
    // --------------------------------

    val totalTarget =
        savingsGoals.sumOf {
            it.targetAmount
        }

    val totalSaved =
        savingsGoals.sumOf {
            it.initialAmount
        }

    val totalProgress =
        if (totalTarget > 0) {
            min(
                totalSaved / totalTarget,
                1.0
            ).toFloat()
        } else {
            0f
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------
        // HEADER
        // --------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "Ahorros",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = {
                    showAddGoalSheet = true
                }
            ) {
                Text("Agregar meta")
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------
        // AHORROS TOTALES
        // --------------------------------

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Ahorros totales",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "${formatCurrency(totalSaved)} de ${
                        formatCurrency(totalTarget)
                    }",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LinearProgressIndicator(
                    progress = {
                        totalProgress
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "${(totalProgress * 100).toInt()}% del objetivo total",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // --------------------------------
        // TÍTULO
        // --------------------------------

        Text(
            text = "Metas de ahorro",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // --------------------------------
        // LISTA
        // --------------------------------

        if (savingsGoals.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "No hay metas de ahorro registradas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
                contentPadding =
                    PaddingValues(bottom = 24.dp)
            ) {

                items(
                    items = savingsGoals,
                    key = {
                        it.id
                    }
                ) { goal ->

                    SavingsGoalCard(
                        goal = goal,
                        categories = categories,
                        onUpdateSavings = {
                            selectedGoal = goal
                        },
                        onEditGoal = {
                            goalToEdit = goal
                        },
                        onDeleteGoal = {
                            goalToDelete = goal
                        }
                    )
                }
            }
        }
    }

    // --------------------------------
    // NUEVA META
    // --------------------------------

    if (showAddGoalSheet) {
        AddSavingsGoalSheet(
            categories = categories,
            onDismiss = {
                showAddGoalSheet = false
            },
            onSave = { goal ->
                viewModel.insert(goal)
                showAddGoalSheet = false
            },
            onCreateCategory = onCreateCategory
        )
    }

// --------------------------------
// MODIFICAR AHORRO
// --------------------------------

    selectedGoal?.let { goal ->
        AddSavingsIncomeDialog(
            goal = goal,
            onDismiss = {
                selectedGoal = null
            },
            onConfirm = { amount ->
                viewModel.update(
                    goal.copy(
                        initialAmount = amount
                    )
                )

                selectedGoal = null
            }
        )
    }

    goalToEdit?.let { goal ->
        EditSavingsGoalDialog(
            goal = goal,
            categories = categories,
            onDismiss = {
                goalToEdit = null
            },
            onConfirm = { updatedGoal ->
                viewModel.update(updatedGoal)
                goalToEdit = null
            }
        )
    }

    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = {
                goalToDelete = null
            },
            title = {
                Text("Eliminar meta")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar " +
                            "\"${goal.name}\"? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(goal)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        goalToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    categories: List<Category>,
    onUpdateSavings: () -> Unit,
    onEditGoal: () -> Unit,
    onDeleteGoal: () -> Unit
) {
    val categoryName =
        categories
            .find { it.id == goal.categoryId }
            ?.name
            ?: "Categoría desconocida"

    val progress =
        if (goal.targetAmount > 0) {
            min(
                goal.initialAmount / goal.targetAmount,
                1.0
            ).toFloat()
        } else {
            0f
        }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onEditGoal
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar meta"
                    )
                }

                IconButton(
                    onClick = onDeleteGoal
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar meta",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = formatCurrency(goal.initialAmount),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = formatCurrency(goal.targetAmount),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "${(progress * 100).toInt()}% completado",
                style = MaterialTheme.typography.bodySmall
            )

            goal.deadline?.let { deadline ->

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Fecha límite: ${formatSavingsDate(deadline)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = onUpdateSavings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar ahorro")
            }
        }
    }
}

@Composable
private fun AddSavingsIncomeDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember(goal.id) {
        mutableStateOf("")
    }

    var operation by remember(goal.id) {
        mutableStateOf("abonar")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Gestionar ahorro")
        },
        text = {
            Column {
                Text("Meta: ${goal.name}")

                Spacer(Modifier.height(8.dp))

                Text(
                    "Saldo actual: ${
                        formatCurrency(goal.initialAmount)
                    }"
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Operación",
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = operation == "abonar",
                        onClick = {
                            operation = "abonar"
                            amountText = ""
                            errorMessage = null
                        }
                    )
                    Text("Abonar")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = operation == "retirar",
                        onClick = {
                            operation = "retirar"
                            amountText = ""
                            errorMessage = null
                        }
                    )
                    Text("Retirar")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = operation == "establecer",
                        onClick = {
                            operation = "establecer"
                            amountText =
                                goal.initialAmount.toString()
                            errorMessage = null
                        }
                    )
                    Text("Establecer saldo")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = {
                        Text(
                            if (operation == "establecer") {
                                "Nuevo saldo"
                            } else {
                                "Importe"
                            }
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { Text(it) }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText
                        .trim()
                        .replace(",", ".")
                        .toDoubleOrNull()

                    if (
                        amount == null ||
                        !amount.isFinite() ||
                        amount < 0.0 ||
                        (operation != "establecer" && amount == 0.0)
                    ) {
                        errorMessage =
                            "Introduce un importe válido."
                    } else {
                        val newBalance = when (operation) {
                            "abonar" ->
                                goal.initialAmount + amount

                            "retirar" ->
                                goal.initialAmount - amount

                            else -> amount
                        }

                        if (newBalance < 0.0) {
                            errorMessage =
                                "El retiro supera el ahorro disponible."
                        } else {
                            onConfirm(newBalance)
                        }
                    }
                }
            ) {
                Text("Guardar")
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
private fun EditSavingsGoalDialog(
    goal: SavingsGoal,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (SavingsGoal) -> Unit
) {
    var name by remember(goal.id) {
        mutableStateOf(goal.name)
    }

    var targetText by remember(goal.id) {
        mutableStateOf(goal.targetAmount.toString())
    }

    var categoryId by remember(goal.id) {
        mutableIntStateOf(goal.categoryId)
    }

    var deadline by remember(goal.id) {
        mutableStateOf(goal.deadline)
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val selectedCategory = categories.find {
        it.id == categoryId
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Editar meta de ahorro")
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
                    label = { Text("Nombre de la meta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name
                            ?: "Selecciona una categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                MenuAnchorType.PrimaryNotEditable
                            )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(category.name)
                                },
                                onClick = {
                                    categoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it
                        errorMessage = null
                    },
                    label = {
                        Text("Monto objetivo")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Saldo actual: ${
                        formatCurrency(goal.initialAmount)
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedButton(
                    onClick = {
                        showDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        deadline?.let {
                            "Fecha límite: ${formatSavingsDate(it)}"
                        } ?: "Agregar fecha límite"
                    )
                }

                if (deadline != null) {
                    TextButton(
                        onClick = {
                            deadline = null
                        }
                    ) {
                        Text("Quitar fecha límite")
                    }
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
                    val nameValue = name.trim()
                    val target = targetText
                        .trim()
                        .replace(",", ".")
                        .toDoubleOrNull()

                    when {
                        nameValue.isBlank() -> {
                            errorMessage =
                                "Introduce el nombre de la meta."
                        }

                        selectedCategory == null -> {
                            errorMessage =
                                "Selecciona una categoría válida."
                        }

                        target == null ||
                                !target.isFinite() ||
                                target <= 0.0 -> {
                            errorMessage =
                                "El monto objetivo debe ser mayor que cero."
                        }

                        else -> {
                            onConfirm(
                                goal.copy(
                                    name = nameValue,
                                    categoryId = categoryId,
                                    targetAmount = target,
                                    deadline = deadline
                                )
                            )
                        }
                    }
                }
            ) {
                Text("Guardar cambios")
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
            initialSelectedDateMillis = deadline
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deadline =
                            datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

private fun formatSavingsDate(timestamp: Long): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale("es", "MX")
    )

    return formatter.format(Date(timestamp))
}