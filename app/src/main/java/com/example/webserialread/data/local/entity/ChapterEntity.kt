package com.example.webserialread.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SerialEntity::class,
            parentColumns = ["id"],
            childColumns = ["serialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("serialId")]
)
data class ChapterEntity(
    @PrimaryKey val id: Long,
    val serialId: Long,
    val title: String,
    val url: String,
    val content: String? = null,
    val publishedAt: Long,
    val isRead: Boolean = false,
    val scrollPosition: Int = 0,
    val readAt: Long = 0           // timestamp when last read, 0 = never
)
