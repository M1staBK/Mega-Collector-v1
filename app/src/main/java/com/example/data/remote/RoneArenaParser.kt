package com.example.data.remote

import com.example.model.HeroBuildGuide
import com.example.model.HeroBuildItem
import com.example.model.HeroCounterMatchup
import com.example.model.HeroMetaDetail
import com.example.model.MetaHeroStats
import com.example.model.MetaRankFilter
import com.example.model.MetaTierGrade
import com.example.model.RoneEndpointDiscovery
import com.squareup.moshi.Moshi
import java.io.IOException
import java.util.Locale

object RoneArenaParser {
    private const val BASE_DOMAIN = "https://arena.rone.dev"

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
    private fun Any?.asMap(): Map<String, Any?>? =
        (this as? Map<*, *>)?.entries?.associate { (k, v) -> k.toString() to v }

    private fun Any?.asList(): List<Any?>? = this as? List<*>

    fun discoverEndpoints(openApiJson: String): RoneEndpointDiscovery {
        val root = parseRoot(openApiJson).asMap()
        val pathsMap = root?.get("paths").asMap()
        val rawPaths = pathsMap?.keys?.toList().orEmpty()

        val rankPaths = mutableListOf<String>()
        val listPaths = mutableListOf<String>()
        val detailTemplates = mutableListOf<String>()
        val counterTemplates = mutableListOf<String>()
        val buildTemplates = mutableListOf<String>()

        for (raw in rawPaths) {
            val path = raw.trimStart('/')
            val lower = path.lowercase(Locale.US)
            val hasParam = "{" in path
            when {
                hasParam && ("counter" in lower || "matchup" in lower || "relation" in lower) ->
                    counterTemplates += path
                hasParam && ("build" in lower || "equipment" in lower || "item" in lower || "guide" in lower) ->
                    buildTemplates += path
                hasParam && ("hero" in lower) && !("skin" in lower) ->
                    detailTemplates += path
                !hasParam && ("rank" in lower || "meta" in lower || "tier" in lower || "win" in lower) ->
                    rankPaths += path
                !hasParam && ("heroes" in lower) ->
                    listPaths += path
            }
        }

        // Prioritize academy/rank endpoints then known defaults
        val orderedRank = (rankPaths.sortedByDescending {
            when {
                "hero-rank" in it.lowercase(Locale.US) -> 3
                "academy" in it.lowercase(Locale.US) -> 2
                else -> 1
            }
        } + listOf(
            "api/v1/academy/hero-rank",
            "api/v1/heroes/rank",
            "api/v1/meta/heroes",
            "api/v1/heroes"
        )).distinct()

        val orderedList = (listPaths + listOf(
            "api/v1/heroes",
            "api/v1/academy/heroes"
        )).distinct()

        val orderedDetail = (detailTemplates + listOf(
            "api/v1/heroes/{hero_id_or_name}",
            "api/v1/academy/heroes/{hero_id_or_name}"
        )).distinct()

        val orderedCounter = (counterTemplates + listOf(
            "api/v1/heroes/{hero_id_or_name}/counters",
            "api/v1/academy/heroes/{hero_id_or_name}/counters",
            "api/v1/heroes/{hero_id_or_name}/matchups"
        )).distinct()

        val orderedBuild = (buildTemplates + listOf(
            "api/v1/heroes/{hero_id_or_name}/builds",
            "api/v1/academy/heroes/{hero_id_or_name}/builds",
            "api/v1/heroes/{hero_id_or_name}/guides"
        )).distinct()

        return RoneEndpointDiscovery(
            heroRankPaths = orderedRank,
            heroListPaths = orderedList,
            heroDetailTemplates = orderedDetail,
            heroCounterTemplates = orderedCounter,
            heroBuildTemplates = orderedBuild
        )
    }

    fun parseHeroRankList(
        rawJson: String,
        rankFilter: MetaRankFilter = MetaRankFilter.ALL
    ): List<MetaHeroStats> {
        val root = parseRoot(rawJson) ?: return emptyList()
        val array = extractRecordArray(root) ?: return emptyList()

        val out = ArrayList<MetaHeroStats>(array.size)
        for ((index, entry) in array.withIndex()) {
            val obj = entry.asMap() ?: continue
            parseHeroEntry(obj, index, rankFilter)?.let { out += it }
        }
        return out.distinctBy { it.heroName.lowercase(Locale.US) }
    }

