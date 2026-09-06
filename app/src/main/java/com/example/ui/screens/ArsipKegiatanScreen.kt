package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.KegiatanEntry
import com.example.ui.components.EditKegiatanModal
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArsipKegiatanScreen(
    dataKegiatan: List<KegiatanEntry>,
    draftEntries: List<KegiatanEntry> = emptyList(),
    daftarBulan: List<String>,
    selectedBulan: String,
    isLoading: Boolean,
    hasUnsavedChanges: Boolean,
    onSelectMonth: (String) -> Unit,
    onRefresh: () -> Unit,
    onDeleteMonth: (String) -> Unit,
    onUpdateArchiveEntry: (index: Int, newTasks: List<String>) -> Unit,
    onRemoveArchiveItem: (rowIndex: Int, itemIndex: Int) -> Unit,
    onDeleteArchiveRow: (index: Int) -> Unit,
    onSaveAllChangesToServer: () -> Unit
) {
    var editRowIndex by remember { mutableIntStateOf(-1) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NokiaBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "📊 Arsip Kegiatan Tersimpan",
                trailingContent = {
                    if (dataKegiatan.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = NokiaCyanLight,
                            border = BorderStroke(1.dp, NokiaCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${dataKegiatan.size} Entri",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NokiaCyanDark
                                )
                            )
                        }
                    }
                }
            )
        }

        // Filter & Action Toolbar - Saved Months Only (Horizontal Scroll)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NokiaNavy,
                                border = BorderStroke(1.dp, NokiaCyanGlow),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NokiaCyanGlow, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pilih Bulan & Tahun Arsip",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NokiaTextPrimary,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        if (daftarBulan.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = NokiaCyanLight,
                                border = BorderStroke(1.dp, NokiaCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${daftarBulan.size} Bulan Tersimpan",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NokiaCyanDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (daftarBulan.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NokiaCardSurfaceVariant,
                            border = BorderStroke(1.dp, NokiaBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Belum ada bulan tersimpan di server",
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NokiaTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            daftarBulan.chunked(2).forEach { rowMonths ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowMonths.forEach { b ->
                                        val isSelected = b == selectedBulan
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) NokiaCyan else NokiaCardSurfaceVariant,
                                            border = BorderStroke(1.5.dp, if (isSelected) NokiaCyanDark else NokiaBorder),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clickable { onSelectMonth(b) }
                                                .testTag("month_chip_$b")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else NokiaCyanDark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = DateUtils.formatBulanText(b),
                                                    color = if (isSelected) Color.White else NokiaTextPrimary,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    if (rowMonths.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_refresh_arsip"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaCyanDark)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Refresh Data",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.5.sp
                            )
                        }

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            enabled = selectedBulan.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = NokiaRed, contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_hapus_bulan"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hapus Bulan",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Warning banner if unsaved changes
        if (hasUnsavedChanges) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NokiaOrangeLight),
                    border = BorderStroke(1.5.dp, NokiaOrange.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NokiaOrangeDark, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ada perubahan yang belum disimpan ke server Google Sheets. Klik tombol Simpan di bawah untuk menyimpan permanen.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaOrangeDark,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }

        // Local Draft List Section if exists
        if (draftEntries.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                    border = BorderStroke(1.5.dp, NokiaOrange.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NokiaOrange,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "📝",
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "List Draf Tersimpan di Memori",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NokiaTextPrimary,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Text(
                                        text = "${draftEntries.size} entri draf lokal siap dikirim ke Google Sheets",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = NokiaOrangeDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        draftEntries.forEachIndexed { dIdx, draft ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NokiaCardSurfaceVariant,
                                border = BorderStroke(1.dp, NokiaBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📅 ${DateUtils.formatTanggalIndo(draft.tanggal)} (${DateUtils.getDayNameFromIso(draft.tanggal)})",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NokiaTextPrimary,
                                                fontSize = 13.5.sp
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NokiaOrangeLight,
                                            border = BorderStroke(1.dp, NokiaOrange.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "Draf ${dIdx + 1}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = NokiaOrangeDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    draft.items.forEachIndexed { tIdx, task ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 1.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "• ",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = NokiaOrangeDark
                                                )
                                            )
                                            Text(
                                                text = task,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = NokiaTextPrimary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Empty state
        if (dataKegiatan.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                    border = BorderStroke(1.5.dp, NokiaBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📭",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedBulan.isBlank()) "Silakan pilih bulan untuk menampilkan data." else "Belum ada data untuk bulan $selectedBulan",
                                style = MaterialTheme.typography.bodyMedium.copy(color = NokiaTextSecondary, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        } else {
            itemsIndexed(dataKegiatan) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                    border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NokiaNavy,
                                    border = BorderStroke(1.dp, NokiaCyanGlow),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NokiaCyanGlow
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${DateUtils.formatTanggalIndo(item.tanggal)}  (${DateUtils.getDayNameFromIso(item.tanggal)})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NokiaTextPrimary,
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { editRowIndex = index },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Baris",
                                        tint = NokiaCyanDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteArchiveRow(index) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus Baris",
                                        tint = NokiaRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        item.items.forEachIndexed { itemIdx, task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${itemIdx + 1}.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NokiaCyanDark
                                    ),
                                    modifier = Modifier.width(22.dp)
                                )
                                Text(
                                    text = task,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NokiaTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onRemoveArchiveItem(index, itemIdx) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus rincian",
                                        tint = NokiaTextTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onSaveAllChangesToServer,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_all_to_server"),
                    colors = ButtonDefaults.buttonColors(containerColor = NokiaLime, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Perubahan ke Server", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // Edit modal
    if (editRowIndex >= 0 && editRowIndex in dataKegiatan.indices) {
        val entry = dataKegiatan[editRowIndex]
        EditKegiatanModal(
            tanggal = entry.tanggal,
            initialTasks = entry.items,
            onDismiss = { editRowIndex = -1 },
            onSave = { newTasks ->
                onUpdateArchiveEntry(editRowIndex, newTasks)
                editRowIndex = -1
            }
        )
    }

    // Delete Month Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = NokiaCardSurface,
            title = {
                Text("Konfirmasi Hapus Bulan", fontWeight = FontWeight.Bold, color = NokiaTextPrimary)
            },
            text = {
                Text("PERINGATAN: Apakah Anda yakin ingin menghapus seluruh arsip data beserta Sheet untuk bulan \"$selectedBulan\"? Tindakan ini tidak dapat dibatalkan!", color = NokiaTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteMonth(selectedBulan)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NokiaRed, contentColor = Color.White)
                ) {
                    Text("Ya, Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NokiaBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextSecondary)
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

