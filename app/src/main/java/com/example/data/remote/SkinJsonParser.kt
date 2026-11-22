package com.example.data.remote

import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.squareup.moshi.Moshi
import java.io.IOException

data class ParsedSkinsPage(
    val items: List<SkinCatalogItem>,
    val totalCount: Int? = null,
    val totalPages: Int? = null,
    val hasNextPage: Boolean? = null
)

data class ParsedHeroRef(
    val slug: String,
    val name: String,
    val role: String
)

object SkinJsonParser {
    private const val BASE_DOMAIN = "https://mlbbdex.com"

    private val moshi: Moshi = Moshi.Builder().build()
    private val anyAdapter = moshi.adapter(Any::class.java)

    private fun parseRoot(rawJson: String): Any? {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return null
        return try {
            anyAdapter.fromJson(trimmed)
        } catch (_: IOException) {
            null
        } catch (_: RuntimeException) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Any?.asJsonMap(): Map<String, Any?>? =
        (this as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v }

    private fun Any?.asJsonList(): List<Any?>? = this as? List<*>

    fun parseSkins(rawJson: String): List<SkinCatalogItem> =
        parseSkinsPage(rawJson).items

    fun parseSkinsPage(rawJson: String): ParsedSkinsPage {
        val parsed = parseRoot(rawJson) ?: return ParsedSkinsPage(emptyList())
        val array = extractArray(
            parsed,
            listOf("data", "skins", "results", "items", "records", "list")
        ) ?: return ParsedSkinsPage(emptyList())

        val items = ArrayList<SkinCatalogItem>(array.size)
        for (i in array.indices) {
            val obj = array[i].asJsonMap() ?: continue
            parseSkinObject(obj, i)?.let { items.add(it) }
        }

        val rootObj = parsed.asJsonMap()
        val metaObj = rootObj?.get("meta").asJsonMap()
            ?: rootObj?.get("pagination").asJsonMap()
            ?: rootObj?.get("pageInfo").asJsonMap()

        val totalCount = extractOptionalInt(rootObj, metaObj, listOf("total", "totalCount", "total_count", "count"))
        val totalPages = extractOptionalInt(rootObj, metaObj, listOf("totalPages", "total_pages", "pages", "last_page"))
        val hasNext = extractOptionalBoolean(rootObj, metaObj, listOf("hasNext", "has_next", "hasMore", "has_more"))
            ?: ((rootObj?.get("next") as? String)?.isNotBlank() == true)

        return ParsedSkinsPage(
            items = items.distinctBy { it.id },
            totalCount = totalCount,
            totalPages = totalPages,
            hasNextPage = hasNext
        )
    }

    fun parseHeroRefs(rawJson: String): List<ParsedHeroRef> {
        val parsed = parseRoot(rawJson) ?: return emptyList()
        val array = extractArray(
            parsed,
            listOf("data", "heroes", "results", "items", "records", "list")
        ) ?: return emptyList()

        val heroes = ArrayList<ParsedHeroRef>(array.size)
        for (i in array.indices) {
            val obj = array[i].asJsonMap() ?: continue
            val name = firstNonBlankString(obj, listOf("name", "hero_name", "heroName", "title")) ?: continue
            val slug = firstNonBlankString(obj, listOf("slug", "hero_slug", "id", "hero_id"))
                ?: name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            if (slug.isBlank()) continue
            val role = extractRole(obj) ?: "Hero"
            heroes += ParsedHeroRef(slug = slug, name = name, role = role)
        }
        return heroes.distinctBy { it.slug }
    }

    fun parseHeroDetailSkins(
        rawJson: String,
        fallbackHeroName: String = "Hero",
        fallbackRole: String = "Fighter"
    ): List<SkinCatalogItem> {
        val parsed = parseRoot(rawJson) ?: return emptyList()
        val rootObj = parsed.asJsonMap() ?: return emptyList()
        val heroContainer = rootObj["data"].asJsonMap()
            ?: rootObj["hero"].asJsonMap()
            ?: rootObj

        val heroName = firstNonBlankString(
            heroContainer,
            listOf("name", "hero_name", "heroName", "title")
        ) ?: fallbackHeroName
        val heroRole = extractRole(heroContainer) ?: fallbackRole

        // Extract hero-level artwork maps:
        // 1) data.illustrations[skinName] -> "/visuels/heros/.../illustrations/..."
        // 2) data.images.skins[skinId] -> "/visuels/heros/.../skins/..."
        // 3) data.images.portrait -> "/visuels/heros/.../portrait.webp"
        val illustrationsByName = LinkedHashMap<String, String>()
        heroContainer["illustrations"].asJsonMap()?.forEach { (key, value) ->
            val rawUrl = (value as? String)?.trim().orEmpty()
            if (rawUrl.isNotEmpty() && !rawUrl.equals("null", ignoreCase = true)) {
                illustrationsByName[key.trim().lowercase()] = rawUrl
            }
        }

        val imagesObj = heroContainer["images"].asJsonMap()
        val skinImagesById = LinkedHashMap<String, String>()
        imagesObj?.get("skins").asJsonMap()?.forEach { (key, value) ->
            val rawUrl = (value as? String)?.trim().orEmpty()
            if (rawUrl.isNotEmpty() && !rawUrl.equals("null", ignoreCase = true)) {
                skinImagesById[key.trim()] = rawUrl
            }
        }
        val portraitFallback = imagesObj?.let {
            firstNonBlankString(it, listOf("portrait", "icon", "cover"))
        } ?: ""

        val skinsArray = heroContainer["skins"].asJsonList()
            ?: rootObj["skins"].asJsonList()
            ?: extractArray(parsed, listOf("skins", "data", "items"))
            ?: return emptyList()

        val items = ArrayList<SkinCatalogItem>(skinsArray.size)
        for (i in skinsArray.indices) {
            val obj = skinsArray[i].asJsonMap() ?: continue
            parseSkinObject(
                obj = obj,
                index = i,
                defaultHeroName = heroName,
                defaultRole = heroRole,
                illustrationsByName = illustrationsByName,
                skinImagesById = skinImagesById,
                heroPortraitFallback = portraitFallback
            )?.let { items.add(it) }
        }
        return items.distinctBy { it.id }
    }

    private fun extractRole(obj: Map<String, Any?>): String? {
        firstNonBlankString(obj, listOf("role", "hero_role", "heroRole", "class", "lane"))?.let { return it }
        val rolesArr = obj["roles"].asJsonList()
        if (!rolesArr.isNullOrEmpty()) {
            val joined = rolesArr
                .mapNotNull { (it as? String)?.trim()?.takeIf { s -> s.isNotBlank() && s != "null" } }
                .joinToString(" / ")
            if (joined.isNotBlank()) return joined
        }
        return null
    }

    private fun extractArray(parsed: Any?, candidateKeys: List<String>): List<Any?>? {
        parsed.asJsonList()?.let { return it }
        val obj = parsed.asJsonMap() ?: return null
        for (key in candidateKeys) {
            obj[key].asJsonList()?.let { return it }
        }
        val dataObj = obj["data"].asJsonMap()
        if (dataObj != null) {
            for (key in candidateKeys) {
                dataObj[key].asJsonList()?.let { return it }
            }
        }
        return null
    }

    private fun parseSkinObject(
        obj: Map<String, Any?>,
        index: Int,
        defaultHeroName: String = "Legend",
        defaultRole: String = "Fighter",
        illustrationsByName: Map<String, String> = emptyMap(),
        skinImagesById: Map<String, String> = emptyMap(),
        heroPortraitFallback: String = ""
    ): SkinCatalogItem? {
        val skinName = firstNonBlankString(obj, listOf("name", "skin_name", "skinName", "title", "skin"))
            ?: return null

        val heroObj = obj["hero"].asJsonMap()
        val rawHeroName = firstNonBlankString(obj, listOf("hero_name", "heroName", "hero", "character", "champion"))
            ?: heroObj?.let { firstNonBlankString(it, listOf("name", "hero_name", "title")) }
            ?: defaultHeroName
        val heroName = rawHeroName.replaceFirstChar { it.uppercase() }

        val role = extractRole(obj)
            ?: heroObj?.let { extractRole(it) }
            ?: defaultRole

        val tierObj = obj["tier"].asJsonMap() ?: obj["rarity"].asJsonMap() ?: obj["series"].asJsonMap()
        // Prefer specific series/label/tier over broad rarity tag so "Luckybox", "Elite", "StarLight" classify accurately!
        val specificLabel = firstNonBlankString(
            obj,
            listOf("series", "label", "tier", "skin_tier", "skinTier", "grade", "tag", "category", "type", "badge")
        )
        val broadRarity = firstNonBlankString(obj, listOf("rarity"))
            ?: tierObj?.let { firstNonBlankString(it, listOf("name", "title", "label")) }
        val rawTier = specificLabel ?: broadRarity ?: "Common"

        val explicitId = firstNonBlankString(obj, listOf("id", "skin_id", "skinId", "_id", "slug"))
        val stableId = explicitId ?: "${heroName}_${skinName}_$index"
            .lowercase()
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')

        val imageObj = obj["images"].asJsonMap() ?: obj["media"].asJsonMap()
        val directImage = firstNonBlankString(
            obj,
            listOf(
                "image", "image_url", "imageUrl", "splash", "splash_url", "splashUrl",
                "portrait", "artwork", "img", "icon", "thumbnail", "cover", "poster"
            )
        ) ?: imageObj?.let {
            firstNonBlankString(it, listOf("splash", "portrait", "full", "url", "image", "thumbnail"))
        }

        val illustrationHit = illustrationsByName[skinName.trim().lowercase()]
        val skinImageHit = explicitId?.let { skinImagesById[it] } ?: skinImagesById[stableId]
        val rawImageUrl = directImage ?: illustrationHit ?: skinImageHit ?: heroPortraitFallback

        val normalizedUrl = normalizeImageUrl(rawImageUrl)
        // Check combined tier & broadRarity & skinName for accurate classification
        val rarity = SkinRarity.fromApiTier("$rawTier ${broadRarity.orEmpty()}", skinName)

        return SkinCatalogItem(
            id = stableId,
            skinName = skinName,
            heroName = heroName,
            role = role,
            rawTier = rawTier,
            rarity = rarity,
            imageUrl = normalizedUrl
        )
    }

    private fun extractOptionalInt(
        root: Map<String, Any?>?,
        meta: Map<String, Any?>?,
        keys: List<String>
    ): Int? {
        for (source in listOfNotNull(meta, root)) {
            for (k in keys) {
                val value = source[k]
                if (value is Number) {
                    val v = value.toInt()
                    if (v >= 0) return v
                } else if (value is String) {
                    val v = value.toIntOrNull()
                    if (v != null && v >= 0) return v
                }
            }
        }
        return null
    }

    private fun extractOptionalBoolean(
        root: Map<String, Any?>?,
        meta: Map<String, Any?>?,
        keys: List<String>
    ): Boolean? {
        for (source in listOfNotNull(meta, root)) {
            for (k in keys) {
                val value = source[k]
                if (value is Boolean) return value
                if (value is String) {
                    if (value.equals("true", ignoreCase = true)) return true
                    if (value.equals("false", ignoreCase = true)) return false
                }
            }
        }
        return null
    }

    private fun firstNonBlankString(obj: Map<String, Any?>, keys: List<String>): String? {
        for (key in keys) {
            val value = obj[key] ?: continue
            when (value) {
                is String -> {
                    if (value.isNotBlank() && !value.equals("null", ignoreCase = true)) {
                        return value.trim()
                    }
                }
                is Number -> {
                    val asDouble = value.toDouble()
                    val asLong = value.toLong()
                    return if (asDouble == asLong.toDouble()) asLong.toString() else value.toString()
                }
            }
        }
        return null
    }

    private fun normalizeImageUrl(raw: String): String {
        val trimmed = raw.trim().replace(" ", "%20")
        return when {
            trimmed.isEmpty() -> ""
            trimmed.startsWith("http://") -> trimmed.replaceFirst("http://", "https://")
            trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("//") -> "https:$trimmed"
            trimmed.startsWith("/") -> "$BASE_DOMAIN$trimmed"
            else -> "$BASE_DOMAIN/$trimmed"
        }
    }
}
