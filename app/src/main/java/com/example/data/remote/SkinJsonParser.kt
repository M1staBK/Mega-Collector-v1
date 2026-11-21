package com.example.data.remote

import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

object SkinJsonParser {
    private const val BASE_DOMAIN = "https://mlbbdex.com"

    fun parseSkins(rawJson: String): List<SkinCatalogItem> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()

        val parsed = JSONTokener(trimmed).nextValue()
        val array = extractSkinsArray(parsed) ?: return emptyList()

        val items = ArrayList<SkinCatalogItem>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            parseSkinObject(obj, i)?.let { items.add(it) }
        }
        return items.distinctBy { it.id }
    }

    private fun extractSkinsArray(parsed: Any?): JSONArray? {
        return when (parsed) {
            is JSONArray -> parsed
            is JSONObject -> {
                val candidateKeys = listOf("data", "skins", "results", "items", "records", "list")
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

    private fun parseSkinObject(obj: JSONObject, index: Int): SkinCatalogItem? {
        val skinName = firstNonBlankString(obj, listOf("name", "skin_name", "skinName", "title", "skin"))
            ?: return null

        val heroObj = obj.optJSONObject("hero")
        val heroName = firstNonBlankString(obj, listOf("hero_name", "heroName", "hero", "character", "champion"))
            ?: heroObj?.let { firstNonBlankString(it, listOf("name", "hero_name", "title")) }
            ?: "Legend"

        val role = firstNonBlankString(obj, listOf("role", "hero_role", "heroRole", "class", "lane"))
            ?: heroObj?.let { firstNonBlankString(it, listOf("role", "class")) }
            ?: "Fighter"

        val tierObj = obj.optJSONObject("tier") ?: obj.optJSONObject("rarity")
        val rawTier = firstNonBlankString(
            obj,
            listOf("tier", "rarity", "skin_tier", "skinTier", "grade", "tag", "category", "type")
        ) ?: tierObj?.let { firstNonBlankString(it, listOf("name", "title", "label")) }
            ?: "Common"

        val rawId = firstNonBlankString(obj, listOf("id", "skin_id", "skinId", "_id", "slug"))
            ?: "${heroName}_${skinName}_$index".lowercase().replace(Regex("[^a-z0-9_]"), "_")

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
            id = rawId,
            skinName = skinName,
            heroName = heroName,
            role = role,
            rawTier = rawTier,
            rarity = rarity,
            imageUrl = normalizedUrl
        )
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
