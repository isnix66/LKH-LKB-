package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun MonthYearPickerDialog(
    currentMonthIso: String, // e.g. "2026-06"
    onDismiss: () -> Unit,
    onMonthSelected: (String) -> Unit // returns "YYYY-MM"
) {
    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    val parts = currentMonthIso.split("-")
    val initialYear = parts.getOrNull(0)?.toIntOrNull() ?: 2025
    val initialMonthIndex = ((parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1).coerceIn(0, 11)

    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var selectedMonthIndex by remember { mutableIntStateOf(initialMonthIndex) }

    val formattedSelectedMonthIso = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonthIndex + 1)
    val weekdaysCount = remember(formattedSelectedMonthIso) {
        DateUtils.generateWeekdaysForMonth(formattedSelectedMonthIso, skipNationalHolidays = true).size
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pilih Bulan & Tahun",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Year selector controls
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedYear-- },
                            modifier = Modifier.testTag("btn_prev_year")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tahun Sebelumnya", tint = RoyalBlue)
                        }

                        Text(
                            text = "$selectedYear",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalBlue
                            )
                        )

                        IconButton(
                            onClick = { selectedYear++ },
                            modifier = Modifier.testTag("btn_next_year")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Tahun Berikutnya", tint = RoyalBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of 12 Months
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(monthNames) { index, name ->
                        val isSelected = index == selectedMonthIndex
                        val isCurrentMonthOfSystem = run {
                            val cur = DateUtils.getCurrentMonthIso().split("-")
                            val cYear = cur.getOrNull(0)?.toIntOrNull() ?: 0
                            val cMonth = (cur.getOrNull(1)?.toIntOrNull() ?: 0) - 1
                            cYear == selectedYear && cMonth == index
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RoyalBlue else if (isCurrentMonthOfSystem) RoyalBlueLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = if (isSelected) null else if (isCurrentMonthOfSystem) BorderStroke(1.dp, RoyalBlue) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable {
                                    selectedMonthIndex = index
                                }
                                .testTag("month_chip_$index")
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.take(3),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected || isCurrentMonthOfSystem) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else if (isCurrentMonthOfSystem) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary of working days
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RoyalBlueLight.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 Terpilih: ${monthNames[selectedMonthIndex]} $selectedYear (~$weekdaysCount hari kerja)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepNavy
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            val result = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonthIndex + 1)
                            onMonthSelected(result)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_confirm_month_selection"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pilih Bulan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
