package com.example.finconapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.ui.components.DateRangeSelector
import com.example.finconapp.ui.viewmodel.TransactionViewModel

@Composable
fun CategoryScreen(
    paddingValues: PaddingValues,
    viewModel: TransactionViewModel
) {
    val customStartDate by
    viewModel.customStartDate.collectAsState()

    val customEndDate by
    viewModel.customEndDate.collectAsState()

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

            Text(
                text = "Categorías",
                style = MaterialTheme.typography.headlineMedium
            )

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
    }
}