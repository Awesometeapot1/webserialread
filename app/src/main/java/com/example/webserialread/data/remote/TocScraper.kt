package com.example.webserialread.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URI
import java.util.concurrent.TimeUnit

object TocScraper {

    // Use OkHttp (same stack as the REST API) — handles modern TLS, redirects, gzip automatically
    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Upgrade-Insecure-Requests", "1")
                    .build()
                chain.proceed(req)
            }
            .build()
    }

    private val TOC_PATHS = listOf(
        "/table-of-contents/",
        "/table-of-contents",
        "/toc/",
        "/toc",
        "/chapters/",
        "/chapters",
    )

    private val TOC_KEYWORDS = listOf(
        "table of contents", "table-of-contents", "toc", "chapters", "chapter list", "chapter index"
    )

    private val NAV_PATHS = setOf(
        "about", "contact", "privacy", "privacy-policy", "terms", "terms-of-service",
        "donate", "patreon", "faq", "search", "tag", "category", "author", "feed",
        "wp-content", "wp-includes", "wp-admin", "shop", "cart", "sitemap",
        "login", "register", "account", "members"
    )

    private val CHAPTER_SELECTORS = listOf(
        ".entry-content a[href]",
        ".post-content a[href]",
        "article a[href]",
        ".toc a[href]",
        ".chapter-list a[href]",
        "main a[href]",
        "body a[href]",  // last resort — broadest possible
    )

    /**
     * Scrape a specific ToC URL the user provided.
     * Uses the site root only for host-matching and title fetching.
     */
    fun scrapeFromUrl(tocUrl: String, siteUrl: String): ScrapeResult? {
        val baseHost = runCatching { URI(siteUrl).host }.getOrNull() ?: return null
        val normalizedBase = normalizeHost(baseHost)
        val chapters = runCatching {
            scrapeChaptersFrom(tocUrl.trimEnd('/'), normalizedBase, minLinks = 1)
        }.getOrNull() ?: return null
        if (chapters.isEmpty()) return null
        return ScrapeResult(title = fetchTitle(siteUrl, baseHost), chapters = chapters)
    }

    /**
     * Auto-detect a ToC page and scrape chapters from it.
     */
    fun scrape(siteUrl: String): ScrapeResult? {
        val baseHost = runCatching { URI(siteUrl).host }.getOrNull() ?: return null
        val normalizedBase = normalizeHost(baseHost)

        // 1. Try well-known ToC paths
        for (path in TOC_PATHS) {
            val url = siteUrl.trimEnd('/') + path
            val chapters = runCatching { scrapeChaptersFrom(url, normalizedBase) }.getOrNull()
            if (!chapters.isNullOrEmpty()) {
                return ScrapeResult(title = fetchTitle(siteUrl, baseHost), chapters = chapters)
            }
        }

        // 2. Find a ToC link from the homepage
        val tocPageUrl = runCatching { findTocLink(siteUrl, normalizedBase) }.getOrNull()
        if (tocPageUrl != null) {
            val chapters = runCatching { scrapeChaptersFrom(tocPageUrl, normalizedBase) }.getOrNull()
            if (!chapters.isNullOrEmpty()) {
                return ScrapeResult(title = fetchTitle(siteUrl, baseHost), chapters = chapters)
            }
        }

        // 3. Treat homepage itself as chapter list (low bar)
        val chapters = runCatching { scrapeChaptersFrom(siteUrl, normalizedBase, minLinks = 2) }.getOrNull()
        if (!chapters.isNullOrEmpty()) {
            return ScrapeResult(title = fetchTitle(siteUrl, baseHost), chapters = chapters)
        }

        return null
    }

    private fun findTocLink(siteUrl: String, normalizedBase: String): String? {
        val doc = fetchDoc(siteUrl) ?: return null
        return doc.select("a[href]")
            .firstOrNull { el ->
                val text = el.text().trim().lowercase()
                val href = el.attr("href").lowercase()
                TOC_KEYWORDS.any { kw -> text.contains(kw) || href.contains(kw) }
            }
            ?.absUrl("href")
            ?.trimEnd('/')
    }

    private fun scrapeChaptersFrom(
        url: String,
        normalizedBase: String,
        minLinks: Int = 3
    ): List<ScrapedChapter>? {
        val doc = fetchDoc(url) ?: return null
        val rootHref = url.trimEnd('/')

        for (selector in CHAPTER_SELECTORS) {
            val links = doc.select(selector).mapNotNull { el ->
                val href = el.absUrl("href").trimEnd('/')
                if (href.isBlank()) return@mapNotNull null

                val host = runCatching { URI(href).host }.getOrElse { return@mapNotNull null }
                if (normalizeHost(host) != normalizedBase) return@mapNotNull null
                if (href == rootHref) return@mapNotNull null

                val path = runCatching { URI(href).path.trim('/') }.getOrElse { "" }
                if (path.isEmpty()) return@mapNotNull null
                if (path.substringBefore('/').lowercase() in NAV_PATHS) return@mapNotNull null

                val text = el.text().trim()
                if (text.isBlank()) return@mapNotNull null

                ScrapedChapter(id = urlToId(href), title = text, url = href)
            }.distinctBy { it.url }

            if (links.size >= minLinks) return links
        }
        return null
    }

    fun scrapeContent(chapterUrl: String): String {
        val doc = fetchDoc(chapterUrl) ?: return ""
        val content = doc.selectFirst(".entry-content")
            ?: doc.selectFirst(".post-content")
            ?: doc.selectFirst("article")
            ?: return ""
        content.select(
            "nav, .navigation, .post-navigation, " +
            ".sharedaddy, .jp-relatedposts, .wpcnt, " +
            "script, style, [class*=share], [class*=related]"
        ).remove()
        return content.html()
    }

    private fun fetchDoc(url: String): org.jsoup.nodes.Document? {
        val response = http.newCall(Request.Builder().url(url).build()).execute()
        if (!response.isSuccessful) return null
        val html = response.body?.string() ?: return null
        val finalUrl = response.request.url.toString()
        return Jsoup.parse(html, finalUrl)
    }

    private fun fetchTitle(siteUrl: String, fallback: String): String =
        runCatching { fetchDoc(siteUrl)?.title()?.ifBlank { fallback } ?: fallback }.getOrElse { fallback }

    fun urlToId(url: String): Long {
        var h = url.hashCode().toLong() and 0xFFFFFFFFL
        if (h == 0L) h = 1L
        return -h
    }

    fun isScrapedId(id: Long) = id < 0

    /**
     * Tries to find a cover/banner image for the site by checking:
     * 1. og:image meta tag (most reliable — set by most WordPress themes)
     * 2. twitter:image meta tag
     * 3. apple-touch-icon link
     * Returns an absolute image URL or null.
     */
    fun scrapeOgImage(siteUrl: String): String? = runCatching {
        val doc = fetchDoc(siteUrl) ?: return null
        val baseUri = doc.baseUri().ifBlank { siteUrl }

        // og:image
        doc.selectFirst("meta[property=og:image]")?.attr("abs:content")?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("meta[name=twitter:image]")?.attr("abs:content")?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("link[rel=apple-touch-icon]")?.attr("abs:href")?.takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun normalizeHost(host: String) = host.removePrefix("www.")

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Mobile Safari/537.36"
}

data class ScrapeResult(
    val title: String,
    val chapters: List<ScrapedChapter>,
    val coverUrl: String? = null
)
data class ScrapedChapter(val id: Long, val title: String, val url: String)
