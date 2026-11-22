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
    val iconStyle: BadgeIconStyle
)

data class AchievementBadgeUi(
    val definition: AchievementBadgeDefinition,
    val currentProgress: Int,
    val isUnlocked: Boolean,
    val isPinned: Boolean
) {
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
            iconStyle = BadgeIconStyle.STAR_PACK
        ),
        AchievementBadgeDefinition(
            id = "badge_pack_maniac",
            title = "Pack Ripper",
            description = "Open 25 Booster Packs",
            targetCount = 25,
            accentColor = Color(0xFF818CF8),
            iconStyle = BadgeIconStyle.COLLECTOR_TROPHY
        ),
        AchievementBadgeDefinition(
            id = "badge_vault_50",
            title = "Dex Pathfinder",
            description = "Collect 50 unique skins",
            targetCount = 50,
            accentColor = Color(0xFF10B981),
            iconStyle = BadgeIconStyle.VAULT_CHEST
        ),
        AchievementBadgeDefinition(
            id = "badge_dex_master",
            title = "Dex Sovereign",
            description = "Collect 150 unique skins",
            targetCount = 150,
            accentColor = Color(0xFFF59E0B),
            iconStyle = BadgeIconStyle.DEX_CROWN
        ),
        AchievementBadgeDefinition(
            id = "badge_epic_hunter",
            title = "Epic Seeker",
            description = "Pull an Epic+ rarity card",
            targetCount = 1,
            accentColor = Color(0xFF8B5CF6),
            iconStyle = BadgeIconStyle.EPIC_GEM
        ),
        AchievementBadgeDefinition(
            id = "badge_mythic_owner",
            title = "Mythic Royalty",
            description = "Own a Collector or Legend skin",
            targetCount = 1,
            accentColor = Color(0xFFE54D67),
            iconStyle = BadgeIconStyle.MYTHIC_WINGS
        ),
        AchievementBadgeDefinition(
            id = "badge_anomaly_1of1",
            title = "1/1 Anomaly",
            description = "Discover a 1-of-1 Misprint card",
            targetCount = 1,
            accentColor = Color(0xFFC084FC),
            iconStyle = BadgeIconStyle.MISPRINT_SPARK
        ),
        AchievementBadgeDefinition(
            id = "badge_forge_smith",
            title = "Master Forger",
            description = "Recycle 15 cards in the Forge",
            targetCount = 15,
            accentColor = Color(0xFFD97706),
            iconStyle = BadgeIconStyle.FORGE_ANVIL
        ),
        AchievementBadgeDefinition(
            id = "badge_bazaar_broker",
            title = "Bazaar Broker",
            description = "Complete 3 Collector Trades",
            targetCount = 3,
            accentColor = Color(0xFF0EA5E9),
            iconStyle = BadgeIconStyle.TRADE_HANDS
        ),
        AchievementBadgeDefinition(
            id = "badge_trivia_ace",
            title = "Lore Scholar",
            description = "Complete 2 Hourly Quizzes",
            targetCount = 2,
            accentColor = Color(0xFF6366F1),
            iconStyle = BadgeIconStyle.QUIZ_SCROLL
        ),
        AchievementBadgeDefinition(
            id = "badge_lucky_wheel",
            title = "Wheel Spinner",
            description = "Spin the Collector Wheel 3 times",
            targetCount = 3,
            accentColor = Color(0xFFEC4899),
            iconStyle = BadgeIconStyle.WHEEL_FORTUNE
        ),
        AchievementBadgeDefinition(
            id = "badge_streak_3",
            title = "Dedicated Fan",
            description = "Reach a 3-day Daily Streak",
            targetCount = 3,
            accentColor = Color(0xFFEF4444),
            iconStyle = BadgeIconStyle.STREAK_FLAME
        )
    )

    fun byId(id: String): AchievementBadgeDefinition? = ALL.firstOrNull { it.id == id }

    fun evaluate(
        wallet: PlayerWalletState,
        ownedCards: List<OwnedCard>
    ): List<AchievementBadgeUi> {
        val uniqueSkins = ownedCards.map { it.catalogId }.distinct().size
        val epicPlusCount = ownedCards.count { it.rarity.tierRank >= SkinRarity.EPIC.tierRank }
        val mythicCount = ownedCards.count { it.rarity.tierRank >= SkinRarity.COLLECTOR.tierRank }
        val misprintCount = ownedCards.count { it.isMisprint }

        return ALL.map { def ->
            val progress = when (def.id) {
                "badge_first_rip", "badge_pack_maniac" -> wallet.totalPacksOpened
                "badge_vault_50", "badge_dex_master" -> uniqueSkins
                "badge_epic_hunter" -> epicPlusCount
                "badge_mythic_owner" -> mythicCount
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
                isPinned = unlocked && def.id in wallet.pinnedBadgeIds
            )
        }
    }
}
