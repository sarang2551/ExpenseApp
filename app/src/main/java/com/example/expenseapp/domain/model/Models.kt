package com.example.expenseapp.domain.model

import java.time.LocalDate

enum class TransactionType { EXPENSE, INCOME }
enum class TransactionOrigin { MANUAL, RECURRING }
enum class RecurrenceFrequency { DAILY, WEEKLY, MONTHLY, BIANNUAL, ANNUAL }

data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
)

data class Transaction(
    val id: String,
    val amount: Double,
    val currency: String,
    val type: TransactionType,
    val categoryId: String,
    val categoryNameSnapshot: String,
    val note: String,
    val date: LocalDate,
    val origin: TransactionOrigin = TransactionOrigin.MANUAL,
    val recurringRuleId: String? = null,
)

data class RecurringRule(
    val id: String,
    val amount: Double,
    val currency: String,
    val type: TransactionType,
    val categoryId: String,
    val categoryNameSnapshot: String,
    val note: String,
    val frequency: RecurrenceFrequency,
    val startDate: LocalDate,
    val nextRunDate: LocalDate,
    val isActive: Boolean = true,
)
