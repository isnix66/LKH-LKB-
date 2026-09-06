package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.JadwalEntity
import com.example.data.local.KegiatanEntity
import com.example.data.local.ProfileEntity
import com.example.data.local.UserEntity
import com.example.data.model.JadwalItem
import com.example.data.model.KegiatanEntry
import com.example.data.model.UserAccount
import com.example.data.model.UserProfile
import com.example.data.remote.GasApiClient
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LkhRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val apiClient: GasApiClient
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lkh_lkb_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "LkhRepository"
        private const val PREF_SAVED_USERNAME = "saved_username"
        private const val PREF_SAVED_PASSWORD = "saved_password"
        private const val PREF_REMEMBER_ME = "remember_me"
        private const val PREF_CURRENT_USER = "current_user"
        private const val PREF_CURRENT_DISPLAY_NAME = "current_display_name"
        private const val PREF_CURRENT_ROLE = "current_role"
        private const val PREF_LOGO_BASE64 = "logo_base64"
        private const val PREF_SCRIPT_URL = "script_url"
    }

    init {
        val savedUrl = prefs.getString(PREF_SCRIPT_URL, null)
        if (!savedUrl.isNullOrBlank()) {
            apiClient.updateScriptUrl(savedUrl)
        }
    }

    fun setScriptUrl(url: String) {
        if (url.isNotBlank()) {
            prefs.edit().putString(PREF_SCRIPT_URL, url.trim()).apply()
            apiClient.updateScriptUrl(url)
        }
    }

    fun getScriptUrl(): String = apiClient.getScriptUrl()

    // Session Management
    fun getSavedCredentials(): Triple<String, String, Boolean> {
        val u = prefs.getString(PREF_SAVED_USERNAME, "") ?: ""
        val p = prefs.getString(PREF_SAVED_PASSWORD, "") ?: ""
        val r = prefs.getBoolean(PREF_REMEMBER_ME, false)
        return Triple(u, p, r)
    }

    fun saveCredentials(username: String, password: String, remember: Boolean) {
        prefs.edit().apply {
            if (remember) {
                putString(PREF_SAVED_USERNAME, username)
                putString(PREF_SAVED_PASSWORD, password)
                putBoolean(PREF_REMEMBER_ME, true)
            } else {
                remove(PREF_SAVED_USERNAME)
                remove(PREF_SAVED_PASSWORD)
                putBoolean(PREF_REMEMBER_ME, false)
            }
        }.apply()
    }

    fun getCurrentUser(): UserAccount? {
        val u = prefs.getString(PREF_CURRENT_USER, null) ?: return null
        val d = prefs.getString(PREF_CURRENT_DISPLAY_NAME, u) ?: u
        val r = prefs.getString(PREF_CURRENT_ROLE, "user") ?: "user"
        return UserAccount(username = u, displayName = d, role = r)
    }

    fun setCurrentUser(user: UserAccount?) {
        prefs.edit().apply {
            if (user == null) {
                remove(PREF_CURRENT_USER)
                remove(PREF_CURRENT_DISPLAY_NAME)
                remove(PREF_CURRENT_ROLE)
            } else {
                putString(PREF_CURRENT_USER, user.username)
                putString(PREF_CURRENT_DISPLAY_NAME, user.displayName)
                putString(PREF_CURRENT_ROLE, user.role)
            }
        }.apply()
    }

    fun getLocalLogo(): String? = prefs.getString(PREF_LOGO_BASE64, null)

    fun setLocalLogo(base64: String?) {
        prefs.edit().apply {
            if (base64 == null) remove(PREF_LOGO_BASE64)
            else putString(PREF_LOGO_BASE64, base64)
        }.apply()
    }

    // Authentication: try online GAS first; if offline or fails, fallback to Room local users
    suspend fun login(username: String, password: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val onlineResult = apiClient.login(username, password)
        if (onlineResult.isSuccess) {
            val (displayName, role) = onlineResult.getOrThrow()
            val user = UserAccount(username = username, displayName = displayName, role = role)
            database.userDao().insertOrUpdate(
                UserEntity(
                    username = username,
                    password = password,
                    displayName = displayName,
                    role = role
                )
            )
            setCurrentUser(user)
            return@withContext Result.success(user)
        }

        // Fallback to local DB
        val localUser = database.userDao().authenticate(username, password)
        if (localUser != null) {
            val user = UserAccount(
                username = localUser.username,
                displayName = localUser.displayName,
                role = localUser.role
            )
            setCurrentUser(user)
            return@withContext Result.success(user)
        }

        Result.failure(onlineResult.exceptionOrNull() ?: Exception("Username atau password salah"))
    }

    // Profile Management
    suspend fun getProfile(username: String): UserProfile = withContext(Dispatchers.IO) {
        val onlineResult = apiClient.getProfile()
        if (onlineResult.isSuccess) {
            val prof = onlineResult.getOrThrow()
            database.profileDao().saveProfile(
                ProfileEntity(
                    username = username,
                    pegNama = prof.pegNama,
                    pegNIP = prof.pegNIP,
                    pegJabatan = prof.pegJabatan,
                    pegPangkat = prof.pegPangkat,
                    pegGolongan = prof.pegGolongan,
                    pegSatker = prof.pegSatker,
                    kepNama = prof.kepNama,
                    kepNIP = prof.kepNIP
                )
            )
            return@withContext prof
        }

        val local = database.profileDao().getProfile(username)
        if (local != null) {
            return@withContext UserProfile(
                pegNama = local.pegNama,
                pegNIP = local.pegNIP,
                pegJabatan = local.pegJabatan,
                pegPangkat = local.pegPangkat,
                pegGolongan = local.pegGolongan,
                pegSatker = local.pegSatker,
                kepNama = local.kepNama,
                kepNIP = local.kepNIP
            )
        }

        UserProfile()
    }

    suspend fun saveProfile(username: String, profile: UserProfile): Result<String> = withContext(Dispatchers.IO) {
        database.profileDao().saveProfile(
            ProfileEntity(
                username = username,
                pegNama = profile.pegNama,
                pegNIP = profile.pegNIP,
                pegJabatan = profile.pegJabatan,
                pegPangkat = profile.pegPangkat,
                pegGolongan = profile.pegGolongan,
                pegSatker = profile.pegSatker,
                kepNama = profile.kepNama,
                kepNIP = profile.kepNIP
            )
        )

        val onlineResult = apiClient.saveProfileAndCreateSheet(profile)
        if (onlineResult.isSuccess) {
            return@withContext Result.success(onlineResult.getOrThrow())
        }

        Result.success("Profil tersimpan di perangkat lokal.")
    }

    // Kegiatan / Data Management
    suspend fun loadData(username: String, filterBulanText: String): Pair<List<KegiatanEntry>, List<String>> = withContext(Dispatchers.IO) {
        val onlineResult = apiClient.getAllData(filterBulanText)
        if (onlineResult.isSuccess) {
            val (onlineEntries, onlineBulanList) = onlineResult.getOrThrow()
            // Sync to local DB for this month/filter
            if (filterBulanText.isNotBlank()) {
                database.kegiatanDao().deleteByMonth(username, filterBulanText, filterBulanText)
                val entities = onlineEntries.map {
                    KegiatanEntity(
                        username = username,
                        tanggal = it.tanggal,
                        kegiatan = it.kegiatan,
                        bulanText = it.bulan,
                        sheetKey = it.sheetKey,
                        rowIndex = it.rowIndex
                    )
                }
                database.kegiatanDao().insertAll(entities)
            }
            return@withContext Pair(onlineEntries, onlineBulanList)
        }

        // Offline fallback from Room
        val localList = if (filterBulanText.isNotBlank()) {
            database.kegiatanDao().getByMonth(username, filterBulanText, filterBulanText)
        } else {
            emptyList()
        }

        val allMonthKeys = database.kegiatanDao().getAllMonthKeys(username)
        val formattedMonths = allMonthKeys.map { DateUtils.formatBulanText(it) }.distinct().sorted()

        val entries = localList.mapIndexed { index, e ->
            KegiatanEntry(
                no = index + 1,
                tanggal = e.tanggal,
                kegiatan = e.kegiatan,
                bulan = e.bulanText,
                sheetKey = e.sheetKey,
                rowIndex = e.rowIndex
            )
        }

        Pair(entries, formattedMonths)
    }

    suspend fun saveBulkData(username: String, entries: List<KegiatanEntry>): Result<Int> = withContext(Dispatchers.IO) {
        // Save local
        val entities = entries.map {
            KegiatanEntity(
                username = username,
                tanggal = it.tanggal,
                kegiatan = it.kegiatan,
                bulanText = DateUtils.formatBulanText(it.tanggal.take(7)),
                sheetKey = it.tanggal.take(7),
                rowIndex = -1
            )
        }
        database.kegiatanDao().insertAll(entities)

        // Sync online
        val onlineResult = apiClient.saveBulkData(entries)
        if (onlineResult.isSuccess) {
            return@withContext Result.success(onlineResult.getOrThrow())
        }

        Result.success(entries.size)
    }

    suspend fun saveAllMonthData(username: String, bulanText: String, dataList: List<KegiatanEntry>): Result<String> = withContext(Dispatchers.IO) {
        database.kegiatanDao().deleteByMonth(username, bulanText, bulanText)
        val entities = dataList.mapIndexed { idx, item ->
            KegiatanEntity(
                username = username,
                tanggal = item.tanggal,
                kegiatan = item.kegiatan,
                bulanText = bulanText,
                sheetKey = item.sheetKey.ifBlank { item.tanggal.take(7) },
                rowIndex = idx + 1
            )
        }
        database.kegiatanDao().insertAll(entities)

        val online = apiClient.saveAllMonthData(bulanText, dataList)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }

        Result.success("Perubahan disimpan secara lokal (Offline mode).")
    }

    suspend fun deleteEntireMonth(username: String, bulanText: String): Result<String> = withContext(Dispatchers.IO) {
        database.kegiatanDao().deleteByMonth(username, bulanText, bulanText)
        val online = apiClient.deleteEntireMonth(bulanText)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("Bulan berhasil dihapus dari data lokal.")
    }

    // Schedule / Routine Management
    suspend fun getSchedules(username: String): List<JadwalItem> = withContext(Dispatchers.IO) {
        val onlineResult = apiClient.getSchedule()
        if (onlineResult.isSuccess) {
            val list = onlineResult.getOrThrow()
            database.jadwalDao().deleteAllForUser(username)
            val entities = list.mapIndexed { i, j ->
                JadwalEntity(username = username, hari = j.hari, kegiatan = j.kegiatan, urutan = i)
            }
            database.jadwalDao().insertAll(entities)
            return@withContext list
        }

        val local = database.jadwalDao().getListByUser(username)
        local.map { JadwalItem(hari = it.hari, kegiatan = it.kegiatan) }
    }

    suspend fun saveSchedule(username: String, hari: String, kegiatan: String): Result<String> = withContext(Dispatchers.IO) {
        database.jadwalDao().insert(JadwalEntity(username = username, hari = hari, kegiatan = kegiatan))
        val online = apiClient.saveSchedule(hari, kegiatan)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("Jadwal tersimpan di lokal.")
    }

    suspend fun saveAllSchedules(username: String, jadwalList: List<JadwalItem>): Result<String> = withContext(Dispatchers.IO) {
        database.jadwalDao().deleteAllForUser(username)
        val entities = jadwalList.mapIndexed { index, j ->
            JadwalEntity(username = username, hari = j.hari, kegiatan = j.kegiatan, urutan = index)
        }
        database.jadwalDao().insertAll(entities)

        val online = apiClient.saveAllSchedules(jadwalList)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("Urutan jadwal disimpan di lokal.")
    }

    suspend fun deleteSchedule(username: String, index: Int, currentList: List<JadwalItem>): Result<String> = withContext(Dispatchers.IO) {
        if (index in currentList.indices) {
            val updated = currentList.toMutableList()
            updated.removeAt(index)
            saveAllSchedules(username, updated)
        }
        val online = apiClient.deleteSchedule(index)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("Jadwal dihapus.")
    }

    suspend fun generateBulk(
        bulan: String,
        userSchedule: List<JadwalItem>,
        skipNationalHolidays: Boolean = true
    ): Result<List<KegiatanEntry>> = withContext(Dispatchers.IO) {
        val online = apiClient.generateBulkData(bulan)
        if (online.isSuccess) {
            val list = online.getOrThrow()
            val filtered = if (skipNationalHolidays) {
                list.filter { !DateUtils.isHoliday(it.tanggal) }
            } else {
                list
            }
            return@withContext Result.success(filtered)
        }

        // Offline generation logic
        val days = DateUtils.generateWeekdaysForMonth(bulan, skipNationalHolidays)
        val scheduleMap = mutableMapOf<String, MutableList<String>>()
        userSchedule.forEach { item ->
            val list = scheduleMap.getOrPut(item.hari) { mutableListOf() }
            list.addAll(item.items)
        }

        val entries = days.mapIndexed { idx, (isoDate, dayName) ->
            val tasks = scheduleMap[dayName]
                ?: scheduleMap["Setiap Hari"]
                ?: scheduleMap["Setiap Hari Kerja"]
                ?: listOf("Melaksanakan tugas kedinasan lainnya")

            KegiatanEntry(
                no = idx + 1,
                tanggal = isoDate,
                kegiatan = tasks.joinToString("\n")
            )
        }

        Result.success(entries)
    }

    // Logo Management
    suspend fun loadLogo(): String? = withContext(Dispatchers.IO) {
        val online = apiClient.getLogo()
        if (online.isSuccess && !online.getOrNull().isNullOrBlank()) {
            val logo = online.getOrNull()
            setLocalLogo(logo)
            return@withContext logo
        }
        getLocalLogo()
    }

    suspend fun uploadLogo(base64: String): Result<String> = withContext(Dispatchers.IO) {
        setLocalLogo(base64)
        val online = apiClient.uploadLogo(base64, "logo_instansi.png", "image/png")
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("Logo disimpan secara lokal.")
    }

    suspend fun deleteLogo(): Result<String> = withContext(Dispatchers.IO) {
        setLocalLogo(null)
        val online = apiClient.deleteLogo()
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("Logo direset.")
    }

    // Admin User Management
    suspend fun adminListUsers(): List<UserAccount> = withContext(Dispatchers.IO) {
        val online = apiClient.adminListUsers()
        if (online.isSuccess) {
            val users = online.getOrThrow()
            users.forEach {
                database.userDao().insertOrUpdate(
                    UserEntity(
                        username = it.username,
                        password = it.password,
                        displayName = it.displayName,
                        role = it.role,
                        spreadsheetUrl = it.spreadsheetUrl
                    )
                )
            }
            return@withContext users
        }

        database.userDao().getAllUsers().map {
            UserAccount(
                username = it.username,
                password = it.password,
                displayName = it.displayName,
                role = it.role,
                spreadsheetUrl = it.spreadsheetUrl
            )
        }
    }

    suspend fun adminAddUser(user: UserAccount): Result<String> = withContext(Dispatchers.IO) {
        database.userDao().insertOrUpdate(
            UserEntity(
                username = user.username,
                password = user.password,
                displayName = user.displayName,
                role = user.role,
                spreadsheetUrl = user.spreadsheetUrl
            )
        )
        val online = apiClient.adminAddUser(user)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("User berhasil ditambahkan ke database lokal.")
    }

    suspend fun adminUpdateUser(originalUsername: String, user: UserAccount): Result<String> = withContext(Dispatchers.IO) {
        database.userDao().insertOrUpdate(
            UserEntity(
                username = user.username,
                password = user.password,
                displayName = user.displayName,
                role = user.role,
                spreadsheetUrl = user.spreadsheetUrl
            )
        )
        val online = apiClient.adminUpdateUser(originalUsername, user)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("User berhasil diperbarui.")
    }

    suspend fun adminDeleteUser(username: String): Result<String> = withContext(Dispatchers.IO) {
        database.userDao().deleteUser(username)
        val online = apiClient.adminDeleteUser(username)
        if (online.isSuccess) {
            return@withContext Result.success(online.getOrThrow())
        }
        Result.success("User dihapus.")
    }
}
