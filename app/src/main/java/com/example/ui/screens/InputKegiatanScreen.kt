package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JadwalItem
import com.example.data.model.KegiatanEntry
import com.example.ui.components.DynamicTaskListEditor
import com.example.ui.components.EditKegiatanModal
import com.example.ui.components.JadwalModal
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
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
            .background(NokiaBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "📝 Input Kegiatan Harian & Bulanan")
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // 1. TANGGAL KEGIATAN
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NokiaNavy,
                            border = BorderStroke(1.dp, NokiaCyanGlow),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Today, contentDescription = null, tint = NokiaCyanGlow, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tanggal Kegiatan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 15.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

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
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NokiaCyan,
                                unfocusedBorderColor = NokiaBorder,
                                focusedTextColor = NokiaTextPrimary,
                                unfocusedTextColor = NokiaTextPrimary
                            ),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pilih tanggal", tint = NokiaCyan)
                                }
                            }
                        )
                        OutlinedButton(
                            onClick = { inputTanggal = DateUtils.getTodayIso() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, NokiaCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaCyanDark)
                        ) {
                            Text("Hari Ini", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Show holiday warning on single date if applicable
                    val singleDayHolidayName = DateUtils.getHolidayName(inputTanggal)
                    if (singleDayHolidayName != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NokiaRedLight,
                            border = BorderStroke(1.5.dp, NokiaRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = NokiaRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hari Libur Nasional: $singleDayHolidayName",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NokiaRedDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = NokiaBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. DAFTAR RINCIAN KEGIATAN MANUAL
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NokiaNavy,
                            border = BorderStroke(1.dp, NokiaCyanGlow),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = NokiaCyanGlow, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daftar Rincian Pekerjaan Manual",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 15.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tambahkan satu atau beberapa rincian pekerjaan untuk tanggal di atas.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NokiaTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DynamicTaskListEditor(
                        tasks = inputTaskList,
                        onTasksChange = { inputTaskList = it },
                        placeholder = "Ketik kegiatan harian..."
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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
                            .height(50.dp)
                            .testTag("btn_simpan_ke_daftar"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NokiaCyan, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan ke Daftar Pratinjau", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = NokiaBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. BULAN OTOMATIS (ISI OTOMATIS BULANAN)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NokiaNavy,
                            border = BorderStroke(1.dp, NokiaOrange),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NokiaOrange, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Isi Otomatis Bulanan (Pilih Bulan Saja)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 15.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pilih bulan untuk otomatis membuat draft seluruh hari kerja dari jadwal rutin tanpa perlu klik tanggal satu per satu.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NokiaTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Month picker field with direct dialog trigger
                    OutlinedTextField(
                        value = if (inputBulan.isNotBlank()) DateUtils.formatBulanText(inputBulan) else "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Pilih Bulan", fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("Contoh: Juni 2026", color = NokiaTextTertiary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMonthPickerModal = true }
                            .testTag("field_pilih_bulan_saja"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NokiaCyan)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showMonthPickerModal = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Ubah Bulan", tint = NokiaCyan)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Switch for skipping national holidays
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NokiaCardSurfaceVariant,
                        border = BorderStroke(1.dp, NokiaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lewati Hari Libur Nasional",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NokiaTextPrimary
                                    )
                                )
                                Text(
                                    text = "Tidak memasukkan tanggal merah resmi ke dalam draf",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = NokiaTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Switch(
                                checked = skipNationalHolidays,
                                onCheckedChange = { skipNationalHolidays = it },
                                modifier = Modifier.testTag("switch_skip_holidays"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NokiaLime,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = NokiaBorder
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onGenerateBulk(inputBulan, skipNationalHolidays) },
                        enabled = !isLoading && inputBulan.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NokiaOrange, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_generate_bulk"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (inputBulan.isNotBlank()) "Buat Draft Otomatis (${DateUtils.formatBulanText(inputBulan)})" else "Buat Draft Otomatis",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showJadwalModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_setup_jadwal"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaCyanDark)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Atur Jadwal Rutin (${jadwalList.size} Jadwal)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                            shape = RoundedCornerShape(100.dp),
                            color = NokiaCyanLight,
                            border = BorderStroke(1.dp, NokiaCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Draft Belum Disimpan",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NokiaCyanDark
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
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NokiaRedLight),
                        border = BorderStroke(1.5.dp, NokiaRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                    tint = NokiaRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Terdapat ${holidayEntriesInDraft.size} tanggal hari libur nasional di dalam draf.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NokiaRedDark,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            Button(
                                onClick = onRemoveHolidayEntries,
                                colors = ButtonDefaults.buttonColors(containerColor = NokiaRed, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHoliday) NokiaRedLight else NokiaCardSurface
                    ),
                    border = BorderStroke(1.5.dp, if (isHoliday) NokiaRed.copy(alpha = 0.6f) else NokiaCyan.copy(alpha = 0.3f)),
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
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isHoliday) NokiaRed else NokiaNavy,
                                    border = BorderStroke(1.dp, if (isHoliday) NokiaRed else NokiaCyanGlow),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isHoliday) Color.White else NokiaCyanGlow
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${DateUtils.formatTanggalIndo(entry.tanggal)}  (${DateUtils.getDayNameFromIso(entry.tanggal)})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isHoliday) NokiaRedDark else NokiaTextPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                    if (holidayName != null) {
                                        Text(
                                            text = "🎉 Libur: $holidayName",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = NokiaRedDark
                                            )
                                        )
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { editDraftIndex = index },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Draft",
                                        tint = NokiaCyanDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteDraftRow(index) },
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

                        entry.items.forEachIndexed { itemIdx, task ->
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
                                    onClick = { onRemoveDraftItem(index, itemIdx) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus item ini",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearDraft,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_batal_bulk"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, NokiaBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextSecondary)
                    ) {
                        Text("Batalkan Draf", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSaveDraftPermanently,
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("btn_simpan_bulk_permanen"),
                        colors = ButtonDefaults.buttonColors(containerColor = NokiaLime, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simpan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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


