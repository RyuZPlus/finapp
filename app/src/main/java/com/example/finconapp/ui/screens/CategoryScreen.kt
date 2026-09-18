package com.example.finconapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.finconapp.data.local.entity.Transaction
import com.example.finconapp.ui.components.DateRangeSelector
import com.example.finconapp.ui.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategoryScreen(
    paddingValues: PaddingValues,
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.filteredTransactions.collectAsState()

    val customStartDate by
    viewModel.customStartDate.collectAsState()

    val customEndDate by
    viewModel.customEndDate.collectAsState()

    var selectedCategory by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (selectedCategory == null) {

                Text(
                    text = "Categorías",
                    style = MaterialTheme.typography.headlineMedium
                )

            } else {

                IconButton(
                    onClick = {
                        selectedCategory = null
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }

                Text(
                    text = selectedCategory ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            DateRangeSelector(
                selectedRange =
                    viewModel.dateRangeType.collectAsState().value,

                customStartDate =
                    customStartDate,

                customEndDate =
                    customEndDate,

                onRangeSelected = { type ->
                    viewModel.setDateRange(type)
                },

                onCustomRangeSelected = { startDate, endDate ->
                    viewModel.setCustomDateRange(
                        startDate,
                        endDate
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedCategory == null) {

            CategoryList(
                transactions = transactions,
                onCategorySelected = {
                    selectedCategory = it
                }
            )

        } else {

            CategoryDetail(
                category = selectedCategory!!,
                transactions = transactions
            )
        }
    }
}

@Composable
private fun CategoryList(
    transactions: List<Transaction>,
    onCategorySelected: (String) -> Unit
) {
    val categories = transactions
        .groupBy { it.category }
        .map { (category, categoryTransactions) ->

            val income = categoryTransactions
                .filter { it.type == "Ingreso" }
                .sumOf { it.amount }

            val expenses = categoryTransactions
                .filter { it.type == "Gasto" }
                .sumOf { it.amount }

            CategorySummary(
                name = category,
                income = income,
                expenses = expenses,
                balance = income - expenses
            )
        }
        .sortedBy { it.name }

    if (categories.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay movimientos registrados."
            )
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(categories) { category ->

            CategoryCard(
                category = category,
                onClick = {
                    onCategorySelected(category.name)
                }
            )
        }
    }
}

private data class CategorySummary(
    val name: String,
    val income: Double,
    val expenses: Double,
    val balance: Double
)

@Composable
private fun CategoryCard(
    category: CategorySummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                CategoryAmount(
                    label = "Ingresos",
                    amount = category.income,
                    color = Color(0xFF2E7D32)
                )

                CategoryAmount(
                    label = "Gastos",
                    amount = category.expenses,
                    color = Color(0xFFC62828)
                )

                CategoryAmount(
                    label = "Balance",
                    amount = category.balance
                )
            }
        }
    }
}

@Composable
private fun CategoryAmount(
    label: String,
    amount: Double,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}

@Composable
private fun CategoryDetail(
    category: String,
    transactions: List<Transaction>
) {
    val categoryTransactions = transactions
        .filter { it.category == category }

    val income = categoryTransactions
        .filter { it.type == "Ingreso" }
        .sumOf { it.amount }

    val expenses = categoryTransactions
        .filter { it.type == "Gasto" }
        .sumOf { it.amount }

    val balance = income - expenses

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Transacciones",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        text = categoryTransactions.size.toString(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                CategoryStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Ingresos",
                    value = income,
                    color = Color(0xFF2E7D32)
                )

                CategoryStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Gastos",
                    value = expenses,
                    color = Color(0xFFC62828)
                )

                CategoryStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Balance",
                    value = balance
                )
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Evolución mensual",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Aquí irá el gráfico de evolución mensual.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {

            Text(
                text = "Transacciones",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(categoryTransactions) { transaction ->

            CategoryTransactionItem(
                transaction = transaction
            )
        }
    }
}

@Composable
private fun CategoryStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: Double,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = formatCurrency(value),
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
        }
    }
}

@Composable
private fun CategoryTransactionItem(
    transaction: Transaction
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleMedium
            )

            transaction.description?.let {

                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = transaction.type,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun formatCurrency(
    amount: Double
): String {

    return NumberFormat
        .getCurrencyInstance(Locale("es", "MX"))
        .format(amount)
}