    private fun extractRecordArray(node: Any?): List<Any?>? {
        node.asList()?.let { if (it.isNotEmpty()) return it }
        val map = node.asMap() ?: return null
        val candidateKeys = listOf(
            "records", "heroes", "data", "items", "list", "results", "ranking", "rankings"
        )
        for (key in candidateKeys) {
            val child = map[key]
            child.asList()?.let { if (it.isNotEmpty()) return it }
            val childMap = child.asMap()
            if (childMap != null) {
                for (nestedKey in candidateKeys) {
                    childMap[nestedKey].asList()?.let { if (it.isNotEmpty()) return it }
                }
            }
        }
        return null
    }

    private fun parseHeroEntry(
        obj: Map<String, Any?>,
        index: Int,
        rankFilter: MetaRankFilter
    ): MetaHeroStats? {
        val mainHero = obj["main_hero"].asMap()
        val nestedHero = obj["hero"].asMap()
        val dataObj = mainHero?.get("data").asMap() ?: obj["data"].asMap()

        val heroName = firstNonBlank(
            obj.str("hero_name"),
            obj.str("name"),
            obj.str("heroName"),
            nestedHero?.str("name"),
            nestedHero?.str("hero_name"),
            mainHero?.str("name"),
            dataObj?.str("name"),
            dataObj?.str("hero_name")
        )
        if (heroName.isBlank()) return null

        val heroId = firstNonBlank(
            obj.str("hero_id"),
            obj.str("id"),
            obj.str("heroid"),
            nestedHero?.str("id"),
            nestedHero?.str("hero_id"),
            dataObj?.str("heroid"),
            "${heroName.lowercase(Locale.US)}_$index"
        )
        val slug = firstNonBlank(
            obj.str("slug"),
            nestedHero?.str("slug"),
            heroName.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "-")
        )

        val roles = extractRoles(obj, nestedHero, dataObj)
        val lane = firstNonBlank(
            obj.str("lane"),
            obj.str("road"),
            obj.str("position"),
            nestedHero?.str("lane"),
            dataObj?.str("lane"),
            defaultLaneForRole(roles.firstOrNull().orEmpty())
        )

        val pickRaw = firstNumber(
            obj["pick_rate"],
            obj["main_hero_appearance_rate"],
            obj["appearance_rate"],
            obj["use_rate"],
            obj["pickRate"],
            obj["pick_pct"]
        ) ?: 0.0
        val banRaw = firstNumber(
            obj["ban_rate"],
            obj["main_hero_ban_rate"],
            obj["banRate"],
            obj["ban_pct"]
        ) ?: 0.0
        val winRaw = firstNumber(
            obj["win_rate"],
            obj["main_hero_win_rate"],
            obj["winRate"],
            obj["win_pct"]
        ) ?: 50.0

        val pickPct = normalizePercent(pickRaw)
        val banPct = normalizePercent(banRaw)
        val winPct = normalizePercent(winRaw).let { if (it <= 0.0) 50.0 else it }

        val iconUrl = normalizeImageUrl(
            firstNonBlank(
                obj.str("icon"),
                obj.str("image"),
                obj.str("avatar"),
                obj.str("head"),
                obj.str("portrait"),
                nestedHero?.str("icon"),
                nestedHero?.str("head"),
                nestedHero?.str("image"),
                dataObj?.str("head"),
                dataObj?.str("icon")
            )
        )

        val score = (winPct - 48.0) * 1.6 + pickPct * 0.65 + banPct * 0.45
        val explicitTier = firstNonBlank(obj.str("tier"), obj.str("grade")).uppercase(Locale.US)
        val tierGrade = when (explicitTier) {
            "S+", "SS", "META" -> MetaTierGrade.S_PLUS
            "S" -> MetaTierGrade.S
            "A" -> MetaTierGrade.A
            "B" -> MetaTierGrade.B
            "C", "D" -> MetaTierGrade.C
            else -> MetaTierGrade.fromScore(winPct, pickPct, banPct)
        }

