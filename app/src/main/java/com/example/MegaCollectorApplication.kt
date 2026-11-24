package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class MegaCollectorApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val httpCacheDir = File(cacheDir, "mlbb_http_cache").apply { mkdirs() }
        val coilDiskCacheDir = File(cacheDir, "mlbb_card_art_cache").apply { mkdirs() }

        val dispatcher = Dispatcher().apply {
            maxRequests = 32
            maxRequestsPerHost = 16
        }

        val okHttpClient = OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .cache(Cache(httpCacheDir, 150L * 1024L * 1024L))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                val existingCacheControl = response.header("Cache-Control")
                if (existingCacheControl.isNullOrBlank() || existingCacheControl.contains("no-store", ignoreCase = true)) {
                    response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=604800")
                        .build()
                } else {
                    response
                }
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(coilDiskCacheDir)
                    .maxSizeBytes(220L * 1024L * 1024L)
                    .build()
            }
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(100)
            .build()
    }
}
