package com.example.data.remote

import android.content.Context
import com.example.model.CounterBuildAdvisor
import com.example.model.CounterBuildItem
import com.example.model.CounterBuildPlan
import com.example.model.HeroBuildGuide
import com.example.model.HeroBuildItem
import com.example.model.HeroCounterMatchup
import com.example.model.HeroMetaDetail
import com.example.model.MetaHeroStats
import com.example.model.MetaRankFilter
import com.example.model.MetaTierGrade
import com.example.model.SkinCatalogItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

interface MobaMetaApi {
    @GET
    suspend fun get(@Url url: String): ResponseBody
}

/**
 * Meta data for the MOBA Companion Meta tab.
 *
 * Source: the Rone Arena community API (the same ranked-match statistics MLBBDex publishes), last 7 days.
 * Important facts about that API, taken from its documentation:
 *  - win / pick / ban rates come back as FRACTIONS (0.44 = 44%), so they are multiplied by 100 here;
 *  - roles and lanes are NOT part of the rank list, they come from /heroes/positions;
 *  - builds are keyed by lane and use equipment ids that are resolved to names through /academy/equipment.
 *
 * MLBBDex's own public JSON API only has heroes, items, patches and a rankings route, so it cannot
 * replace this source for counters and builds. Tiers use MLBBDex's published formula:
 * score = win rate + a quarter of the ban rate.
 */
class MobaMetaRepository(@Suppress("unused") private val context: Context) {

