package com.gosuraksha.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun NextRefreshIndicator(scoreMonth: String) {

    val monthStart = LocalDate.parse(scoreMonth.substring(0, 10))
    val nextMonth = monthStart.plusMonths(1)
    val today = LocalDate.now()

    val daysLeft = ChronoUnit.DAYS.between(today, nextMonth)

    Text(
        text = "Next score refresh in $daysLeft days"
    )
}
