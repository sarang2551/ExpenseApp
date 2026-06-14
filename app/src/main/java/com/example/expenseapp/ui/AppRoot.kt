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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.expenseapp.domain.model.Category
import com.example.expenseapp.domain.model.RecurrenceFrequency
import com.example.expenseapp.domain.model.Transaction
import com.example.expenseapp.domain.model.TransactionOrigin
import com.example.expenseapp.domain.model.TransactionType
import com.example.expenseapp.ui.components.TodayBarChart
import com.example.expenseapp.ui.screens.OverviewScreen
import com.example.expenseapp.ui.screens.SettingsScreen
import com.patrykandpatrick.vico.compose.common.shader.color
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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

    Column(modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Today ${today.dayOfMonth}/${today.monthValue}/${today.year.toString().takeLast(2)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        TodayBarChart().BarChart(todayExpense = todayExpense, todayIncome = todayIncome)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { showAddSheet = true }) { Text("Add Transaction") }
        Spacer(Modifier.height(10.dp))
        Text("History", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions.sortedByDescending { it.date }) { tx ->
                val recurringTint = if (tx.origin == TransactionOrigin.RECURRING) Color(0xFFE7F1FF) else Color(0xFFF7F7F7)
                Card(Modifier
                    .fillMaxWidth()
                    .background(recurringTint)) {
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
    var newCategoryModal by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    val sheetColor = if (selectedType == TransactionType.EXPENSE) {
        Color(0xFFFFEBEE)
    } else {
        Color(0xFFE8F5E9)
    }
    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        errorContainerColor = Color.White
    )
    var selectedCategory by remember(selectedType, categories) {
        mutableStateOf(categories.firstOrNull { it.type == selectedType })
    }
    val scopedCategories = categories.filter { it.type == selectedType }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetColor
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        selectedType =
                            if (dragAmount < 0) TransactionType.INCOME else TransactionType.EXPENSE
                        selectedCategory = categories.firstOrNull { it.type == selectedType }
                    }
            }
        ) {
            Text("Swipe to toggle: ${if (selectedType == TransactionType.EXPENSE) "Money Out" else "Money In"}")
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                colors = inputColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = defaultCurrency, onValueChange = {}, readOnly = true, label = { Text("Currency") }, colors = inputColors, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { categoryExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCategory?.name ?: "Select Category")
                    }
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        scopedCategories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            })
                        }
                    }
                }
                IconButton(
                    onClick = { newCategoryModal = true },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Add category" }
                ) {
                    Canvas(Modifier.size(22.dp)) {
                        val strokeWidth = 2.5.dp.toPx()
                        drawLine(
                            color = Color(0xFF444444),
                            strokeWidth = strokeWidth,
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height)
                        )
                        drawLine(
                            color = Color(0xFF444444),
                            strokeWidth = strokeWidth,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f)
                        )
                    }
                }
            }

            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, colors = inputColors, modifier = Modifier.fillMaxWidth())
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

    if (newCategoryModal) {
        AlertDialog(
            onDismissRequest = {
                newCategoryModal = false
                newCategoryName = ""
            },
            title = { Text("New category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category name") },
                    colors = inputColors,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val categoryName = newCategoryName.trim()
                        if (categoryName.isNotEmpty()) {
                            onCreateCategory(categoryName, selectedType)
                            newCategoryName = ""
                            newCategoryModal = false
                        }
                    },
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newCategoryModal = false
                        newCategoryName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
