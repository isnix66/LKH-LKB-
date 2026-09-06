package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.KegiatanEntry
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PrintDocumentHelper {

    private const val DEFAULT_LOGO = "https://upload.wikimedia.org/wikipedia/commons/4/44/Kementerian_Agama_new_logo.png"

    fun getEffectiveLogoBitmap(context: Context, logoBase64: String?): Bitmap? {
        if (!logoBase64.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(logoBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) return bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_kemenag_badge)
        } catch (e: Exception) {
            null
        }
    }

    fun generateHtml(
        type: String, // "sampul", "lkb", "lkh", "semua"
        profile: UserProfile,
        dataKegiatan: List<KegiatanEntry>,
        selectedBulanFilter: String,
        logoBase64: String?
    ): String {
        val (targetYear, targetMonthIndex) = DateUtils.resolveYearAndMonth(
            selectedBulanFilter,
            dataKegiatan.map { it.tanggal }
        )
        val strBulanTahun = DateUtils.formatBulanText(
            String.format(Locale.US, "%04d-%02d", targetYear, targetMonthIndex + 1)
        ).uppercase(Locale.getDefault())
        val tanggalCetak = DateUtils.getWorkingEndMonthDate(targetYear, targetMonthIndex)
        val currentLogoSrc = if (!logoBase64.isNullOrBlank()) {
            "data:image/png;base64,$logoBase64"
        } else {
            DEFAULT_LOGO
        }

        val kopSurat = """
            <table style="width:100%; border-bottom:4px solid #000; margin-bottom:20px; font-family:'Times New Roman', Times, serif;">
                <tr>
                    <td width="15%" align="center"><img src="$currentLogoSrc" style="width:90px; height:auto;"></td>
                    <td align="center">
                        <div style="font-size:16pt; font-weight:bold;">KEMENTERIAN AGAMA REPUBLIK INDONESIA</div>
                        <div style="font-size:14pt; font-weight:bold;">KANTOR KEMENTERIAN AGAMA KABUPATEN GARUT</div>
                        <div style="font-size: min(14pt, 2.8vw); font-weight:bold; white-space: nowrap; overflow: hidden;">${profile.pegSatker.uppercase(Locale.getDefault())}</div>
                        <div style="font-size:11pt;">Jalan Raya Wanakerta No.28 Cibatu-Garut 44185</div>
                        <div style="font-size:11pt;">Telepon (0262) 2860000 Email: mtsn1cibatu@yahoo.co.id</div>
                    </td>
                </tr>
            </table>
        """.trimIndent()

        val ttdBlock = """
            <table style="width:100%; margin-top:40px; page-break-inside: avoid; font-family:'Times New Roman', Times, serif; font-size:12pt;">
                <tr>
                    <td width="50%" align="center">Mengetahui<br>Kepala MTsN 2 Garut<br><br><br><br><br><b>${profile.kepNama}</b><br>NIP. ${profile.kepNIP}</td>
                    <td width="50%" align="center">Garut, $tanggalCetak<br>Penyusun<br><br><br><br><br><b>${profile.pegNama}</b><br>NIP. ${profile.pegNIP}</td>
                </tr>
            </table>
        """.trimIndent()

        val kontenHtml = StringBuilder()

        // 1. Sampul
        if (type == "sampul" || type == "semua") {
            kontenHtml.append("""
                <div style="text-align:center; font-family:'Times New Roman', Times, serif; padding: 60px 30px;">
                    <img src="$currentLogoSrc" style="height:120px; margin-bottom:20px;">
                    <h1 style="font-size:20pt; letter-spacing:1px; margin-bottom:10px;">LAPORAN KINERJA BULANAN</h1>
                    <h2 style="font-size:16pt; margin-bottom:25px;">APARATUR SIPIL NEGARA KEMENTERIAN AGAMA<br>BULAN $strBulanTahun</h2>
                    <table style="margin:0 auto; font-size:13pt; text-align:left; margin-bottom:120px;">
                        <tr><td width="160">NAMA</td><td>: <b>${profile.pegNama}</b></td></tr>
                        <tr><td>NIP</td><td>: ${profile.pegNIP}</td></tr>
                        <tr><td>JABATAN</td><td>: ${profile.pegJabatan.uppercase(Locale.getDefault())}</td></tr>
                        <tr><td>SATUAN KERJA</td><td style="white-space: nowrap; font-size: min(13pt, 2.6vw); overflow: hidden;">: ${profile.pegSatker.uppercase(Locale.getDefault())}</td></tr>
                    </table>
                    <h3 style="font-size:15pt; margin:0;">KEMENTERIAN AGAMA</h3>
                    <h3 style="font-size: min(15pt, 2.8vw); margin:0; white-space: nowrap; overflow: hidden;">${profile.pegSatker.uppercase(Locale.getDefault())}</h3>
                    <p style="font-size:11pt;">JL. Raya Wanakerta No. 28 Cibatu - Garut</p>
                </div>
            """)
            if (type == "semua") {
                kontenHtml.append("<div style=\"page-break-after: always;\"></div>")
            }
        }

        // 2. LKH (Laporan Kerja Harian)
        if (type == "lkh" || type == "semua") {
            val lkhRows = StringBuilder()
            dataKegiatan.forEachIndexed { i, item ->
                val kList = item.items.mapIndexed { j, k -> "${j + 1}. $k" }.joinToString("<br>")
                lkhRows.append("""
                    <tr>
                        <td align="center" valign="top">${i + 1}</td>
                        <td valign="top">Laporan Kerja Harian</td>
                        <td valign="top">$kList</td>
                        <td align="center" valign="top">${DateUtils.formatTanggalIndo(item.tanggal)}</td>
                    </tr>
                """.trimIndent())
            }

            kontenHtml.append("""
                <div style="font-family:'Times New Roman', Times, serif; font-size:12pt; padding:15px;">
                    <h2 style="text-align:center; font-size:16pt; margin-bottom:20px; text-decoration:underline;">Laporan Kerja</h2>
                    <table style="width:100%; border:none; margin-bottom:15px;">
                        <tr><td width="150">Nama</td><td>: ${profile.pegNama.uppercase(Locale.getDefault())}</td></tr>
                        <tr><td>NIP</td><td>: ${profile.pegNIP}</td></tr>
                        <tr><td>Jabatan</td><td>: ${profile.pegJabatan}</td></tr>
                        <tr><td>Pangkat</td><td>: ${profile.pegPangkat}</td></tr>
                        <tr><td>Golongan Ruang</td><td>: ${profile.pegGolongan}</td></tr>
                    </table>
                    <table border="1" cellpadding="8" style="width:100%; border-collapse:collapse; margin-bottom:20px;">
                        <thead>
                            <tr style="background:#f2f2f2;">
                                <th width="5%" style="text-align:center;">No.</th>
                                <th width="30%" style="text-align:center;">Kegiatan</th>
                                <th width="45%" style="text-align:center;">Pekerjaan</th>
                                <th width="20%" style="text-align:center;">Tanggal</th>
                            </tr>
                        </thead>
                        <tbody>
                            $lkhRows
                        </tbody>
                    </table>
                    $ttdBlock
                </div>
            """)
            if (type == "semua") {
                kontenHtml.append("<div style=\"page-break-after: always;\"></div>")
            }
        }

        // 3. LKB (Laporan Kerja Bulanan)
        if (type == "lkb" || type == "semua") {
            val rekap = aggregateLkb(dataKegiatan)
            val lkbRows = StringBuilder()
            var index = 1
            for ((key, count) in rekap) {
                var volumeText = "Kegiatan"
                var jumlah = count.toString()
                if (key.contains("KBM", ignoreCase = true)) {
                    volumeText = "Kegiatan"
                    jumlah = "${count * 3} JP"
                }
                lkbRows.append("""
                    <tr>
                        <td align="center">$index</td>
                        <td>$key</td>
                        <td align="center">$jumlah</td>
                        <td align="center">$volumeText</td>
                    </tr>
                """.trimIndent())
                index++
            }

            kontenHtml.append("""
                $kopSurat
                <div style="font-family:'Times New Roman', Times, serif; font-size:12pt; padding:0 15px;">
                    <h2 style="text-align:center; font-size:14pt; margin-bottom:5px;">LAPORAN KERJA BULANAN</h2>
                    <h2 style="text-align:center; font-size:14pt; margin-top:0; margin-bottom:20px;">BULAN $strBulanTahun</h2>
                    <table style="width:100%; border:none; margin-bottom:15px;">
                        <tr><td width="150">Nama</td><td>: ${profile.pegNama}</td></tr>
                        <tr><td>NIP</td><td>: ${profile.pegNIP}</td></tr>
                        <tr><td>Jabatan</td><td>: ${profile.pegJabatan}</td></tr>
                    </table>
                    <table border="1" cellpadding="8" style="width:100%; border-collapse:collapse; margin-bottom:20px;">
                        <thead>
                            <tr style="background:#f2f2f2;">
                                <th width="5%" rowspan="2" style="text-align:center; vertical-align:middle;">NO</th>
                                <th width="55%" rowspan="2" style="text-align:center; vertical-align:middle;">KEGIATAN</th>
                                <th width="40%" colspan="2" style="text-align:center; border-bottom:1px solid #000;">VOLUME</th>
                            </tr>
                            <tr style="background:#f2f2f2;">
                                <th width="20%" style="text-align:center;">JUMLAH</th>
                                <th width="20%" style="text-align:center;">SATUAN</th>
                            </tr>
                        </thead>
                        <tbody>
                            $lkbRows
                        </tbody>
                    </table>
                    $ttdBlock
                </div>
            """)
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Laporan - ${profile.pegNama}</title>
                <style>
                    body { font-family: 'Times New Roman', Times, serif; margin: 0; padding: 20px; background:#fff; color:#000; }
                    table { width: 100%; border-collapse: collapse; table-layout: auto; }
                    td, th { word-wrap: break-word; }
                    @page { size: A4 portrait; margin: 15mm; }
                    tr { page-break-inside: avoid; }
                </style>
            </head>
            <body>
                $kontenHtml
            </body>
            </html>
        """.trimIndent()
    }

    fun aggregateLkb(dataKegiatan: List<KegiatanEntry>): Map<String, Int> {
        val rekap = mutableMapOf<String, Int>()
        dataKegiatan.forEach { d ->
            d.items.forEach { k ->
                var text = k.trim()
                if (text.contains("kbm 7", ignoreCase = true)) text = "KBM Kelas 7"
                if (text.contains("kbm 8", ignoreCase = true)) text = "KBM Kelas 8"
                if (text.contains("kbm 9", ignoreCase = true)) text = "KBM Kelas 9"
                if (text.contains("upacara", ignoreCase = true)) text = "Upacara Bendera"
                rekap[text] = (rekap[text] ?: 0) + 1
            }
        }
        return rekap
    }

    // ==========================================
    // NATIVE PDF GENERATION (Zero Chromium/Mesa)
    // ==========================================

    fun buildPdfDocument(
        context: Context,
        type: String,
        profile: UserProfile,
        dataKegiatan: List<KegiatanEntry>,
        selectedBulanFilter: String,
        logoBitmap: Bitmap?
    ): PdfDocument {
        val doc = PdfDocument()
        val tfBold = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        val tfNormal = Typeface.create(Typeface.SERIF, Typeface.NORMAL)

        val (targetYear, targetMonthIndex) = DateUtils.resolveYearAndMonth(
            selectedBulanFilter,
            dataKegiatan.map { it.tanggal }
        )
        val strBulanTahun = DateUtils.formatBulanText(
            String.format(Locale.US, "%04d-%02d", targetYear, targetMonthIndex + 1)
        ).uppercase(Locale.getDefault())
        val tanggalCetak = DateUtils.getWorkingEndMonthDate(targetYear, targetMonthIndex)
        var pageCounter = 1

        if (type == "sampul" || type == "semua") {
            drawSampulPage(doc, profile, strBulanTahun, logoBitmap, tfBold, tfNormal, pageCounter++)
        }

        if (type == "lkh" || type == "semua") {
            pageCounter = drawLkhPages(doc, profile, dataKegiatan, tanggalCetak, tfBold, tfNormal, pageCounter)
        }

        if (type == "lkb" || type == "semua") {
            drawLkbPage(doc, profile, dataKegiatan, strBulanTahun, tanggalCetak, logoBitmap, tfBold, tfNormal, pageCounter++)
        }

        return doc
    }

    private fun drawKopSurat(
        canvas: Canvas,
        logo: Bitmap?,
        satker: String,
        tfBold: Typeface,
        tfNormal: Typeface
    ): Float {
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val normPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textAlign = Paint.Align.CENTER
        }

        if (logo != null) {
            val destRect = Rect(45, 30, 95, 80)
            canvas.drawBitmap(logo, null, destRect, null)
        }

        val centerX = (100f + 550f) / 2f
        boldPaint.textSize = 12f
        canvas.drawText("KEMENTERIAN AGAMA REPUBLIK INDONESIA", centerX, 44f, boldPaint)
        boldPaint.textSize = 11f
        canvas.drawText("KANTOR KEMENTERIAN AGAMA KABUPATEN GARUT", centerX, 58f, boldPaint)
        drawCanvasAutoFitText(canvas, satker.uppercase(Locale.getDefault()), centerX, 72f, boldPaint, targetSize = 11f, minSize = 3f, maxWidth = 450f)
        normPaint.textSize = 8f
        canvas.drawText("Jalan Raya Wanakerta No.28 Cibatu-Garut 44185", centerX, 84f, normPaint)
        canvas.drawText("Telepon (0262) 2860000 Email: mtsn1cibatu@yahoo.co.id", centerX, 94f, normPaint)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
        }
        linePaint.strokeWidth = 2.0f
        canvas.drawLine(45f, 100f, 550f, 100f, linePaint)
        linePaint.strokeWidth = 0.8f
        canvas.drawLine(45f, 103f, 550f, 103f, linePaint)

        return 115f
    }

    private fun drawTtdBlock(
        canvas: Canvas,
        profile: UserProfile,
        tanggalCetak: String,
        startY: Float,
        tfBold: Typeface,
        tfNormal: Typeface
    ): Float {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textAlign = Paint.Align.CENTER
            textSize = 10f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfBold
            textAlign = Paint.Align.CENTER
            textSize = 10f
        }

        val leftX = 160f
        val rightX = 435f

        canvas.drawText("Mengetahui", leftX, startY, textPaint)
        canvas.drawText("Kepala MTsN 2 Garut", leftX, startY + 14f, textPaint)

        canvas.drawText("Garut, $tanggalCetak", rightX, startY, textPaint)
        canvas.drawText("Penyusun", rightX, startY + 14f, textPaint)

        val signY = startY + 65f
        boldPaint.isUnderlineText = true
        canvas.drawText(profile.kepNama, leftX, signY, boldPaint)
        canvas.drawText(profile.pegNama, rightX, signY, boldPaint)
        boldPaint.isUnderlineText = false

        canvas.drawText("NIP. ${profile.kepNIP}", leftX, signY + 14f, textPaint)
        canvas.drawText("NIP. ${profile.pegNIP}", rightX, signY + 14f, textPaint)

        return signY + 25f
    }

    private fun drawSampulPage(
        doc: PdfDocument,
        profile: UserProfile,
        strBulanTahun: String,
        logo: Bitmap?,
        tfBold: Typeface,
        tfNormal: Typeface,
        pageNum: Int
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val normPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textAlign = Paint.Align.CENTER
        }

        if (logo != null) {
            val dest = Rect((595 - 90) / 2, 70, (595 + 90) / 2, 160)
            canvas.drawBitmap(logo, null, dest, null)
        }

        boldPaint.textSize = 18f
        canvas.drawText("LAPORAN KINERJA BULANAN", 595f / 2, 200f, boldPaint)
        boldPaint.textSize = 13f
        canvas.drawText("APARATUR SIPIL NEGARA KEMENTERIAN AGAMA", 595f / 2, 225f, boldPaint)
        canvas.drawText("BULAN $strBulanTahun", 595f / 2, 245f, boldPaint)

        // Identity Box
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val boxLeft = 90f
        val boxTop = 310f
        val boxRight = 505f
        val boxBottom = 450f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F9FAFB")
            style = Paint.Style.FILL
        }
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, bgPaint)
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, linePaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfBold
            textSize = 11f
            textAlign = Paint.Align.LEFT
        }
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textSize = 11f
            textAlign = Paint.Align.LEFT
        }

        var rowY = boxTop + 32f
        val lx = boxLeft + 25f
        val vx = boxLeft + 150f

        fun drawRow(label: String, value: String, isValBold: Boolean = false) {
            canvas.drawText(label, lx, rowY, labelPaint)
            canvas.drawText(":", vx - 12f, rowY, labelPaint)
            val p = if (isValBold) labelPaint else valPaint
            drawCanvasAutoFitText(canvas, value, vx, rowY, p, targetSize = 11f, minSize = 3f, maxWidth = boxRight - 20f - vx)
            rowY += 26f
        }

        drawRow("NAMA", profile.pegNama, true)
        drawRow("NIP", profile.pegNIP)
        drawRow("JABATAN", profile.pegJabatan.uppercase(Locale.getDefault()))
        drawRow("SATUAN KERJA", profile.pegSatker.uppercase(Locale.getDefault()))

        // Footer
        boldPaint.textSize = 13f
        canvas.drawText("KEMENTERIAN AGAMA", 595f / 2, 700f, boldPaint)
        drawCanvasAutoFitText(canvas, profile.pegSatker.uppercase(Locale.getDefault()), 595f / 2, 720f, boldPaint, targetSize = 13f, minSize = 3f, maxWidth = 500f)
        normPaint.textSize = 9.5f
        canvas.drawText("JL. Raya Wanakerta No. 28 Cibatu - Garut", 595f / 2, 738f, normPaint)

        doc.finishPage(page)
    }

    private fun drawLkbPage(
        doc: PdfDocument,
        profile: UserProfile,
        dataKegiatan: List<KegiatanEntry>,
        strBulanTahun: String,
        tanggalCetak: String,
        logo: Bitmap?,
        tfBold: Typeface,
        tfNormal: Typeface,
        pageNum: Int
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = drawKopSurat(canvas, logo, profile.pegSatker, tfBold, tfNormal)

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val normPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textAlign = Paint.Align.LEFT
            textSize = 10f
        }
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textAlign = Paint.Align.CENTER
            textSize = 10f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val bgHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EFEFEF")
            style = Paint.Style.FILL
        }

        // Title
        boldPaint.textSize = 13f
        canvas.drawText("LAPORAN KERJA BULANAN", 595f / 2, currentY + 12f, boldPaint)
        canvas.drawText("BULAN $strBulanTahun", 595f / 2, currentY + 28f, boldPaint)
        currentY += 46f

        // Info Block
        normPaint.textSize = 10f
        val lblX = 45f
        val colX = 130f
        val valX = 140f
        
        fun drawInfoLkb(label: String, value: String, yOffset: Float) {
            canvas.drawText(label, lblX, currentY + yOffset, normPaint)
            canvas.drawText(":", colX, currentY + yOffset, normPaint)
            canvas.drawText(value, valX, currentY + yOffset, normPaint)
        }

        drawInfoLkb("Nama", profile.pegNama, 0f)
        drawInfoLkb("NIP", profile.pegNIP, 14f)
        drawInfoLkb("Jabatan", profile.pegJabatan, 28f)
        currentY += 42f

        // Table
        val colNo = 45f
        val colKeg = 80f
        val colJml = 390f
        val colSat = 470f
        val colRight = 550f

        val thTop = currentY
        val thMid = thTop + 16f
        val thBot = thTop + 32f

        canvas.drawRect(colNo, thTop, colRight, thBot, bgHeader)
        canvas.drawRect(colNo, thTop, colRight, thBot, linePaint)

        // Lines in header
        canvas.drawLine(colKeg, thTop, colKeg, thBot, linePaint)
        canvas.drawLine(colJml, thTop, colJml, thBot, linePaint)
        canvas.drawLine(colSat, thMid, colSat, thBot, linePaint)
        canvas.drawLine(colJml, thMid, colRight, thMid, linePaint)

        boldPaint.textSize = 9.5f
        canvas.drawText("NO", (colNo + colKeg) / 2, thTop + 20f, boldPaint)
        canvas.drawText("KEGIATAN", (colKeg + colJml) / 2, thTop + 20f, boldPaint)
        canvas.drawText("VOLUME", (colJml + colRight) / 2, thTop + 12f, boldPaint)
        canvas.drawText("JUMLAH", (colJml + colSat) / 2, thMid + 12f, boldPaint)
        canvas.drawText("SATUAN", (colSat + colRight) / 2, thMid + 12f, boldPaint)

        currentY = thBot

        val rekap = aggregateLkb(dataKegiatan)
        var rowIdx = 1

        if (rekap.isEmpty()) {
            val rHeight = 22f
            canvas.drawRect(colNo, currentY, colRight, currentY + rHeight, linePaint)
            canvas.drawText("Belum ada kegiatan tercatat pada bulan ini", 595f / 2, currentY + 15f, centerPaint)
            currentY += rHeight
        } else {
            for ((key, count) in rekap) {
                var volumeText = "Kegiatan"
                var jumlah = count.toString()
                if (key.contains("KBM", ignoreCase = true)) {
                    volumeText = "Kegiatan"
                    jumlah = "${count * 3} JP"
                }
                val rHeight = 22f
                canvas.drawRect(colNo, currentY, colRight, currentY + rHeight, linePaint)
                canvas.drawLine(colKeg, currentY, colKeg, currentY + rHeight, linePaint)
                canvas.drawLine(colJml, currentY, colJml, currentY + rHeight, linePaint)
                canvas.drawLine(colSat, currentY, colSat, currentY + rHeight, linePaint)

                canvas.drawText(rowIdx.toString(), (colNo + colKeg) / 2, currentY + 15f, centerPaint)
                canvas.drawText(key, colKeg + 6f, currentY + 15f, normPaint)
                canvas.drawText(jumlah, (colJml + colSat) / 2, currentY + 15f, centerPaint)
                canvas.drawText(volumeText, (colSat + colRight) / 2, currentY + 15f, centerPaint)

                currentY += rHeight
                rowIdx++
            }
        }

        // TTD
        drawTtdBlock(canvas, profile, tanggalCetak, currentY + 30f, tfBold, tfNormal)
        doc.finishPage(page)
    }

    private fun drawLkhPages(
        doc: PdfDocument,
        profile: UserProfile,
        dataKegiatan: List<KegiatanEntry>,
        tanggalCetak: String,
        tfBold: Typeface,
        tfNormal: Typeface,
        startPageNum: Int
    ): Int {
        var pageNum = startPageNum
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
        var page = doc.startPage(pageInfo)
        var canvas = page.canvas

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfBold
            textAlign = Paint.Align.CENTER
        }
        val normPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textAlign = Paint.Align.LEFT
            textSize = 9.5f
        }
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = tfNormal
            textAlign = Paint.Align.CENTER
            textSize = 9.5f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val bgHeader = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EFEFEF")
            style = Paint.Style.FILL
        }

        val colNo = 45f
        val colKeg = 75f
        val colPek = 185f
        val colTgl = 445f
        val colRight = 550f

        fun drawTableHeader(y: Float): Float {
            canvas.drawRect(colNo, y, colRight, y + 24f, bgHeader)
            canvas.drawRect(colNo, y, colRight, y + 24f, linePaint)
            canvas.drawLine(colKeg, y, colKeg, y + 24f, linePaint)
            canvas.drawLine(colPek, y, colPek, y + 24f, linePaint)
            canvas.drawLine(colTgl, y, colTgl, y + 24f, linePaint)

            boldPaint.textSize = 9.5f
            canvas.drawText("No.", (colNo + colKeg) / 2, y + 16f, boldPaint)
            canvas.drawText("Kegiatan", (colKeg + colPek) / 2, y + 16f, boldPaint)
            canvas.drawText("Pekerjaan", (colPek + colTgl) / 2, y + 16f, boldPaint)
            canvas.drawText("Tanggal", (colTgl + colRight) / 2, y + 16f, boldPaint)
            return y + 24f
        }

        // Header on Page 1
        boldPaint.textSize = 15f
        boldPaint.isUnderlineText = true
        canvas.drawText("Laporan Kerja", 595f / 2, 45f, boldPaint)
        boldPaint.isUnderlineText = false

        var currentY = 70f
        normPaint.textSize = 9.5f
        
        val lblX = 45f
        val colX = 130f
        val valX = 140f
        
        fun drawInfoLkh(label: String, value: String, yOffset: Float) {
            canvas.drawText(label, lblX, currentY + yOffset, normPaint)
            canvas.drawText(":", colX, currentY + yOffset, normPaint)
            canvas.drawText(value, valX, currentY + yOffset, normPaint)
        }

        drawInfoLkh("Nama", profile.pegNama.uppercase(Locale.getDefault()), 0f)
        drawInfoLkh("NIP", profile.pegNIP, 13f)
        drawInfoLkh("Jabatan", profile.pegJabatan, 26f)
        drawInfoLkh("Pangkat", profile.pegPangkat, 39f)
        drawInfoLkh("Golongan", profile.pegGolongan, 52f)
        currentY += 70f

        currentY = drawTableHeader(currentY)

        val textPaint = TextPaint(normPaint)
        val pekWidth = (colTgl - colPek - 12f).toInt()

        dataKegiatan.forEachIndexed { i, item ->
            val pekerjaanText = item.items.mapIndexed { idx, str -> "${idx + 1}. $str" }.joinToString("\n")
            val layout = StaticLayout.Builder.obtain(pekerjaanText, 0, pekerjaanText.length, textPaint, pekWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1f)
                .setIncludePad(false)
                .build()

            val rowHeight = (layout.height + 14f).coerceAtLeast(24f)

            if (currentY + rowHeight > 770f) {
                doc.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas
                currentY = drawTableHeader(45f)
            }

            canvas.drawRect(colNo, currentY, colRight, currentY + rowHeight, linePaint)
            canvas.drawLine(colKeg, currentY, colKeg, currentY + rowHeight, linePaint)
            canvas.drawLine(colPek, currentY, colPek, currentY + rowHeight, linePaint)
            canvas.drawLine(colTgl, currentY, colTgl, currentY + rowHeight, linePaint)

            canvas.drawText((i + 1).toString(), (colNo + colKeg) / 2, currentY + 15f, centerPaint)
            canvas.drawText("Laporan Kerja Harian", colKeg + 5f, currentY + 15f, normPaint)

            canvas.save()
            canvas.translate(colPek + 6f, currentY + 6f)
            layout.draw(canvas)
            canvas.restore()

            val tglIndo = DateUtils.formatTanggalIndo(item.tanggal)
            canvas.drawText(tglIndo, (colTgl + colRight) / 2, currentY + 15f, centerPaint)

            currentY += rowHeight
        }

        // Check if TTD fits
        if (currentY + 110f > 800f) {
            doc.finishPage(page)
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            page = doc.startPage(pageInfo)
            canvas = page.canvas
            currentY = 45f
        }

        drawTtdBlock(canvas, profile, tanggalCetak, currentY + 25f, tfBold, tfNormal)
        doc.finishPage(page)

        return pageNum + 1
    }

    // ==========================================
    // ACTIONS: NATIVE PRINT & NATIVE SHARE
    // ==========================================

    fun printNativeDocument(
        context: Context,
        docName: String,
        type: String,
        profile: UserProfile,
        dataKegiatan: List<KegiatanEntry>,
        selectedBulanFilter: String,
        logoBase64: String?
    ) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: run {
                Toast.makeText(context, "Layanan cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
                return
            }

            val logoBmp = getEffectiveLogoBitmap(context, logoBase64)
            val pdfDoc = buildPdfDocument(
                context = context,
                type = type,
                profile = profile,
                dataKegiatan = dataKegiatan,
                selectedBulanFilter = selectedBulanFilter,
                logoBitmap = logoBmp
            )

            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("$docName.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(pdfDoc.pages.size.coerceAtLeast(1))
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    try {
                        FileOutputStream(destination.fileDescriptor).use { out ->
                            pdfDoc.writeTo(out)
                        }
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onWriteFailed(e.message)
                    }
                }

                override fun onFinish() {
                    super.onFinish()
                    try {
                        pdfDoc.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build()

            printManager.print(docName, adapter, printAttributes)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdfDocument(
        context: Context,
        docName: String,
        type: String,
        profile: UserProfile,
        dataKegiatan: List<KegiatanEntry>,
        selectedBulanFilter: String,
        logoBase64: String?
    ) {
        try {
            val logoBmp = getEffectiveLogoBitmap(context, logoBase64)
            val pdfDoc = buildPdfDocument(
                context = context,
                type = type,
                profile = profile,
                dataKegiatan = dataKegiatan,
                selectedBulanFilter = selectedBulanFilter,
                logoBitmap = logoBmp
            )

            val docsDir = File(context.cacheDir, "docs").apply { mkdirs() }
            val cleanFileName = "${docName.replace("[^a-zA-Z0-9_\\-]".toRegex(), "_")}.pdf"
            val file = File(docsDir, cleanFileName)

            FileOutputStream(file).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                setType("application/pdf")
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, docName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Dokumen PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawCanvasAutoFitText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        targetSize: Float = 11f,
        minSize: Float = 3f,
        maxWidth: Float = 450f
    ) {
        val origSize = paint.textSize
        var curSize = targetSize
        paint.textSize = curSize
        while (curSize > minSize && paint.measureText(text) > maxWidth) {
            curSize -= 0.5f
            paint.textSize = curSize
        }
        canvas.drawText(text, x, y, paint)
        paint.textSize = origSize
    }

    fun printDocument(context: Context, docName: String, htmlContent: String) {
        // Safe fallback redirection to native printing or user toast
        Toast.makeText(context, "Silakan gunakan tombol Cetak / PDF untuk mencetak dokumen resmi", Toast.LENGTH_SHORT).show()
    }
}
