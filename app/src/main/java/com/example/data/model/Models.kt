package com.example.data.model

data class UserProfile(
    val pegNama: String = "Irvan Suwandi, S.Pd",
    val pegNIP: String = "198606132023211016",
    val pegJabatan: String = "Guru",
    val pegPangkat: String = "Penata Muda",
    val pegGolongan: String = "III/a",
    val pegSatker: String = "MADRASAH TSANAWIYAH NEGERI 2 GARUT",
    val kepNama: String = "H. Asep Sodikin, S.Pd., M.M",
    val kepNIP: String = "197305071997031002"
)

data class KegiatanEntry(
    val no: Int = 1,
    val tanggal: String = "", // Format: YYYY-MM-DD
    val kegiatan: String = "", // multi-line
    val bulan: String = "",
    val sheetKey: String = "",
    val rowIndex: Int = -1
) {
    val items: List<String>
        get() = parseKegiatanList(kegiatan)

    companion object {
        fun parseKegiatanList(raw: String): List<String> {
            if (raw.isBlank()) return emptyList()
            var lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.size == 1 && lines[0].contains(",")) {
                val parts = lines[0].split(",").map { it.trim() }.filter { it.isNotBlank() }
                if (parts.size > 1) lines = parts
            }
            return lines.map { line ->
                line.replace(Regex("^\\d+[\\.\\)]\\s*"), "").trim()
            }
        }
    }
}

data class JadwalItem(
    val hari: String = "Senin",
    val kegiatan: String = ""
) {
    val items: List<String>
        get() = KegiatanEntry.parseKegiatanList(kegiatan)
}

data class UserAccount(
    val username: String = "",
    val password: String = "",
    val displayName: String = "",
    val role: String = "user",
    val spreadsheetUrl: String = "",
    val spreadsheetId: String = "",
    val expireDate: String = "" // Added expiration date (e.g. YYYY-MM-DD or empty for unlimited)
)

data class MonthRekap(
    val kegiatan: String,
    val volume: String,
    val satuan: String
)
