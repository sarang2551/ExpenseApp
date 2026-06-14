package com.example.expenseapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.expenseapp.domain.model.Category
import com.example.expenseapp.domain.model.RecurrenceFrequency
import com.example.expenseapp.domain.model.TransactionType
import com.example.expenseapp.ui.AppViewModel

@Composable
fun SettingsScreen(modifier: Modifier, vm: AppViewModel, categories: List<Category>) {
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