package com.example.webserialread.ui.screen

import android.graphics.Color
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webserialread.data.preferences.ReaderSettings
import com.example.webserialread.ui.viewmodel.ReaderViewModel
import com.example.webserialread.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterId: Long,
    onBack: () -> Unit,
    onNavigateToChapter: (Long) -> Unit,
    vm: ReaderViewModel = viewModel(),
    settingsVm: SettingsViewModel = viewModel()
) {
    LaunchedEffect(chapterId) { vm.loadChapter(chapterId) }

    val chapter by vm.chapter.collectAsStateWithLifecycle()
    val htmlContent by vm.htmlContent.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val prevId by vm.prevChapterId.collectAsStateWithLifecycle()
    val nextId by vm.nextChapterId.collectAsStateWithLifecycle()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        chapter?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier.height(56.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { prevId?.let { onNavigateToChapter(it) } },
                        enabled = prevId != null
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Previous")
                    }
                    TextButton(
                        onClick = { nextId?.let { onNavigateToChapter(it) } },
                        enabled = nextId != null
                    ) {
                        Text("Next")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                error != null -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Failed to load chapter", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { vm.loadChapter(chapterId) }) { Text("Retry") }
                }

                htmlContent != null -> ReaderWebView(
                    html = htmlContent!!,
                    baseUrl = chapter?.url,
                    readerSettings = settings
                )
            }
        }
    }
}

@Composable
private fun ReaderWebView(html: String, baseUrl: String?, readerSettings: ReaderSettings) {
    val bgColor     = readerSettings.background.bgHex
    val textColor   = readerSettings.background.textHex
    val fontFamily  = readerSettings.font.css
    val fontSize    = readerSettings.textSizeSp.toInt()
    val webBgColor  = Color.parseColor(bgColor)

    // Derive link/quote/border from whether background is dark
    val isDark = bgColor == "#1C1C1E" || bgColor == "#000000"
    val linkColor   = if (isDark) "#64b5f6" else "#1565c0"
    val quoteColor  = if (isDark) "#999999" else "#555555"
    val borderColor = if (isDark) "#3a3a3c" else "#cccccc"

    val css = """
        body {
            background-color: $bgColor;
            color: $textColor;
            font-family: $fontFamily;
            font-size: ${fontSize}px;
            line-height: 1.85;
            max-width: 700px;
            margin: 0 auto;
            padding: 16px 20px 40px;
            word-break: break-word;
        }
        a { color: $linkColor; }
        img { max-width: 100%; height: auto; display: block; margin: 12px auto; }
        p { margin: 0 0 1.2em; }
        hr { border-color: $borderColor; }
        blockquote {
            border-left: 3px solid $borderColor;
            margin: 1em 0;
            padding-left: 1em;
            color: $quoteColor;
        }
    """.trimIndent()

    val fullHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>$css</style>
        </head>
        <body>$html</body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(webBgColor)
                settings.javaScriptEnabled = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.setBackgroundColor(webBgColor)
            webView.loadDataWithBaseURL(baseUrl, fullHtml, "text/html", "UTF-8", null)
        },
        modifier = Modifier.fillMaxSize()
    )
}
