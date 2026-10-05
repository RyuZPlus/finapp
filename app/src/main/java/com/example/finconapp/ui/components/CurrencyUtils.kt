package com.example.finconapp.ui.components

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
    return NumberFormat
        .getCurrencyInstance(Locale("es", "MX"))
        .format(amount)
}