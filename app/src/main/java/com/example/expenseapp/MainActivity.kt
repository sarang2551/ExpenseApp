package com.example.expenseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.expenseapp.ui.AppRoot
import com.example.expenseapp.ui.AppViewModel

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.processRecurringForToday()
        setContent {
            AppRoot(vm = vm)
        }
    }
}
