package com.example

import com.example.data.model.JadwalItem
import com.example.data.model.KegiatanEntry
import com.example.data.model.UserProfile
import com.example.util.DateUtils
import com.example.util.PrintDocumentHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testFormatTanggalIndo() {
        val formatted = DateUtils.formatTanggalIndo("2025-05-02")
        assertEquals("02-05-2025", formatted)
    }

    @Test
    fun testFormatBulanText() {
        val formatted = DateUtils.formatBulanText("2025-05")
        assertEquals("Mei 2025", formatted)
    }

    @Test
    fun testGenerateWorkingDays() {
        val days = DateUtils.generateWeekdaysForMonth("2025-05")
        assertTrue(days.isNotEmpty())
        assertEquals("2025-05-02", days.first().first)
        assertEquals("2025-05-30", days.last().first)
    }

    @Test
    fun testGenerateHtmlReport() {
        val profile = UserProfile(
            pegNama = "Irvan Suwandi, S.Pd",
            pegNIP = "198606132023211016",
            pegJabatan = "Guru",
            pegSatker = "MTsN 2 Garut",
            kepNama = "H. Asep Sodikin, S.Pd., M.M",
            kepNIP = "197305071997031002"
        )
        val data = listOf(
            KegiatanEntry(no = 1, tanggal = "2025-05-02", kegiatan = "KBM Kelas 7\nKBM Kelas 8")
        )

        val html = PrintDocumentHelper.generateHtml(
            type = "semua",
            profile = profile,
            dataKegiatan = data,
            selectedBulanFilter = "2025-05",
            logoBase64 = null
        )

        assertTrue(html.contains("LAPORAN KINERJA BULANAN"))
        assertTrue(html.contains("Irvan Suwandi, S.Pd"))
        assertTrue(html.contains("H. Asep Sodikin, S.Pd., M.M"))
        assertTrue(html.contains("KBM Kelas 7"))
    }
}
