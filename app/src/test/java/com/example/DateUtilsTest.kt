package com.example

import com.example.data.model.JadwalItem
import com.example.data.model.KegiatanEntry
import com.example.data.model.UserProfile
import com.example.util.DateUtils
import com.example.util.PrintDocumentHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    @Test
    fun testLkbGroupingPerJenjangAndJpCalculation() {
        val data = listOf(
            KegiatanEntry(no = 1, tanggal = "2025-05-02", kegiatan = "KBM Kelas 8A\nKBM Kelas 8B\nUpacara Bendera"),
            KegiatanEntry(no = 2, tanggal = "2025-05-03", kegiatan = "KBM Kelas 8C\nKBM Kelas 8D\nKBM Kelas 8A"), // 8A repeated
            KegiatanEntry(no = 3, tanggal = "2025-05-04", kegiatan = "KBM Kelas 9A\nKBM Kelas 9B\nKBM Kelas 9C"),
            KegiatanEntry(no = 4, tanggal = "2025-05-05", kegiatan = "Upacara Bendera")
        )

        val items = PrintDocumentHelper.aggregateLkbItems(data)

        // Find KBM Kelas 8
        val kbm8 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 8" }
        // Frequency: 5 meetings * 2 JP = 10 JP
        assertEquals("Kegiatan Belajar Mengajar Kelas 8", kbm8?.kegiatan)
        assertEquals("10 JP", kbm8?.jumlah)
        assertEquals("Kegiatan", kbm8?.satuan)

        // Find KBM Kelas 9
        val kbm9 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 9" }
        // Frequency: 3 meetings * 2 JP = 6 JP
        assertEquals("Kegiatan Belajar Mengajar Kelas 9", kbm9?.kegiatan)
        assertEquals("6 JP", kbm9?.jumlah)
        assertEquals("Kegiatan", kbm9?.satuan)

        // Upacara Bendera appears 2 times
        val upacara = items.find { it.kegiatan == "Upacara Bendera" }
        assertEquals("2", upacara?.jumlah)
    }

    @Test
    fun testLkb28Pertemuan56Jp() {
        val entries = (1..28).map { i ->
            val day = if (i <= 9) "0$i" else "$i"
            KegiatanEntry(no = i, tanggal = "2025-05-$day", kegiatan = "KBM Kelas 8")
        }
        val items = PrintDocumentHelper.aggregateLkbItems(entries)
        val kbm8 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 8" }
        assertEquals("Kegiatan Belajar Mengajar Kelas 8", kbm8?.kegiatan)
        assertEquals("56 JP", kbm8?.jumlah)
        assertEquals("Kegiatan", kbm8?.satuan)
    }

    @Test
    fun testLkbAccumulatedPerJenjangNotPerClass() {
        // Teacher teaches 8A to 8F and 9A to 9D throughout the month
        val data = listOf(
            KegiatanEntry(no = 1, tanggal = "2025-05-02", kegiatan = "KBM Kelas 8A\nKBM Kelas 8B"),
            KegiatanEntry(no = 2, tanggal = "2025-05-03", kegiatan = "KBM Kelas 8C\nKBM Kelas 8D"),
            KegiatanEntry(no = 3, tanggal = "2025-05-04", kegiatan = "KBM Kelas 8E\nKBM Kelas 8F"),
            KegiatanEntry(no = 4, tanggal = "2025-05-05", kegiatan = "KBM Kelas 9A\nKBM Kelas 9B"),
            KegiatanEntry(no = 5, tanggal = "2025-05-06", kegiatan = "KBM Kelas 9C\nKBM Kelas 9D"),
            KegiatanEntry(no = 6, tanggal = "2025-05-07", kegiatan = "Upacara Bendera")
        )
        val items = PrintDocumentHelper.aggregateLkbItems(data)

        // Verify there are NO per-class rows like "8A" or "8B"
        val perClassRows = items.filter { it.kegiatan.contains("8A") || it.kegiatan.contains("8B") || it.kegiatan.contains("9A") }
        assertTrue(perClassRows.isEmpty())

        // Verify exactly one row for Kelas 8: 6 meetings * 2 = 12 JP
        val kbm8 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 8" }
        assertNotNull(kbm8)
        assertEquals("12 JP", kbm8?.jumlah)
        assertEquals("Kegiatan", kbm8?.satuan)

        // Verify exactly one row for Kelas 9: 4 meetings * 2 = 8 JP
        val kbm9 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 9" }
        assertNotNull(kbm9)
        assertEquals("8 JP", kbm9?.jumlah)
        assertEquals("Kegiatan", kbm9?.satuan)

        // Total rows in LKB: Kelas 8, Kelas 9, Upacara Bendera = 3 rows
        assertEquals(3, items.size)
    }

    @Test
    fun testLkbIndexHtmlBehavior() {
        val data = listOf(
            KegiatanEntry(no = 1, tanggal = "2025-05-02", kegiatan = "Sholat Dhuha\nKBM Kelas 9A\nSholat Dzuhur"),
            KegiatanEntry(no = 2, tanggal = "2025-05-03", kegiatan = "Upacara Hari Senin\nKBM Kelas 8A\nRapat Dewan Guru"),
            KegiatanEntry(no = 3, tanggal = "2025-05-04", kegiatan = "KBM Kelas 7A")
        )
        val items = PrintDocumentHelper.aggregateLkbItems(data)

        // Grades must be first, sorted ascending: Kelas 7, Kelas 8, Kelas 9
        assertEquals("Kegiatan Belajar Mengajar Kelas 7", items[0].kegiatan)
        assertEquals("2 JP", items[0].jumlah)
        assertEquals("Kegiatan", items[0].satuan)

        assertEquals("Kegiatan Belajar Mengajar Kelas 8", items[1].kegiatan)
        assertEquals("2 JP", items[1].jumlah)
        assertEquals("Kegiatan", items[1].satuan)

        assertEquals("Kegiatan Belajar Mengajar Kelas 9", items[2].kegiatan)
        assertEquals("2 JP", items[2].jumlah)
        assertEquals("Kegiatan", items[2].satuan)

        // Non-teaching items follow in order of appearance
        assertEquals("Sholat Dhuha", items[3].kegiatan)
        assertEquals("1", items[3].jumlah)

        assertEquals("Sholat Dzuhur Berjamaah", items[4].kegiatan)
        assertEquals("1", items[4].jumlah)

        assertEquals("Upacara Bendera", items[5].kegiatan)
        assertEquals("1", items[5].jumlah)

        assertEquals("Rapat Koordinasi", items[6].kegiatan)
        assertEquals("1", items[6].jumlah)
    }

    @Test
    fun testOnlyJenjang789Supported() {
        val data = listOf(
            KegiatanEntry(no = 1, tanggal = "2025-05-02", kegiatan = "KBM Kelas 7A\nKBM Kelas 8B\nKBM Kelas 9C\nKBM Kelas 6A\nKBM Kelas 10A")
        )
        val items = PrintDocumentHelper.aggregateLkbItems(data)
        
        // Only 7, 8, 9 should appear as "Kegiatan Belajar Mengajar Kelas X"
        val kbm7 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 7" }
        val kbm8 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 8" }
        val kbm9 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 9" }
        assertNotNull(kbm7)
        assertNotNull(kbm8)
        assertNotNull(kbm9)

        val kbm6 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 6" }
        val kbm10 = items.find { it.kegiatan == "Kegiatan Belajar Mengajar Kelas 10" }
        assertTrue(kbm6 == null)
        assertTrue(kbm10 == null)
    }
}
