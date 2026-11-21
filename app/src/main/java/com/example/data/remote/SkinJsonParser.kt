package com.example.data.remote

import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

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

    fun parseSkins(rawJson: String): List<SkinCatalogItem> =
        parseSkinsPage(rawJson).items

    fun parseSkinsPage(rawJson: String): ParsedSkinsPage {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return ParsedSkinsPage(emptyList())

        val parsed = JSONTokener(trimmed).nextValue()
        val array = extractArray(
            parsed,
            listOf("data", "skins", "results", "items", "records", "list")
        ) ?: return ParsedSkinsPage(emptyList())

        val items = ArrayList<SkinCatalogItem>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            parseSkinObject(obj, i)?.let { items.add(it) }
        }

        val rootObj = parsed as? JSONObject
        val metaObj = rootObj?.optJSONObject("meta")
            ?: rootObj?.optJSONObject("pagination")
            ?: rootObj?.optJSONObject("pageInfo")

        val totalCount = extractOptionalInt(rootObj, metaObj, listOf("total", "totalCount", "total_count", "count"))
        val totalPages = extractOptionalInt(rootObj, metaObj, listOf("totalPages", "total_pages", "pages", "last_page"))
        val hasNext = extractOptionalBoolean(rootObj, metaObj, listOf("hasNext", "has_next", "hasMore", "has_more"))
            ?: (rootObj?.optString("next", "")?.isNotBlank() == true)

        return ParsedSkinsPage(
            items = items.distinctBy { it.id },
            totalCount = totalCount,
            totalPages = totalPages,
            hasNextPage = hasNext
        )
    }

    fun parseHeroRefs(rawJson: String): List<ParsedHeroRef> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()

        val parsed = JSONTokener(trimmed).nextValue()
        val array = extractArray(
            parsed,
            listOf("data", "heroes", "results", "items", "records", "list")
        ) ?: return emptyList()

        val heroes = ArrayList<ParsedHeroRef>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val name = firstNonBlankString(obj, listOf("name", "hero_name", "heroName", "title")) ?: continue
            val slug = firstNonBlankString(obj, listOf("slug", "hero_slug", "id", "hero_id"))
                ?: name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
            if (slug.isBlank()) continue
            val role = firstNonBlankString(obj, listOf("role", "class", "lane")) ?: "Hero"
            heroes += ParsedHeroRef(slug = slug, name = name, role = role)
        }
        return heroes.distinctBy { it.slug }
    }

    fun parseHeroDetailSkins(
        rawJson: String,
        fallbackHeroName: String = "Hero",
        fallbackRole: String = "Fighter"
    ): List<SkinCatalogItem> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()

        val parsed = JSONTokener(trimmed).nextValue()
        val rootObj = parsed as? JSONObject ?: return emptyList()
        val heroContainer = rootObj.optJSONObject("data")
            ?: rootObj.optJSONObject("hero")
            ?: rootObj

        val heroName = firstNonBlankString(
            heroContainer,
            listOf("name", "hero_name", "heroName", "title")
        ) ?: fallbackHeroName
        val heroRole = firstNonBlankString(
            heroContainer,
            listOf("role", "hero_role", "class", "lane")
        ) ?: fallbackRole

        val skinsArray = heroContainer.optJSONArray("skins")
            ?: rootObj.optJSONArray("skins")
            ?: extractArray(parsed, listOf("skins", "data", "items"))
            ?: return emptyList()

        val items = ArrayList<SkinCatalogItem>(skinsArray.length())
        for (i in 0 until skinsArray.length()) {
            val obj = skinsArray.optJSONObject(i) ?: continue
            parseSkinObject(
                obj = obj,
                index = i,
                defaultHeroName = heroName,
                defaultRole = heroRole
            )?.let { items.add(it) }
        }
        return items.distinctBy { it.id }
    }

    private fun extractArray(parsed: Any?, candidateKeys: List<String>): JSONArray? {
        return when (parsed) {
            is JSONArray -> parsed
            is JSONObject -> {
                for (key in candidateKeys) {
                    val arr = parsed.optJSONArray(key)
                    if (arr != null) return arr
                }
                val dataObj = parsed.optJSONObject("data")
                if (dataObj != null) {
                    for (key in candidateKeys) {
                        val arr = dataObj.optJSONArray(key)
                        if (arr != null) return arr
                    }
                }
                null
            }
            else -> null
        }
    }

    private fun parseSkinObject(
        obj: JSONObject,
        index: Int,
        defaultHeroName: String = "Legend",
        defaultRole: String = "Fighter"
    ): SkinCatalogItem? {
        val skinName = firstNonBlankString(obj, listOf("name", "skin_name", "skinName", "title", "skin"))
            ?: return null

        val heroObj = obj.optJSONObject("hero")
        val heroName = firstNonBlankString(obj, listOf("hero_name", "heroName", "hero", "character", "champion"))
            ?: heroObj?.let { firstNonBlankString(it, listOf("name", "hero_name", "title")) }
            ?: defaultHeroName

        val role = firstNonBlankString(obj, listOf("role", "hero_role", "heroRole", "class", "lane"))
            ?: heroObj?.let { firstNonBlankString(it, listOf("role", "class")) }
            ?: defaultRole

        val tierObj = obj.optJSONObject("tier") ?: obj.optJSONObject("rarity") ?: obj.optJSONObject("series")
        val rawTier = firstNonBlankString(
            obj,
            listOf(
                "tier", "rarity", "skin_tier", "skinTier", "series",
                "grade", "tag", "category", "label", "type", "badge"
            )
        ) ?: tierObj?.let { firstNonBlankString(it, listOf("name", "title", "label")) }
            ?: "Common"

        val explicitId = firstNonBlankString(obj, listOf("id", "skin_id", "skinId", "_id", "slug"))
        val stableId = explicitId ?: "${heroName}_${skinName}_$index"
            .lowercase()
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')

        val imageObj = obj.optJSONObject("images") ?: obj.optJSONObject("media")
        val rawImageUrl = firstNonBlankString(
            obj,
            listOf(
                "image", "image_url", "imageUrl", "splash", "splash_url", "splashUrl",
                "portrait", "artwork", "img", "icon", "thumbnail", "cover", "poster"
            )
        ) ?: imageObj?.let {
            firstNonBlankString(it, listOf("splash", "portrait", "full", "url", "image", "thumbnail"))
        } ?: ""

        val normalizedUrl = normalizeImageUrl(rawImageUrl)
        val rarity = SkinRarity.fromApiTier(rawTier, skinName)

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

    private fun extractOptionalInt(root: JSONObject?, meta: JSONObject?, keys: List<String>): Int? {
        for (source in listOfNotNull(meta, root)) {
            for (k in keys) {
                if (source.has(k) && !source.isNull(k)) {
                    val v = source.optInt(k, -1)
                    if (v >= 0) return v
                }
            }
        }
        return null
    }

    private fun extractOptionalBoolean(root: JSONObject?, meta: JSONObject?, keys: List<String>): Boolean? {
        for (source in listOfNotNull(meta, root)) {
            for (k in keys) {
                if (source.has(k) && !source.isNull(k)) {
                    return source.optBoolean(k)
                }
            }
        }
        return null
    }

    private fun firstNonBlankString(obj: JSONObject, keys: List<String>): String? {
        for (key in keys) {
            if (!obj.has(key) || obj.isNull(key)) continue
            val value = obj.opt(key)
            if (value is String && value.isNotBlank() && !value.equals("null", ignoreCase = true)) {
                return value.trim()
            } else if (value is Number) {
                return value.toString()
            }
        }
        return null
    }

    private fun normalizeImageUrl(raw: String): String {
        val trimmed = raw.trim()
        return when {
            trimmed.isEmpty() -> ""
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("//") -> "https:$trimmed"
            trimmed.startsWith("/") -> "$BASE_DOMAIN$trimmed"
            else -> "$BASE_DOMAIN/$trimmed"
        }
    }
}
