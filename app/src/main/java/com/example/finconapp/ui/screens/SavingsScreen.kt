package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Category
import com.example.finconapp.data.local.entity.SavingsGoal
import com.example.finconapp.ui.viewmodel.SavingsGoalViewModel
import com.example.finconapp.ui.components.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

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
                        categories = categories
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
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    categories: List<Category>
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

            Text(
                text = goal.name,
                style = MaterialTheme.typography.titleMedium
            )

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