package com.example.expenseapp.ui

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expenseapp.domain.model.Category
import com.example.expenseapp.domain.model.RecurrenceFrequency
import com.example.expenseapp.domain.model.Transaction
import com.example.expenseapp.domain.model.TransactionOrigin
import com.example.expenseapp.domain.model.TransactionType
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class Screen { DASHBOARD, OVERVIEW, SETTINGS }

@Composable
fun AppRoot(vm: AppViewModel) {
    val transactions by vm.transactions.collectAsState()
    val categories by vm.categories.collectAsState()
    var screen by remember { mutableStateOf(Screen.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = screen == Screen.DASHBOARD, onClick = { screen = Screen.DASHBOARD }, label = { Text("Dashboard") }, icon = {})
                NavigationBarItem(selected = screen == Screen.OVERVIEW, onClick = { screen = Screen.OVERVIEW }, label = { Text("Overview") }, icon = {})
                NavigationBarItem(selected = screen == Screen.SETTINGS, onClick = { screen = Screen.SETTINGS }, label = { Text("Settings") }, icon = {})
            }
        }
    ) { padding ->
        when (screen) {
            Screen.DASHBOARD -> DashboardScreen(Modifier.padding(padding), vm, transactions, categories)
            Screen.OVERVIEW -> OverviewScreen(Modifier.padding(padding), vm, categories)
            Screen.SETTINGS -> SettingsScreen(Modifier.padding(padding), vm, categories)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(modifier: Modifier, vm: AppViewModel, transactions: List<Transaction>, categories: List<Category>) {
    var showAddSheet by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val todayTx = transactions.filter { it.date == today }
    val todayExpense = todayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val todayIncome = todayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        TodayBarChart(todayExpense = todayExpense, todayIncome = todayIncome)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { showAddSheet = true }) { Text("Add Transaction") }
        Spacer(Modifier.height(10.dp))
        Text("History", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions.sortedByDescending { it.date }) { tx ->
                val recurringTint = if (tx.origin == TransactionOrigin.RECURRING) Color(0xFFE7F1FF) else Color(0xFFF7F7F7)
                Card(Modifier.fillMaxWidth().background(recurringTint)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("${tx.categoryNameSnapshot} (${tx.type})")
                            Text(tx.date.toString(), style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${tx.currency} ${tx.amount}")
                            if (tx.origin == TransactionOrigin.RECURRING) Text("Recurring", color = Color(0xFF3D6FB4))
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            categories = categories,
            defaultCurrency = vm.defaultCurrency.collectAsState().value,
            onDismiss = { showAddSheet = false },
            onCreateCategory = { name, type -> vm.addCategory(name, type) },
            onSave = { amount, type, category, note ->
                vm.addTransaction(amount, type, category, note)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun TodayBarChart(todayExpense: Double, todayIncome: Double) {
    val maxVal = maxOf(todayExpense, todayIncome, 1.0)
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFF2F2F2))) {
        val availableHeight = size.height - 16f
        val barWidth = size.width / 5f
        val expHeight = (todayExpense / maxVal * availableHeight).toFloat()
        val incHeight = (todayIncome / maxVal * availableHeight).toFloat()
        drawRect(color = Color.Red, topLeft = Offset(size.width * 0.35f, size.height - expHeight), size = Size(barWidth, expHeight))
        drawRect(color = Color(0xFF2EAD5B), topLeft = Offset(size.width * 0.55f, size.height - incHeight), size = Size(barWidth, incHeight))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    categories: List<Category>,
    defaultCurrency: String,
    onDismiss: () -> Unit,
    onCreateCategory: (String, TransactionType) -> Unit,
    onSave: (Double, TransactionType, Category, String) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var selectedCategory by remember(selectedType, categories) {
        mutableStateOf(categories.firstOrNull { it.type == selectedType })
    }
    val scopedCategories = categories.filter { it.type == selectedType }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        selectedType = if (dragAmount < 0) TransactionType.INCOME else TransactionType.EXPENSE
                        selectedCategory = categories.firstOrNull { it.type == selectedType }
                    }
                }
        ) {
            Text("Swipe to toggle: ${if (selectedType == TransactionType.EXPENSE) "Money Out" else "Money In"}")
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = defaultCurrency, onValueChange = {}, readOnly = true, label = { Text("Currency") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = { categoryExpanded = true }) { Text(selectedCategory?.name ?: "Select Category") }
            DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                scopedCategories.forEach { category ->
                    DropdownMenuItem(text = { Text(category.name) }, onClick = {
                        selectedCategory = category
                        categoryExpanded = false
                    })
                }
            }
            OutlinedTextField(value = newCategoryName, onValueChange = { newCategoryName = it }, label = { Text("New category") }, modifier = Modifier.fillMaxWidth())
            TextButton(onClick = {
                if (newCategoryName.isNotBlank()) {
                    onCreateCategory(newCategoryName.trim(), selectedType)
                    newCategoryName = ""
                }
            }) { Text("Create category") }

            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    val parsed = amount.toDoubleOrNull() ?: return@Button
                    val category = selectedCategory ?: return@Button
                    onSave(parsed, selectedType, category, note)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OverviewScreen(modifier: Modifier, vm: AppViewModel, categories: List<Category>) {
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

@Composable
private fun PieChart(transactions: List<Transaction>) {
    val sums = transactions.groupBy { it.categoryNameSnapshot }.mapValues { it.value.sumOf { tx -> tx.amount } }
    val total = sums.values.sum().takeIf { it > 0 } ?: 1.0
    val colors = listOf(Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFF42A5F5), Color(0xFFFFCA28), Color(0xFFAB47BC))
    Canvas(modifier = Modifier.fillMaxWidth().height(220.dp).padding(8.dp)) {
        var startAngle = -90f
        sums.entries.forEachIndexed { idx, entry ->
            val sweep = ((entry.value / total) * 360f).toFloat()
            drawArc(
                color = colors[idx % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(size.width / 2f - 100f, size.height / 2f - 100f),
                size = Size(200f, 200f)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier, vm: AppViewModel, categories: List<Category>) {
    val context = LocalContext.current
    var showRecurring by remember { mutableStateOf(false) }
    var exportPreview by remember { mutableStateOf<String?>(null) }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = {
            val csv = vm.exportCsv()
            exportPreview = csv
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csv)
                type = "text/csv"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export CSV to Google Drive"))
        }) { Text("Export to Google Drive") }
        Button(onClick = { showRecurring = true }) { Text("Manage Recurring Transactions") }
        exportPreview?.let { Text("Rows exported: ${it.lines().size - 1}") }
    }

    if (showRecurring) {
        RecurringDialog(
            categories = categories,
            currency = vm.defaultCurrency.collectAsState().value,
            onDismiss = { showRecurring = false },
            onSave = { amount, type, category, note, frequency ->
                vm.addRecurringRule(amount, type, category, note, frequency)
                showRecurring = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringDialog(
    categories: List<Category>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Double, TransactionType, Category, String, RecurrenceFrequency) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var frequency by remember { mutableStateOf(RecurrenceFrequency.MONTHLY) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showFreqMenu by remember { mutableStateOf(false) }
    var selectedCategory by remember(type, categories) { mutableStateOf(categories.firstOrNull { it.type == type }) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add Recurring Transaction", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = currency, onValueChange = {}, readOnly = true, label = { Text("Currency") }, modifier = Modifier.fillMaxWidth())

            Button(onClick = { showTypeMenu = true }) { Text("Type: $type") }
            DropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                TransactionType.entries.forEach { t ->
                    DropdownMenuItem(text = { Text(t.name) }, onClick = {
                        type = t
                        selectedCategory = categories.firstOrNull { it.type == t }
                        showTypeMenu = false
                    })
                }
            }

            val scoped = categories.filter { it.type == type }
            Button(onClick = {}) { Text("Category: ${selectedCategory?.name ?: "Select"}") }
            Spacer(Modifier.height(1.dp))
            scoped.forEach { c ->
                TextButton(onClick = { selectedCategory = c }) { Text(c.name) }
            }

            Button(onClick = { showFreqMenu = true }) { Text("Frequency: $frequency") }
            DropdownMenu(expanded = showFreqMenu, onDismissRequest = { showFreqMenu = false }) {
                RecurrenceFrequency.entries.forEach { f ->
                    DropdownMenuItem(text = { Text(f.name) }, onClick = { frequency = f; showFreqMenu = false })
                }
            }
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                val parsed = amount.toDoubleOrNull() ?: return@Button
                val category = selectedCategory ?: return@Button
                onSave(parsed, type, category, note, frequency)
            }, modifier = Modifier.fillMaxWidth()) { Text("Save Recurring") }
        }
    }
}
