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
import org.json.JSONArray
import java.net.URI
import kotlin.coroutines.resume

/**
 * Uses an Android WebView (real Chromium engine) to load a ToC page and extract chapter links.
 * Works on sites that block non-browser clients (Cloudflare, etc.) because WebView IS a real
 * browser — it handles JS challenges, cookies, and TLS fingerprinting natively.
 */
object WebViewScraper {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val NAV_PATHS = setOf(
        "about", "contact", "privacy", "privacy-policy", "terms", "terms-of-service",
        "donate", "patreon", "faq", "search", "tag", "category", "author", "feed",
        "wp-content", "wp-includes", "wp-admin", "shop", "cart", "sitemap",
        "login", "register", "account", "members"
    )

    suspend fun scrapeFromUrl(context: Context, tocUrl: String, siteUrl: String): ScrapeResult? {
        val baseHost = runCatching { URI(siteUrl).host }.getOrNull() ?: return null
        val normalizedBase = baseHost.removePrefix("www.")

        val json = loadAndExtract(context.applicationContext, tocUrl) ?: return null

        val chapters = parseJson(json, normalizedBase)
        if (chapters.isEmpty()) return null

        return ScrapeResult(title = baseHost, chapters = chapters)
    }

    private fun parseJson(json: String, normalizedBase: String): List<ScrapedChapter> {
        val arr = runCatching { JSONArray(json) }.getOrNull() ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            runCatching {
                val obj = arr.getJSONObject(i)
                val url = obj.optString("url").trimEnd('/')
                val text = obj.optString("text").trim()
                if (url.isBlank() || text.isBlank()) return@mapNotNull null
                val host = runCatching { URI(url).host }.getOrElse { return@mapNotNull null }
                if (host.removePrefix("www.") != normalizedBase) return@mapNotNull null
                val path = runCatching { URI(url).path.trim('/') }.getOrElse { "" }
                if (path.isEmpty()) return@mapNotNull null
                if (path.substringBefore('/').lowercase() in NAV_PATHS) return@mapNotNull null
                ScrapedChapter(id = TocScraper.urlToId(url), title = text, url = url)
            }.getOrNull()
        }.distinctBy { it.url }
    }

    /** Loads [url] in a hidden WebView, waits for JS to settle, then extracts chapter links. */
    private suspend fun loadAndExtract(context: Context, url: String): String? =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val webView = WebView(context)
                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true
                webView.settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Mobile Safari/537.36"

                var completed = false

                fun complete(result: String?) {
                    if (!completed) {
                        completed = true
                        mainHandler.post { webView.destroy() }
                        if (cont.isActive) cont.resume(result)
                    }
                }

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, pageUrl: String) {
                        // Poll every 2 s (up to 3 polls) — lets Cloudflare JS challenges resolve
                        // before we read the DOM. Each new onPageFinished resets the wait.
                        tryExtract(view, attemptsLeft = 3)
                    }

                    private fun tryExtract(view: WebView, attemptsLeft: Int) {
                        if (completed) return
                        mainHandler.postDelayed({
                            if (completed) return@postDelayed
                            view.evaluateJavascript(QUICK_CHECK_JS) { countStr ->
                                val count = countStr?.trim()?.toIntOrNull() ?: 0
                                if (count >= 2 || attemptsLeft <= 1) {
                                    // Found links, or out of retries — extract now
                                    view.evaluateJavascript(EXTRACT_JS) { json -> complete(json) }
                                } else {
                                    // Page not ready yet — try again after another 2 s
                                    tryExtract(view, attemptsLeft - 1)
                                }
                            }
                        }, 2_000)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView, request: WebResourceRequest
                    ) = false // let WebView follow all redirects itself
                }

                webView.loadUrl(url)

                // Hard cap — give up after 25 s
                mainHandler.postDelayed({ complete(null) }, 25_000)

                cont.invokeOnCancellation { complete(null) }
            }
        }

    /** Quick count of internal-looking anchor tags — used to detect when the real page has loaded. */
    private val QUICK_CHECK_JS =
        "(function(){ try { return document.querySelectorAll('a[href]').length; } catch(e){ return 0; } })()"

    /**
     * Evaluates each CSS selector in order; returns the first group with 2+ links as a JS array.
     * WebView serialises the returned array to a JSON string for us.
     */
    private val EXTRACT_JS = """
        (function() {
            try {
                var sel = [
                    '.entry-content a', '.post-content a',
                    'article a', '.toc a', 'main a', 'body a'
                ];
                for (var s = 0; s < sel.length; s++) {
                    var nodes = document.querySelectorAll(sel[s]);
                    var out = [];
                    for (var i = 0; i < nodes.length; i++) {
                        var a = nodes[i];
                        var t = (a.innerText || a.textContent || '').replace(/\s+/g, ' ').trim();
                        if (a.href && t.length > 0 && a.href.indexOf('http') === 0) {
                            out.push({url: a.href, text: t});
                        }
                    }
                    if (out.length >= 2) return out;
                }
            } catch(e) {}
            return [];
        })()
    """.trimIndent()
}
