package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    fun `app launches cleanly and navigates all 5 tabs and settings cog`() {
        composeRule.waitForIdle()

        // Default screen is Packs -> verify settings cog and daily claim or packs UI
        composeRule.onNodeWithTag("settings_cog_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_vertical_popup").assertExists()
        composeRule.onNodeWithTag("settings_accent_emerald_mint").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_dark_mode_switch").performClick()
        composeRule.waitForIdle()

        // Navigate across all 5 tabs: Play -> Quests -> Packs -> Vault (sub-tabs) -> Profile
        composeRule.onNodeWithTag("nav_play").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_quests").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_packs").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_vault").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("vault_subtab_recycle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("vault_subtab_trade").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.waitForIdle()
    }
}
