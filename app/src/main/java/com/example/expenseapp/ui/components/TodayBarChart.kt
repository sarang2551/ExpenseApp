package com.example.expenseapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class TodayBarChart {
    @Composable
    fun BarChart(todayExpense: Double, todayIncome: Double) {
        val textMeasurer = rememberTextMeasurer()
        val tickCount = 4
        val axisMax = remember(todayExpense, todayIncome) {
            todayAxisMax(maxOf(todayExpense, todayIncome))
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFFF2F2F2))) {
            val labelWidth = 46.dp.toPx()
            val topPadding = 12.dp.toPx()
            val bottomPadding = 24.dp.toPx()
            val rightPadding = 12.dp.toPx()
            val plotLeft = labelWidth
            val plotTop = topPadding
            val plotRight = size.width - rightPadding
            val plotBottom = size.height - bottomPadding
            val plotWidth = plotRight - plotLeft
            val plotHeight = plotBottom - plotTop
            val axisColor = Color(0xFF555555)
            val gridColor = Color(0xFFD6D6D6)
            val labelStyle = TextStyle(
                color = Color(0xFF555555),
                fontSize = 10.sp
            )

            repeat(tickCount + 1) { index ->
                val tickValue = axisMax / tickCount * index
                val y = plotBottom - (tickValue / axisMax).toFloat() * plotHeight
                drawLine(
                    color = gridColor,
                    strokeWidth = 1.dp.toPx(),
                    start = Offset(plotLeft, y),
                    end = Offset(plotRight, y)
                )
                val label = formatAxisTick(tickValue)
                val measuredLabel = textMeasurer.measure(label, style = labelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = labelStyle,
                    topLeft = Offset(
                        x = plotLeft - measuredLabel.size.width - 6.dp.toPx(),
                        y = y - measuredLabel.size.height / 2f
                    )
                )
            }

            drawLine(color = axisColor, strokeWidth = 2.dp.toPx(), start = Offset(plotLeft, plotTop), end = Offset(plotLeft, plotBottom))
            drawLine(color = axisColor, strokeWidth = 2.dp.toPx(), start = Offset(plotLeft, plotBottom), end = Offset(plotRight, plotBottom))

            val barWidth = plotWidth / 5f
            val expenseHeight = (todayExpense / axisMax).toFloat() * plotHeight
            val incomeHeight = (todayIncome / axisMax).toFloat() * plotHeight
            val expenseLeft = plotLeft + plotWidth * 0.28f
            val incomeLeft = plotLeft + plotWidth * 0.55f

            drawRect(color = Color.Red, topLeft = Offset(expenseLeft, plotBottom - expenseHeight), size = Size(barWidth, expenseHeight))
            drawRect(color = Color(0xFF2EAD5B), topLeft = Offset(incomeLeft, plotBottom - incomeHeight), size = Size(barWidth, incomeHeight))

            val expenseLabel = "Expense"
            val incomeLabel = "Income"
            val expenseLabelWidth = textMeasurer.measure(expenseLabel, style = labelStyle).size.width
            val incomeLabelWidth = textMeasurer.measure(incomeLabel, style = labelStyle).size.width
            val labelY = plotBottom + 6.dp.toPx()

            drawText(
                textMeasurer = textMeasurer,
                text = expenseLabel,
                style = labelStyle,
                topLeft = Offset(expenseLeft + barWidth / 2f - expenseLabelWidth / 2f, labelY)
            )
            drawText(
                textMeasurer = textMeasurer,
                text = incomeLabel,
                style = labelStyle,
                topLeft = Offset(incomeLeft + barWidth / 2f - incomeLabelWidth / 2f, labelY)
            )
        }
    }

    private fun todayAxisMax(maxValue: Double, tickCount: Int = 4): Double {
        if (maxValue <= 0.0) return 1.0

        val roughStep = maxValue / tickCount
        val magnitude = 10.0.pow(floor(log10(roughStep)))
        val normalizedStep = roughStep / magnitude
        val niceStep = when {
            normalizedStep <= 1.0 -> 1.0
            normalizedStep <= 2.0 -> 2.0
            normalizedStep <= 5.0 -> 5.0
            else -> 10.0
        } * magnitude

        return niceStep * ceil(maxValue / niceStep)
    }

    private fun formatAxisTick(value: Double): String {
        return when {
            abs(value) >= 1_000_000 -> "${(value / 1_000_000).formatCompact()}M"
            abs(value) >= 1_000 -> "${(value / 1_000).formatCompact()}k"
            value % 1.0 == 0.0 -> value.toInt().toString()
            else -> value.formatCompact()
        }
    }

    private fun Double.formatCompact(): String {
        return if (this % 1.0 == 0.0) {
            toInt().toString()
        } else {
            "%.1f".format(this).trimEnd('0').trimEnd('.')
        }
    }
}