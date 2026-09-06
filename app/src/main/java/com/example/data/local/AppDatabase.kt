package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        KegiatanEntity::class,
        JadwalEntity::class,
        ProfileEntity::class,
        UserEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun kegiatanDao(): KegiatanDao
    abstract fun jadwalDao(): JadwalDao
    abstract fun profileDao(): ProfileDao
    abstract fun userDao(): UserDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lkh_lkb_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            prepopulateData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateData(db: AppDatabase) {
            // Default Users
            db.userDao().insertAll(
                listOf(
                    UserEntity(
                        username = "admin",
                        password = "123",
                        displayName = "Administrator",
                        role = "admin",
                        spreadsheetUrl = "https://script.google.com/macros/s/AKfycbzsX_CTPXVgJaECn_DHtsu4vUYe_tFcNwFdAjDvqDfq9wM_z56fq54EV1rHRph4SQ4a0Q/exec"
                    ),
                    UserEntity(
                        username = "user1",
                        password = "123",
                        displayName = "Irvan Suwandi, S.Pd",
                        role = "user",
                        spreadsheetUrl = "https://script.google.com/macros/s/AKfycbzsX_CTPXVgJaECn_DHtsu4vUYe_tFcNwFdAjDvqDfq9wM_z56fq54EV1rHRph4SQ4a0Q/exec"
                    )
                )
            )

            // Default Profiles
            val defaultProfile = ProfileEntity(
                username = "admin",
                pegNama = "Irvan Suwandi, S.Pd",
                pegNIP = "198606132023211016",
                pegJabatan = "Guru",
                pegPangkat = "Penata Muda",
                pegGolongan = "III/a",
                pegSatker = "MADRASAH TSANAWIYAH NEGERI 2 GARUT",
                kepNama = "H. Asep Sodikin, S.Pd., M.M",
                kepNIP = "197305071997031002"
            )
            db.profileDao().saveProfile(defaultProfile)
            db.profileDao().saveProfile(defaultProfile.copy(username = "user1"))

            // Default Routines
            val defaultJadwalAdmin = listOf(
                JadwalEntity(username = "admin", hari = "Senin", kegiatan = "Apel Pagi Bendera\nMenyusun perangkat KBM\nKBM Kelas 7", urutan = 1),
                JadwalEntity(username = "admin", hari = "Selasa", kegiatan = "KBM Kelas 8\nKBM Kelas 9\nBimbingan siswa berprestasi", urutan = 2),
                JadwalEntity(username = "admin", hari = "Rabu", kegiatan = "KBM Kelas 7\nEvaluasi materi ajar", urutan = 3),
                JadwalEntity(username = "admin", hari = "Kamis", kegiatan = "KBM Kelas 8\nPembinaan kepribadian siswa", urutan = 4),
                JadwalEntity(username = "admin", hari = "Jumat", kegiatan = "Sholat Dhuha bersama\nKBM Kelas 9\nJumat Bersih", urutan = 5),
                JadwalEntity(username = "admin", hari = "Setiap Hari Kerja", kegiatan = "Sholat Dzuhur Berjamaah\nAbsensi kehadiran pegawai", urutan = 6)
            )
            db.jadwalDao().insertAll(defaultJadwalAdmin)

            val defaultJadwalUser = defaultJadwalAdmin.map { it.copy(id = 0, username = "user1") }
            db.jadwalDao().insertAll(defaultJadwalUser)
        }
    }
}
