package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class BazaarCollectorProfile(
    val traderId: String,
    val alias: String,
    val rankTitle: String,
    val avatarColor: Color,
    val prestigeScore: Int,
    val uniqueSkinsCount: Int,
    val packsOpened: Int,
    val tradesDone: Int,
    val motto: String,
    val badgeIds: List<String>,
    val showcaseSkins: List<SkinCatalogItem> = emptyList()
)

data class BazaarTraderListing(
    val listingId: String,
    val trader: BazaarCollectorProfile,
    val offeredSkin: SkinCatalogItem,
    val isMisprint: Boolean,
    val anomaly: MisprintAnomaly?,
    val serialCode: String,
    val minRequiredRarity: SkinRarity,
    val coinFee: Int,
    val seekingLabel: String
)

object BazaarMarketCatalog {

    private val personas = listOf(
        BazaarCollectorProfile(
            traderId = "trader_kagura",
            alias = "UmbrellaSora",
            rankTitle = "Mythical Glory",
            avatarColor = Color(0xFF6366F1),
            prestigeScore = 9420,
            uniqueSkinsCount = 248,
            packsOpened = 96,
            tradesDone = 41,
            motto = "Collecting every Mage & Collector crest across Dawn.",
            badgeIds = listOf("badge_mythic_owner", "badge_dex_master", "badge_bazaar_broker")
        ),
        BazaarCollectorProfile(
            traderId = "trader_gusion",
            alias = "BladeTempo99",
            rankTitle = "Immortal Collector",
            avatarColor = Color(0xFFE54D67),
            prestigeScore = 16850,
            uniqueSkinsCount = 412,
            packsOpened = 188,
            tradesDone = 74,
            motto = "Always hunting 1/1 Misprints & Assassin Legends.",
            badgeIds = listOf("badge_anomaly_1of1", "badge_mythic_owner", "badge_pack_maniac")
        ),
        BazaarCollectorProfile(
            traderId = "trader_tigreal",
            alias = "MoniyanVanguard",
            rankTitle = "Mythic",
            avatarColor = Color(0xFF10B981),
            prestigeScore = 4380,
            uniqueSkinsCount = 132,
            packsOpened = 54,
            tradesDone = 19,
            motto = "Fair swaps only! Tank & Fighter mains welcome.",
            badgeIds = listOf("badge_vault_50", "badge_bazaar_broker", "badge_streak_3")
        ),
        BazaarCollectorProfile(
            traderId = "trader_miya",
            alias = "MoonbowAria",
            rankTitle = "Legend",
            avatarColor = Color(0xFF0EA5E9),
            prestigeScore = 2640,
            uniqueSkinsCount = 87,
            packsOpened = 39,
            tradesDone = 12,
            motto = "Trading extra Luckybox & Special skins daily.",
            badgeIds = listOf("badge_epic_hunter", "badge_lucky_wheel", "badge_trivia_ace")
        ),
        BazaarCollectorProfile(
            traderId = "trader_ling",
            alias = "CyanWallStep",
            rankTitle = "Mythical Glory",
            avatarColor = Color(0xFFF59E0B),
            prestigeScore = 8150,
            uniqueSkinsCount = 219,
            packsOpened = 81,
            tradesDone = 33,
            motto = "Fast trades, rare pulls, clean profiles.",
            badgeIds = listOf("badge_mythic_owner", "badge_forge_smith", "badge_pack_maniac")
        ),
        BazaarCollectorProfile(
            traderId = "trader_nana",
            alias = "MolinaSpark",
            rankTitle = "Epic",
            avatarColor = Color(0xFFEC4899),
            prestigeScore = 1290,
            uniqueSkinsCount = 61,
            packsOpened = 27,
            tradesDone = 9,
            motto = "Swapping starters & Elite skins for missing favorites!",
            badgeIds = listOf("badge_first_rip", "badge_vault_50", "badge_lucky_wheel")
        )
    )

    fun buildDailyBazaarListings(
        catalog: List<SkinCatalogItem>,
        epochDay: Long,
        rotationSeed: Int = 0
    ): List<BazaarTraderListing> {
        if (catalog.isEmpty()) return emptyList()
        val byRarity = catalog.groupBy { it.rarity }

        fun pickSkin(preferred: SkinRarity, slotIdx: Int): SkinCatalogItem {
            val pool = byRarity[preferred]?.takeIf { it.isNotEmpty() }
                ?: catalog.sortedBy { abs(it.rarity.tierRank - preferred.tierRank) }
            val hash = abs((epochDay * 31L + rotationSeed * 17L + slotIdx * 97L).toInt())
            return pool[hash % pool.size]
        }

        val showcasePool = catalog.sortedByDescending { it.rarity.tierRank }
        fun showcaseFor(personaIdx: Int): List<SkinCatalogItem> {
            return (0 until 3).map { offset ->
                val idx = abs((personaIdx * 13 + offset * 7 + rotationSeed)) % showcasePool.size
                showcasePool[idx]
            }
        }

        val slotSpecs = listOf(
            Triple(SkinRarity.UNCOMMON, SkinRarity.COMMON, 0),
            Triple(SkinRarity.SPECIAL, SkinRarity.UNCOMMON, 5),
            Triple(SkinRarity.EPIC, SkinRarity.SPECIAL, 10),
            Triple(SkinRarity.LUCKYBOX, SkinRarity.EPIC, 15),
            Triple(SkinRarity.COLLECTOR, SkinRarity.LUCKYBOX, 25),
            Triple(SkinRarity.LEGEND, SkinRarity.COLLECTOR, 40)
        )

        return slotSpecs.mapIndexed { idx, (offerRarity, minReqRarity, coinFee) ->
            val skin = pickSkin(offerRarity, idx)
            val baseTrader = personas[idx % personas.size]
            val trader = baseTrader.copy(showcaseSkins = showcaseFor(idx))
            val misprintSlot = idx == 4 && ((epochDay + rotationSeed) % 5L == 0L)
            val anomaly = if (misprintSlot) {
                MisprintAnomaly.entries[abs((epochDay + rotationSeed).toInt()) % MisprintAnomaly.entries.size]
            } else null
            val hex = Integer.toHexString(
                abs(((epochDay * 131 + idx * 911 + rotationSeed * 57) % 0xEFFF) + 0x1000).toInt()
            ).uppercase()

            BazaarTraderListing(
                listingId = "bz_${epochDay}_${rotationSeed}_$idx",
                trader = trader,
                offeredSkin = skin,
                isMisprint = misprintSlot,
                anomaly = anomaly,
                serialCode = if (misprintSlot) "1/1 • BZ-$hex" else "BZ-$hex",
                minRequiredRarity = minReqRarity,
                coinFee = coinFee,
                seekingLabel = if (coinFee > 0) {
                    "Any ${minReqRarity.displayName}+ card + $coinFee Coins"
                } else {
                    "Any ${minReqRarity.displayName}+ card"
                }
            )
        }
    }
}
