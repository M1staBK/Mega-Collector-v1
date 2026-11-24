package com.example

import android.content.Context
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PlayerPreferencesStore
import com.example.model.MlbbCollectionTier
import com.example.model.OwnedCard
import com.example.model.SkinRarity
import com.example.ui.components.MlbbChestStyle
import com.example.ui.screens.COLLECTOR_WHEEL_SLICES
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MEGA COLLECTOR", appName)
    }

    @Test
    fun `mlbb collection badges order, 11 chest styles, and 12-slit prize wheel with rarer pack`() {
        val ladder = MlbbCollectionTier.RAREST_LEFT_TO_RIGHT
        assertEquals(12, ladder.size)
        assertEquals(MlbbCollectionTier.WORLD, ladder.first())
        assertEquals(MlbbCollectionTier.MEGA_1, ladder[1])
        assertEquals(MlbbCollectionTier.NOVICE, ladder.last())
        assertTrue(MlbbCollectionTier.MEGA_3.minPower > MlbbCollectionTier.EXALTED_1.minPower)
        for (i in 0 until ladder.lastIndex) {
            assertTrue(ladder[i].levelNumber > ladder[i + 1].levelNumber)
            assertTrue(ladder[i].minPower > ladder[i + 1].minPower)
        }

        // Verify all 11 MLBB chest styles from reference screenshot
        assertEquals(11, MlbbChestStyle.entries.size)

        // Verify Prize Wheel has 12 slices and rarer Pack probability (<= 4%)
        assertEquals(12, COLLECTOR_WHEEL_SLICES.size)
        val totalWeight = COLLECTOR_WHEEL_SLICES.sumOf { it.weight }
        val packWeight = COLLECTOR_WHEEL_SLICES.filter { it.freePacks > 0 }.sumOf { it.weight }
        assertTrue(packWeight > 0)
        assertTrue("Pack chance should be <= 4% (${packWeight.toDouble() / totalWeight})", packWeight.toDouble() / totalWeight <= 0.04)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PlayerPreferencesStore(context)
        runBlocking {
            prefs.setThemeAccentId("OBSIDIAN_PACK")
            prefs.setDarkModeEnabled(true)
        }
        val boot = prefs.readBootThemeSync()
        assertEquals("OBSIDIAN_PACK", boot.themeAccentId)
        assertTrue(boot.darkModeEnabled)
    }

    @Test
    fun `holographic edition is strictly gated to Collector and Legend tiers`() {
        val lowerTiers = listOf(
            SkinRarity.COMMON,
            SkinRarity.UNCOMMON,
            SkinRarity.SPECIAL,
            SkinRarity.EPIC,
            SkinRarity.LUCKYBOX
        )
        lowerTiers.forEachIndexed { idx, rarity ->
            val card = OwnedCard(
                instanceId = idx.toLong(),
                catalogId = "test_$idx",
                skinName = "Test Skin",
                heroName = "Test Hero",
                role = "Fighter",
                rawTier = rarity.displayName,
                rarity = rarity,
                imageUrl = "",
                isMisprint = false,
                misprintAnomaly = null,
                serialCode = "#000$idx",
                pullRatePercent = 1.0,
                obtainedAtMs = 0L,
                isListedForTrade = false,
                isHolographic = true
            )
            assertFalse("${rarity.name} must never be holographic", card.isHolographicEdition)
            assertEquals(rarity.prestigePower, card.prestigeScore)
        }

        val collectorHolo = OwnedCard(
            instanceId = 100L,
            catalogId = "collector_holo",
            skinName = "Celestial Rebel",
            heroName = "Sun",
            role = "Fighter",
            rawTier = "Collector",
            rarity = SkinRarity.COLLECTOR,
            imageUrl = "",
            isMisprint = false,
            misprintAnomaly = null,
            serialCode = "#0100",
            pullRatePercent = 0.5,
            obtainedAtMs = 0L,
            isListedForTrade = false,
            isHolographic = true
        )
        assertTrue(collectorHolo.isHolographicEdition)
        assertEquals((SkinRarity.COLLECTOR.prestigePower * 3) / 2, collectorHolo.prestigeScore)

        val legendHolo = collectorHolo.copy(
            instanceId = 101L,
            catalogId = "legend_holo",
            rawTier = "Legend",
            rarity = SkinRarity.LEGEND
        )
        assertTrue(legendHolo.isHolographicEdition)
        assertEquals((SkinRarity.LEGEND.prestigePower * 3) / 2, legendHolo.prestigeScore)
    }

    @Test
    fun `app supports swipe switching, chest suspense opening, and click-to-view collection badges`() {
        composeRule.waitForIdle()

        // Default screen is Packs -> verify settings cog and theme switching
        composeRule.onNodeWithTag("settings_cog_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_vertical_popup").assertExists()
        composeRule.onNodeWithTag("settings_accent_obsidian_pack").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_dark_mode_switch").performClick()
        composeRule.waitForIdle()

        // Swipe right from Packs (page 2) -> Quests (page 1) and check Play tab minigames
        composeRule.onNodeWithTag("main_tab_pager").performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_play").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("play_arcade_list").performScrollToNode(hasTestTag("prize_wheel_canvas"))
        composeRule.onNodeWithTag("prize_wheel_canvas").assertExists()

        // Mystery Vault Pick: clicking chest 0 keeps the other 4 chests hidden as "???"
        composeRule.onNodeWithTag("play_arcade_list").performScrollToNode(hasTestTag("mystery_chest_0"))
        composeRule.onNodeWithTag("mystery_chest_0").performClick()
        composeRule.waitForIdle()
        assertEquals(4, composeRule.onAllNodesWithText("???").fetchSemanticsNodes().size)

        composeRule.onNodeWithTag("play_arcade_list").performScrollToNode(hasTestTag("memory_card_0"))
        composeRule.onNodeWithTag("memory_card_0").assertExists()

        // Navigate to Vault and verify swiping across sub-tabs (Cards -> Recycle -> Trade)
        composeRule.onNodeWithTag("nav_vault").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("vault_filter_row").performScrollToNode(hasTestTag("vault_filter_holo"))
        composeRule.onNodeWithTag("vault_filter_holo").assertExists()
        composeRule.onNodeWithTag("vault_subtab_pager").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("vault_subtab_trade").performClick()
        composeRule.waitForIdle()

        // Navigate to Profile -> Collection Level card shows active badge only until clicked
        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("profile_list").performScrollToNode(hasTestTag("collection_level_card"))
        composeRule.onNodeWithTag("collection_current_badge", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("collection_level_dialog").assertDoesNotExist()

        // Click Collection Level card -> dialog opens with all 12 badges
        composeRule.onNodeWithTag("collection_level_card").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("collection_level_dialog").assertExists()
        composeRule.onNodeWithTag("collection_level_ladder").assertExists()
        composeRule.onNodeWithTag("collection_tier_12").assertExists()
        composeRule.onNodeWithTag("collection_tier_1").assertExists()
    }
}


