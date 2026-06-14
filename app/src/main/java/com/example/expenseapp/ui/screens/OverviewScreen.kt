package com.example.expenseapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expenseapp.domain.model.Category
import com.example.expenseapp.ui.AppViewModel
import com.example.expenseapp.ui.FilterState
import com.example.expenseapp.ui.TransactionFilterType
import com.example.expenseapp.ui.components.PieChart
import java.time.LocalDate

@Composable
fun OverviewScreen(modifier: Modifier, vm: AppViewModel, categories: List<Category>) {
    val filters by vm.filters.collectAsState()
    val filtered by vm.filteredTransactions.collectAsState()

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Spending Overview", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filters.type == TransactionFilterType.MONEY_OUT, onClick = {
                vm.updateFilters { copy(type = TransactionFilterType.MONEY_OUT) }
            }, label = { Text("Money Out") })
            FilterChip(selected = filters.type == TransactionFilterType.MONEY_IN, onClick = {
                vm.updateFilters { copy(type = TransactionFilterType.MONEY_IN) }
            }, label = { Text("Money In") })
            FilterChip(selected = filters.type == TransactionFilterType.BOTH, onClick = {
                vm.updateFilters { copy(type = TransactionFilterType.BOTH) }
            }, label = { Text("Both") })
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { vm.updateFilters { copy(year = LocalDate.now().year) } }) { Text("Year") }
            TextButton(onClick = { vm.updateFilters { copy(month = LocalDate.now().monthValue) } }) { Text("Month") }
            TextButton(onClick = { vm.updateFilters { copy(day = LocalDate.now().dayOfMonth) } }) { Text("Day") }
            TextButton(onClick = { vm.updateFilters { FilterState() } }) { Text("Reset") }
        }
        CategoryFilterDropdown(categories = categories, selectedId = filters.categoryId, onSelect = { id ->
            vm.updateFilters { copy(categoryId = id) }
        })
        PieChart(filtered)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered.sortedByDescending { it.date }) {
                Text("${it.date}  ${it.categoryNameSnapshot}  ${it.amount}")
            }
        }
    }
}

@Composable
private fun CategoryFilterDropdown(categories: List<Category>, selectedId: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedId }?.name ?: "All categories"
    Button(onClick = { expanded = true }) { Text(selectedName) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("All categories") }, onClick = { onSelect(null); expanded = false })
        categories.forEach { c ->
            DropdownMenuItem(text = { Text(c.name) }, onClick = { onSelect(c.id); expanded = false })
        }
    }
}
