package com.example.webserialread.data.remote

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.jsoup.Jsoup
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Specialist scraper for sonicmtl.com.
 *
 * Chapter lists are JS-rendered and paginated behind "Show more" buttons, so
 * we load the novel page in a hidden WebView, recursively click every "Show
 * more" button until the full list is visible, then inject JS to extract all
 * chapter links in reading order.
 *
 * Chapter content is fetched with OkHttp + Jsoup (no JS needed for the reader page).
 */
object SonicMtlScraper {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
                        .build()
                )
            }
            .build()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun isSonicMtl(url: String): Boolean {
        val host = runCatching { URI(url).host }.getOrNull() ?: return false
        return host.removePrefix("www.") == "sonicmtl.com"
    }

    /**
     * Loads the novel page in a WebView, expands all chapters, and returns
     * a [ScrapeResult] with the novel title, cover URL, and full chapter list.
     */
    suspend fun scrapeNovel(context: Context, novelUrl: String): ScrapeResult? {
        val slug = novelSlug(novelUrl) ?: return null
        val json = loadAndExtractChapters(context.applicationContext, novelUrl, slug)
            ?: return null

        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return null
        if (arr.length() == 0) return null

        // Index 0 is always the metadata object {novelTitle, coverUrl}
        val meta = runCatching { arr.getJSONObject(0) }.getOrNull()
        val title = meta?.optString("novelTitle")?.ifBlank { null }
            ?: slug.slugToTitle()
        val coverUrl = meta?.optString("coverUrl")?.ifBlank { null }

        val chapters = (1 until arr.length()).mapNotNull { i ->
            runCatching {
                val obj = arr.getJSONObject(i)
                val url = obj.optString("url").trimEnd('/')
                val text = obj.optString("text").trim()
                if (url.isBlank() || text.isBlank()) return@mapNotNull null
                ScrapedChapter(id = TocScraper.urlToId(url), title = text, url = url)
            }.getOrNull()
        }.distinctBy { it.url }

        if (chapters.isEmpty()) return null
        return ScrapeResult(title = title, chapters = chapters, coverUrl = coverUrl)
    }

    /**
     * Fetches a chapter page with OkHttp and extracts the readable content via Jsoup.
     */
    fun scrapeContent(chapterUrl: String): String {
        val response = runCatching {
            http.newCall(Request.Builder().url(chapterUrl).build()).execute()
        }.getOrNull() ?: return ""
        val html = response.body?.string() ?: return ""
        val doc = Jsoup.parse(html, chapterUrl)

        val content = doc.selectFirst(".chapter-content")
            ?: doc.selectFirst(".reading-content")
            ?: doc.selectFirst("#chapter-content")
            ?: doc.selectFirst("article .content")
            ?: doc.selectFirst(".entry-content")
            ?: return ""

        // Strip ads, scripts, nav elements
        content.select(
            "script, style, ins, iframe, " +
            "[class*=ad], [id*=ad], [class*=google], [class*=sponsor], " +
            "nav, .navigation, .chapter-nav, .prev-next, " +
            ".report, [class*=share], [class*=social]"
        ).remove()

        return content.html()
    }

    // ── WebView expansion + extraction ────────────────────────────────────────

    private suspend fun loadAndExtractChapters(
        context: Context,
        novelUrl: String,
        slug: String
    ): String? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            val webView = WebView(context)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.userAgentString = USER_AGENT

            var completed = false

            fun complete(result: String?) {
                if (!completed) {
                    completed = true
                    mainHandler.post { webView.destroy() }
                    if (cont.isActive) cont.resume(result)
                }
            }

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    // Initial render settle time before we start clicking
                    mainHandler.postDelayed({
                        if (!completed) expandAndExtract(view, slug, maxClicks = 80, ::complete)
                    }, 2_000)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView, request: WebResourceRequest
                ) = false
            }

            webView.loadUrl(novelUrl)

            // Hard cap — large novels with hundreds of chapters can take a while
            mainHandler.postDelayed({ complete(null) }, 120_000)

            cont.invokeOnCancellation { complete(null) }
        }
    }

    /**
     * Recursively clicks "Show more" buttons.
     * When none remain (or [maxClicks] is exhausted), extracts all chapter links.
     */
    private fun expandAndExtract(
        view: WebView,
        slug: String,
        maxClicks: Int,
        complete: (String?) -> Unit
    ) {
        if (maxClicks <= 0) {
            view.evaluateJavascript(extractJs(slug)) { json -> complete(json) }
            return
        }
        view.evaluateJavascript(CLICK_SHOW_MORE_JS) { result ->
            if (result == "true") {
                // Button was clicked — wait for new rows to render, then try again
                mainHandler.postDelayed({
                    expandAndExtract(view, slug, maxClicks - 1, complete)
                }, 1_500)
            } else {
                // No visible "Show more" remaining — do final extraction
                view.evaluateJavascript(extractJs(slug)) { json -> complete(json) }
            }
        }
    }

    // ── JavaScript snippets ───────────────────────────────────────────────────

    /**
     * Tries to find and click a visible "Show more / Load more" button.
     * Returns JS boolean true if a button was clicked, false otherwise.
     */
    private val CLICK_SHOW_MORE_JS = """
        (function() {
            try {
                // Text-based match (most reliable across site redesigns)
                var all = document.querySelectorAll('button, a, [role="button"]');
                for (var i = 0; i < all.length; i++) {
                    var el = all[i];
                    if (el.offsetParent === null) continue; // hidden
                    var t = (el.innerText || el.textContent || '').toLowerCase().trim();
                    if (t === 'show more' || t === 'load more' ||
                        t === 'more chapters' || t === 'see more' ||
                        t === 'load chapters' || t === 'show chapters') {
                        el.click();
                        return true;
                    }
                }
                // Class-based fallback
                var cls = document.querySelector(
                    '.show-more:not([style*="display:none"]):not([style*="display: none"]),' +
                    '.load-more:not([style*="display:none"]):not([style*="display: none"]),' +
                    '.c-btn-see-more,' +
                    '[class*="show-more"]'
                );
                if (cls && cls.offsetParent !== null) { cls.click(); return true; }
            } catch(e) {}
            return false;
        })()
    """.trimIndent()

    /**
     * Extracts novel title, cover image, and all chapter links for [novelSlug].
     * Returns a JS array where index 0 is {novelTitle, coverUrl} and the rest
     * are {url, text} chapter objects in reading order (oldest first).
     */
    private fun extractJs(novelSlug: String): String {
        val safeSlug = novelSlug.replace("'", "\\'")
        return """
            (function() {
                var slug = '$safeSlug';

                // Novel metadata
                var titleEl = document.querySelector(
                    'h1.novel-title, h1[class*=title], .book-info h1, h1');
                var novelTitle = titleEl
                    ? (titleEl.innerText || titleEl.textContent || '').replace(/\s+/g, ' ').trim()
                    : '';
                var coverEl = document.querySelector(
                    'img.novel-cover, .cover img, [class*=cover] img, ' +
                    '.book-cover img, .thumbnail img, .img-responsive');
                var coverUrl = coverEl ? (coverEl.src || coverEl.dataset.src || '') : '';

                var out = [{novelTitle: novelTitle, coverUrl: coverUrl}];

                // Chapter links — must contain /novel/<slug>/ and have an extra path segment
                var anchors = document.querySelectorAll('a[href]');
                var seen = {};
                var chapters = [];
                for (var i = 0; i < anchors.length; i++) {
                    var a = anchors[i];
                    var href = a.href.split('?')[0].split('#')[0];
                    var marker = '/novel/' + slug + '/';
                    if (href.indexOf(marker) < 0) continue;
                    var after = href.split(marker)[1] || '';
                    if (!after.replace(/\/$/, '')) continue; // skip novel root
                    if (seen[href]) continue;
                    seen[href] = true;
                    var t = (a.innerText || a.textContent || '').replace(/\s+/g, ' ').trim();
                    if (!t) t = after.replace(/\/$/, '').replace(/-/g, ' ');
                    chapters.push({url: href, text: t});
                }

                // sonicmtl lists newest first — reverse to get reading order
                chapters.reverse();
                return out.concat(chapters);
            })()
        """.trimIndent()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun novelSlug(url: String): String? {
        val path = runCatching { URI(url).path }.getOrNull() ?: return null
        val parts = path.trim('/').split('/')
        // Expected: ["novel", "<slug>"] or ["novel", "<slug>", ...]
        return if (parts.size >= 2 && parts[0] == "novel") parts[1].ifBlank { null } else null
    }

    private fun String.slugToTitle(): String =
        split('-').joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Mobile Safari/537.36"
}
