package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JadwalItem
import com.example.data.model.KegiatanEntry
import com.example.ui.components.DynamicTaskListEditor
import com.example.ui.components.EditKegiatanModal
import com.example.ui.components.JadwalModal
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputKegiatanScreen(
    draftEntries: List<KegiatanEntry>,
    jadwalList: List<JadwalItem>,
    isLoading: Boolean,
    onAddToDraft: (tanggal: String, tasks: List<String>) -> Unit,
    onGenerateBulk: (bulan: String, skipHolidays: Boolean) -> Unit,
    onRemoveDraftItem: (draftIndex: Int, itemIndex: Int) -> Unit,
    onDeleteDraftRow: (draftIndex: Int) -> Unit,
    onUpdateDraftRow: (draftIndex: Int, newTasks: List<String>) -> Unit,
    onRemoveHolidayEntries: () -> Unit,
    onClearDraft: () -> Unit,
    onSaveDraftPermanently: () -> Unit,
    onSaveJadwal: (hari: String, tasks: List<String>, editIndex: Int) -> Unit,
    onDeleteJadwal: (index: Int) -> Unit
) {
    val context = LocalContext.current
    var inputTanggal by remember { mutableStateOf(DateUtils.getTodayIso()) }
    var inputBulan by remember { mutableStateOf(DateUtils.getCurrentMonthIso()) }
    var skipNationalHolidays by remember { mutableStateOf(true) }
    var inputTaskList by remember { mutableStateOf(listOf("")) }

    var showMonthPickerModal by remember { mutableStateOf(false) }
    var showJadwalModal by remember { mutableStateOf(false) }
    var editDraftIndex by remember { mutableIntStateOf(-1) }

    // Check how many holiday entries in current draft
    val holidayEntriesInDraft = remember(draftEntries) {
        draftEntries.filter { DateUtils.isHoliday(it.tanggal) }
    }

    // DatePicker Dialog helper for single date
    fun showDatePicker() {
        val cal = Calendar.getInstance()
        val parts = inputTanggal.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            inputTanggal = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            inputBulan = String.format("%04d-%02d", year, month + 1)
        }, y, m, d).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "📝 Input Kegiatan Harian")
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 1. TANGGAL KEGIATAN
                    Text(
                        text = "📅 Tanggal Kegiatan",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = "${DateUtils.formatTanggalIndo(inputTanggal)} (${DateUtils.getDayNameFromIso(inputTanggal)})",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("field_tanggal_kegiatan"),
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pilih tanggal", tint = RoyalBlue)
                                }
                            }
                        )
                        OutlinedButton(
                            onClick = { inputTanggal = DateUtils.getTodayIso() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Hari Ini")
                        }
                    }

                    // Show holiday warning on single date if applicable
                    val singleDayHolidayName = DateUtils.getHolidayName(inputTanggal)
                    if (singleDayHolidayName != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hari Libur Nasional: $singleDayHolidayName",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. DAFTAR RINCIAN KEGIATAN MANUAL
                    Text(
                        text = "📄 Daftar Rincian Pekerjaan Manual",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Text(
                        text = "Tambahkan satu atau beberapa rincian pekerjaan untuk tanggal di atas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DynamicTaskListEditor(
                        tasks = inputTaskList,
                        onTasksChange = { inputTaskList = it },
                        placeholder = "Ketik kegiatan harian..."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val valid = inputTaskList.map { it.trim() }.filter { it.isNotBlank() }
                            if (valid.isNotEmpty()) {
                                onAddToDraft(inputTanggal, valid)
                                inputTaskList = listOf("")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_simpan_ke_daftar"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan ke Daftar Pratinjau ➔", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. BULAN OTOMATIS (ISI OTOMATIS BULANAN)
                    Text(
                        text = "📆 Isi Otomatis Bulanan (Pilih Bulan Saja)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Text(
                        text = "Pilih bulan untuk otomatis membuat draft seluruh hari kerja dari jadwal rutin tanpa perlu klik tanggal satu per satu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Month picker field with direct dialog trigger
                    OutlinedTextField(
                        value = if (inputBulan.isNotBlank()) DateUtils.formatBulanText(inputBulan) else "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Pilih Bulan") },
                        placeholder = { Text("Contoh: Juni 2026") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMonthPickerModal = true }
                            .testTag("field_pilih_bulan_saja"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = RoyalBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showMonthPickerModal = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Ubah Bulan", tint = RoyalBlue)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Switch for skipping national holidays
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lewati Hari Libur Nasional",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tidak memasukkan tanggal merah resmi ke dalam draf",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = skipNationalHolidays,
                                onCheckedChange = { skipNationalHolidays = it },
                                modifier = Modifier.testTag("switch_skip_holidays")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onGenerateBulk(inputBulan, skipNationalHolidays) },
                        enabled = !isLoading && inputBulan.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_generate_bulk"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Buat Draft Otomatis Bulan ${DateUtils.formatBulanText(inputBulan)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showJadwalModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_setup_jadwal"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📅 Atur Jadwal Rutin (${jadwalList.size} Jadwal Tersedia)")
                    }
                }
            }
        }

        // Draft Section
        if (draftEntries.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "📋 Daftar Pratinjau Draft (${draftEntries.size} Hari)",
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RoyalBlueLight
                        ) {
                            Text(
                                text = "Draft Belum Disimpan",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue
                                )
                            )
                        }
                    }
                )
            }

            // If holidays found in draft, show helper card with quick cleanup action
            if (holidayEntriesInDraft.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Terdapat ${holidayEntriesInDraft.size} tanggal hari libur nasional di dalam draf.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            }

                            Button(
                                onClick = onRemoveHolidayEntries,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_purge_holidays")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Hapus Hari Libur", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            itemsIndexed(draftEntries) { index, entry ->
                val holidayName = DateUtils.getHolidayName(entry.tanggal)
                val isHoliday = holidayName != null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHoliday) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isHoliday) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isHoliday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHoliday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${DateUtils.formatTanggalIndo(entry.tanggal)}  (${DateUtils.getDayNameFromIso(entry.tanggal)})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHoliday) MaterialTheme.colorScheme.error else DeepNavy
                                        )
                                    )
                                    if (holidayName != null) {
                                        Text(
                                            text = "🎉 Libur: $holidayName",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        )
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { editDraftIndex = index },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Draft",
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteDraftRow(index) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus Baris",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        entry.items.forEachIndexed { itemIdx, task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${itemIdx + 1}.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.width(22.dp)
                                )
                                Text(
                                    text = task,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onRemoveDraftItem(index, itemIdx) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus item ini",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearDraft,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_batal_bulk"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batalkan Draf")
                    }

                    Button(
                        onClick = onSaveDraftPermanently,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("btn_simpan_bulk_permanen"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan Permanen ke Sheets", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showMonthPickerModal) {
        MonthYearPickerDialog(
            currentMonthIso = inputBulan,
            onDismiss = { showMonthPickerModal = false },
            onMonthSelected = { selected ->
                inputBulan = selected
            }
        )
    }

    if (showJadwalModal) {
        JadwalModal(
            jadwalList = jadwalList,
            onDismiss = { showJadwalModal = false },
            onSaveJadwal = onSaveJadwal,
            onDeleteJadwal = onDeleteJadwal
        )
    }

    if (editDraftIndex >= 0 && editDraftIndex in draftEntries.indices) {
        val entry = draftEntries[editDraftIndex]
        EditKegiatanModal(
            tanggal = entry.tanggal,
            initialTasks = entry.items,
            onDismiss = { editDraftIndex = -1 },
            onSave = { newTasks ->
                onUpdateDraftRow(editDraftIndex, newTasks)
                editDraftIndex = -1
            }
        )
    }
}

