package com.example.webserialread.data.local

import androidx.room.*
import com.example.webserialread.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE serialId = :serialId ORDER BY publishedAt ASC")
    fun getChaptersForSerial(serialId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    @Query("UPDATE chapters SET isRead = 1, scrollPosition = :position, readAt = :readAt WHERE id = :id")
    suspend fun markRead(id: Long, position: Int = 0, readAt: Long = System.currentTimeMillis())

    @Query("UPDATE chapters SET content = :content WHERE id = :id")
    suspend fun updateContent(id: Long, content: String)

    @MapInfo(keyColumn = "serialId", valueColumn = "cnt")
    @Query("SELECT serialId, COUNT(*) as cnt FROM chapters WHERE isRead = 0 GROUP BY serialId")
    fun getUnreadCountMap(): Flow<Map<Long, Int>>

    @Query("SELECT COUNT(*) FROM chapters WHERE serialId = :serialId AND isRead = 0")
    suspend fun unreadCount(serialId: Long): Int

    @Query("SELECT COUNT(*) FROM chapters WHERE serialId = :serialId")
    suspend fun totalCount(serialId: Long): Int

    @Query("SELECT * FROM chapters WHERE isRead = 1 ORDER BY readAt DESC LIMIT 60")
    fun getRecentlyReadChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE serialId = :serialId ORDER BY publishedAt ASC")
    suspend fun getChaptersSnapshot(serialId: Long): List<ChapterEntity>

    @Query("DELETE FROM chapters WHERE serialId = :serialId")
    suspend fun deleteAllForSerial(serialId: Long)
}
