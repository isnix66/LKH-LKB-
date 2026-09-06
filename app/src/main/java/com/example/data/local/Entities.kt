package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kegiatan")
data class KegiatanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val tanggal: String,
    val kegiatan: String,
    val bulanText: String,
    val sheetKey: String,
    val rowIndex: Int
)

@Entity(tableName = "jadwal")
data class JadwalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val hari: String,
    val kegiatan: String,
    val urutan: Int = 0
)

@Entity(tableName = "profil")
data class ProfileEntity(
    @PrimaryKey
    val username: String,
    val pegNama: String,
    val pegNIP: String,
    val pegJabatan: String,
    val pegPangkat: String,
    val pegGolongan: String,
    val pegSatker: String,
    val kepNama: String,
    val kepNIP: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val password: String,
    val displayName: String,
    val role: String,
    val spreadsheetUrl: String = "",
    val spreadsheetId: String = ""
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
