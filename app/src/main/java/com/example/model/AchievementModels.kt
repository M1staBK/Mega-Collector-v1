package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.data.local.PlayerWalletState

enum class BadgeIconStyle {
    STAR_PACK,
    DEX_CROWN,
    EPIC_GEM,
    MYTHIC_WINGS,
    MISPRINT_SPARK,
    FORGE_ANVIL,
    TRADE_HANDS,
    QUIZ_SCROLL,
    WHEEL_FORTUNE,
    STREAK_FLAME,
    VAULT_CHEST,
    COLLECTOR_TROPHY
}

data class AchievementBadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val accentColor: Color,
    val iconStyle: BadgeIconStyle,
    val rewardCoins: Int = 0,
    val rewardDiamonds: Int = 0,
    val rewardFrameId: String? = null,
    val rewardBinderId: String? = null,
    val rewardThemeId: String? = null,
    val rewardSummaryText: String = ""
) {
    val hasReward: Boolean
        get() = rewardCoins > 0 ||
            rewardDiamonds > 0 ||
            rewardFrameId != null ||
            rewardBinderId != null ||
            rewardThemeId != null
}

data class AchievementBadgeUi(
    val definition: AchievementBadgeDefinition,
    val currentProgress: Int,
    val isUnlocked: Boolean,
    val isPinned: Boolean,
    val isClaimed: Boolean = false
) {
    val canClaim: Boolean
        get() = isUnlocked && !isClaimed && definition.hasReward

    val progressFraction: Float
        get() = (currentProgress.toFloat() / definition.targetCount.coerceAtLeast(1).toFloat())
            .coerceIn(0f, 1f)
}

object AchievementCatalog {
    const val MAX_PINNED_BADGES = 3

    val ALL: List<AchievementBadgeDefinition> = listOf(
        AchievementBadgeDefinition(
            id = "badge_first_rip",
            title = "First Rip",
            description = "Open your first Booster Pack",
            targetCount = 1,
            accentColor = Color(0xFF38BDF8),
            iconStyle = BadgeIconStyle.STAR_PACK,
            rewardCoins = 75,
            rewardSummaryText = "+75 Coins"
        ),
        AchievementBadgeDefinition(
            id = "badge_starter_5",
            title = "Rising Collector",
            description = "Collect 5 unique skins in your Vault",
            targetCount = 5,
            accentColor = Color(0xFF10B981),
            iconStyle = BadgeIconStyle.VAULT_CHEST,
            rewardCoins = 120,
            rewardDiamonds = 4,
            rewardSummaryText = "+120 Coins • +4 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_hero_roster_15",
            title = "Hero Roster",
            description = "Collect skins across 15 different MLBB Heroes",
            targetCount = 15,
            accentColor = Color(0xFFFACC15),
            iconStyle = BadgeIconStyle.COLLECTOR_TROPHY,
            rewardCoins = 150,
            rewardFrameId = "frame_mega_aurum",
            rewardSummaryText = "Mega Yellow Flare Frame + 150 Coins"
        ),
        AchievementBadgeDefinition(
            id = "badge_vault_25",
            title = "Vault Architect",
            description = "Collect 25 unique skins",
            targetCount = 25,
            accentColor = Color(0xFFF59E0B),
            iconStyle = BadgeIconStyle.DEX_CROWN,
            rewardDiamonds = 10,
            rewardThemeId = "CELESTIAL_GOLD",
            rewardSummaryText = "Celestial Aurum Theme + 10 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_vault_50",
            title = "Dex Pathfinder",
            description = "Collect 50 unique skins",
            targetCount = 50,
            accentColor = Color(0xFF10B981),
            iconStyle = BadgeIconStyle.VAULT_CHEST,
            rewardDiamonds = 15,
            rewardFrameId = "frame_mythic_lattice",
            rewardSummaryText = "Mythic Starlight Frame + 15 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_dex_master",
            title = "Dex Sovereign",
            description = "Collect 150 unique skins",
            targetCount = 150,
            accentColor = Color(0xFFF59E0B),
            iconStyle = BadgeIconStyle.DEX_CROWN,
            rewardDiamonds = 30,
            rewardBinderId = "binder_world_archive",
            rewardSummaryText = "Dex Sovereign Binder + 30 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_epic_hunter",
            title = "Epic Seeker",
            description = "Pull an Epic+ rarity card",
            targetCount = 1,
            accentColor = Color(0xFF8B5CF6),
            iconStyle = BadgeIconStyle.EPIC_GEM,
            rewardCoins = 100,
            rewardSummaryText = "+100 Coins"
        ),
        AchievementBadgeDefinition(
            id = "badge_mythic_owner",
            title = "Mythic Royalty",
            description = "Own a Collector or Legend skin",
            targetCount = 1,
            accentColor = Color(0xFFE54D67),
            iconStyle = BadgeIconStyle.MYTHIC_WINGS,
            rewardDiamonds = 12,
            rewardThemeId = "MYTHIC_PRISM",
            rewardSummaryText = "Mythic Prism Theme + 12 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_holo_collector",
            title = "Holo Specialist",
            description = "Collect 1 Holographic Collector+ card",
            targetCount = 1,
            accentColor = Color(0xFF06B6D4),
            iconStyle = BadgeIconStyle.MISPRINT_SPARK,
            rewardBinderId = "binder_void_prism",
            rewardDiamonds = 8,
            rewardSummaryText = "Holo Prism Album + 8 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_legend_conqueror",
            title = "Legend Conqueror",
            description = "Collect a Legend tier skin",
            targetCount = 1,
            accentColor = Color(0xFFDC2626),
            iconStyle = BadgeIconStyle.DEX_CROWN,
            rewardFrameId = "frame_world_sovereign",
            rewardDiamonds = 25,
            rewardSummaryText = "World Collector Crown Frame + 25 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_pack_maniac",
            title = "Pack Ripper",
            description = "Open 25 Booster Packs",
            targetCount = 25,
            accentColor = Color(0xFF818CF8),
            iconStyle = BadgeIconStyle.COLLECTOR_TROPHY,
            rewardCoins = 250,
            rewardDiamonds = 6,
            rewardSummaryText = "+250 Coins • +6 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_anomaly_1of1",
            title = "1/1 Anomaly",
            description = "Discover a 1-of-1 Misprint card",
            targetCount = 1,
            accentColor = Color(0xFFC084FC),
            iconStyle = BadgeIconStyle.MISPRINT_SPARK,
            rewardDiamonds = 35,
            rewardCoins = 500,
            rewardSummaryText = "+500 Coins • +35 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_forge_smith",
            title = "Master Forger",
            description = "Recycle 15 cards in the Forge",
            targetCount = 15,
            accentColor = Color(0xFFD97706),
            iconStyle = BadgeIconStyle.FORGE_ANVIL,
            rewardCoins = 180,
            rewardSummaryText = "+180 Coins"
        ),
        AchievementBadgeDefinition(
            id = "badge_bazaar_broker",
            title = "Bazaar Broker",
            description = "Complete 3 Collector Trades",
            targetCount = 3,
            accentColor = Color(0xFF0EA5E9),
            iconStyle = BadgeIconStyle.TRADE_HANDS,
            rewardDiamonds = 10,
            rewardSummaryText = "+10 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_trivia_ace",
            title = "Lore Scholar",
            description = "Complete 2 Hourly Quizzes",
            targetCount = 2,
            accentColor = Color(0xFF6366F1),
            iconStyle = BadgeIconStyle.QUIZ_SCROLL,
            rewardDiamonds = 8,
            rewardSummaryText = "+8 Diamonds"
        ),
        AchievementBadgeDefinition(
            id = "badge_streak_3",
            title = "Dedicated Fan",
            description = "Reach a 3-day Daily Streak",
            targetCount = 3,
            accentColor = Color(0xFFEF4444),
            iconStyle = BadgeIconStyle.STREAK_FLAME,
            rewardCoins = 150,
            rewardDiamonds = 5,
            rewardSummaryText = "+150 Coins • +5 Diamonds"
        )
    )

