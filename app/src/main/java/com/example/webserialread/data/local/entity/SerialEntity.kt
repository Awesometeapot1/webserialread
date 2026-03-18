package com.example.webserialread.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "serials")
data class SerialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val siteUrl: String,        // normalized base URL, trailing slash stripped
    val description: String,
    val siteIconUrl: String? = null,
    val tocUrl: String? = null,            // stored so sync can re-scrape non-WP sites
    val wpCategorySlug: String? = null,   // filter posts by category on multi-series WP blogs
    val lastReadChapterId: Long? = null,
    val addedAt: Long = System.currentTimeMillis()
)
