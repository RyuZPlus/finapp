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
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import com.example.finconapp.data.local.entity.Category

import com.example.finconapp.data.local.entity.Transaction
import com.example.finconapp.ui.components.DateRangeSelector
import com.example.finconapp.ui.viewmodel.TransactionViewModel
import com.example.finconapp.ui.components.TransactionDayHeader
import com.example.finconapp.ui.components.formatTransactionDay
import com.example.finconapp.ui.viewmodel.CategoryViewModel
import com.example.finconapp.ui.components.formatCurrency

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

import java.util.Locale
import java.util.Calendar

@Composable
fun CategoryScreen(
    paddingValues: PaddingValues,
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    categoriesList: List<Category>
) {
    val transactions by viewModel.filteredTransactions.collectAsState()

    val customStartDate by
    viewModel.customStartDate.collectAsState()

    val customEndDate by
    viewModel.customEndDate.collectAsState()

    var selectedCategoryId by remember {
        mutableStateOf<Int?>(null)
    }

    var showEditCategoryDialog by remember {
        mutableStateOf(false)
    }

    var selectedCategoryHasTransactions by remember {
        mutableStateOf(false)
    }

    var showDeleteCategoryDialog by remember {
        mutableStateOf(false)
    }

    var deleteCategoryError by remember {
        mutableStateOf(false)
    }

    val selectedCategory =
        categoriesList.firstOrNull {
            it.id == selectedCategoryId
        }

    BackHandler(
        enabled = selectedCategoryId != null
    ) {
        selectedCategoryId = null
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
                        selectedCategoryId = null
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }

                Text(
                    text = selectedCategory?.name ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        showEditCategoryDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar categoría"
                    )
                }

                IconButton(
                    enabled = !selectedCategoryHasTransactions,
                    onClick = {
                        deleteCategoryError = false
                        showDeleteCategoryDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar categoría"
                    )
                }
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
                categories = categoriesList,
                onCategorySelected = {
                    selectedCategoryId = it.id

                    viewModel.categoryHasTransactions(it.id) { hasTransactions ->
                        selectedCategoryHasTransactions = hasTransactions
                    }
                }
            )

        } else {

            CategoryDetail(
                category = selectedCategory!!,
                transactions = transactions
            )
        }
        if (
            showEditCategoryDialog &&
            selectedCategory != null
        ) {
            EditCategoryDialog(
                category = selectedCategory,
                onDismiss = {
                    showEditCategoryDialog = false
                },
                onSave = { updatedCategory, onDuplicate ->

                    categoryViewModel.update(
                        updatedCategory
                    ) { success ->

                        if (success) {
                            showEditCategoryDialog = false
                        } else {
                            onDuplicate()
                        }
                    }
                }
            )
        }
        if (
            showDeleteCategoryDialog &&
            selectedCategory != null
        ) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteCategoryDialog = false
                },

                title = {
                    Text("Eliminar categoría")
                },

                text = {
                    Text(
                        "¿Deseas eliminar la categoría " +
                                "\"${selectedCategory.name}\"?"
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {

                            categoryViewModel.delete(
                                selectedCategory.id
                            ) { success ->

                                if (success) {

                                    showDeleteCategoryDialog = false
                                    selectedCategoryId = null

                                } else {

                                    showDeleteCategoryDialog = false
                                    deleteCategoryError = true
                                }
                            }
                        }
                    ) {
                        Text("Eliminar")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteCategoryDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun CategoryList(
    transactions: List<Transaction>,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit
) {
    val categorySummaries = categories
        .map { category ->

            val categoryTransactions =
                transactions.filter {
                    it.categoryId == category.id
                }

            val income =
                categoryTransactions
                    .filter { it.type == "Ingreso" }
                    .sumOf { it.amount }

            val expenses =
                categoryTransactions
                    .filter { it.type == "Gasto" }
                    .sumOf { it.amount }

            CategorySummary(
                id = category.id,
                name = category.name,
                income = income,
                expenses = expenses,
                balance = income - expenses
            )
        }
        .sortedBy { it.name }

    if (categorySummaries.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay categorías registradas."
            )
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(categorySummaries) { category ->

            CategoryCard(
                category = category,
                onClick = {

                    val selected =
                        categories.firstOrNull {
                            it.id == category.id
                        }

                    if (selected != null) {
                        onCategorySelected(selected)
                    }
                }
            )
        }
    }
}

private data class CategorySummary(
    val id: Int,
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
    category: Category,
    transactions: List<Transaction>
) {
    val categoryTransactions = transactions
        .filter { it.categoryId == category.id }

    val groupedTransactions =
        categoryTransactions
            .sortedByDescending { it.date }
            .groupBy { transaction ->
                formatTransactionDay(transaction.date)
            }

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

        groupedTransactions.forEach { (day, dayTransactions) ->

            item {

                TransactionDayHeader(
                    date = day,
                    transactions = dayTransactions
                )
            }

            items(
                items = dayTransactions,
                key = { transaction ->
                    transaction.id
                }
            ) { transaction ->

                CategoryTransactionItem(
                    transaction = transaction
                )
            }
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

private fun getMonthlyCategoryData(
    transactions: List<Transaction>
): List<MonthlyCategoryData> {

    if (transactions.isEmpty()) {
        return emptyList()
    }

    val monthFormat = java.text.SimpleDateFormat(
        "MMM",
        Locale("es", "MX")
    )

    val monthKeyFormat = java.text.SimpleDateFormat(
        "yyyy-MM",
        Locale("es", "MX")
    )

    /*
     * Agrupamos las transacciones existentes por mes.
     */
    val transactionsByMonth =
        transactions.groupBy { transaction ->
            monthKeyFormat.format(
                java.util.Date(transaction.date)
            )
        }

    /*
     * Obtenemos el primer y último mes
     * que tienen movimientos.
     */
    val firstDate = transactions.minOf { it.date }
    val lastDate = transactions.maxOf { it.date }

    val calendar = Calendar.getInstance().apply {
        timeInMillis = firstDate

        set(
            Calendar.DAY_OF_MONTH,
            1
        )

        set(
            Calendar.HOUR_OF_DAY,
            0
        )

        set(
            Calendar.MINUTE,
            0
        )

        set(
            Calendar.SECOND,
            0
        )

        set(
            Calendar.MILLISECOND,
            0
        )
    }

    val lastCalendar = Calendar.getInstance().apply {
        timeInMillis = lastDate

        set(
            Calendar.DAY_OF_MONTH,
            1
        )

        set(
            Calendar.HOUR_OF_DAY,
            0
        )

        set(
            Calendar.MINUTE,
            0
        )

        set(
            Calendar.SECOND,
            0
        )

        set(
            Calendar.MILLISECOND,
            0
        )
    }

    val result = mutableListOf<MonthlyCategoryData>()

    /*
     * Recorremos todos los meses desde el primero
     * hasta el último.
     */
    while (
        calendar.get(Calendar.YEAR) <
        lastCalendar.get(Calendar.YEAR) ||

        (
                calendar.get(Calendar.YEAR) ==
                        lastCalendar.get(Calendar.YEAR) &&

                        calendar.get(Calendar.MONTH) <=
                        lastCalendar.get(Calendar.MONTH)
                )
    ) {

        val monthKey =
            monthKeyFormat.format(
                calendar.time
            )

        val monthTransactions =
            transactionsByMonth[monthKey]
                ?: emptyList()

        val income =
            monthTransactions
                .filter { it.type == "Ingreso" }
                .sumOf { it.amount }

        val expenses =
            monthTransactions
                .filter { it.type == "Gasto" }
                .sumOf { it.amount }

        result.add(
            MonthlyCategoryData(
                month = monthFormat
                    .format(calendar.time)
                    .replaceFirstChar {
                        it.uppercase()
                    },

                income = income,
                expenses = expenses
            )
        )

        /*
         * Pasamos al siguiente mes.
         */
        calendar.add(
            Calendar.MONTH,
            1
        )
    }

    return result
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

    if (data.size < 2) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aún no hay suficientes datos",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Selecciona un periodo que incluyan movimientos de más de un mes para ver la evolución.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

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

@Composable
private fun EditCategoryDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (Category, () -> Unit) -> Unit
) {
    var name by remember(category.id) {
        mutableStateOf(category.name)
    }

    var duplicateError by remember(category.id) {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Editar categoría"
            )
        },

        text = {

            Column {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        duplicateError = false
                    },
                    label = {
                        Text("Nombre")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = duplicateError
                )

                if (duplicateError) {

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Ya existe una categoría con ese nombre.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val newName = name.trim()

                    if (newName.isNotEmpty()) {

                        onSave(
                            category.copy(
                                name = newName
                            )
                        ) {
                            duplicateError = true
                        }
                    }
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Guardar")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}