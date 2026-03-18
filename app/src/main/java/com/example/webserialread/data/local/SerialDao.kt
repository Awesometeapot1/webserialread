package com.example.webserialread.data.local

import androidx.room.*
import com.example.webserialread.data.local.entity.SerialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SerialDao {
    @Query("SELECT * FROM serials ORDER BY addedAt DESC")
    fun getAllSerials(): Flow<List<SerialEntity>>

    @Query("SELECT * FROM serials WHERE id = :id")
    suspend fun getSerialById(id: Long): SerialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(serial: SerialEntity): Long

    @Delete
    suspend fun delete(serial: SerialEntity)

    @Query("UPDATE serials SET lastReadChapterId = :chapterId WHERE id = :serialId")
    suspend fun updateLastRead(serialId: Long, chapterId: Long)
}
