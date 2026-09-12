package com.example.finconapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.finconapp.ui.model.DateRangeType
import java.util.Calendar

fun formatDateRange(
    startDate: Long,
    endDate: Long
): String {

    val formatterSameYear =
        java.text.SimpleDateFormat(
            "dd MMM",
            java.util.Locale("es", "MX")
        )

    val formatterDifferentYear =
        java.text.SimpleDateFormat(
            "dd MMM yyyy",
            java.util.Locale("es", "MX")
        )

    val startCalendar =
        java.util.Calendar.getInstance()

    val endCalendar =
        java.util.Calendar.getInstance()

    startCalendar.timeInMillis = startDate
    endCalendar.timeInMillis = endDate

    return if (
        startCalendar.get(java.util.Calendar.YEAR) ==
        endCalendar.get(java.util.Calendar.YEAR)
    ) {

        "${formatterSameYear.format(startDate)} – ${
            formatterSameYear.format(endDate)
        }"

    } else {

        "${formatterDifferentYear.format(startDate)} – ${
            formatterDifferentYear.format(endDate)
        }"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    selectedRange: DateRangeType,
    customStartDate: Long?,
    customEndDate: Long?,
    onRangeSelected: (DateRangeType) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    var showDateRangePicker by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
    ) {

        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {

            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Seleccionar periodo"
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = when (selectedRange) {

                    DateRangeType.DAY ->
                        "Hoy"

                    DateRangeType.WEEK ->
                        "1 semana"

                    DateRangeType.MONTH ->
                        "1 mes"

                    DateRangeType.CUSTOM -> {

                        if (
                            customStartDate != null &&
                            customEndDate != null
                        ) {

                            formatDateRange(
                                customStartDate,
                                customEndDate
                            )

                        } else {

                            "Personalizado"
                        }
                    }
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            DropdownMenuItem(
                text = {
                    Text("Hoy")
                },
                onClick = {

                    onRangeSelected(
                        DateRangeType.DAY
                    )

                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text("1 semana")
                },
                onClick = {

                    onRangeSelected(
                        DateRangeType.WEEK
                    )

                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text("1 mes")
                },
                onClick = {

                    onRangeSelected(
                        DateRangeType.MONTH
                    )

                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Personalizado")
                },
                onClick = {

                    expanded = false
                    showDateRangePicker = true
                }
            )
        }
    }

    // Selector de rango personalizado
    if (showDateRangePicker) {

        val dateRangePickerState =
            rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = {
                showDateRangePicker = false
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        val startDate =
                            dateRangePickerState.selectedStartDateMillis

                        val endDate =
                            dateRangePickerState.selectedEndDateMillis

                        if (startDate != null && endDate != null) {

                            val localStartDate =
                                pickerDateToLocalStartOfDay(startDate)

                            val localEndDate =
                                pickerDateToLocalEndOfDay(endDate)

                            onCustomRangeSelected(
                                localStartDate,
                                localEndDate
                            )

                            showDateRangePicker = false
                        }
                    }
                ) {
                    Text("Aplicar")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDateRangePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {

            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Seleccionar periodo"
                    )
                },
                headline = {
                    Text(
                        text = "Selecciona las fechas"
                    )
                },
                showModeToggle = false
            )
        }
    }
}
private fun pickerDateToLocalStartOfDay(
    millis: Long
): Long {

    val utcCalendar =
        Calendar.getInstance(
            java.util.TimeZone.getTimeZone("UTC")
        )

    utcCalendar.timeInMillis = millis

    val localCalendar = Calendar.getInstance()

    localCalendar.set(
        utcCalendar.get(Calendar.YEAR),
        utcCalendar.get(Calendar.MONTH),
        utcCalendar.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0
    )

    localCalendar.set(
        Calendar.MILLISECOND,
        0
    )

    return localCalendar.timeInMillis
}

private fun pickerDateToLocalEndOfDay(
    millis: Long
): Long {

    val utcCalendar =
        Calendar.getInstance(
            java.util.TimeZone.getTimeZone("UTC")
        )

    utcCalendar.timeInMillis = millis

    val localCalendar = Calendar.getInstance()

    localCalendar.set(
        utcCalendar.get(Calendar.YEAR),
        utcCalendar.get(Calendar.MONTH),
        utcCalendar.get(Calendar.DAY_OF_MONTH),
        23,
        59,
        59
    )

    localCalendar.set(
        Calendar.MILLISECOND,
        999
    )

    return localCalendar.timeInMillis
}