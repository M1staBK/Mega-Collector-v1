package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.model.HeroMetaDetail
import com.example.model.MetaHeroStats
import com.example.model.MetaRankFilter
import com.example.model.RoneEndpointDiscovery
import com.example.model.SkinCatalogItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.io.File
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

interface RoneArenaApi {
    @GET("api/openapi.json")
    suspend fun getOpenApiSpec(): ResponseBody

    @GET
    suspend fun fetchRelative(
        @Url pathOrUrl: String,
        @Query("rank") rank: String? = null,
        @Query("limit") limit: Int? = null
    ): ResponseBody
}

class RoneArenaRepository(private val context: Context) {
    private val baseUrl = "https://arena.rone.dev/"
    private val cacheDir = File(context.cacheDir, "rone_meta").apply { mkdirs() }
    private val specCacheFile = File(cacheDir, "openapi.json")

    private val api: RoneArenaApi by lazy {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "MegaCollectorAndroid/3.0")
                    .build()
                chain.proceed(req)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttp)
            .build()
            .create(RoneArenaApi::class.java)
    }

    private suspend fun getDiscovery(): RoneEndpointDiscovery = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cachedValid = specCacheFile.exists() && (now - specCacheFile.lastModified()) < 86_400_000L
        val rawSpec = if (cachedValid) {
            runCatching { specCacheFile.readText() }.getOrNull()
        } else {
            runCatching { api.getOpenApiSpec().string() }
                .onSuccess { spec ->
                    if (spec.isNotBlank()) runCatching { specCacheFile.writeText(spec) }
                }
                .getOrNull()
                ?: runCatching { if (specCacheFile.exists()) specCacheFile.readText() else null }.getOrNull()
        }
        RoneArenaParser.discoverEndpoints(rawSpec.orEmpty())
    }

    suspend fun fetchMetaHeroStats(
        rankFilter: MetaRankFilter,
        catalog: List<SkinCatalogItem>,
        forceRefresh: Boolean = false
    ): Result<List<MetaHeroStats>> = withContext(Dispatchers.IO) {
        val rankCacheFile = File(cacheDir, "hero_rank_${rankFilter.apiParam}.json")
        val now = System.currentTimeMillis()
        val catalogByHero = buildHeroThumbLookup(catalog)

        if (!forceRefresh && rankCacheFile.exists() && (now - rankCacheFile.lastModified()) < 3_600_000L) {
            val cachedRaw = runCatching { rankCacheFile.readText() }.getOrNull()
            if (!cachedRaw.isNullOrBlank()) {
                val parsed = RoneArenaParser.parseHeroRankList(cachedRaw, rankFilter)
                if (parsed.isNotEmpty()) {
                    return@withContext Result.success(enrichIcons(parsed, catalogByHero))
                }
            }
        }

        try {
            val discovery = getDiscovery()
            val candidatePaths = (discovery.heroRankPaths + discovery.heroListPaths).distinct()

            var bestHeroes: List<MetaHeroStats> = emptyList()
            var rawWinner = ""

            for (path in candidatePaths) {
                val body = runCatching {
                    api.fetchRelative(
                        pathOrUrl = path,
                        rank = if (rankFilter == MetaRankFilter.ALL) null else rankFilter.apiParam,
                        limit = 200
                    ).string()
                }.getOrNull() ?: continue

                val parsed = RoneArenaParser.parseHeroRankList(body, rankFilter)
                if (parsed.isNotEmpty()) {
                    val hasRealAnalytics = parsed.any { it.pickRatePct > 0.0 || it.banRatePct > 0.0 }
                    if (hasRealAnalytics || parsed.size > bestHeroes.size) {
                        bestHeroes = parsed
                        rawWinner = body
                        if (hasRealAnalytics) break
                    }
                }
            }

            if (bestHeroes.isNotEmpty()) {
                if (rawWinner.isNotBlank()) {
                    runCatching { rankCacheFile.writeText(rawWinner) }
                }
                return@withContext Result.success(enrichIcons(bestHeroes, catalogByHero))
            }

            // Fallback to stale cache if offline
            if (rankCacheFile.exists()) {
                val stale = runCatching { rankCacheFile.readText() }.getOrNull()
                if (!stale.isNullOrBlank()) {
                    val parsed = RoneArenaParser.parseHeroRankList(stale, rankFilter)
                    if (parsed.isNotEmpty()) {
                        return@withContext Result.success(enrichIcons(parsed, catalogByHero))
                    }
                }
            }

            Result.failure(IllegalStateException("Could not load live hero analytics from arena.rone.dev."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("RoneArenaRepo", "Meta fetch error: ${e.message}")
            Result.failure(IllegalStateException("Unable to reach arena.rone.dev right now."))
        }
    }

    suspend fun fetchHeroMetaDetail(
        hero: MetaHeroStats,
        catalog: List<SkinCatalogItem>
    ): Result<HeroMetaDetail> = withContext(Dispatchers.IO) {
        try {
            val discovery = getDiscovery()
            val catalogByHero = buildHeroThumbLookup(catalog)
            val heroTokens = listOf(hero.heroId, hero.slug, hero.heroName)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            coroutineScope {
                val detailDeferred = async {
                    fetchFirstMatching(discovery.heroDetailTemplates, heroTokens)
                }
                val countersDeferred = async {
                    fetchFirstMatching(discovery.heroCounterTemplates, heroTokens)
                }
                val buildsDeferred = async {
                    fetchFirstMatching(discovery.heroBuildTemplates, heroTokens)
                }

                val detail = RoneArenaParser.mergeDetailPayloads(
                    baseHero = hero,
                    detailJson = detailDeferred.await(),
                    countersJson = countersDeferred.await(),
                    buildsJson = buildsDeferred.await()
                )

                val enrichedDetail = detail.copy(
                    heroStats = detail.heroStats.copy(
                        iconUrl = detail.heroStats.iconUrl.ifBlank {
                            catalogByHero[hero.heroName.lowercase(Locale.US)].orEmpty()
                        }
                    ),
                    strongAgainst = detail.strongAgainst.map { m ->
                        m.copy(iconUrl = m.iconUrl.ifBlank { catalogByHero[m.heroName.lowercase(Locale.US)].orEmpty() })
                    },
                    counteredBy = detail.counteredBy.map { m ->
                        m.copy(iconUrl = m.iconUrl.ifBlank { catalogByHero[m.heroName.lowercase(Locale.US)].orEmpty() })
                    },
                    synergies = detail.synergies.map { m ->
                        m.copy(iconUrl = m.iconUrl.ifBlank { catalogByHero[m.heroName.lowercase(Locale.US)].orEmpty() })
                    }
                )
                Result.success(enrichedDetail)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("RoneArenaRepo", "Detail fetch failed: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun fetchFirstMatching(
        templates: List<String>,
        tokens: List<String>
    ): String? {
        for (template in templates) {
            for (token in tokens) {
                val encoded = URLEncoder.encode(token, "UTF-8").replace("+", "%20")
                val resolvedPath = template
                    .replace(Regex("\\{[^}]+\\}"), encoded)
                val raw = runCatching { api.fetchRelative(resolvedPath).string() }.getOrNull()
                if (!raw.isNullOrBlank() && raw.contains("{")) {
                    return raw
                }
            }
        }
        return null
    }

    private fun buildHeroThumbLookup(catalog: List<SkinCatalogItem>): Map<String, String> {
        val map = HashMap<String, String>()
        for (item in catalog) {
            if (item.imageUrl.isNotBlank() && item.heroName.isNotBlank()) {
                val key = item.heroName.trim().lowercase(Locale.US)
                if (!map.containsKey(key)) {
                    map[key] = item.imageUrl
                }
            }
        }
        return map
    }

    private fun enrichIcons(
        heroes: List<MetaHeroStats>,
        catalogByHero: Map<String, String>
    ): List<MetaHeroStats> = heroes.map { h ->
        if (h.iconUrl.isNotBlank()) h
        else h.copy(iconUrl = catalogByHero[h.heroName.lowercase(Locale.US)].orEmpty())
    }
}