    fun byId(id: String): AchievementBadgeDefinition? = ALL.firstOrNull { it.id == id }

    fun evaluate(
        wallet: PlayerWalletState,
        ownedCards: List<OwnedCard>
    ): List<AchievementBadgeUi> {
        val uniqueSkins = ownedCards.map { it.catalogId }.distinct().size
        val uniqueHeroes = ownedCards.map { it.heroName.lowercase() }.distinct().size
        val epicPlusCount = ownedCards.count { it.rarity.tierRank >= SkinRarity.EPIC.tierRank }
        val mythicCount = ownedCards.count { it.rarity.tierRank >= SkinRarity.COLLECTOR.tierRank }
        val holoCount = ownedCards.count { it.isHolographicEdition }
        val legendCount = ownedCards.count { it.rarity == SkinRarity.LEGEND }
        val misprintCount = ownedCards.count { it.isMisprint }

        return ALL.map { def ->
            val progress = when (def.id) {
                "badge_first_rip", "badge_pack_maniac" -> wallet.totalPacksOpened
                "badge_starter_5", "badge_vault_25", "badge_vault_50", "badge_dex_master" -> uniqueSkins
                "badge_hero_roster_15" -> uniqueHeroes
                "badge_epic_hunter" -> epicPlusCount
                "badge_mythic_owner" -> mythicCount
                "badge_holo_collector" -> holoCount
                "badge_legend_conqueror" -> legendCount
                "badge_anomaly_1of1" -> misprintCount
                "badge_forge_smith" -> wallet.totalCardsRecycledLifetime
                "badge_bazaar_broker" -> maxOf(wallet.totalTradesCompleted, wallet.communityTradesDone)
                "badge_trivia_ace" -> wallet.totalQuizzesCompleted
                "badge_lucky_wheel" -> wallet.totalWheelSpins
                "badge_streak_3" -> wallet.dailyStreakCount
                else -> 0
            }
            val unlocked = progress >= def.targetCount
            AchievementBadgeUi(
                definition = def,
                currentProgress = progress,
                isUnlocked = unlocked,
                isPinned = unlocked && def.id in wallet.pinnedBadgeIds,
                isClaimed = def.id in wallet.claimedAchievementIds
            )
        }
    }
}
