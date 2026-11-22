package com.example.model

enum class QuestCategory(val title: String) {
    DAILY("Daily Quests"),
    WEEKLY("Weekly Quests"),
    COMMUNITY("Community Quests")
}

data class QuestDefinition(
    val id: String,
    val category: QuestCategory,
    val title: String,
    val description: String,
    val targetCount: Int,
    val coinReward: Int,
    val diamondReward: Int = 0,
    val freePackReward: Int = 0
)

data class QuestUiItem(
    val definition: QuestDefinition,
    val currentProgress: Int,
    val isClaimed: Boolean
) {
    val isCompleted: Boolean get() = currentProgress >= definition.targetCount
    val canClaim: Boolean get() = isCompleted && !isClaimed
    val progressFraction: Float
        get() = (currentProgress.toFloat() / definition.targetCount.coerceAtLeast(1).toFloat())
            .coerceIn(0f, 1f)
}

object QuestCatalog {
    val dailyQuests = listOf(
        QuestDefinition(
            id = "d_open_1",
            category = QuestCategory.DAILY,
            title = "Pack Opener",
            description = "Open 1 Booster Pack today",
            targetCount = 1,
            coinReward = 35
        ),
        QuestDefinition(
            id = "d_open_5",
            category = QuestCategory.DAILY,
            title = "5-Pack Bundle Fever",
            description = "Open 5 Booster Packs today",
            targetCount = 5,
            coinReward = 100,
            diamondReward = 3
        ),
        QuestDefinition(
            id = "d_rare_epic",
            category = QuestCategory.DAILY,
            title = "Epic+ Hunter",
            description = "Collect 1 Epic, Luckybox, Collector, or Legend card",
            targetCount = 1,
            coinReward = 75,
            diamondReward = 4
        ),
        QuestDefinition(
            id = "d_scratch_2",
            category = QuestCategory.DAILY,
            title = "Lucky Fingers",
            description = "Scratch 2 Daily Coin Scratchers",
            targetCount = 2,
            coinReward = 45
        ),
        QuestDefinition(
            id = "d_minigame_2",
            category = QuestCategory.DAILY,
            title = "Diamond Challenger",
            description = "Complete 2 Arcade Minigame rounds",
            targetCount = 2,
            coinReward = 50,
            diamondReward = 3
        )
    )

    val weeklyQuests = listOf(
        QuestDefinition(
            id = "w_open_15",
            category = QuestCategory.WEEKLY,
            title = "Weekly Ripper",
            description = "Open 15 Booster Packs this week",
            targetCount = 15,
            coinReward = 250,
            freePackReward = 1
        ),
        QuestDefinition(
            id = "w_luckybox_3",
            category = QuestCategory.WEEKLY,
            title = "High-Tier Vault",
            description = "Collect 3 Luckybox+ rarity cards this week",
            targetCount = 3,
            coinReward = 300,
            diamondReward = 8
        ),
        QuestDefinition(
            id = "w_recycle_5",
            category = QuestCategory.WEEKLY,
            title = "Forge Crafter",
            description = "Recycle 5 cards in the Forge this week",
            targetCount = 5,
            coinReward = 120
        ),
        QuestDefinition(
            id = "w_minigame_6",
            category = QuestCategory.WEEKLY,
            title = "Arcade MVP",
            description = "Complete 6 Arcade Minigame plays this week",
            targetCount = 6,
            coinReward = 150,
            diamondReward = 10
        )
    )

    val communityQuests = listOf(
        QuestDefinition(
            id = "c_packs_25",
            category = QuestCategory.COMMUNITY,
            title = "Season Pack Drive",
            description = "Rip 25 Booster Packs toward the Season Pack goal",
            targetCount = 25,
            coinReward = 450,
            diamondReward = 10,
            freePackReward = 1
        ),
        QuestDefinition(
            id = "c_epic_10",
            category = QuestCategory.COMMUNITY,
            title = "Rarity Showcase",
            description = "Pull 10 Epic+ skins for the Collector Showcase",
            targetCount = 10,
            coinReward = 400,
            diamondReward = 12
        ),
        QuestDefinition(
            id = "c_mythic_1",
            category = QuestCategory.COMMUNITY,
            title = "Mythic Trophy Hunt",
            description = "Pull 1 Collector, Legend, or 1/1 Misprint card",
            targetCount = 1,
            coinReward = 550,
            diamondReward = 20
        ),
        QuestDefinition(
            id = "c_trade_1",
            category = QuestCategory.COMMUNITY,
            title = "Bazaar Contributor",
            description = "Complete or list 1 Collector Trade",
            targetCount = 1,
            coinReward = 150,
            diamondReward = 5
        )
    )
}
