package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DeepNavy
import com.example.util.DateUtils

@Composable
fun EditKegiatanModal(
    tanggal: String,
    initialTasks: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var taskList by remember(initialTasks) {
        mutableStateOf(if (initialTasks.isNotEmpty()) initialTasks else listOf(""))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "✏️ Ubah Rincian Kegiatan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                )
                Text(
                    text = "Tanggal: ${DateUtils.formatTanggalIndo(tanggal)} (${DateUtils.getDayNameFromIso(tanggal)})",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Text(
                    text = "Daftar Rincian Pekerjaan",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                DynamicTaskListEditor(
                    tasks = taskList,
                    onTasksChange = { taskList = it }
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            val valid = taskList.map { it.trim() }.filter { it.isNotBlank() }
                            if (valid.isNotEmpty()) {
                                onSave(valid)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_edit_kegiatan"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
