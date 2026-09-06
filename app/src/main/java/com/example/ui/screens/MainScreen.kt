package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LkhUiState
import com.example.ui.LkhViewModel
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueLight

enum class MainTab(val title: String, val iconName: String) {
    PROFIL("Profil", "Person"),
    INPUT("Input", "Edit"),
    ARSIP("Arsip", "Archive"),
    CETAK("Cetak", "Print"),
    ADMIN("Admin", "AdminPanelSettings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: LkhViewModel,
    uiState: LkhUiState
) {
    var selectedTab by remember { mutableStateOf(MainTab.PROFIL) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser = uiState.currentUser
    val isAdmin = currentUser?.role == "admin"

    val availableTabs = remember(isAdmin) {
        if (isAdmin) {
            MainTab.entries
        } else {
            MainTab.entries.filter { it != MainTab.ADMIN }
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.infoMessage) {
        if (!uiState.errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = "❌ ${uiState.errorMessage}",
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        } else if (!uiState.infoMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = "✅ ${uiState.infoMessage}",
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Aplikasi LKH & LKB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepNavy
                            )
                        )
                        Text(
                            text = "${currentUser?.displayName ?: "Pegawai"} • ${currentUser?.role?.uppercase() ?: "USER"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RoyalBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(
                            onClick = {
                                selectedTab = MainTab.ADMIN
                                viewModel.loadAdminUsers()
                            },
                            modifier = Modifier.testTag("top_btn_admin")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Panel Admin", tint = RoyalBlue)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("top_btn_logout")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Keluar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                availableTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val icon = when (tab) {
                        MainTab.INPUT -> Icons.Default.EditNote
                        MainTab.ARSIP -> Icons.Default.Assessment
                        MainTab.CETAK -> Icons.Default.Print
                        MainTab.PROFIL -> Icons.Default.Person
                        MainTab.ADMIN -> Icons.Default.AdminPanelSettings
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedTab = tab
                            if (tab == MainTab.ADMIN) {
                                viewModel.loadAdminUsers()
                            }
                        },
                        icon = { Icon(icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RoyalBlue,
                            selectedTextColor = RoyalBlue,
                            indicatorColor = RoyalBlueLight
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                MainTab.INPUT -> {
                    InputKegiatanScreen(
                        draftEntries = uiState.draftEntries,
                        jadwalList = uiState.jadwalList,
                        isLoading = uiState.isLoading,
                        onAddToDraft = { tgl, tasks -> viewModel.addToDraft(tgl, tasks) },
                        onGenerateBulk = { bln, skipHolidays -> viewModel.generateBulkDraft(bln, skipHolidays) },
                        onRemoveDraftItem = { dIdx, iIdx -> viewModel.removeDraftItem(dIdx, iIdx) },
                        onDeleteDraftRow = { dIdx -> viewModel.deleteDraftRow(dIdx) },
                        onUpdateDraftRow = { dIdx, tasks -> viewModel.updateDraftRow(dIdx, tasks) },
                        onRemoveHolidayEntries = { viewModel.removeHolidayEntriesFromDraft() },
                        onClearDraft = { viewModel.clearDraft() },
                        onSaveDraftPermanently = { viewModel.saveDraftPermanently() },
                        onSaveJadwal = { hari, tasks, eIdx -> viewModel.saveSchedule(hari, tasks, eIdx) },
                        onDeleteJadwal = { idx -> viewModel.deleteSchedule(idx) }
                    )
                }
                MainTab.ARSIP -> {
                    ArsipKegiatanScreen(
                        dataKegiatan = uiState.dataKegiatan,
                        daftarBulan = uiState.daftarBulan,
                        selectedBulan = uiState.selectedBulan,
                        isLoading = uiState.isLoading,
                        hasUnsavedChanges = uiState.hasUnsavedChanges,
                        onSelectMonth = { viewModel.selectMonth(it) },
                        onRefresh = { viewModel.selectMonth(uiState.selectedBulan) },
                        onDeleteMonth = { viewModel.deleteEntireMonth(it) },
                        onUpdateArchiveEntry = { idx, tasks -> viewModel.updateArchiveEntry(idx, tasks) },
                        onRemoveArchiveItem = { rIdx, iIdx -> viewModel.removeArchiveItem(rIdx, iIdx) },
                        onDeleteArchiveRow = { idx -> viewModel.deleteArchiveRow(idx) },
                        onSaveAllChangesToServer = { viewModel.saveAllMonthChangesToServer() }
                    )
                }
                MainTab.CETAK -> {
                    PusatCetakScreen(
                        profile = uiState.userProfile,
                        dataKegiatan = uiState.dataKegiatan,
                        selectedBulan = uiState.selectedBulan,
                        logoBase64 = uiState.logoBase64,
                        isLoading = uiState.isLoading,
                        onUploadLogo = { viewModel.uploadLogo(it) },
                        onDeleteLogo = { viewModel.deleteLogo() }
                    )
                }
                MainTab.PROFIL -> {
                    ProfilScreen(
                        initialProfile = uiState.userProfile,
                        scriptUrl = uiState.scriptUrl,
                        isLoading = uiState.isLoading,
                        onSaveProfile = { viewModel.saveProfile(it) },
                        onSaveScriptUrl = { viewModel.setScriptUrl(it) }
                    )
                }
                MainTab.ADMIN -> {
                    AdminScreen(
                        users = uiState.adminUsers,
                        isLoading = uiState.isLoading,
                        onRefresh = { viewModel.loadAdminUsers() },
                        onAddUser = { viewModel.adminAddUser(it) },
                        onUpdateUser = { orig, u -> viewModel.adminUpdateUser(orig, u) },
                        onDeleteUser = { viewModel.adminDeleteUser(it) }
                    )
                }
            }

            if (uiState.isLoading) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = RoyalBlue
                        )
                        Text(
                            text = "Menyinkronkan dengan Google Sheets...",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}
