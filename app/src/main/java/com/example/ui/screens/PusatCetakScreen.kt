package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.AutoFitText
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.KegiatanEntry
import com.example.data.model.UserProfile
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.util.DateUtils
import com.example.util.PrintDocumentHelper
import java.io.ByteArrayOutputStream
import java.util.Locale

@Composable
fun PusatCetakScreen(
    profile: UserProfile,
    dataKegiatan: List<KegiatanEntry>,
    selectedBulan: String,
    logoBase64: String?,
    isLoading: Boolean,
    onUploadLogo: (base64: String) -> Unit,
    onDeleteLogo: () -> Unit
) {
    val context = LocalContext.current
    var activePreviewType by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("") }

    // Android Photo Picker (zero permission needed)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    onUploadLogo(base64)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val decodedLogoBitmap = remember(logoBase64) {
        if (!logoBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(logoBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun openDocument(type: String, title: String) {
        activePreviewType = type
        previewTitle = title
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NokiaBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "🖨️ Pusat Cetak Dokumen")
        }

        // Cetak Options Cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pilih Format Laporan (${if (dataKegiatan.isEmpty()) "Tidak ada data pada bulan terpilih" else "${dataKegiatan.size} kegiatan"})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = NokiaTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    OutlinedButton(
                        onClick = { openDocument("sampul", "Cetak Sampul Laporan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_cetak_sampul"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp), tint = NokiaCyanDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "1. Cetak Sampul Laporan Kinerja",
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { openDocument("lkh", "Cetak LKH (Laporan Kerja Harian)") },
                        enabled = dataKegiatan.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_cetak_lkh"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Default.ViewList, contentDescription = null, modifier = Modifier.size(20.dp), tint = NokiaCyanDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "2. Cetak LKH (Laporan Kerja Harian)",
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { openDocument("lkb", "Cetak LKB (Laporan Kerja Bulanan)") },
                        enabled = dataKegiatan.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_cetak_lkb"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaTextPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp), tint = NokiaCyanDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "3. Cetak LKB (Laporan Kerja Bulanan)",
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = { openDocument("semua", "Cetak Seluruh Laporan Lengkap") },
                        enabled = dataKegiatan.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_cetak_semua"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NokiaCyan, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Cetak Lengkap: Sampul + LKH + LKB",
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

        // Kop Surat & Logo Section
        item {
            SectionHeader(title = "🖼️ Pengaturan Kop Surat & Logo Instansi")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Logo ini dicetak pada Kop Surat LKH dan LKB.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NokiaTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                NokiaCardSurfaceVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(1.5.dp, NokiaBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (decodedLogoBitmap != null) {
                            Image(
                                bitmap = decodedLogoBitmap,
                                contentDescription = "Logo Instansi Khusus",
                                modifier = Modifier
                                    .fillMaxHeight(0.85f)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_kemenag_badge),
                                contentDescription = "Logo Kemenag Default",
                                modifier = Modifier
                                    .fillMaxHeight(0.85f)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_upload_logo"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NokiaCyan, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Logo", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDeleteLogo,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_hapus_logo"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, NokiaRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NokiaRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Logo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Dirancang khusus untuk MTs N 2 Garut • Terintegrasi Google Workspace",
                    style = MaterialTheme.typography.labelSmall.copy(color = NokiaTextTertiary, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }

    // Native Document Preview & Print Dialog (Zero Chromium/Mesa Dependency)
    if (activePreviewType != null) {
        val currentType = activePreviewType!!
        val docName = "Laporan_${currentType.uppercase(Locale.getDefault())}_${profile.pegNama.replace("\\s+".toRegex(), "_")}"

        Dialog(
            onDismissRequest = { activePreviewType = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NokiaCardSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Dialog Header with actions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NokiaCardSurface)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = previewTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NokiaTextPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Pratinjau Dokumen Format A4 Resmi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NokiaTextSecondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            IconButton(
                                onClick = { activePreviewType = null }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = NokiaTextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    PrintDocumentHelper.sharePdfDocument(
                                        context = context,
                                        docName = docName,
                                        type = currentType,
                                        profile = profile,
                                        dataKegiatan = dataKegiatan,
                                        selectedBulanFilter = selectedBulan,
                                        logoBase64 = logoBase64
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = NokiaCyanLight, contentColor = NokiaCyanDark),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_share_pdf")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    PrintDocumentHelper.printNativeDocument(
                                        context = context,
                                        docName = docName,
                                        type = currentType,
                                        profile = profile,
                                        dataKegiatan = dataKegiatan,
                                        selectedBulanFilter = selectedBulan,
                                        logoBase64 = logoBase64
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NokiaCyan, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_trigger_print")
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cetak / PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = NokiaBorder, thickness = 1.dp)

                    // Native Compose Document Sheet
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(NokiaBackground)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 620.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            shadowElevation = 4.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                NativeDocumentSheetView(
                                    type = currentType,
                                    profile = profile,
                                    dataKegiatan = dataKegiatan,
                                    selectedBulan = selectedBulan,
                                    customLogoBitmap = decodedLogoBitmap
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = { activePreviewType = null },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Text("Tutup Pratinjau")
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// NATIVE COMPOSE DOCUMENT PREVIEW (High Performance, Pure Vector & Text)
// =========================================================================

@Composable
private fun NativeDocumentSheetView(
    type: String,
    profile: UserProfile,
    dataKegiatan: List<KegiatanEntry>,
    selectedBulan: String,
    customLogoBitmap: androidx.compose.ui.graphics.ImageBitmap?
) {
    val (targetYear, targetMonthIndex) = remember(selectedBulan, dataKegiatan) {
        DateUtils.resolveYearAndMonth(selectedBulan, dataKegiatan.map { it.tanggal })
    }
    val strBulanTahun = DateUtils.formatBulanText(
        String.format(Locale.US, "%04d-%02d", targetYear, targetMonthIndex + 1)
    ).uppercase(Locale.getDefault())
    val tanggalCetak = DateUtils.getWorkingEndMonthDate(targetYear, targetMonthIndex)

    when (type) {
        "sampul" -> {
            NativeSampulSection(profile, strBulanTahun, customLogoBitmap)
        }
        "lkh" -> {
            NativeLkhSection(profile, dataKegiatan, tanggalCetak, customLogoBitmap)
        }
        "lkb" -> {
            NativeLkbSection(profile, dataKegiatan, strBulanTahun, tanggalCetak, customLogoBitmap)
        }
        "semua" -> {
            NativeSampulSection(profile, strBulanTahun, customLogoBitmap)
            PageBreakBadge(pageNumber = 2, title = "LAPORAN KERJA HARIAN (LKH)")
            NativeLkhSection(profile, dataKegiatan, tanggalCetak, customLogoBitmap)
            PageBreakBadge(pageNumber = 3, title = "LAPORAN KERJA BULANAN (LKB)")
            NativeLkbSection(profile, dataKegiatan, strBulanTahun, tanggalCetak, customLogoBitmap)
        }
    }
}

@Composable
private fun PageBreakBadge(pageNumber: Int, title: String) {
    Spacer(modifier = Modifier.height(28.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = RoyalBlue.copy(alpha = 0.4f))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = RoyalBlue.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Halaman $pageNumber • $title",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlue),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = RoyalBlue.copy(alpha = 0.4f))
    }
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun NativeKopSuratView(
    satker: String,
    customLogoBitmap: androidx.compose.ui.graphics.ImageBitmap?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (customLogoBitmap != null) {
            Image(
                bitmap = customLogoBitmap,
                contentDescription = "Logo Kemenag",
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_kemenag_badge),
                contentDescription = "Logo Kemenag",
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "KEMENTERIAN AGAMA REPUBLIK INDONESIA",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                text = "KANTOR KEMENTERIAN AGAMA KABUPATEN GARUT",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            AutoFitText(
                text = satker.uppercase(Locale.getDefault()),
                targetTextSize = 11.sp,
                minTextSize = 3.sp,
                isBold = true,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                color = Color.Black,
                maxLines = 1
            )
            Text(
                text = "Jalan Raya Wanakerta No.28 Cibatu-Garut 44185",
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
            Text(
                text = "Telepon (0262) 2860000 Email: mtsn1cibatu@yahoo.co.id",
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(thickness = 2.5.dp, color = Color.Black)
    Spacer(modifier = Modifier.height(2.dp))
    HorizontalDivider(thickness = 0.8.dp, color = Color.Black)
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun NativeTtdBlockView(
    profile: UserProfile,
    tanggalCetak: String
) {
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Mengetahui", fontSize = 10.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            Text("Kepala MTsN 2 Garut", fontSize = 10.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = profile.kepNama,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Serif,
                textDecoration = TextDecoration.Underline,
                color = Color.Black
            )
            Text("NIP. ${profile.kepNIP}", fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Garut, $tanggalCetak", fontSize = 10.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            Text("Penyusun", fontSize = 10.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = profile.pegNama,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Serif,
                textDecoration = TextDecoration.Underline,
                color = Color.Black
            )
            Text("NIP. ${profile.pegNIP}", fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
        }
    }
}

@Composable
private fun NativeSampulSection(
    profile: UserProfile,
    strBulanTahun: String,
    customLogoBitmap: androidx.compose.ui.graphics.ImageBitmap?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (customLogoBitmap != null) {
            Image(
                bitmap = customLogoBitmap,
                contentDescription = "Logo Kemenag",
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_kemenag_badge),
                contentDescription = "Logo Kemenag",
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "LAPORAN KINERJA BULANAN",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        Text(
            text = "APARATUR SIPIL NEGARA KEMENTERIAN AGAMA",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        Text(
            text = "BULAN $strBulanTahun",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IdentitasRow("NAMA", profile.pegNama, isBold = true)
                IdentitasRow("NIP", profile.pegNIP)
                IdentitasRow("JABATAN", profile.pegJabatan.uppercase(Locale.getDefault()))
                IdentitasRow("SATUAN KERJA", profile.pegSatker.uppercase(Locale.getDefault()))
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "KEMENTERIAN AGAMA",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        AutoFitText(
            text = profile.pegSatker.uppercase(Locale.getDefault()),
            targetTextSize = 12.sp,
            minTextSize = 3.sp,
            isBold = true,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            color = Color.Black,
            maxLines = 1
        )
        Text(
            text = "JL. Raya Wanakerta No. 28 Cibatu - Garut",
            fontSize = 10.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )
    }
}

@Composable
private fun IdentitasRow(
    label: String,
    value: String,
    labelWidth: androidx.compose.ui.unit.Dp = 110.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.5.sp,
    isBold: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            modifier = Modifier.width(labelWidth),
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = ":  ",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color.Black
        )
        AutoFitText(
            text = value,
            modifier = Modifier.weight(1f),
            targetTextSize = fontSize,
            minTextSize = 3.sp,
            isBold = isBold,
            fontFamily = FontFamily.Serif,
            color = Color.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun NativeLkbSection(
    profile: UserProfile,
    dataKegiatan: List<KegiatanEntry>,
    strBulanTahun: String,
    tanggalCetak: String,
    customLogoBitmap: androidx.compose.ui.graphics.ImageBitmap?
) {
    NativeKopSuratView(profile.pegSatker, customLogoBitmap)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LAPORAN KERJA BULANAN",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Serif,
            color = Color.Black
        )
        Text(
            text = "BULAN $strBulanTahun",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Serif,
            color = Color.Black
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IdentitasRow("Nama", profile.pegNama, labelWidth = 100.dp, fontSize = 10.sp, isBold = true)
        IdentitasRow("NIP", profile.pegNIP, labelWidth = 100.dp, fontSize = 10.sp)
        IdentitasRow("Jabatan", profile.pegJabatan, labelWidth = 100.dp, fontSize = 10.sp)
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Structured Table
    val rekap = PrintDocumentHelper.aggregateLkb(dataKegiatan)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFEFEF))
                .border(0.5.dp, Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("NO", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(0.5.dp, Color.Black)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("KEGIATAN", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            }
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .border(0.5.dp, Color.Black)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VOLUME", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                }
                HorizontalDivider(color = Color.Black, thickness = 0.5.dp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JUMLAH", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color.Black)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SATUAN", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                    }
                }
            }
        }

        // Table Rows
        if (rekap.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada kegiatan tercatat pada bulan ini", fontSize = 10.sp, fontFamily = FontFamily.Serif, color = Color.Gray)
            }
        } else {
            var index = 1
            for ((key, count) in rekap) {
                var volumeText = "Kegiatan"
                var jumlah = count.toString()
                if (key.contains("KBM", ignoreCase = true)) {
                    volumeText = "Kegiatan"
                    jumlah = "${count * 3} JP"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, Color.Black),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(index.toString(), fontSize = 9.5.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color.Black)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(key, fontSize = 9.5.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                    }
                    Row(
                        modifier = Modifier
                            .width(130.dp)
                            .border(0.5.dp, Color.Black)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(jumlah, fontSize = 9.5.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, Color.Black)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(volumeText, fontSize = 9.5.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                        }
                    }
                }
                index++
            }
        }
    }

    NativeTtdBlockView(profile, tanggalCetak)
}

@Composable
private fun NativeLkhSection(
    profile: UserProfile,
    dataKegiatan: List<KegiatanEntry>,
    tanggalCetak: String,
    customLogoBitmap: androidx.compose.ui.graphics.ImageBitmap?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Laporan Kerja",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            fontFamily = FontFamily.Serif,
            textDecoration = TextDecoration.Underline,
            color = Color.Black
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IdentitasRow("Nama", profile.pegNama.uppercase(Locale.getDefault()), labelWidth = 100.dp, fontSize = 9.5.sp, isBold = true)
        IdentitasRow("NIP", profile.pegNIP, labelWidth = 100.dp, fontSize = 9.5.sp)
        IdentitasRow("Jabatan", profile.pegJabatan, labelWidth = 100.dp, fontSize = 9.5.sp)
        IdentitasRow("Pangkat", profile.pegPangkat, labelWidth = 100.dp, fontSize = 9.5.sp)
        IdentitasRow("Golongan", profile.pegGolongan, labelWidth = 100.dp, fontSize = 9.5.sp)
    }

    Spacer(modifier = Modifier.height(14.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEFEFEF))
                .border(0.5.dp, Color.Black)
        ) {
            Box(modifier = Modifier.width(32.dp).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text("No.", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            }
            Box(modifier = Modifier.width(90.dp).border(0.5.dp, Color.Black).padding(6.dp), contentAlignment = Alignment.Center) {
                Text("Kegiatan", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            }
            Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(6.dp), contentAlignment = Alignment.Center) {
                Text("Pekerjaan", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            }
            Box(modifier = Modifier.width(85.dp).border(0.5.dp, Color.Black).padding(6.dp), contentAlignment = Alignment.Center) {
                Text("Tanggal", fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
            }
        }

        // Table Rows
        if (dataKegiatan.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada kegiatan harian tercatat", fontSize = 10.sp, fontFamily = FontFamily.Serif, color = Color.Gray)
            }
        } else {
            dataKegiatan.forEachIndexed { i, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, Color.Black)
                ) {
                    Box(modifier = Modifier.width(32.dp).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        Text((i + 1).toString(), fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                    }
                    Box(modifier = Modifier.width(90.dp).border(0.5.dp, Color.Black).padding(6.dp)) {
                        Text("Laporan Kerja Harian", fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                    }
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.Black).padding(6.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            item.items.forEachIndexed { idx, act ->
                                Text("${idx + 1}. $act", fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                            }
                        }
                    }
                    Box(modifier = Modifier.width(85.dp).border(0.5.dp, Color.Black).padding(6.dp), contentAlignment = Alignment.Center) {
                        Text(DateUtils.formatTanggalIndo(item.tanggal), fontSize = 9.sp, fontFamily = FontFamily.Serif, color = Color.Black)
                    }
                }
            }
        }
    }

    NativeTtdBlockView(profile, tanggalCetak)
}
