package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.JadwalItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalModal(
    jadwalList: List<JadwalItem>,
    onDismiss: () -> Unit,
    onSaveJadwal: (hari: String, tasks: List<String>, editIndex: Int) -> Unit,
    onDeleteJadwal: (index: Int) -> Unit
) {
    var editIndex by remember { mutableIntStateOf(-1) }
    var selectedHari by remember { mutableStateOf("Senin") }
    var taskList by remember { mutableStateOf(listOf("")) }

    val hariOptions = listOf(
        "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu", "Setiap Hari Kerja"
    )

    fun startEdit(index: Int) {
        val item = jadwalList.getOrNull(index) ?: return
        editIndex = index
        selectedHari = item.hari
        val parsed = item.items
        taskList = if (parsed.isNotEmpty()) parsed else listOf("")
    }

    fun resetForm() {
        editIndex = -1
        selectedHari = "Senin"
        taskList = listOf("")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
            border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📅 Setup Jadwal Kegiatan Rutin",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NokiaTextPrimary
                    )
                )
                Text(
                    text = "Jadwal ini digunakan otomatis saat generate bulanan.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NokiaTextSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NokiaCardSurfaceVariant),
                            border = BorderStroke(1.dp, NokiaBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (editIndex >= 0) "✏️ Edit Jadwal" else "➕ Tambah Jadwal Baru",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NokiaTextPrimary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "Hari Pelaksanaan",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NokiaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedHari,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NokiaCyan,
                                            unfocusedBorderColor = NokiaBorder,
                                            focusedTextColor = NokiaTextPrimary,
                                            unfocusedTextColor = NokiaTextPrimary,
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        hariOptions.forEach { h ->
                                            DropdownMenuItem(
                                                text = { Text(h, color = NokiaTextPrimary, fontWeight = FontWeight.SemiBold) },
                                                onClick = {
                                                    selectedHari = h
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Daftar Rincian Kegiatan Rutin",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NokiaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                DynamicTaskListEditor(
                                    tasks = taskList,
                                    onTasksChange = { taskList = it },
                                    placeholder = "Contoh: Apel Pagi, KBM Kelas..."
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (editIndex >= 0) {
                                        OutlinedButton(
                                            onClick = { resetForm() },
                                            modifier = Modifier.weight(1f).height(46.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, NokiaBorder),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextSecondary)
                                        ) {
                                            Text("Batal Edit", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            val validTasks = taskList.map { it.trim() }.filter { it.isNotBlank() }
                                            if (validTasks.isNotEmpty()) {
                                                onSaveJadwal(selectedHari, validTasks, editIndex)
                                                resetForm()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .testTag("btn_simpan_jadwal"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NokiaCyan, contentColor = Color.White),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                    ) {
                                        Text(if (editIndex >= 0) "Perbarui Jadwal" else "Simpan Jadwal", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = NokiaBorder)
                        Text(
                            text = "DAFTAR JADWAL AKTIF",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaCyanDark,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    if (jadwalList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada jadwal rutin tersimpan.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = NokiaTextSecondary, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    } else {
                        itemsIndexed(jadwalList) { idx, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                                border = BorderStroke(1.5.dp, NokiaBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = NokiaCyanLight,
                                            border = BorderStroke(1.dp, NokiaCyan.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = item.hari,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = NokiaCyanDark
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        item.items.forEachIndexed { i, task ->
                                            Text(
                                                text = "${i + 1}. $task",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = NokiaTextPrimary,
                                                    fontWeight = FontWeight.Medium
                                                ),
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { startEdit(idx) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = NokiaCyanDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteJadwal(idx) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = NokiaRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_jadwal"),
                    colors = ButtonDefaults.buttonColors(containerColor = NokiaNavy, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup Dialog", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
