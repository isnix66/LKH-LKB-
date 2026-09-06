package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AppHeaderBanner(
    modifier: Modifier = Modifier,
    title: String = "Aplikasi LKH & LKB",
    subtitle: String = "Sistem Pelaporan Kegiatan Harian & Bulanan",
    badgeText: String = "☁️ Sinkronisasi Google Workspace",
    actions: @Composable RowScope.() -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
        border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Nokia Belle signature metallic cyan header strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                NokiaCyan,
                                NokiaCyanGlow,
                                NokiaNavy
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(44.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 21.sp,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = NokiaTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        actions()
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = NokiaCyanLight,
                    border = BorderStroke(1.dp, NokiaCyan.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NokiaCyanDark,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = NokiaTextPrimary,
                fontSize = 17.sp,
                letterSpacing = (-0.2).sp
            )
        )
        trailingContent()
    }
}

@Composable
fun DynamicTaskListEditor(
    tasks: List<String>,
    onTasksChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ketik rincian kegiatan..."
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tasks.forEachIndexed { index, taskText ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NokiaCyan,
                    modifier = Modifier.size(28.dp),
                    border = BorderStroke(1.dp, NokiaCyanGlow)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = taskText,
                    onValueChange = { newText ->
                        val updated = tasks.toMutableList()
                        updated[index] = newText
                        onTasksChange(updated)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("task_input_$index"),
                    placeholder = { Text(placeholder, color = NokiaTextTertiary, fontWeight = FontWeight.Normal) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NokiaCyan,
                        unfocusedBorderColor = NokiaBorder,
                        focusedTextColor = NokiaTextPrimary,
                        unfocusedTextColor = NokiaTextPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = NokiaCardSurfaceVariant
                    )
                )

                IconButton(
                    onClick = {
                        val updated = tasks.toMutableList()
                        updated.removeAt(index)
                        if (updated.isEmpty()) updated.add("")
                        onTasksChange(updated)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("delete_task_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus item",
                        tint = NokiaRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = {
                val updated = tasks.toMutableList()
                updated.add("")
                onTasksChange(updated)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("add_task_item_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, NokiaCyan),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = NokiaCyanDark,
                containerColor = NokiaCyanLight.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = NokiaCyanDark)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tambah Rincian Kegiatan", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
        }
    }
}


