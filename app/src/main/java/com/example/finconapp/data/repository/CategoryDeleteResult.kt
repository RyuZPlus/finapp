package com.example.finconapp.data.repository

enum class CategoryDeleteResult {
    DELETED,
    HAS_TRANSACTIONS,
    HAS_SAVINGS_GOALS,
    NOT_FOUND
}