package com.example.expenseapp.data

import com.example.expenseapp.domain.model.Category
import com.example.expenseapp.domain.model.RecurringRule
import com.example.expenseapp.domain.model.Transaction
import com.example.expenseapp.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.UUID

class AppRepository {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val _categories = MutableStateFlow(seedCategories())
    private val _recurringRules = MutableStateFlow<List<RecurringRule>>(emptyList())
    private val _defaultCurrency = MutableStateFlow("USD")

    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    val recurringRules: StateFlow<List<RecurringRule>> = _recurringRules.asStateFlow()
    val defaultCurrency: StateFlow<String> = _defaultCurrency.asStateFlow()

    fun addTransaction(tx: Transaction) {
        _transactions.value = listOf(tx.copy(id = UUID.randomUUID().toString())) + _transactions.value
    }

    fun addCategory(name: String, type: TransactionType): Category {
        val c = Category(UUID.randomUUID().toString(), name, type)
        _categories.value = _categories.value + c
        return c
    }

    fun addRecurringRule(rule: RecurringRule) {
        _recurringRules.value = _recurringRules.value + rule
    }

    fun updateRecurringRule(rule: RecurringRule) {
        _recurringRules.value = _recurringRules.value.map { if (it.id == rule.id) rule else it }
    }

    fun setDefaultCurrency(currency: String) {
        _defaultCurrency.value = currency
    }

    fun allDataForExport(): List<Transaction> = _transactions.value.sortedByDescending { it.date }

    private fun seedCategories(): List<Category> = listOf(
        Category("e_food", "Food", TransactionType.EXPENSE),
        Category("e_transport", "Transport", TransactionType.EXPENSE),
        Category("e_bills", "Bills", TransactionType.EXPENSE),
        Category("i_salary", "Salary", TransactionType.INCOME),
        Category("i_freelance", "Freelance", TransactionType.INCOME),
        Category("i_gift", "Gift", TransactionType.INCOME),
    )
}

fun buildRecurringTransactionsForToday(
    rules: List<RecurringRule>,
    today: LocalDate = LocalDate.now(),
): Pair<List<Transaction>, List<RecurringRule>> {
    val generated = mutableListOf<Transaction>()
    val updatedRules = rules.map { rule ->
        if (!rule.isActive) return@map rule
        var next = rule.nextRunDate
        while (!next.isAfter(today)) {
            generated += Transaction(
                id = UUID.randomUUID().toString(),
                amount = rule.amount,
                currency = rule.currency,
                type = rule.type,
                categoryId = rule.categoryId,
                categoryNameSnapshot = rule.categoryNameSnapshot,
                note = rule.note,
                date = next,
                recurringRuleId = rule.id,
                origin = com.example.expenseapp.domain.model.TransactionOrigin.RECURRING,
            )
            next = when (rule.frequency) {
                com.example.expenseapp.domain.model.RecurrenceFrequency.DAILY -> next.plusDays(1)
                com.example.expenseapp.domain.model.RecurrenceFrequency.WEEKLY -> next.plusWeeks(1)
                com.example.expenseapp.domain.model.RecurrenceFrequency.MONTHLY -> next.plusMonths(1)
                com.example.expenseapp.domain.model.RecurrenceFrequency.BIANNUAL -> next.plusMonths(6)
                com.example.expenseapp.domain.model.RecurrenceFrequency.ANNUAL -> next.plusYears(1)
            }
        }
        rule.copy(nextRunDate = next)
    }
    return generated to updatedRules
}
