package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.ui.components.ScriptUrlModal
import com.example.ui.components.SectionHeader
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlue

@Composable
fun ProfilScreen(
    initialProfile: UserProfile,
    scriptUrl: String,
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "👤 Data Profil Pegawai & Instansi")
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Data Pegawai Penyusun Laporan",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    )

                    OutlinedTextField(
                        value = pegNama,
                        onValueChange = { pegNama = it },
                        label = { Text("Nama & Gelar") },
                        modifier = Modifier.fillMaxWidth().testTag("input_peg_nama"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pegNIP,
                        onValueChange = { pegNIP = it },
                        label = { Text("NIP") },
                        modifier = Modifier.fillMaxWidth().testTag("input_peg_nip"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pegJabatan,
                        onValueChange = { pegJabatan = it },
                        label = { Text("Jabatan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pegPangkat,
                        onValueChange = { pegPangkat = it },
                        label = { Text("Pangkat") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pegGolongan,
                        onValueChange = { pegGolongan = it },
                        label = { Text("Golongan Ruang") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pegSatker,
                        onValueChange = { pegSatker = it },
                        label = { Text("Satuan Kerja") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = false,
                        minLines = 2,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Data Kepala Madrasah (Pejabat Berwenang)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    )

                    OutlinedTextField(
                        value = kepNama,
                        onValueChange = { kepNama = it },
                        label = { Text("Nama Kepsek") },
                        modifier = Modifier.fillMaxWidth().testTag("input_kep_nama"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = kepNIP,
                        onValueChange = { kepNIP = it },
                        label = { Text("NIP Kepsek") },
                        modifier = Modifier.fillMaxWidth().testTag("input_kep_nip"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

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
                            .height(50.dp)
                            .testTag("btn_save_profile"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚙️ URL Apps Script",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DeepNavy)
                        )
                        Text(
                            text = if (scriptUrl.length > 30) scriptUrl.take(30) + "..." else scriptUrl,
                            style = MaterialTheme.typography.bodySmall.copy(color = RoyalBlue),
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { showScriptModal = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ubah URL", tint = RoyalBlue)
                    }
                }
            }
        }
    }

    if (showScriptModal) {
        ScriptUrlModal(
            currentUrl = scriptUrl,
            onDismiss = { showScriptModal = false },
            onSaveUrl = onSaveScriptUrl
        )
    }
}
