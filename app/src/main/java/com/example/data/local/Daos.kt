package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KegiatanDao {
    @Query("SELECT * FROM kegiatan WHERE username = :username ORDER BY tanggal ASC")
    fun getAllByUser(username: String): Flow<List<KegiatanEntity>>

    @Query("SELECT * FROM kegiatan WHERE username = :username AND (sheetKey = :sheetKey OR bulanText = :bulanText) ORDER BY tanggal ASC")
    suspend fun getByMonth(username: String, sheetKey: String, bulanText: String): List<KegiatanEntity>

    @Query("SELECT DISTINCT sheetKey FROM kegiatan WHERE username = :username ORDER BY sheetKey ASC")
    suspend fun getAllMonthKeys(username: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KegiatanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<KegiatanEntity>)

    @Update
    suspend fun update(entity: KegiatanEntity)

    @Query("DELETE FROM kegiatan WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM kegiatan WHERE username = :username AND (sheetKey = :sheetKey OR bulanText = :bulanText)")
    suspend fun deleteByMonth(username: String, sheetKey: String, bulanText: String)

    @Query("DELETE FROM kegiatan WHERE username = :username")
    suspend fun deleteAllForUser(username: String)
}

@Dao
interface JadwalDao {
    @Query("SELECT * FROM jadwal WHERE username = :username ORDER BY urutan ASC, id ASC")
    fun getAllByUser(username: String): Flow<List<JadwalEntity>>

    @Query("SELECT * FROM jadwal WHERE username = :username ORDER BY urutan ASC, id ASC")
    suspend fun getListByUser(username: String): List<JadwalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: JadwalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<JadwalEntity>)

    @Query("DELETE FROM jadwal WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM jadwal WHERE username = :username")
    suspend fun deleteAllForUser(username: String)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profil WHERE username = :username LIMIT 1")
    suspend fun getProfile(username: String): ProfileEntity?

    @Query("SELECT * FROM profil WHERE username = :username LIMIT 1")
    fun getProfileFlow(username: String): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(entity: ProfileEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun authenticate(username: String, password: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)
}

@Dao
interface SettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setValue(setting: SettingEntity)
}
