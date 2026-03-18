package com.example.webserialread.data.repository

import android.content.Context
import com.example.webserialread.data.local.ChapterDao
import com.example.webserialread.data.local.SerialDao
import com.example.webserialread.data.local.entity.ChapterEntity
import com.example.webserialread.data.local.entity.SerialEntity
import com.example.webserialread.data.remote.TocScraper
import com.example.webserialread.data.remote.WebViewScraper
import com.example.webserialread.data.remote.categoriesApiUrl
import com.example.webserialread.data.remote.createWordPressApi
import com.example.webserialread.data.remote.model.WpPost
import com.example.webserialread.data.remote.postApiUrl
import com.example.webserialread.data.remote.postsApiUrl
import kotlinx.coroutines.flow.Flow
import org.jsoup.Jsoup
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class SerialRepository(
    private val serialDao: SerialDao,
    private val chapterDao: ChapterDao,
    private val context: Context
) {
    fun getAllSerials(): Flow<List<SerialEntity>> = serialDao.getAllSerials()
    fun getChaptersForSerial(id: Long): Flow<List<ChapterEntity>> = chapterDao.getChaptersForSerial(id)
    fun getUnreadCountMap(): Flow<Map<Long, Int>> = chapterDao.getUnreadCountMap()
    fun getRecentlyReadChapters(): Flow<List<ChapterEntity>> = chapterDao.getRecentlyReadChapters()

    suspend fun getSerialById(id: Long): SerialEntity? = serialDao.getSerialById(id)
    suspend fun getChapterById(id: Long): ChapterEntity? = chapterDao.getChapterById(id)
    suspend fun getChaptersSnapshot(id: Long): List<ChapterEntity> = chapterDao.getChaptersSnapshot(id)

    suspend fun addSerial(
        rawUrl: String,
        tocUrl: String? = null,
        wpCategorySlug: String? = null
    ): Result<Long> = runCatching {
        val baseUrl = normalizeUrl(rawUrl)
        val api = createWordPressApi(baseUrl)

        val site = runCatching { api.getSiteInfo() }.getOrNull()
        val fallbackTitle = runCatching { URI(baseUrl).host }.getOrNull() ?: baseUrl

        // Look up category ID if a slug was provided (for multi-series blogs)
        val categoryId: Long? = wpCategorySlug?.let { slug ->
            runCatching {
                api.getCategories(url = categoriesApiUrl(baseUrl), slug = slug)
                    .body()?.firstOrNull()?.id
            }.getOrNull()
        }

        val entity = SerialEntity(
            title = site?.name ?: fallbackTitle,
            siteUrl = baseUrl,
            tocUrl = tocUrl?.ifBlank { null },
            wpCategorySlug = wpCategorySlug,
            description = site?.description ?: "",
            siteIconUrl = site?.siteIconUrl
        )

        val postsUrl = postsApiUrl(baseUrl)

        // Try with _fields first; some sites/plugins reject it so fall back without
        var response = api.getPosts(url = postsUrl, page = 1, fields = "id,title,link,date,status", categoryId = categoryId)
        val useFields = response.isSuccessful
        if (!useFields) {
            response = api.getPosts(url = postsUrl, page = 1, fields = null, categoryId = categoryId)
        }
        if (!response.isSuccessful) {
            // REST API blocked — try scraping (OkHttp first, then WebView as fallback).
            val scraped = if (tocUrl != null) {
                TocScraper.scrapeFromUrl(tocUrl, baseUrl)
                    ?: WebViewScraper.scrapeFromUrl(context, tocUrl, baseUrl)
                    ?: error("Could not find chapter links at the Table of Contents URL.\n" +
                        "Make sure it points to the page that lists all chapters.")
            } else {
                TocScraper.scrape(baseUrl)
                    ?: WebViewScraper.scrapeFromUrl(context, baseUrl + "/table-of-contents/", baseUrl)
                    ?: WebViewScraper.scrapeFromUrl(context, baseUrl + "/toc/", baseUrl)
                    ?: error("Could not reach WordPress API (HTTP ${response.code()}) " +
                        "and could not auto-detect chapters.\n" +
                        "Re-add the serial and paste its Table of Contents URL in the optional field.")
            }

            val scrapedEntity = entity.copy(title = scraped.title)
            val id = serialDao.insert(scrapedEntity)
            chapterDao.insertAll(scraped.chapters.mapIndexed { index, ch ->
                ChapterEntity(
                    id = ch.id,
                    serialId = id,
                    title = ch.title,
                    url = ch.url,
                    publishedAt = index.toLong()
                )
            })
            return@runCatching id
        }

        val id = serialDao.insert(entity)
        val totalPages = response.headers()["X-WP-TotalPages"]?.toIntOrNull() ?: 1
        response.body()?.let { chapterDao.insertAll(it.map { p -> p.toChapter(id) }) }

        for (page in 2..totalPages) {
            val resp = api.getPosts(
                url = postsUrl, page = page,
                fields = if (useFields) "id,title,link,date,status" else null,
                categoryId = categoryId
            )
            resp.body()?.let { chapterDao.insertAll(it.map { p -> p.toChapter(id) }) }
        }
        id
    }

    suspend fun syncChapters(serial: SerialEntity) {
        val api = createWordPressApi(serial.siteUrl)
        val postsUrl = postsApiUrl(serial.siteUrl)

        val categoryId: Long? = serial.wpCategorySlug?.let { slug ->
            runCatching {
                api.getCategories(url = categoriesApiUrl(serial.siteUrl), slug = slug)
                    .body()?.firstOrNull()?.id
            }.getOrNull()
        }

        // Detect whether this site supports _fields
        var firstResp = api.getPosts(url = postsUrl, page = 1, fields = "id,title,link,date,status", categoryId = categoryId)
        val useFields = firstResp.isSuccessful
        if (!useFields) firstResp = api.getPosts(url = postsUrl, page = 1, fields = null, categoryId = categoryId)

        if (!firstResp.isSuccessful) {
            // Fall back to scraping — prefer stored tocUrl, then auto-detect
            val scraped = serial.tocUrl?.let { TocScraper.scrapeFromUrl(it, serial.siteUrl) }
                ?: TocScraper.scrape(serial.siteUrl)
                ?: serial.tocUrl?.let { WebViewScraper.scrapeFromUrl(context, it, serial.siteUrl) }
                ?: return
            val existing = chapterDao.getChaptersSnapshot(serial.id).map { it.url }.toSet()
            val newChapters = scraped.chapters.filter { it.url !in existing }
            chapterDao.insertAll(newChapters.mapIndexed { index, ch ->
                ChapterEntity(
                    id = ch.id,
                    serialId = serial.id,
                    title = ch.title,
                    url = ch.url,
                    publishedAt = System.currentTimeMillis() + index
                )
            })
            return
        }

        val totalPages = firstResp.headers()["X-WP-TotalPages"]?.toIntOrNull() ?: 1
        firstResp.body()?.let { chapterDao.insertAll(it.map { p -> p.toChapter(serial.id) }) }

        for (page in 2..totalPages) {
            val resp = api.getPosts(
                url = postsUrl, page = page,
                fields = if (useFields) "id,title,link,date,status" else null,
                categoryId = categoryId
            )
            resp.body()?.let { chapterDao.insertAll(it.map { p -> p.toChapter(serial.id) }) }
        }
    }

    suspend fun fetchChapterContent(chapter: ChapterEntity): String {
        val html = if (TocScraper.isScrapedId(chapter.id)) {
            // Scraped chapter — fetch content directly from the chapter URL
            TocScraper.scrapeContent(chapter.url)
        } else {
            val serial = serialDao.getSerialById(chapter.serialId)
                ?: error("Serial not found for chapter ${chapter.id}")
            val api = createWordPressApi(serial.siteUrl)
            val post = api.getPost(url = postApiUrl(serial.siteUrl, chapter.id))
            post.content?.rendered ?: ""
        }
        chapterDao.updateContent(chapter.id, html)
        return html
    }

    suspend fun downloadAllChapters(serialId: Long, onProgress: (done: Int, total: Int) -> Unit) {
        val all = chapterDao.getChaptersSnapshot(serialId)
        val needed = all.filter { it.content == null }
        needed.forEachIndexed { index, chapter ->
            runCatching { fetchChapterContent(chapter) }
            onProgress(index + 1, needed.size)
        }
    }

    suspend fun markChapterRead(chapterId: Long, scrollPosition: Int = 0) {
        chapterDao.markRead(chapterId, scrollPosition)
    }

    suspend fun updateLastReadChapter(serialId: Long, chapterId: Long) {
        serialDao.updateLastRead(serialId, chapterId)
    }

    suspend fun deleteSerial(serial: SerialEntity) = serialDao.delete(serial)

    // ----

    private fun WpPost.toChapter(serialId: Long) = ChapterEntity(
        id = id,
        serialId = serialId,
        title = Jsoup.parse(title.rendered).text(),
        url = link,
        publishedAt = parseWpDate(date)
    )

    private fun normalizeUrl(input: String): String {
        var url = input.trim()
        if (!url.startsWith("http")) url = "https://$url"
        return url.trimEnd('/')
    }

    private fun parseWpDate(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
