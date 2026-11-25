package com.example.model

enum class MetaSortMode(val label: String) {
    TIER_LIST("Tier List"),
    TOP_PICKS("Top Picks"),
    TOP_BANS("Top Bans")
}

enum class MetaRankFilter(val label: String, val apiParam: String) {
    ALL("All Ranks", "all"),
    EPIC("Epic", "epic"),
    LEGEND("Legend", "legend"),
    MYTHIC("Mythic", "mythic"),
    HONOR("Honor", "honor"),
    GLORY("Glory", "glory")
}

enum class MetaTierGrade(val label: String) {
    S_PLUS("S+"),
    S("S"),
    A("A"),
    B("B"),
    C("C");

    companion object {
        fun fromScore(winRatePct: Double, pickRatePct: Double, banRatePct: Double): MetaTierGrade {
            val weighted = (winRatePct - 48.0) * 1.6 + pickRatePct * 0.65 + banRatePct * 0.45
            return when {
                weighted >= 18.0 -> S_PLUS
                weighted >= 10.0 -> S
                weighted >= 3.0 -> A
                weighted >= -2.5 -> B
                else -> C
            }
        }
    }
}

data class MetaHeroStats(
    val heroId: String,
    val slug: String,
    val heroName: String,
    val roles: List<String>,
    val lane: String,
    val rankTier: String,
    val pickRatePct: Double,
    val banRatePct: Double,
    val winRatePct: Double,
    val tierGrade: MetaTierGrade,
    val tierScore: Double,
    val iconUrl: String
)

data class HeroCounterMatchup(
    val heroId: String,
    val heroName: String,
    val winRatePct: Double,
    val deltaWinRatePct: Double,
    val note: String,
    val iconUrl: String
)

data class HeroBuildItem(
    val name: String,
    val category: String,
    val iconUrl: String = ""
)

data class HeroBuildGuide(
    val title: String,
    val laneRole: String,
    val proWinRatePct: Double,
    val emblemSet: String,
    val battleSpell: String,
    val skillPriority: String,
    val items: List<HeroBuildItem>,
    val comboTip: String
)

data class CounterBuildItem(
    val name: String,
    val reason: String,
    val iconUrl: String = ""
)

/** Rule-based "how to build against this hero" advice (not live match data). */
data class CounterBuildPlan(
    val headline: String,
    val traits: List<String>,
    val items: List<CounterBuildItem>,
    val note: String
)

data class HeroMetaDetail(
    val heroStats: MetaHeroStats,
    val strongAgainst: List<HeroCounterMatchup>,
    val counteredBy: List<HeroCounterMatchup>,
    val synergies: List<HeroCounterMatchup>,
    val buildGuides: List<HeroBuildGuide>,
    val skillSummary: List<Pair<String, String>>,
    val counterBuild: CounterBuildPlan? = null,
    /** Moonton's own matchup notes (strong / weak / assist descriptions). */
    val officialNotes: List<Pair<String, String>> = emptyList()
)
