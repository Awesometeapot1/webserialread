package com.example.webserialread.data.remote

import com.example.webserialread.data.remote.model.WpCategory
import com.example.webserialread.data.remote.model.WpPost
import com.example.webserialread.data.remote.model.WpSite
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WordPressApi {

    @GET("wp-json/")
    suspend fun getSiteInfo(): WpSite

    /** fields=null omits the _fields param entirely (needed for some sites that reject it) */
    @GET
    suspend fun getPosts(
        @Url url: String,
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("orderby") orderBy: String = "date",
        @Query("order") order: String = "asc",
        @Query("status") status: String = "publish",
        @Query("_fields") fields: String? = "id,title,link,date,status",
        @Query("categories") categoryId: Long? = null
    ): Response<List<WpPost>>

    @GET
    suspend fun getCategories(
        @Url url: String,
        @Query("slug") slug: String,
        @Query("_fields") fields: String = "id,slug"
    ): Response<List<WpCategory>>

    @GET
    suspend fun getPost(
        @Url url: String,
        @Query("_fields") fields: String? = "id,content"
    ): WpPost
}

fun categoriesApiUrl(siteUrl: String): String {
    val host = runCatching { java.net.URI(siteUrl).host }.getOrNull() ?: ""
    return if (host.endsWith(".wordpress.com")) {
        "https://public-api.wordpress.com/wp/v2/sites/$host/categories"
    } else {
        "$siteUrl/wp-json/wp/v2/categories"
    }
}

fun postsApiUrl(siteUrl: String): String {
    val host = runCatching { java.net.URI(siteUrl).host }.getOrNull() ?: ""
    return if (host.endsWith(".wordpress.com")) {
        "https://public-api.wordpress.com/wp/v2/sites/$host/posts"
    } else {
        "$siteUrl/wp-json/wp/v2/posts"
    }
}

fun postApiUrl(siteUrl: String, postId: Long): String {
    val host = runCatching { java.net.URI(siteUrl).host }.getOrNull() ?: ""
    return if (host.endsWith(".wordpress.com")) {
        "https://public-api.wordpress.com/wp/v2/sites/$host/posts/$postId"
    } else {
        "$siteUrl/wp-json/wp/v2/posts/$postId"
    }
}

fun createWordPressApi(baseUrl: String): WordPressApi {
    val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json, */*")
                .build()
            chain.proceed(request)
        }
        .build()
    return Retrofit.Builder()
        .baseUrl(url)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WordPressApi::class.java)
}
