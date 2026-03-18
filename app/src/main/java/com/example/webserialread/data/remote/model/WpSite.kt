package com.example.webserialread.data.remote.model

import com.google.gson.annotations.SerializedName

data class WpSite(
    val name: String,
    val description: String,
    val url: String,
    @SerializedName("site_icon_url") val siteIconUrl: String? = null
)
