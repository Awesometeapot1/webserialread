package com.example.webserialread.data.remote.model

data class WpPost(
    val id: Long,
    val title: WpRendered,
    val content: WpRendered? = null,  // null when not requested via _fields
    val link: String,
    val date: String,
    val status: String = "publish"
)

data class WpRendered(val rendered: String)