    private val api: MobaMetaApi by lazy {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "MOBACompanionAndroid/1.0")
                    .build()
                chain.proceed(req)
            }
            .build()
        Retrofit.Builder()
            .baseUrl("https://arena.rone.dev/")
            .client(okHttp)
            .build()
            .create(MobaMetaApi::class.java)
    }

    private val baseUrls = listOf(
        "https://arena.rone.dev/api",
        "https://arena-hv.fastapicloud.dev/api",
        "https://arena.fastapicloud.dev/api"
    )

    private class Timed<T>(val atMs: Long, val value: T)

    private val ttlMs = 30 * 60 * 1000L
    private var profilesCache: Timed<Map<Int, HeroProfile>>? = null
    private var equipmentCache: Timed<Map<Int, EquipInfo>>? = null
    private val rankCache = ConcurrentHashMap<String, Timed<List<MetaHeroStats>>>()

    private data class HeroProfile(
        val id: Int,
        val name: String,
        val icon: String,
        val roles: List<String>,
        val lanes: List<String>,
        val strong: List<Int>,
        val weak: List<Int>,
        val assist: List<Int>
    )

    private data class EquipInfo(val id: Int, val name: String, val icon: String)

    private fun fresh(atMs: Long) = System.currentTimeMillis() - atMs < ttlMs

    // ------------------------------------------------------------------ http

    private suspend fun getJson(path: String): JSONObject = withContext(Dispatchers.IO) {
        var last: Exception? = null
        for (base in baseUrls) {
            try {
                val raw = api.get(base + path).string()
                val json = JSONObject(raw)
                if (json.optInt("code", 0) != 0) {
                    throw IllegalStateException(json.optString("message", "The MLBB data service reported an error"))
                }
                return@withContext json
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IllegalStateException("The MLBB data service did not answer")
    }

    private fun JSONArray?.objects(): List<JSONObject> {
        if (this == null) return emptyList()
        val out = ArrayList<JSONObject>()
        for (i in 0 until length()) optJSONObject(i)?.let { out.add(it) }
        return out
    }

    private fun JSONObject.records(): List<JSONObject> =
        optJSONObject("data")?.optJSONArray("records").objects()

    private fun JSONArray?.ints(): List<Int> {
        if (this == null) return emptyList()
        val out = ArrayList<Int>()
        for (i in 0 until length()) {
            val v = optInt(i, 0)
            if (v > 0) out.add(v)
        }
        return out
    }

    // ------------------------------------------------------------ profiles

    private fun normalizeLane(title: String): String {
        val t = title.lowercase(Locale.US)
        return when {
            t.contains("exp") -> "EXP"
            t.contains("mid") -> "Mid"
            t.contains("gold") -> "Gold"
            t.contains("jungle") -> "Jungle"
            t.contains("roam") -> "Roam"
            else -> title.trim()
        }
    }

    private fun laneParam(lane: String): String = lane.lowercase(Locale.US)

    private suspend fun loadProfiles(force: Boolean): Map<Int, HeroProfile> {
        profilesCache?.let { if (!force && fresh(it.atMs)) return it.value }
        val json = getJson("/heroes/positions?size=300&index=1&lang=en")
        val map = LinkedHashMap<Int, HeroProfile>()
        for (rec in json.records()) {
            val data = rec.optJSONObject("data") ?: continue
            val id = data.optInt("hero_id", -1)
            if (id <= 0) continue
            val hero = data.optJSONObject("hero")?.optJSONObject("data") ?: continue
            val name = hero.optString("name").trim()
            if (name.isBlank()) continue
            val roles = hero.optJSONArray("sortid").objects()
                .mapNotNull { it.optJSONObject("data")?.optString("sort_title")?.takeIf { t -> t.isNotBlank() } }
                .map { r -> r.replaceFirstChar { c -> c.uppercase() } }
                .distinct()
            val lanes = hero.optJSONArray("roadsort").objects()
                .mapNotNull { it.optJSONObject("data")?.optString("road_sort_title")?.takeIf { t -> t.isNotBlank() } }
                .map { normalizeLane(it) }
                .distinct()
            val relation = data.optJSONObject("relation")
            map[id] = HeroProfile(
                id = id,
                name = name,
                icon = hero.optString("smallmap"),
                roles = roles,
                lanes = lanes,
                strong = relation?.optJSONObject("strong")?.optJSONArray("target_hero_id").ints(),
                weak = relation?.optJSONObject("weak")?.optJSONArray("target_hero_id").ints(),
                assist = relation?.optJSONObject("assist")?.optJSONArray("target_hero_id").ints()
            )
        }
        if (map.isEmpty()) throw IllegalStateException("The hero list came back empty")
        profilesCache = Timed(System.currentTimeMillis(), map)
        return map
    }

    private suspend fun loadEquipment(): Map<Int, EquipInfo> {
        equipmentCache?.let { if (fresh(it.atMs)) return it.value }
        val json = getJson("/academy/equipment?size=400&index=1&lang=en")
        val map = HashMap<Int, EquipInfo>()
        for (rec in json.records()) {
            val d = rec.optJSONObject("data") ?: continue
            val id = d.optInt("equipid", -1)
            val name = d.optString("equipname").trim()
            if (id > 0 && name.isNotBlank()) map[id] = EquipInfo(id, name, d.optString("equipicon"))
        }
        equipmentCache = Timed(System.currentTimeMillis(), map)
        return map
    }

    // ---------------------------------------------------------------- rank

    private class RawRate(
        val id: Int,
        val name: String,
        val icon: String,
        val win: Double,
        val pick: Double,
        val ban: Double
    )

    /**
     * Rank list for the chosen rank filter (last 7 days), with roles and lanes attached.
     * [catalog] is accepted for API compatibility and is not needed any more.
     */
    suspend fun fetchMetaHeroStats(
        rank: MetaRankFilter,
        @Suppress("UNUSED_PARAMETER") catalog: List<SkinCatalogItem>,
        forceRefresh: Boolean = false
    ): Result<List<MetaHeroStats>> = runCatching {
        val key = rank.apiParam
        rankCache[key]?.let { if (!forceRefresh && fresh(it.atMs)) return@runCatching it.value }

        val profiles = runCatching { loadProfiles(forceRefresh) }.getOrDefault(emptyMap())
        val json = getJson(
            "/heroes/rank?days=7&rank=${rank.apiParam}&sort_field=win_rate&sort_order=desc&size=200&index=1&lang=en"
        )

        val raws = ArrayList<RawRate>()
        for (rec in json.records()) {
            val d = rec.optJSONObject("data") ?: continue
            val id = d.optInt("main_heroid", -1)
            val mainData = d.optJSONObject("main_hero")?.optJSONObject("data")
            val name = mainData?.optString("name").orEmpty().ifBlank { profiles[id]?.name.orEmpty() }
            if (id <= 0 || name.isBlank()) continue
            val win = d.optDouble("main_hero_win_rate", Double.NaN)
            val pick = d.optDouble("main_hero_appearance_rate", Double.NaN)
            val ban = d.optDouble("main_hero_ban_rate", Double.NaN)
            if (win.isNaN() || pick.isNaN() || ban.isNaN() || win <= 0.0) continue
            raws.add(
                RawRate(
                    id = id,
                    name = name,
                    icon = mainData?.optString("head").orEmpty().ifBlank { profiles[id]?.icon.orEmpty() },
                    win = win,
                    pick = pick,
                    ban = ban
                )
            )
        }
        if (raws.isEmpty()) throw IllegalStateException("No hero statistics were returned for ${rank.label}")

        // The API returns fractions (0.44). Guard in case a mirror ever returns percentages.
        val factor = if ((raws.maxOfOrNull { it.win } ?: 0.0) > 1.5) 1.0 else 100.0

        val scored = raws.map { r ->
            val win = r.win * factor
            val ban = (r.ban * factor).coerceIn(0.0, 100.0)
            val pick = (r.pick * factor).coerceIn(0.0, 100.0)
            Triple(r, win, win + 0.25 * ban) to Pair(pick, ban)
        }.sortedByDescending { it.first.third }

        val total = scored.size
        val result = scored.mapIndexed { index, (head, pb) ->
            val (r, win, score) = head
            val (pick, ban) = pb
            val share = (index + 1).toDouble() / total
            val grade = when {
                share <= 0.135 -> MetaTierGrade.S_PLUS
                share <= 0.263 -> MetaTierGrade.S
                share <= 0.511 -> MetaTierGrade.A
                share <= 0.827 -> MetaTierGrade.B
                else -> MetaTierGrade.C
            }
            val profile = profiles[r.id]
            MetaHeroStats(
                heroId = r.id.toString(),
                slug = r.name.lowercase(Locale.US).filter { it.isLetterOrDigit() },
                heroName = r.name,
                roles = profile?.roles.orEmpty(),
                lane = profile?.lanes?.joinToString(" / ").orEmpty(),
                rankTier = rank.label,
                pickRatePct = pick,
                banRatePct = ban,
                winRatePct = win,
                tierGrade = grade,
                tierScore = score,
                iconUrl = r.icon
            )
        }
        rankCache[key] = Timed(System.currentTimeMillis(), result)
        result
    }

    // -------------------------------------------------------------- detail

    private fun headOf(sub: JSONObject): String {
        val d = sub.optJSONObject("hero")?.optJSONObject("data")
        val direct = d?.optString("head").orEmpty()
        if (direct.isNotBlank()) return direct
        return d?.optJSONObject("hero")?.optJSONObject("data")?.optString("head").orEmpty()
    }

    private fun matchups(
        array: JSONArray?,
        profiles: Map<Int, HeroProfile>,
        ascending: Boolean,
        noteFor: (Double) -> String
    ): List<HeroCounterMatchup> {
        val subs = array.objects()
        val enough = subs.filter { it.optDouble("hero_appearance_rate", 1.0) >= 0.0005 }
        val pool = if (enough.size >= 3) enough else subs
        return pool
            .sortedBy { if (ascending) it.optDouble("increase_win_rate", 0.0) else -it.optDouble("increase_win_rate", 0.0) }
            .take(6)
            .mapNotNull { sub ->
                val id = sub.optInt("heroid", -1)
                if (id <= 0) return@mapNotNull null
                val delta = sub.optDouble("increase_win_rate", 0.0) * 100.0
                HeroCounterMatchup(
                    heroId = id.toString(),
                    heroName = profiles[id]?.name ?: "Hero $id",
                    winRatePct = sub.optDouble("hero_win_rate", 0.0) * 100.0,
                    deltaWinRatePct = delta,
                    note = noteFor(delta),
                    iconUrl = headOf(sub).ifBlank { profiles[id]?.icon.orEmpty() }
                )
            }
    }

    private fun stripMarkup(text: String): String =
        text.replace(Regex("<[^>]*>"), "").replace(Regex("\\s+"), " ").trim()

    suspend fun fetchHeroMetaDetail(
        hero: MetaHeroStats,
        @Suppress("UNUSED_PARAMETER") catalog: List<SkinCatalogItem>
    ): Result<HeroMetaDetail> = runCatching {
        val id = hero.heroId.toIntOrNull() ?: throw IllegalStateException("Unknown hero")
        val rank = MetaRankFilter.entries.firstOrNull { it.label == hero.rankTier }?.apiParam ?: "all"

        coroutineScope {
            val profilesD = async { runCatching { loadProfiles(false) }.getOrDefault(emptyMap()) }
            val equipD = async { runCatching { loadEquipment() }.getOrDefault(emptyMap()) }
            val detailD = async { runCatching { getJson("/heroes/$id?size=1&index=1&lang=en") }.getOrNull() }
            val countersD = async {
                runCatching { getJson("/heroes/$id/counters?days=7&rank=$rank&size=1&index=1&lang=en") }.getOrNull()
            }
            val compatD = async {
                runCatching { getJson("/heroes/$id/compatibility?days=7&rank=$rank&size=1&index=1&lang=en") }.getOrNull()
            }

            val profiles = profilesD.await()
            val equipment = equipD.await()
            val lanes = profiles[id]?.lanes?.ifEmpty { null } ?: hero.lane.split(" / ").filter { it.isNotBlank() }

            val buildDs = lanes.take(2).map { lane ->
                async {
                    lane to runCatching {
                        getJson("/academy/heroes/$id/builds?lane=${laneParam(lane)}&rank=$rank&size=1&index=1&lang=en")
                    }.getOrNull()
                }
            }

            val detailJson = detailD.await()
            val countersJson = countersD.await()
            val compatJson = compatD.await()

            // ---- hero profile text
            val heroData = detailJson?.records()?.firstOrNull()?.optJSONObject("data")
                ?.optJSONObject("hero")?.optJSONObject("data")
            val specialities = heroData?.optJSONArray("speciality")?.let { arr ->
                (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
            }.orEmpty()
            val skillTags = ArrayList<String>()
            val skillSummary = ArrayList<Pair<String, String>>()
            heroData?.optJSONArray("heroskilllist").objects().forEach { set ->
                set.optJSONArray("skilllist").objects().forEach { skill ->
                    skill.optJSONArray("skilltag").objects().forEach { tag ->
                        tag.optString("tagname").takeIf { it.isNotBlank() }?.let { skillTags.add(it) }
                    }
                    val sName = skill.optString("skillname")
                    val sDesc = stripMarkup(skill.optString("skilldesc"))
                    if (sName.isNotBlank() && sDesc.isNotBlank() && skillSummary.size < 5) {
                        skillSummary.add(sName to (if (sDesc.length > 190) sDesc.take(187) + "..." else sDesc))
                    }
                }
            }

            // ---- official relation notes
            val officialNotes = ArrayList<Pair<String, String>>()
            val relation = detailJson?.records()?.firstOrNull()?.optJSONObject("data")?.optJSONObject("relation")
            fun relationNote(label: String, key: String) {
                val desc = stripMarkup(relation?.optJSONObject(key)?.optString("desc").orEmpty())
                if (desc.isNotBlank()) officialNotes.add(label to desc)
            }
            relationNote("Strong against", "strong")
            relationNote("Weak against", "weak")
            relationNote("Works well with", "assist")

            // ---- matchups
            val counterData = countersJson?.records()?.firstOrNull()?.optJSONObject("data")
            val counteredBy = matchups(counterData?.optJSONArray("sub_hero"), profiles, ascending = false) {
                "+%.1f pts win rate against ${hero.heroName}".format(it)
            }
            val strongAgainst = matchups(counterData?.optJSONArray("sub_hero_last"), profiles, ascending = true) {
                "%.1f pts win rate when facing ${hero.heroName}".format(it)
            }
            val compatData = compatJson?.records()?.firstOrNull()?.optJSONObject("data")
            val synergies = matchups(compatData?.optJSONArray("sub_hero"), profiles, ascending = false) {
                "+%.1f pts win rate when paired".format(it)
            }

            // ---- builds
            val skillOrder = heroData?.optString("recommendlevellabel").orEmpty()
            val guides = ArrayList<HeroBuildGuide>()
            buildDs.forEach { deferred ->
                val (lane, json) = deferred.await()
                val buildData = json?.records()?.firstOrNull()?.optJSONObject("data")
                val builds = buildData?.optJSONArray("build").objects()
                    .sortedByDescending { it.optDouble("build_pick_rate", 0.0) }
                    .take(2)
                builds.forEachIndexed { index, b ->
                    val itemIds = b.optJSONArray("equipid").ints()
                    val items = itemIds.mapNotNull { eid ->
                        equipment[eid]?.let { HeroBuildItem(name = it.name, category = "", iconUrl = it.icon) }
                    }
                    if (items.isEmpty()) return@forEachIndexed
                    val emblemData = b.optJSONObject("emblem")?.optJSONObject("data")
                    val emblemName = emblemData?.optString("emblemname").orEmpty()
                    val emblemAttr = emblemData?.optJSONObject("emblemattr")?.optString("emblemattr").orEmpty()
                        .lines().firstOrNull { it.isNotBlank() }.orEmpty()
                    val spell = b.optJSONObject("battleskill")?.optJSONObject("data")
                        ?.optJSONObject("__data")?.optString("skillname").orEmpty()
                    val pickShare = b.optDouble("build_pick_rate", 0.0) * 100.0
                    guides.add(
                        HeroBuildGuide(
                            title = if (index == 0) "Most played build" else "Alternative build",
                            laneRole = lane,
                            proWinRatePct = b.optDouble("build_win_rate", 0.0) * 100.0,
                            emblemSet = listOf(emblemName, emblemAttr).filter { it.isNotBlank() }.joinToString(" "),
                            battleSpell = spell.ifBlank { "n/a" },
                            skillPriority = skillOrder,
                            items = items,
                            comboTip = buildString {
                                append("Played in %.1f%% of this hero's %s games.".format(pickShare, lane))
                                if (skillOrder.isNotBlank()) append(" Suggested skill order: $skillOrder.")
                            }
                        )
                    )
                }
            }

            // ---- counter build (rule based) with icons from the equipment list
            val plan = CounterBuildAdvisor.plan(
                heroName = hero.heroName,
                roles = hero.roles.ifEmpty { profiles[id]?.roles.orEmpty() },
                specialities = specialities,
                skillTags = skillTags
            )
            val equipByName = equipment.values.associateBy { it.name.lowercase(Locale.US) }
            val plannedWithIcons = CounterBuildPlan(
                headline = plan.headline,
                traits = plan.traits,
                note = plan.note,
                items = plan.items.map { item: CounterBuildItem ->
                    item.copy(iconUrl = equipByName[item.name.lowercase(Locale.US)]?.icon.orEmpty())
                }
            )

            if (counteredBy.isEmpty() && strongAgainst.isEmpty() && synergies.isEmpty() &&
                guides.isEmpty() && skillSummary.isEmpty() && detailJson == null
            ) {
                throw IllegalStateException("Could not load details for ${hero.heroName} right now.")
            }

            HeroMetaDetail(
                heroStats = hero,
                strongAgainst = strongAgainst,
                counteredBy = counteredBy,
                synergies = synergies,
                buildGuides = guides,
                skillSummary = skillSummary,
                counterBuild = plannedWithIcons,
                officialNotes = officialNotes
            )
        }
    }
}
