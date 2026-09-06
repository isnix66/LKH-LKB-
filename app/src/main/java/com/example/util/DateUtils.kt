package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    val INDONESIA_LOCALE = Locale("id", "ID")

    private val NAMA_BULAN = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private val NAMA_HARI = arrayOf(
        "Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"
    )

    // Database Hari Libur Nasional Resmi Indonesia (2024 - 2027 & Tahunan)
    private val HARI_LIBUR_MAP = mapOf(
        // 2024
        "2024-01-01" to "Tahun Baru 2024 Masehi",
        "2024-02-08" to "Isra Mi'raj Nabi Muhammad SAW",
        "2024-02-10" to "Tahun Baru Imlek 2575 Kongzili",
        "2024-03-11" to "Hari Suci Nyepi Saka 1946",
        "2024-03-29" to "Wafat Yesus Kristus",
        "2024-03-31" to "Hari Paskah",
        "2024-04-10" to "Hari Raya Idul Fitri 1445 H (Hari 1)",
        "2024-04-11" to "Hari Raya Idul Fitri 1445 H (Hari 2)",
        "2024-05-01" to "Hari Buruh Internasional",
        "2024-05-09" to "Kenaikan Yesus Kristus",
        "2024-05-23" to "Hari Raya Waisak 2568 BE",
        "2024-06-01" to "Hari Lahir Pancasila",
        "2024-06-17" to "Hari Raya Idul Adha 1445 H",
        "2024-07-07" to "Tahun Baru Islam 1446 H",
        "2024-08-17" to "Hari Kemerdekaan Republik Indonesia Ke-79",
        "2024-09-16" to "Maulid Nabi Muhammad SAW",
        "2024-12-25" to "Hari Raya Natal",

        // 2025
        "2025-01-01" to "Tahun Baru 2025 Masehi",
        "2025-01-27" to "Isra Mi'raj Nabi Muhammad SAW",
        "2025-01-29" to "Tahun Baru Imlek 2576 Kongzili",
        "2025-03-29" to "Hari Suci Nyepi Saka 1947",
        "2025-03-31" to "Hari Raya Idul Fitri 1446 H (Hari 1)",
        "2025-04-01" to "Hari Raya Idul Fitri 1446 H (Hari 2)",
        "2025-04-18" to "Wafat Yesus Kristus",
        "2025-04-20" to "Hari Paskah",
        "2025-05-01" to "Hari Buruh Internasional",
        "2025-05-12" to "Hari Raya Waisak 2569 BE",
        "2025-05-29" to "Kenaikan Yesus Kristus",
        "2025-06-01" to "Hari Lahir Pancasila",
        "2025-06-06" to "Hari Raya Idul Adha 1446 H",
        "2025-06-27" to "Tahun Baru Islam 1447 H",
        "2025-08-17" to "Hari Kemerdekaan Republik Indonesia Ke-80",
        "2025-09-05" to "Maulid Nabi Muhammad SAW",
        "2025-12-25" to "Hari Raya Natal",

        // 2026
        "2026-01-01" to "Tahun Baru 2026 Masehi",
        "2026-01-16" to "Isra Mi'raj Nabi Muhammad SAW",
        "2026-02-17" to "Tahun Baru Imlek 2577 Kongzili",
        "2026-03-19" to "Hari Suci Nyepi Saka 1948",
        "2026-03-20" to "Hari Raya Idul Fitri 1447 H (Hari 1)",
        "2026-03-21" to "Hari Raya Idul Fitri 1447 H (Hari 2)",
        "2026-04-03" to "Wafat Yesus Kristus",
        "2026-05-01" to "Hari Buruh Internasional",
        "2026-05-14" to "Kenaikan Yesus Kristus",
        "2026-05-27" to "Hari Raya Idul Adha 1447 H",
        "2026-05-31" to "Hari Raya Waisak 2570 BE",
        "2026-06-01" to "Hari Lahir Pancasila",
        "2026-06-16" to "Tahun Baru Islam 1448 H",
        "2026-08-17" to "Hari Kemerdekaan Republik Indonesia Ke-81",
        "2026-08-25" to "Maulid Nabi Muhammad SAW",
        "2026-12-25" to "Hari Raya Natal",

        // 2027
        "2027-01-01" to "Tahun Baru 2027 Masehi",
        "2027-01-05" to "Isra Mi'raj Nabi Muhammad SAW",
        "2027-02-06" to "Tahun Baru Imlek 2578 Kongzili",
        "2027-03-08" to "Hari Suci Nyepi Saka 1949",
        "2027-03-10" to "Hari Raya Idul Fitri 1448 H (Hari 1)",
        "2027-03-11" to "Hari Raya Idul Fitri 1448 H (Hari 2)",
        "2027-03-26" to "Wafat Yesus Kristus",
        "2027-05-01" to "Hari Buruh Internasional",
        "2027-05-06" to "Kenaikan Yesus Kristus",
        "2027-05-17" to "Hari Raya Idul Adha 1448 H",
        "2027-05-20" to "Hari Raya Waisak 2571 BE",
        "2027-06-01" to "Hari Lahir Pancasila",
        "2027-06-06" to "Tahun Baru Islam 1449 H",
        "2027-08-15" to "Maulid Nabi Muhammad SAW",
        "2027-08-17" to "Hari Kemerdekaan Republik Indonesia Ke-82",
        "2027-12-25" to "Hari Raya Natal"
    )

    private val RECURRING_ANNUAL_HOLIDAYS = mapOf(
        "01-01" to "Tahun Baru Masehi",
        "05-01" to "Hari Buruh Internasional",
        "06-01" to "Hari Lahir Pancasila",
        "08-17" to "Hari Kemerdekaan RI",
        "12-25" to "Hari Raya Natal"
    )

    fun getHolidayName(isoDate: String): String? {
        if (isoDate.isBlank()) return null
        HARI_LIBUR_MAP[isoDate]?.let { return it }
        val suffix = if (isoDate.length >= 10) isoDate.substring(5) else ""
        return RECURRING_ANNUAL_HOLIDAYS[suffix]
    }

    fun isHoliday(isoDate: String): Boolean {
        return getHolidayName(isoDate) != null
    }

    fun getTodayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getCurrentMonthIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(Date())
    }

    fun formatTanggalIndo(isoDate: String): String {
        if (isoDate.isBlank()) return "-"
        val parts = isoDate.split("-")
        return if (parts.size == 3) {
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            isoDate
        }
    }

    fun formatBulanText(rawBulan: String): String {
        if (rawBulan.isBlank()) return ""
        val str = rawBulan.trim()
        val regex = Regex("^\\d{4}-\\d{2}$")
        if (!regex.matches(str)) return str

        val parts = str.split("-")
        val tahun = parts[0]
        val bulanIndex = (parts[1].toIntOrNull() ?: 1) - 1
        return if (bulanIndex in NAMA_BULAN.indices) {
            "${NAMA_BULAN[bulanIndex]} $tahun"
        } else {
            str
        }
    }

    fun getDayNameFromIso(isoDate: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(isoDate) ?: return ""
            val cal = Calendar.getInstance()
            cal.time = date
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            if (dayOfWeek in NAMA_HARI.indices) NAMA_HARI[dayOfWeek] else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getWorkingEndMonthDate(year: Int, monthIndex: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, maxDay)

        while (true) {
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dStr = String.format(
                Locale.US, "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val isWeekend = dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY
            if (!isWeekend && !isHoliday(dStr)) {
                break
            }
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        val day = cal.get(Calendar.DAY_OF_MONTH)
        val mIdx = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        val mName = if (mIdx in NAMA_BULAN.indices) NAMA_BULAN[mIdx] else ""
        return "$day $mName $y"
    }

    fun generateWeekdaysForMonth(
        yearMonth: String,
        skipNationalHolidays: Boolean = true
    ): List<Pair<String, String>> {
        val parts = yearMonth.split("-")
        if (parts.size != 2) return emptyList()
        val year = parts[0].toIntOrNull() ?: return emptyList()
        val month = parts[1].toIntOrNull() ?: return emptyList()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val result = mutableListOf<Pair<String, String>>()

        for (d in 1..maxDay) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val isoDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) continue
            if (skipNationalHolidays && isHoliday(isoDate)) continue

            val dayIdx = dayOfWeek - 1
            val dayName = if (dayIdx in NAMA_HARI.indices) NAMA_HARI[dayIdx] else ""
            result.add(Pair(isoDate, dayName))
        }
        return result
    }
}
