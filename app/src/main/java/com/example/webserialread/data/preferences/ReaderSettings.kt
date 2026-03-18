package com.example.webserialread.data.preferences

enum class ReaderBackground(val label: String, val bgHex: String, val textHex: String) {
    WHITE("White",  "#FFFFFF", "#1A1A1A"),
    SEPIA("Sepia",  "#F5E6C8", "#3B2A1A"),
    DARK ("Dark",   "#1C1C1E", "#E5E5EA"),
    BLACK("Black",  "#000000", "#CCCCCC"),
}

enum class ReaderFont(val label: String, val css: String) {
    SERIF  ("Serif",      "Georgia, 'Times New Roman', serif"),
    SANS   ("Sans-serif", "Arial, Helvetica, sans-serif"),
    MONO   ("Monospace",  "'Courier New', Courier, monospace"),
}

data class ReaderSettings(
    val textSizeSp: Float = 18f,
    val font: ReaderFont = ReaderFont.SERIF,
    val background: ReaderBackground = ReaderBackground.WHITE
)
