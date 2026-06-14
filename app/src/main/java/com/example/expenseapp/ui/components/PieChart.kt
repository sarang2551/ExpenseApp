package com.example.expenseapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.expenseapp.domain.model.Transaction

@Composable
fun PieChart(transactions: List<Transaction>) {
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