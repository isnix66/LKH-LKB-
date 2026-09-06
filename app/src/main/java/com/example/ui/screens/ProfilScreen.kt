package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.ScriptUrlModal
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun ProfilScreen(
    initialProfile: UserProfile,
    scriptUrl: String,
    isAdmin: Boolean = false,
    isLoading: Boolean,
    onSaveProfile: (UserProfile) -> Unit,
    onSaveScriptUrl: (String) -> Unit
) {
    var pegNama by remember(initialProfile) { mutableStateOf(initialProfile.pegNama) }
    var pegNIP by remember(initialProfile) { mutableStateOf(initialProfile.pegNIP) }
    var pegJabatan by remember(initialProfile) { mutableStateOf(initialProfile.pegJabatan) }
    var pegPangkat by remember(initialProfile) { mutableStateOf(initialProfile.pegPangkat) }
    var pegGolongan by remember(initialProfile) { mutableStateOf(initialProfile.pegGolongan) }
    var pegSatker by remember(initialProfile) { mutableStateOf(initialProfile.pegSatker) }
    var kepNama by remember(initialProfile) { mutableStateOf(initialProfile.kepNama) }
    var kepNIP by remember(initialProfile) { mutableStateOf(initialProfile.kepNIP) }

    var showScriptModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NokiaBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "👤 Data Profil Pegawai & Instansi")
        }

        // Section 1: Data Pegawai Penyusun Laporan
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                border = BorderStroke(1.5.dp, NokiaCyan.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NokiaNavy,
                            border = BorderStroke(1.dp, NokiaCyanGlow),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = NokiaCyanGlow, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Data Pegawai Penyusun Laporan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    OutlinedTextField(
                        value = pegNama,
                        onValueChange = { pegNama = it },
                        label = { Text("Nama Lengkap & Gelar", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth().testTag("input_peg_nama"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = pegNIP,
                        onValueChange = { pegNIP = it },
                        label = { Text("NIP Pegawai", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth().testTag("input_peg_nip"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )

                    OutlinedTextField(
                        value = pegJabatan,
                        onValueChange = { pegJabatan = it },
                        label = { Text("Jabatan", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )

                    OutlinedTextField(
                        value = pegPangkat,
                        onValueChange = { pegPangkat = it },
                        label = { Text("Pangkat", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )

                    OutlinedTextField(
                        value = pegGolongan,
                        onValueChange = { pegGolongan = it },
                        label = { Text("Golongan Ruang", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = pegSatker,
                        onValueChange = { pegSatker = it },
                        label = { Text("Satuan Kerja (Satker)", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = NokiaBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Section 2: Data Pejabat Berwenang
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NokiaNavy,
                            border = BorderStroke(1.dp, NokiaCyanGlow),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = NokiaCyanGlow, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Data Kepala Madrasah (Pejabat Berwenang)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NokiaTextPrimary,
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    OutlinedTextField(
                        value = kepNama,
                        onValueChange = { kepNama = it },
                        label = { Text("Nama Kepala Madrasah", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth().testTag("input_kep_nama"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NokiaCyan,
                            unfocusedBorderColor = NokiaBorder,
                            focusedTextColor = NokiaTextPrimary,
                            unfocusedTextColor = NokiaTextPrimary,
                            focusedLabelColor = NokiaCyanDark,
                            unfocusedLabelColor = NokiaTextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = kepNIP,
                        onValueChange = { kepNIP = it },
                        label = { Text("NIP Kepala Madrasah", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.fillMaxWidth().testTag("input_kep_nip"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
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

                    Button(
                        onClick = {
                            val prof = UserProfile(
                                pegNama = pegNama.trim(),
                                pegNIP = pegNIP.trim(),
                                pegJabatan = pegJabatan.trim(),
                                pegPangkat = pegPangkat.trim(),
                                pegGolongan = pegGolongan.trim(),
                                pegSatker = pegSatker.trim(),
                                kepNama = kepNama.trim(),
                                kepNIP = kepNIP.trim()
                            )
                            onSaveProfile(prof)
                        },
                        enabled = !isLoading && pegNama.isNotBlank() && pegNIP.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_profile"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NokiaLime, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Perubahan Profil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Section 3: URL Apps Script — HANYA MUNCUL UNTUK AKUN ADMIN
        if (isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NokiaCardSurface),
                    border = BorderStroke(1.5.dp, NokiaOrange.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NokiaOrangeLight,
                                    border = BorderStroke(1.dp, NokiaOrange.copy(alpha = 0.5f)),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NokiaOrangeDark, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "URL Apps Script (Khusus Admin)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NokiaTextPrimary,
                                            fontSize = 14.5.sp
                                        )
                                    )
                                    Text(
                                        text = if (scriptUrl.length > 32) scriptUrl.take(32) + "..." else scriptUrl,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = NokiaCyanDark,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                            IconButton(onClick = { showScriptModal = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Ubah URL", tint = NokiaCyanDark)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScriptModal && isAdmin) {
        ScriptUrlModal(
            currentUrl = scriptUrl,
            onDismiss = { showScriptModal = false },
            onSaveUrl = onSaveScriptUrl
        )
    }
}