        return MetaHeroStats(
            heroId = heroId,
            slug = slug,
            heroName = heroName,
            roles = roles.ifEmpty { listOf("Fighter") },
            lane = lane,
            rankTier = rankFilter.label,
            pickRatePct = pickPct,
            banRatePct = banPct,
            winRatePct = winPct,
            tierGrade = tierGrade,
            tierScore = score,
            iconUrl = iconUrl
        )
    }

    fun mergeDetailPayloads(
        baseHero: MetaHeroStats,
        detailJson: String?,
        countersJson: String?,
        buildsJson: String?
    ): HeroMetaDetail {
        val detailRoot = detailJson?.let { parseRoot(it) }
        val detailMap = detailRoot.asMap()?.get("data").asMap() ?: detailRoot.asMap()
        val countersMap = countersJson?.let { parseRoot(it) }
        val buildsRoot = buildsJson?.let { parseRoot(it) }

        val strongAgainst = mutableListOf<HeroCounterMatchup>()
        val counteredBy = mutableListOf<HeroCounterMatchup>()
        val synergies = mutableListOf<HeroCounterMatchup>()

        parseCounterSources(countersMap, strongAgainst, counteredBy, synergies)
        parseCounterSources(detailMap, strongAgainst, counteredBy, synergies)

        val guides = mutableListOf<HeroBuildGuide>()
        parseBuildGuides(buildsRoot, guides, baseHero)
        if (guides.isEmpty()) {
            parseBuildGuides(detailMap, guides, baseHero)
        }

        val skills = parseSkills(detailMap)
        val refinedStats = detailMap?.let { d ->
            val descRoles = extractRoles(d, null, null)
            val updatedWin = firstNumber(d["win_rate"], d["winRate"])?.let { normalizePercent(it) }
                ?: baseHero.winRatePct
            val updatedPick = firstNumber(d["pick_rate"], d["pickRate"])?.let { normalizePercent(it) }
                ?: baseHero.pickRatePct
            val updatedBan = firstNumber(d["ban_rate"], d["banRate"])?.let { normalizePercent(it) }
                ?: baseHero.banRatePct
            val updatedIcon = normalizeImageUrl(
                firstNonBlank(d.str("head"), d.str("icon"), d.str("image"), baseHero.iconUrl)
            )
            baseHero.copy(
                roles = if (descRoles.isNotEmpty()) descRoles else baseHero.roles,
                winRatePct = updatedWin,
                pickRatePct = updatedPick,
                banRatePct = updatedBan,
                iconUrl = updatedIcon.ifBlank { baseHero.iconUrl }
            )
        } ?: baseHero

        return HeroMetaDetail(
            heroStats = refinedStats,
            strongAgainst = strongAgainst.distinctBy { it.heroName.lowercase(Locale.US) }.take(6),
            counteredBy = counteredBy.distinctBy { it.heroName.lowercase(Locale.US) }.take(6),
            synergies = synergies.distinctBy { it.heroName.lowercase(Locale.US) }.take(6),
            buildGuides = guides.take(4),
            skillSummary = skills.take(4)
        )
    }

    private fun parseCounterSources(
        node: Any?,
        strongOut: MutableList<HeroCounterMatchup>,
        weakOut: MutableList<HeroCounterMatchup>,
        synergyOut: MutableList<HeroCounterMatchup>
    ) {
        val map = node.asMap() ?: run {
            node.asList()?.forEach { entry ->
                parseMatchupObject(entry.asMap())?.let { weakOut += it }
            }
            return
        }
        val unwrapped = map["data"].asMap() ?: map

        val strongKeys = listOf("strong_against", "strongAgainst", "best_against", "favorable", "counters_target")
        val weakKeys = listOf("countered_by", "counteredBy", "weak_against", "counters", "hard_counters")
        val synKeys = listOf("synergies", "best_teammates", "teammates", "compatibility", "partners")

        for (k in strongKeys) {
            unwrapped[k].asList()?.forEach { item ->
                parseMatchupObject(item.asMap())?.let { strongOut += it }
            }
        }
        for (k in weakKeys) {
            unwrapped[k].asList()?.forEach { item ->
                parseMatchupObject(item.asMap())?.let { weakOut += it }
            }
        }
        for (k in synKeys) {
            unwrapped[k].asList()?.forEach { item ->
                parseMatchupObject(item.asMap())?.let { synergyOut += it }
            }
        }

        // Flat list under "records" or "items" with relation type field
        val generic = unwrapped["records"].asList() ?: unwrapped["items"].asList()
        generic?.forEach { item ->
            val obj = item.asMap() ?: return@forEach
            val kind = firstNonBlank(obj.str("relation"), obj.str("type"), obj.str("category")).lowercase(Locale.US)
            val matchup = parseMatchupObject(obj) ?: return@forEach
            when {
                "strong" in kind || "favor" in kind -> strongOut += matchup
                "syn" in kind || "team" in kind || "partner" in kind -> synergyOut += matchup
                else -> weakOut += matchup
            }
        }
    }

    private fun parseMatchupObject(obj: Map<String, Any?>?): HeroCounterMatchup? {
        obj ?: return null
        val nested = obj["hero"].asMap() ?: obj["target"].asMap()
        val name = firstNonBlank(
            obj.str("hero_name"),
            obj.str("name"),
            obj.str("heroName"),
            nested?.str("name"),
            nested?.str("hero_name")
        )
        if (name.isBlank()) return null

        val winRate = normalizePercent(
            firstNumber(obj["win_rate"], obj["winRate"], obj["matchup_win_rate"]) ?: 50.0
        )
        val rawDelta = firstNumber(
            obj["increase_win_rate"],
            obj["delta_win_rate"],
            obj["delta"],
            obj["advantage"]
        ) ?: 0.0
        val delta = if (kotlin.math.abs(rawDelta) in 0.0001..1.0) rawDelta * 100.0 else rawDelta

        return HeroCounterMatchup(
            heroId = firstNonBlank(obj.str("hero_id"), obj.str("id"), nested?.str("id"), name),
            heroName = name,
            winRatePct = winRate,
            deltaWinRatePct = delta,
            note = firstNonBlank(obj.str("note"), obj.str("reason"), obj.str("description")),
            iconUrl = normalizeImageUrl(
                firstNonBlank(obj.str("icon"), obj.str("head"), obj.str("image"), nested?.str("icon"), nested?.str("head"))
            )
        )
    }

    private fun parseBuildGuides(
        node: Any?,
        out: MutableList<HeroBuildGuide>,
        hero: MetaHeroStats
    ) {
        if (node == null) return
        val list = extractBuildArray(node) ?: return
        for ((idx, entry) in list.withIndex()) {
            val obj = entry.asMap() ?: continue
            val rawItems = (obj["items"].asList()
                ?: obj["equipments"].asList()
                ?: obj["equipment"].asList()
                ?: obj["equips"].asList()).orEmpty()

            val items = rawItems.mapNotNull { rawItem ->
                when (rawItem) {
                    is String -> if (rawItem.isNotBlank()) HeroBuildItem(rawItem, "Core") else null
                    else -> {
                        val m = rawItem.asMap() ?: return@mapNotNull null
                        val itemName = firstNonBlank(m.str("name"), m.str("item_name"), m.str("title"))
                        if (itemName.isBlank()) null
                        else HeroBuildItem(
                            name = itemName,
                            category = firstNonBlank(m.str("category"), m.str("slot"), "Core"),
                            iconUrl = normalizeImageUrl(firstNonBlank(m.str("icon"), m.str("image")))
                        )
                    }
                }
            }
            if (items.isEmpty()) continue

            val emblemObj = obj["emblem"].asMap()
            val emblemName = firstNonBlank(
                obj.str("emblem_set"),
                obj.str("emblem"),
                emblemObj?.str("name"),
                emblemObj?.str("title"),
                "Custom Emblem"
            )
            val spellObj = obj["spell"].asMap() ?: obj["battle_spell"].asMap()
            val battleSpell = firstNonBlank(
                obj.str("battle_spell"),
                obj.str("spell"),
                spellObj?.str("name"),
                "Flicker"
            )
            val winRate = normalizePercent(firstNumber(obj["win_rate"], obj["winRate"]) ?: hero.winRatePct)
            val laneRole = firstNonBlank(obj.str("lane"), obj.str("role"), hero.lane)

            out += HeroBuildGuide(
                title = firstNonBlank(obj.str("title"), obj.str("name"), "Pro Meta Set #${idx + 1}"),
                laneRole = laneRole,
                proWinRatePct = winRate,
                emblemSet = emblemName,
                battleSpell = battleSpell,
                skillPriority = firstNonBlank(obj.str("skill_priority"), obj.str("skillOrder"), "Max S1 -> S2 -> Ult"),
                items = items.take(6),
                comboTip = firstNonBlank(obj.str("tip"), obj.str("combo"), obj.str("notes"))
            )
        }
    }

    private fun extractBuildArray(node: Any?): List<Any?>? {
        node.asList()?.let { return it }
        val map = node.asMap() ?: return null
        val dataObj = map["data"].asMap()
        val candidates = listOf("builds", "guides", "pro_builds", "recommended_builds", "records", "items")
        for (k in candidates) {
            map[k].asList()?.let { if (it.isNotEmpty()) return it }
            dataObj?.get(k).asList()?.let { if (it.isNotEmpty()) return it }
        }
        return map["data"].asList()
    }

    private fun parseSkills(detailMap: Map<String, Any?>?): List<Pair<String, String>> {
        val rawSkills = detailMap?.get("skills").asList()
            ?: detailMap?.get("skill_list").asList()
            ?: return emptyList()
        return rawSkills.mapNotNull { entry ->
            val obj = entry.asMap() ?: return@mapNotNull null
            val name = firstNonBlank(obj.str("name"), obj.str("skill_name"), obj.str("title"))
            val desc = firstNonBlank(obj.str("description"), obj.str("desc"), obj.str("summary"))
            if (name.isBlank()) null else name to desc
        }
    }

    private fun extractRoles(
        primary: Map<String, Any?>,
        nested: Map<String, Any?>?,
        dataObj: Map<String, Any?>?
    ): List<String> {
        val roleList = (primary["roles"].asList()
            ?: primary["role"].asList()
            ?: nested?.get("roles").asList()
            ?: dataObj?.get("sortid").asList()).orEmpty()
            .mapNotNull { entry ->
                when (entry) {
                    is String -> entry.trim().takeIf { it.isNotBlank() }
                    else -> entry.asMap()?.str("name")?.takeIf { it.isNotBlank() }
                }
            }
        if (roleList.isNotEmpty()) return roleList
        val single = firstNonBlank(
            primary.str("role"),
            nested?.str("role"),
            dataObj?.str("role")
        )
        return single.split('/', ',').map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun defaultLaneForRole(role: String): String = when (role.lowercase(Locale.US)) {
        "marksman" -> "Gold Lane"
        "mage" -> "Mid Lane"
        "assassin" -> "Jungle"
        "tank", "support" -> "Roam"
        "fighter" -> "EXP Lane"
        else -> "Flex"
    }

    private fun normalizePercent(raw: Double): Double {
        if (raw.isNaN() || raw.isInfinite()) return 0.0
        val pct = if (raw in 0.0001..1.0) raw * 100.0 else raw
        return pct.coerceIn(0.0, 100.0)
    }

    private fun normalizeImageUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        return when {
            trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true) -> trimmed
            trimmed.startsWith("//") -> "https:$trimmed"
            trimmed.startsWith("/") -> "$BASE_DOMAIN$trimmed"
            else -> "$BASE_DOMAIN/$trimmed"
        }
    }

    private fun Map<String, Any?>.str(key: String): String {
        val v = this[key] ?: return ""
        return (v as? String)?.trim().orEmpty()
    }

    private fun firstNonBlank(vararg values: String?): String =
        values.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()

    private fun firstNumber(vararg values: Any?): Double? {
        for (v in values) {
            when (v) {
                is Number -> return v.toDouble()
                is String -> v.trim().removeSuffix("%").toDoubleOrNull()?.let { return it }
            }
        }
        return null
    }
}
