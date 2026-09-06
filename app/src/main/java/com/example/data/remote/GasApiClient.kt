package com.example.data.remote

import android.util.Log
import com.example.data.model.JadwalItem
import com.example.data.model.KegiatanEntry
import com.example.data.model.UserAccount
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GasApiClient(
    private var scriptUrl: String = DEFAULT_URL
) {

    companion object {
        const val DEFAULT_URL = "https://script.google.com/macros/s/AKfycbzsX_CTPXVgJaECn_DHtsu4vUYe_tFcNwFdAjDvqDfq9wM_z56fq54EV1rHRph4SQ4a0Q/exec"
        private const val TAG = "GasApiClient"
    }

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    fun updateScriptUrl(newUrl: String) {
        if (newUrl.isNotBlank()) {
            scriptUrl = newUrl.trim()
        }
    }

    fun getScriptUrl(): String = scriptUrl

    suspend fun login(username: String, password: String): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "loginAPI")
            urlBuilder.addQueryParameter("username", username)
            urlBuilder.addQueryParameter("password", password)

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val displayName = json.optString("displayName", username)
                val role = json.optString("role", "user")
                Result.success(Pair(displayName, role))
            } else {
                val msg = json.optString("message", "Login gagal")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "login error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllData(filterBulanText: String = ""): Result<Pair<List<KegiatanEntry>, List<String>>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "getAllData")
            if (filterBulanText.isNotBlank()) {
                urlBuilder.addQueryParameter("filterBulanText", filterBulanText)
            }

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val dataArray = json.optJSONArray("data") ?: JSONArray()
                val entries = mutableListOf<KegiatanEntry>()
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    entries.add(
                        KegiatanEntry(
                            no = item.optInt("no", i + 1),
                            tanggal = item.optString("tanggal"),
                            kegiatan = item.optString("kegiatan"),
                            bulan = item.optString("bulan"),
                            sheetKey = item.optString("sheetKey"),
                            rowIndex = item.optInt("rowIndex", i + 1)
                        )
                    )
                }

                val bulanArray = json.optJSONArray("bulan") ?: JSONArray()
                val bulanList = mutableListOf<String>()
                for (i in 0 until bulanArray.length()) {
                    bulanList.add(bulanArray.getString(i))
                }

                Result.success(Pair(entries, bulanList))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal memuat data")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAllData error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveBulkData(entries: List<KegiatanEntry>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            entries.forEach {
                val obj = JSONObject()
                obj.put("tanggal", it.tanggal)
                obj.put("kegiatan", it.kegiatan)
                jsonArray.put(obj)
            }

            val formBody = FormBody.Builder()
                .add("action", "saveBulkData")
                .add("entries", jsonArray.toString())
                .build()

            val request = Request.Builder().url(scriptUrl).post(formBody).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optInt("jumlah", entries.size))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menyimpan data bulk")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveBulkData error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun generateBulkData(bulan: String): Result<List<KegiatanEntry>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "generateBulkData")
            urlBuilder.addQueryParameter("bulan", bulan)

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val arr = json.optJSONArray("entries") ?: JSONArray()
                val list = mutableListOf<KegiatanEntry>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    var cleanKegiatan = obj.optString("kegiatan")
                    cleanKegiatan = cleanKegiatan.lines().joinToString("\n") { line ->
                        line.replace(Regex("\\s*[-(\\[]?\\s*otomatis\\s*[)\\]]?$", RegexOption.IGNORE_CASE), "").trim()
                    }
                    list.add(
                        KegiatanEntry(
                            no = i + 1,
                            tanggal = obj.optString("tanggal"),
                            kegiatan = cleanKegiatan
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("message", "Gagal generate bulk")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "generateBulkData error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveAllMonthData(bulanText: String, dataList: List<KegiatanEntry>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            dataList.forEach {
                val obj = JSONObject()
                obj.put("tanggal", it.tanggal)
                obj.put("kegiatan", it.kegiatan)
                jsonArray.put(obj)
            }

            val formBody = FormBody.Builder()
                .add("action", "saveAllMonthData")
                .add("bulanText", bulanText)
                .add("dataList", jsonArray.toString())
                .build()

            val request = Request.Builder().url(scriptUrl).post(formBody).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Perubahan berhasil disimpan ke server"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menyimpan perubahan ke server")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveAllMonthData error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteEntireMonth(bulanText: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "deleteEntireMonth")
            urlBuilder.addQueryParameter("bulanText", bulanText)

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Bulan berhasil dihapus"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menghapus bulan")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteEntireMonth error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getSchedule(): Result<List<JadwalItem>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "getSchedule")

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val arr = json.optJSONArray("jadwal") ?: JSONArray()
                val list = mutableListOf<JadwalItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        JadwalItem(
                            hari = obj.optString("hari"),
                            kegiatan = obj.optString("kegiatan")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("message", "Gagal memuat jadwal")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getSchedule error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveSchedule(hari: String, kegiatan: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "saveSchedule")
            urlBuilder.addQueryParameter("hari", hari)
            urlBuilder.addQueryParameter("kegiatan", kegiatan)

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Jadwal tersimpan"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menyimpan jadwal")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveSchedule error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveAllSchedules(jadwalList: List<JadwalItem>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            jadwalList.forEach {
                val obj = JSONObject()
                obj.put("hari", it.hari)
                obj.put("kegiatan", it.kegiatan)
                jsonArray.put(obj)
            }

            val formBody = FormBody.Builder()
                .add("action", "saveAllSchedules")
                .add("jadwalList", jsonArray.toString())
                .build()

            val request = Request.Builder().url(scriptUrl).post(formBody).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Jadwal berhasil disimpan"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menyimpan jadwal")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveAllSchedules error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteSchedule(index: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "deleteSchedule")
            urlBuilder.addQueryParameter("index", index.toString())

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Jadwal dihapus"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menghapus jadwal")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteSchedule error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "getProfile")

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val data = json.optJSONObject("data") ?: JSONObject()
                val profile = UserProfile(
                    pegNama = data.optString("pegNama", "Irvan Suwandi, S.Pd"),
                    pegNIP = data.optString("pegNIP", "198606132023211016"),
                    pegJabatan = data.optString("pegJabatan", "Guru"),
                    pegPangkat = data.optString("pegPangkat", "Penata Muda"),
                    pegGolongan = data.optString("pegGolongan", "III/a"),
                    pegSatker = data.optString("pegSatker", "MADRASAH TSANAWIYAH NEGERI 2 GARUT"),
                    kepNama = data.optString("kepNama", "H. Asep Sodikin, S.Pd., M.M"),
                    kepNIP = data.optString("kepNIP", "197305071997031002")
                )
                Result.success(profile)
            } else {
                Result.failure(Exception(json.optString("message", "Gagal memuat profil")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getProfile error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveProfileAndCreateSheet(profile: UserProfile): Result<String> = withContext(Dispatchers.IO) {
        try {
            val obj = JSONObject().apply {
                put("pegNama", profile.pegNama)
                put("pegNIP", profile.pegNIP)
                put("pegJabatan", profile.pegJabatan)
                put("pegPangkat", profile.pegPangkat)
                put("pegGolongan", profile.pegGolongan)
                put("pegSatker", profile.pegSatker)
                put("kepNama", profile.kepNama)
                put("kepNIP", profile.kepNIP)
            }

            val formBody = FormBody.Builder()
                .add("action", "saveProfileAndCreateSheet")
                .add("data", obj.toString())
                .build()

            val request = Request.Builder().url(scriptUrl).post(formBody).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Profil tersimpan dan sheet dibuat."))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menyimpan profil")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveProfile error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getLogo(): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "getLogo")

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val logo = if (json.isNull("logo")) null else json.optString("logo")
                Result.success(logo)
            } else {
                Result.failure(Exception(json.optString("message", "Gagal memuat logo")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLogo error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadLogo(base64Data: String, fileName: String, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("action", "uploadLogo")
                .add("base64Data", base64Data)
                .add("fileName", fileName)
                .add("mimeType", mimeType)
                .build()

            val request = Request.Builder().url(scriptUrl).post(formBody).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Logo berhasil diupload"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal upload logo")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadLogo error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteLogo(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "deleteLogo")

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "Logo dihapus"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menghapus logo")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteLogo error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Admin endpoints
    suspend fun adminListUsers(): Result<List<UserAccount>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "adminListUsers")

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                val arr = json.optJSONArray("users") ?: JSONArray()
                val list = mutableListOf<UserAccount>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        UserAccount(
                            username = obj.optString("username"),
                            displayName = obj.optString("displayName"),
                            role = obj.optString("role", "user"),
                            spreadsheetUrl = obj.optString("spreadsheetUrl"),
                            spreadsheetId = obj.optString("spreadsheetId")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("message", "Gagal memuat daftar user")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "adminListUsers error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun adminAddUser(user: UserAccount): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "adminAddUser")
            urlBuilder.addQueryParameter("username", user.username)
            urlBuilder.addQueryParameter("password", user.password)
            urlBuilder.addQueryParameter("displayName", user.displayName)
            urlBuilder.addQueryParameter("role", user.role)

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "User ditambahkan"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menambah user")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "adminAddUser error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun adminUpdateUser(originalUsername: String, user: UserAccount): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "adminUpdateUser")
            urlBuilder.addQueryParameter("originalUsername", originalUsername)
            urlBuilder.addQueryParameter("username", user.username)
            urlBuilder.addQueryParameter("password", user.password)
            urlBuilder.addQueryParameter("displayName", user.displayName)
            urlBuilder.addQueryParameter("role", user.role)

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "User berhasil diperbarui"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal mengupdate user")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "adminUpdateUser error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun adminDeleteUser(username: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = scriptUrl.toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(Exception("URL tidak valid"))
            urlBuilder.addQueryParameter("action", "adminDeleteUser")
            urlBuilder.addQueryParameter("username", username)

            val request = Request.Builder().url(urlBuilder.build()).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val json = JSONObject(body)
            if (json.optString("status") == "success") {
                Result.success(json.optString("message", "User berhasil dihapus"))
            } else {
                Result.failure(Exception(json.optString("message", "Gagal menghapus user")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "adminDeleteUser error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
