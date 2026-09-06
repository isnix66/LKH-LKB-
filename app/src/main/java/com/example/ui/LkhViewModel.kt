package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.JadwalItem
import com.example.data.model.KegiatanEntry
import com.example.data.model.UserAccount
import com.example.data.model.UserProfile
import com.example.data.remote.GasApiClient
import com.example.data.repository.LkhRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LkhUiState(
    val currentUser: UserAccount? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val userProfile: UserProfile = UserProfile(),
    val dataKegiatan: List<KegiatanEntry> = emptyList(),
    val draftEntries: List<KegiatanEntry> = emptyList(),
    val manualDraftEntries: List<KegiatanEntry> = emptyList(),
    val daftarBulan: List<String> = emptyList(),
    val selectedBulan: String = "",
    val jadwalList: List<JadwalItem> = emptyList(),
    val logoBase64: String? = null,
    val adminUsers: List<UserAccount> = emptyList(),
    val scriptUrl: String = GasApiClient.DEFAULT_URL,
    val hasUnsavedChanges: Boolean = false,
    val rememberMe: Boolean = true,
    val savedUsername: String = "",
    val savedPassword: String = ""
)

class LkhViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LkhRepository

    private val _uiState = MutableStateFlow(LkhUiState())
    val uiState: StateFlow<LkhUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        val apiClient = GasApiClient()
        repository = LkhRepository(application, db, apiClient)

        val (savedU, savedP, rem) = repository.getSavedCredentials()
        val current = repository.getCurrentUser()

        _uiState.update {
            it.copy(
                savedUsername = savedU,
                savedPassword = savedP,
                rememberMe = rem,
                currentUser = current,
                isLoggedIn = current != null,
                scriptUrl = repository.getScriptUrl()
            )
        }

        if (current != null) {
            refreshAllData(current.username)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun setScriptUrl(url: String) {
        repository.setScriptUrl(url)
        _uiState.update { it.copy(scriptUrl = repository.getScriptUrl(), infoMessage = "URL Script diperbarui") }
    }

    fun login(username: String, password: String, remember: Boolean) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username dan password tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.login(username.trim(), password.trim())
            if (result.isSuccess) {
                val user = result.getOrThrow()
                repository.saveCredentials(username.trim(), password.trim(), remember)
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = true,
                        isLoading = false,
                        infoMessage = "Selamat datang, ${user.displayName}!",
                        rememberMe = remember,
                        savedUsername = if (remember) username.trim() else "",
                        savedPassword = if (remember) password.trim() else ""
                    )
                }
                refreshAllData(user.username)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Login gagal"
                _uiState.update { it.copy(isLoading = false, errorMessage = err) }
            }
        }
    }

    fun logout() {
        repository.setCurrentUser(null)
        _uiState.update {
            it.copy(
                currentUser = null,
                isLoggedIn = false,
                dataKegiatan = emptyList(),
                draftEntries = emptyList(),
                hasUnsavedChanges = false,
                infoMessage = "Anda telah keluar."
            )
        }
    }

    fun refreshAllData(username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val profile = repository.getProfile(username)
            val schedules = repository.getSchedules(username)
            val logo = repository.loadLogo()

            // Initialize default month if not selected
            val currentMonthIso = DateUtils.getCurrentMonthIso()
            val currentMonthText = DateUtils.formatBulanText(currentMonthIso)
            val (entries, bulanList) = repository.loadData(username, currentMonthText)

            _uiState.update {
                it.copy(
                    userProfile = profile,
                    jadwalList = schedules,
                    logoBase64 = logo,
                    dataKegiatan = entries,
                    daftarBulan = (bulanList + currentMonthText).distinct().sorted(),
                    selectedBulan = currentMonthText,
                    isLoading = false,
                    hasUnsavedChanges = false
                )
            }
        }
    }

    // Profile
    fun saveProfile(profile: UserProfile) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.saveProfile(user.username, profile)
            _uiState.update {
                it.copy(
                    userProfile = profile,
                    isLoading = false,
                    infoMessage = if (res.isSuccess) res.getOrThrow() else "Profil disimpan lokal."
                )
            }
        }
    }

    // Data Archive
    fun selectMonth(bulanText: String) {
        val user = _uiState.value.currentUser ?: return
        _uiState.update { it.copy(selectedBulan = bulanText) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val (entries, bulanList) = repository.loadData(user.username, bulanText)
            _uiState.update {
                it.copy(
                    dataKegiatan = entries,
                    daftarBulan = (bulanList + bulanText).filter { b -> b.isNotBlank() }.distinct().sorted(),
                    isLoading = false,
                    hasUnsavedChanges = false
                )
            }
        }
    }

    fun deleteEntireMonth(bulanText: String) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.deleteEntireMonth(user.username, bulanText)
            val updatedBulanList = _uiState.value.daftarBulan.filter { it != bulanText }
            _uiState.update {
                it.copy(
                    daftarBulan = updatedBulanList,
                    selectedBulan = updatedBulanList.firstOrNull() ?: "",
                    dataKegiatan = emptyList(),
                    isLoading = false,
                    infoMessage = res.getOrDefault("Bulan berhasil dihapus.")
                )
            }
            if (updatedBulanList.isNotEmpty()) {
                selectMonth(updatedBulanList.first())
            }
        }
    }

    fun updateArchiveEntry(index: Int, newTasks: List<String>) {
        val current = _uiState.value.dataKegiatan.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            current[index] = item.copy(kegiatan = newTasks.joinToString("\n"))
            _uiState.update { it.copy(dataKegiatan = current, hasUnsavedChanges = true) }
        }
    }

    fun removeArchiveItem(rowIndex: Int, itemIndex: Int) {
        val current = _uiState.value.dataKegiatan.toMutableList()
        if (rowIndex in current.indices) {
            val item = current[rowIndex]
            val tasks = item.items.toMutableList()
            if (itemIndex in tasks.indices) {
                tasks.removeAt(itemIndex)
                if (tasks.isEmpty()) {
                    current.removeAt(rowIndex)
                } else {
                    current[rowIndex] = item.copy(kegiatan = tasks.joinToString("\n"))
                }
                _uiState.update { it.copy(dataKegiatan = current, hasUnsavedChanges = true) }
            }
        }
    }

    fun deleteArchiveRow(index: Int) {
        val current = _uiState.value.dataKegiatan.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.update { it.copy(dataKegiatan = current, hasUnsavedChanges = true) }
        }
    }

    fun saveAllMonthChangesToServer() {
        val user = _uiState.value.currentUser ?: return
        val currentMonth = _uiState.value.selectedBulan
        if (currentMonth.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pilih bulan terlebih dahulu.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.saveAllMonthData(user.username, currentMonth, _uiState.value.dataKegiatan)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasUnsavedChanges = false,
                    infoMessage = res.getOrDefault("Perubahan berhasil disimpan ke server!")
                )
            }
        }
    }

    // Draft / Bulk Input
    fun addToDraft(tanggal: String, tasks: List<String>) {
        if (tanggal.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pilih tanggal kegiatan terlebih dahulu.") }
            return
        }
        if (tasks.isEmpty() || tasks.all { it.isBlank() }) {
            _uiState.update { it.copy(errorMessage = "Silakan isi rincian pekerjaan.") }
            return
        }

        val cleanTasks = tasks.map { it.trim() }.filter { it.isNotBlank() }
        val currentManual = _uiState.value.manualDraftEntries.toMutableList()
        val manualIndex = currentManual.indexOfFirst { it.tanggal == tanggal }

        if (manualIndex != -1) {
            val existing = currentManual[manualIndex]
            val existingTasks = existing.items.map { it.trim() }.filter { it.isNotBlank() }
            val combined = (existingTasks + cleanTasks).distinct()
            currentManual[manualIndex] = existing.copy(kegiatan = combined.joinToString("\n"))
        } else {
            currentManual.add(
                KegiatanEntry(
                    no = currentManual.size + 1,
                    tanggal = tanggal,
                    kegiatan = cleanTasks.joinToString("\n")
                )
            )
        }
        currentManual.sortBy { it.tanggal }

        val currentDraft = _uiState.value.draftEntries.toMutableList()
        val existingIndex = currentDraft.indexOfFirst { it.tanggal == tanggal }

        if (existingIndex != -1) {
            val existing = currentDraft[existingIndex]
            val existingTasks = existing.items.map { it.trim() }.filter { it.isNotBlank() }
            val combined = (existingTasks + cleanTasks).distinct()
            currentDraft[existingIndex] = existing.copy(kegiatan = combined.joinToString("\n"))
        } else {
            currentDraft.add(
                KegiatanEntry(
                    no = currentDraft.size + 1,
                    tanggal = tanggal,
                    kegiatan = cleanTasks.joinToString("\n")
                )
            )
        }

        currentDraft.sortBy { it.tanggal }
        _uiState.update {
            it.copy(
                draftEntries = currentDraft,
                manualDraftEntries = currentManual,
                infoMessage = "Kegiatan manual disimpan di daftar draf."
            )
        }
    }

    fun generateBulkDraft(bulanIso: String, skipNationalHolidays: Boolean = true) { // e.g. "2025-05"
        val schedules = _uiState.value.jadwalList
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.generateBulk(bulanIso, schedules, skipNationalHolidays)
            if (res.isSuccess) {
                val newEntries = res.getOrThrow()
                val currentDraft = _uiState.value.draftEntries.toMutableList()
                val manualEntriesMap = _uiState.value.manualDraftEntries.associateBy { it.tanggal }

                for (newEntry in newEntries) {
                    val generatedTasks = newEntry.items.map { it.trim() }.filter { it.isNotBlank() }
                    val existingIdx = currentDraft.indexOfFirst { it.tanggal == newEntry.tanggal }
                    val manualForDate = manualEntriesMap[newEntry.tanggal]
                    val manualTasks = manualForDate?.items?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()

                    if (existingIdx != -1) {
                        val existing = currentDraft[existingIdx]
                        val existingTasks = existing.items.map { it.trim() }.filter { it.isNotBlank() }
                        // Keep manual tasks at the bottom
                        val combined = (generatedTasks + manualTasks + existingTasks).distinct()
                        currentDraft[existingIdx] = existing.copy(kegiatan = combined.joinToString("\n"))
                    } else {
                        val savedInKegiatan = _uiState.value.dataKegiatan.find { it.tanggal == newEntry.tanggal }
                        val savedTasks = savedInKegiatan?.items?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                        val combined = (generatedTasks + manualTasks + savedTasks).distinct()
                        currentDraft.add(
                            KegiatanEntry(
                                no = currentDraft.size + 1,
                                tanggal = newEntry.tanggal,
                                kegiatan = combined.joinToString("\n")
                            )
                        )
                    }
                }

                currentDraft.sortBy { it.tanggal }
                _uiState.update {
                    it.copy(
                        draftEntries = currentDraft,
                        isLoading = false,
                        infoMessage = "Berhasil generate ${newEntries.size} hari kerja untuk bulan ${DateUtils.formatBulanText(bulanIso)}. Kegiatan manual yang ada tetap dipertahankan."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = res.exceptionOrNull()?.message ?: "Gagal generate jadwal bulanan"
                    )
                }
            }
        }
    }

    fun removeHolidayEntriesFromDraft() {
        val currentDraft = _uiState.value.draftEntries.filter { !DateUtils.isHoliday(it.tanggal) }
        val removedCount = _uiState.value.draftEntries.size - currentDraft.size
        _uiState.update {
            it.copy(
                draftEntries = currentDraft,
                infoMessage = if (removedCount > 0) "$removedCount entri hari libur nasional dihapus dari draf." else "Tidak ada hari libur di dalam draf."
            )
        }
    }

    fun removeDraftItem(draftIndex: Int, itemIndex: Int) {
        val currentDraft = _uiState.value.draftEntries.toMutableList()
        val currentManual = _uiState.value.manualDraftEntries.toMutableList()
        if (draftIndex in currentDraft.indices) {
            val entry = currentDraft[draftIndex]
            val tasks = entry.items.toMutableList()
            if (itemIndex in tasks.indices) {
                tasks.removeAt(itemIndex)
                if (tasks.isEmpty()) {
                    currentDraft.removeAt(draftIndex)
                } else {
                    currentDraft[draftIndex] = entry.copy(kegiatan = tasks.joinToString("\n"))
                }

                // Also update manualDraftEntries if date matches
                val manualIdx = currentManual.indexOfFirst { it.tanggal == entry.tanggal }
                if (manualIdx != -1) {
                    val mEntry = currentManual[manualIdx]
                    val mTasks = mEntry.items.toMutableList()
                    if (itemIndex in mTasks.indices) {
                        mTasks.removeAt(itemIndex)
                        if (mTasks.isEmpty()) {
                            currentManual.removeAt(manualIdx)
                        } else {
                            currentManual[manualIdx] = mEntry.copy(kegiatan = mTasks.joinToString("\n"))
                        }
                    }
                }

                _uiState.update { it.copy(draftEntries = currentDraft, manualDraftEntries = currentManual) }
            }
        }
    }

    fun deleteDraftRow(draftIndex: Int) {
        val currentDraft = _uiState.value.draftEntries.toMutableList()
        val currentManual = _uiState.value.manualDraftEntries.toMutableList()
        if (draftIndex in currentDraft.indices) {
            val removed = currentDraft.removeAt(draftIndex)
            currentManual.removeAll { it.tanggal == removed.tanggal }
            _uiState.update { it.copy(draftEntries = currentDraft, manualDraftEntries = currentManual) }
        }
    }

    fun updateDraftRow(draftIndex: Int, newTasks: List<String>) {
        val currentDraft = _uiState.value.draftEntries.toMutableList()
        val currentManual = _uiState.value.manualDraftEntries.toMutableList()
        if (draftIndex in currentDraft.indices) {
            val entry = currentDraft[draftIndex]
            currentDraft[draftIndex] = entry.copy(kegiatan = newTasks.joinToString("\n"))
            val manualIdx = currentManual.indexOfFirst { it.tanggal == entry.tanggal }
            if (manualIdx != -1) {
                currentManual[manualIdx] = currentManual[manualIdx].copy(kegiatan = newTasks.joinToString("\n"))
            }
            _uiState.update { it.copy(draftEntries = currentDraft, manualDraftEntries = currentManual) }
        }
    }

    fun clearDraft() {
        val manualEntries = _uiState.value.manualDraftEntries
        _uiState.update {
            it.copy(
                draftEntries = manualEntries,
                infoMessage = if (manualEntries.isNotEmpty()) "Draf otomatis dibatalkan. Kegiatan manual yang di-input tetap dipertahankan." else "Draf dibatalkan."
            )
        }
    }

    fun saveDraftPermanently() {
        val user = _uiState.value.currentUser ?: return
        val draft = _uiState.value.draftEntries
        if (draft.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Tidak ada draf untuk disimpan.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val count = repository.saveBulkData(user.username, draft).getOrDefault(draft.size)
            val firstMonth = DateUtils.formatBulanText(draft.first().tanggal.take(7))
            _uiState.update {
                it.copy(
                    draftEntries = emptyList(),
                    manualDraftEntries = emptyList(),
                    isLoading = false,
                    infoMessage = "$count data kegiatan berhasil disimpan permanen ke Google Sheets!"
                )
            }
            selectMonth(firstMonth)
        }
    }

    // Schedule / Routine
    fun loadSchedules() {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val list = repository.getSchedules(user.username)
            _uiState.update { it.copy(jadwalList = list) }
        }
    }

    fun saveSchedule(hari: String, tasks: List<String>, editIndex: Int = -1) {
        val user = _uiState.value.currentUser ?: return
        val kegiatan = tasks.joinToString("\n")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val current = _uiState.value.jadwalList.toMutableList()
            if (editIndex in current.indices) {
                current[editIndex] = JadwalItem(hari = hari, kegiatan = kegiatan)
                repository.saveAllSchedules(user.username, current)
            } else {
                current.add(JadwalItem(hari = hari, kegiatan = kegiatan))
                repository.saveSchedule(user.username, hari, kegiatan)
            }
            _uiState.update {
                it.copy(
                    jadwalList = current,
                    isLoading = false,
                    infoMessage = "Jadwal rutin berhasil disimpan."
                )
            }
        }
    }

    fun deleteSchedule(index: Int) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val current = _uiState.value.jadwalList
            repository.deleteSchedule(user.username, index, current)
            val updated = current.toMutableList()
            if (index in updated.indices) updated.removeAt(index)
            _uiState.update {
                it.copy(
                    jadwalList = updated,
                    isLoading = false,
                    infoMessage = "Jadwal rutin dihapus."
                )
            }
        }
    }

    fun reorderSchedule(from: Int, to: Int) {
        val user = _uiState.value.currentUser ?: return
        val list = _uiState.value.jadwalList.toMutableList()
        if (from in list.indices && to in list.indices) {
            val item = list.removeAt(from)
            list.add(to, item)
            _uiState.update { it.copy(jadwalList = list) }
            viewModelScope.launch {
                repository.saveAllSchedules(user.username, list)
            }
        }
    }

    // Logo
    fun uploadLogo(base64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.uploadLogo(base64)
            _uiState.update {
                it.copy(
                    logoBase64 = base64,
                    isLoading = false,
                    infoMessage = "Logo instansi berhasil diperbarui."
                )
            }
        }
    }

    fun deleteLogo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.deleteLogo()
            _uiState.update {
                it.copy(
                    logoBase64 = null,
                    isLoading = false,
                    infoMessage = "Logo instansi direset ke default."
                )
            }
        }
    }

    // Admin
    fun loadAdminUsers() {
        if (_uiState.value.currentUser?.role != "admin") return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val users = repository.adminListUsers()
            _uiState.update { it.copy(adminUsers = users, isLoading = false) }
        }
    }

    fun adminAddUser(user: UserAccount) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.adminAddUser(user)
            val users = repository.adminListUsers()
            _uiState.update {
                it.copy(
                    adminUsers = users,
                    isLoading = false,
                    infoMessage = res.getOrDefault("User ditambahkan.")
                )
            }
        }
    }

    fun adminUpdateUser(originalUsername: String, user: UserAccount) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.adminUpdateUser(originalUsername, user)
            val users = repository.adminListUsers()
            _uiState.update {
                it.copy(
                    adminUsers = users,
                    isLoading = false,
                    infoMessage = res.getOrDefault("User diperbarui.")
                )
            }
        }
    }

    fun adminDeleteUser(username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.adminDeleteUser(username)
            val users = repository.adminListUsers()
            _uiState.update {
                it.copy(
                    adminUsers = users,
                    isLoading = false,
                    infoMessage = res.getOrDefault("User dihapus.")
                )
            }
        }
    }
}
