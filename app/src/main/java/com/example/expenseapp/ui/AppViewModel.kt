package com.example.expenseapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expenseapp.data.AppRepository
import com.example.expenseapp.data.buildRecurringTransactionsForToday
import com.example.expenseapp.domain.model.Category
import com.example.expenseapp.domain.model.RecurrenceFrequency
import com.example.expenseapp.domain.model.RecurringRule
import com.example.expenseapp.domain.model.Transaction
import com.example.expenseapp.domain.model.TransactionOrigin
import com.example.expenseapp.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class FilterState(
    val categoryId: String? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val type: TransactionFilterType = TransactionFilterType.BOTH,
)

enum class TransactionFilterType { MONEY_IN, MONEY_OUT, BOTH }

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository()
    private val _filters = MutableStateFlow(FilterState())

    val categories = repo.categories
    val transactions = repo.transactions
    val recurringRules = repo.recurringRules
    val defaultCurrency = repo.defaultCurrency
    val filters: StateFlow<FilterState> = _filters

    val filteredTransactions = combine(transactions, filters) { txs, filters ->
        txs.filter { tx ->
            val categoryOk = filters.categoryId?.let { it == tx.categoryId } ?: true
            val yearOk = filters.year?.let { it == tx.date.year } ?: true
            val monthOk = filters.month?.let { it == tx.date.monthValue } ?: true
            val dayOk = filters.day?.let { it == tx.date.dayOfMonth } ?: true
            val typeOk = when (filters.type) {
                TransactionFilterType.BOTH -> true
                TransactionFilterType.MONEY_IN -> tx.type == TransactionType.INCOME
                TransactionFilterType.MONEY_OUT -> tx.type == TransactionType.EXPENSE
            }
            categoryOk && yearOk && monthOk && dayOk && typeOk
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: Category,
        note: String,
        date: LocalDate = LocalDate.now(),
    ) {
        repo.addTransaction(
            Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                currency = defaultCurrency.value,
                type = type,
                categoryId = category.id,
                categoryNameSnapshot = category.name,
                note = note,
                date = date,
                origin = TransactionOrigin.MANUAL,
            )
        )
    }

    fun addCategory(name: String, type: TransactionType) = repo.addCategory(name, type)

    fun addRecurringRule(
        amount: Double,
        type: TransactionType,
        category: Category,
        note: String,
        frequency: RecurrenceFrequency,
        startDate: LocalDate = LocalDate.now(),
    ) {
        repo.addRecurringRule(
            RecurringRule(
                id = UUID.randomUUID().toString(),
                amount = amount,
                currency = defaultCurrency.value,
                type = type,
                categoryId = category.id,
                categoryNameSnapshot = category.name,
                note = note,
                frequency = frequency,
                startDate = startDate,
                nextRunDate = startDate,
            )
        )
        processRecurringForToday()
    }

    fun processRecurringForToday(today: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val (generated, updated) = buildRecurringTransactionsForToday(recurringRules.value, today)
            val existingKeys = transactions.value
                .filter { it.origin == TransactionOrigin.RECURRING && it.recurringRuleId != null }
                .map { "${it.recurringRuleId}-${it.date}" }
                .toSet()
            generated.forEach { tx ->
                val dedupeKey = "${tx.recurringRuleId}-${tx.date}"
                if (!existingKeys.contains(dedupeKey)) repo.addTransaction(tx)
            }
            updated.forEach(repo::updateRecurringRule)
        }
    }

    fun updateFilters(update: FilterState.() -> FilterState) {
        _filters.update(update)
    }

    fun exportCsv(): String {
        val rows = buildList {
            add("date,type,origin,category,amount,currency,note")
            repo.allDataForExport().forEach { tx ->
                add("${tx.date},${tx.type},${tx.origin},${tx.categoryNameSnapshot},${tx.amount},${tx.currency},\"${tx.note}\"")
            }
        }
        return rows.joinToString("\n")
    }
}
