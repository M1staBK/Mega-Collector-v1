package com.example

import android.content.Context
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.model.OwnedCard
import com.example.model.SkinRarity
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
    fun `app launches cleanly and navigates all 5 tabs, holo filter, and arcade minigames`() {
        composeRule.waitForIdle()

        // Default screen is Packs -> verify settings cog and theme switching
        composeRule.onNodeWithTag("settings_cog_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_vertical_popup").assertExists()
        composeRule.onNodeWithTag("settings_accent_emerald_mint").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_dark_mode_switch").performClick()
        composeRule.waitForIdle()

        // Navigate across Play Arcade and verify new Mystery Vault Pick & Memory Blitz minigames
        composeRule.onNodeWithTag("nav_play").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("play_arcade_list").performScrollToNode(hasTestTag("mystery_chest_0"))
        composeRule.onNodeWithTag("mystery_chest_0").assertExists()
        composeRule.onNodeWithTag("play_arcade_list").performScrollToNode(hasTestTag("memory_card_0"))
        composeRule.onNodeWithTag("memory_card_0").assertExists()

        composeRule.onNodeWithTag("nav_quests").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_packs").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_vault").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("vault_filter_row").performScrollToNode(hasTestTag("vault_filter_holo"))
        composeRule.onNodeWithTag("vault_filter_holo").assertExists()
        composeRule.onNodeWithTag("vault_subtab_recycle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("vault_subtab_trade").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.waitForIdle()
    }
}

