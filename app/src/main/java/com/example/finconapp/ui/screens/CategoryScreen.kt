package com.example.finconapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter

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

private data class MonthlyCategoryData(
    val month: String,
    val income: Double,
    val expenses: Double
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

            val monthlyData =
                getMonthlyCategoryData(categoryTransactions)

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

                    CategoryMonthlyChart(
                        data = monthlyData
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        Color(0xFF2E7D32),
                                        CircleShape
                                    )
                            )

                            Spacer(
                                modifier = Modifier.width(6.dp)
                            )

                            Text(
                                text = "Ingresos",
                                style =
                                    MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(20.dp)
                        )

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        Color(0xFFC62828),
                                        CircleShape
                                    )
                            )

                            Spacer(
                                modifier = Modifier.width(6.dp)
                            )

                            Text(
                                text = "Gastos",
                                style =
                                    MaterialTheme.typography.labelMedium
                            )
                        }
                    }
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

private fun getMonthlyCategoryData(
    transactions: List<Transaction>
): List<MonthlyCategoryData> {

    val monthFormat = java.text.SimpleDateFormat(
        "MMM",
        Locale("es", "MX")
    )

    val monthKeyFormat = java.text.SimpleDateFormat(
        "yyyy-MM",
        Locale("es", "MX")
    )

    return transactions
        .groupBy { transaction ->
            monthKeyFormat.format(
                java.util.Date(transaction.date)
            )
        }
        .toSortedMap()
        .map { (_, monthTransactions) ->

            val date = java.util.Date(
                monthTransactions.first().date
            )

            val income = monthTransactions
                .filter { it.type == "Ingreso" }
                .sumOf { it.amount }

            val expenses = monthTransactions
                .filter { it.type == "Gasto" }
                .sumOf { it.amount }

            MonthlyCategoryData(
                month = monthFormat
                    .format(date)
                    .replaceFirstChar {
                        it.uppercase()
                    },
                income = income,
                expenses = expenses
            )
        }
}

private val MonthLabelsKey =
    ExtraStore.Key<List<String>>()

@Composable
private fun CategoryMonthlyChart(
    data: List<MonthlyCategoryData>
) {
    if (data.isEmpty()) {
        Text(
            text = "No hay datos suficientes para mostrar la evolución.",
            style = MaterialTheme.typography.bodyMedium
        )

        return
    }

    val modelProducer =
        remember {
            CartesianChartModelProducer()
        }

    LaunchedEffect(data) {

        modelProducer.runTransaction {

            lineSeries {

                series(
                    data.map { it.income }
                )

                series(
                    data.map { it.expenses }
                )
            }

            extras {
                it[MonthLabelsKey] =
                    data.map { monthData ->
                        monthData.month
                    }
            }
        }
    }

    val incomeLine =
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(
                Fill(Color(0xFF2E7D32))
            )
        )

    val expenseLine =
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(
                Fill(Color(0xFFC62828))
            )
        )

    val lineLayer =
        rememberLineCartesianLayer(
            lineProvider =
                LineCartesianLayer.LineProvider.series(
                    incomeLine,
                    expenseLine
                )
        )

    val monthFormatter =
        CartesianValueFormatter { context, value, _ ->

            context.model
                .extraStore[MonthLabelsKey]
                .getOrNull(value.toInt())
                ?: ""
        }

    CartesianChartHost(
        chart = rememberCartesianChart(
            lineLayer,

            startAxis =
                VerticalAxis.rememberStart(),

            bottomAxis =
                HorizontalAxis.rememberBottom(
                    valueFormatter = monthFormatter
                )
        ),

        modelProducer = modelProducer,

        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}