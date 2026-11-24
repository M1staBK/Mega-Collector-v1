package com.example

import com.example.data.local.PlayerWalletState
import com.example.model.AchievementCatalog
import com.example.model.CosmeticCatalog
import com.example.model.MlbbCollectionTier
import com.example.model.OwnedCard
import com.example.model.PackBundleOption
import com.example.model.SkinRarity
import com.example.ui.components.BoosterPouchPhysics
import com.example.ui.theme.ObsidianPackCanvas
import com.example.ui.theme.ObsidianPackGold
import com.example.ui.theme.ThemeAccentOption
import com.example.ui.theme.buildCollectorPalette
import com.example.ui.viewmodel.PackOpeningSession
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `rankings place Mega above Exalted with bright yellow color and 3-2-1 subranks`() {
        val ladder = MlbbCollectionTier.RAREST_LEFT_TO_RIGHT
        assertEquals(12, ladder.size)
        assertEquals(MlbbCollectionTier.WORLD, ladder[0])
        assertEquals(MlbbCollectionTier.MEGA_1, ladder[1])
        assertEquals(MlbbCollectionTier.MEGA_2, ladder[2])
        assertEquals(MlbbCollectionTier.MEGA_3, ladder[3])
        assertEquals(MlbbCollectionTier.EXALTED_1, ladder[4])
        assertEquals(MlbbCollectionTier.EXALTED_2, ladder[5])
        assertEquals(MlbbCollectionTier.EXALTED_3, ladder[6])

        assertTrue(MlbbCollectionTier.MEGA_3.minPower > MlbbCollectionTier.EXALTED_1.minPower)
        assertTrue(MlbbCollectionTier.WORLD.minPower > MlbbCollectionTier.MEGA_1.minPower)

        // Verify Mega Collector uses bright yellow/gold hue (red & green high, blue low)
        listOf(MlbbCollectionTier.MEGA_1, MlbbCollectionTier.MEGA_2, MlbbCollectionTier.MEGA_3).forEach { mega ->
            assertTrue("${mega.name} should have bright yellow-gold red channel", mega.primaryColor.red >= 0.78f)
            assertTrue("${mega.name} should have yellow-gold green channel", mega.primaryColor.green >= 0.50f)
            assertTrue("${mega.name} should keep low blue channel for yellow", mega.primaryColor.blue <= 0.15f)
        }
    }

    @Test
    fun `booster pack 3D prism geometry separates front and back faces at 90 deg and crimps ends`() {
        val midDepth = BoosterPouchPhysics.bellyHalfDepthFraction(0.50f)
        val topCrimpDepth = BoosterPouchPhysics.bellyHalfDepthFraction(0.05f)
        val bottomCrimpDepth = BoosterPouchPhysics.bellyHalfDepthFraction(0.95f)
        assertTrue(midDepth > topCrimpDepth * 4f)
        assertEquals(topCrimpDepth, bottomCrimpDepth, 0.0001f)

        // Sleek elongated 3D mesh proportions (aspect >= 1.85, slim pillow depth < 0.05)
        assertTrue(BoosterPouchPhysics.PACK_ASPECT_RATIO >= 1.85f)
        assertTrue(BoosterPouchPhysics.BELLY_HALF_DEPTH_RATIO < 0.05f)

        // Pillow cross-section is thicker at center (u=0.5) than outer crimp edges (u=0.0 or u=1.0)
        val centerMeshZ = BoosterPouchPhysics.pouchMeshHalfDepthFraction(0.5f, 0.5f)
        val edgeMeshZ = BoosterPouchPhysics.pouchMeshHalfDepthFraction(0.0f, 0.5f)
        assertTrue(centerMeshZ > edgeMeshZ * 3f)

        // Folded back spine fin only exists on body belly and peaks at u=0.5
        assertTrue(BoosterPouchPhysics.pouchBackCenterFinFraction(0.5f, 0.5f) > 0.005f)
        assertEquals(0f, BoosterPouchPhysics.pouchBackCenterFinFraction(0.5f, 0.03f), 0.0001f)
        assertEquals(0f, BoosterPouchPhysics.pouchBackCenterFinFraction(0.1f, 0.5f), 0.0001f)

        val front90 = BoosterPouchPhysics.computeFacePlacement(
            isFront = true,
            yawDeg = 90f,
            pitchDeg = 0f,
            halfDepthPx = 28f,
            cameraDistancePx = 2000f
        )
        val back90 = BoosterPouchPhysics.computeFacePlacement(
            isFront = false,
            yawDeg = 90f,
            pitchDeg = 0f,
            halfDepthPx = 28f,
            cameraDistancePx = 2000f
        )
        assertTrue(
            "Rotating 90 deg must preserve physical Z thickness across X axis",
            abs(front90.translationX - back90.translationX) > 40f
        )
    }

    @Test
    fun `cosmetics shop is diamonds-only across frames and binders`() {
        val purchasableFrames = CosmeticCatalog.FRAMES.filter { !it.isFreeDefault && !it.isAchievementExclusive }
        val purchasableBinders = CosmeticCatalog.BINDERS.filter { !it.isFreeDefault && !it.isAchievementExclusive }

        assertTrue(purchasableFrames.isNotEmpty())
        assertTrue(purchasableBinders.isNotEmpty())

        purchasableFrames.forEach { frame ->
            assertTrue("Frame ${frame.id} must cost diamonds", frame.diamondCost > 0)
        }
        purchasableBinders.forEach { binder ->
            assertTrue("Binder ${binder.id} must cost diamonds", binder.diamondCost > 0)
        }
    }

    @Test
    fun `ten pack bundle tracks full 10-pack stack and per-pack card slices`() {
        val fiftyCards = (1..50).map { i ->
            OwnedCard(
                instanceId = i.toLong(),
                catalogId = "skin_$i",
                skinName = "Skin $i",
                heroName = "Hero $i",
                role = "Assassin",
                rawTier = "Epic",
                rarity = SkinRarity.EPIC,
                imageUrl = "",
                isMisprint = false,
                misprintAnomaly = null,
                serialCode = "#$i",
                pullRatePercent = 8.0,
                obtainedAtMs = 0L,
                isListedForTrade = false,
                isHolographic = false
            )
        }

        val initial = PackOpeningSession(
            bundle = PackBundleOption.TEN_PACK,
            pulledCards = fiftyCards,
            bundleCommitted = true,
            currentPackIndex = 0,
            showingPackStack = true
        )
        assertEquals(10, initial.remainingPacksInStack)
        assertTrue(initial.hasNextPackInStack)
        assertEquals(0..4, initial.currentPackCardIndices)

        val midPack = initial.copy(
            currentPackIndex = 4,
            showingPackStack = false,
            revealedIndices = (0..23).toSet()
        )
        assertEquals(6, midPack.remainingPacksInStack)
        assertEquals(20..24, midPack.currentPackCardIndices)
        assertFalse(midPack.currentPackAllRevealed)
        assertEquals(24, midPack.nextUnrevealedIndex)

        val midPackFinished = midPack.copy(revealedIndices = (0..24).toSet())
        assertTrue(midPackFinished.currentPackAllRevealed)
        assertTrue(midPackFinished.hasNextPackInStack)

        val lastPack = initial.copy(
            currentPackIndex = 9,
            showingPackStack = true
        )
        assertEquals(1, lastPack.remainingPacksInStack)
        assertFalse(lastPack.hasNextPackInStack)
        assertEquals(45..49, lastPack.currentPackCardIndices)
    }

    @Test
    fun `achievements grant frames, binders, and unlockable themes cleanly`() {
        val unlockedRewards = AchievementCatalog.ALL.filter { it.hasReward }
        assertTrue(unlockedRewards.isNotEmpty())

        val frameIds = CosmeticCatalog.FRAMES.map { it.id }.toSet()
        val binderIds = CosmeticCatalog.BINDERS.map { it.id }.toSet()

        AchievementCatalog.ALL.forEach { def ->
            def.rewardFrameId?.let { assertTrue("Unknown frame reward $it", it in frameIds) }
            def.rewardBinderId?.let { assertTrue("Unknown binder reward $it", it in binderIds) }
            def.rewardThemeId?.let { themeId ->
                assertNotNull("Unknown theme reward $themeId", ThemeAccentOption.entries.firstOrNull { it.id == themeId })
            }
        }

        val fakeCards = (1..25).map { i ->
            OwnedCard(
                instanceId = i.toLong(),
                catalogId = "skin_$i",
                skinName = "Skin $i",
                heroName = "Hero $i",
                role = "Fighter",
                rawTier = if (i == 1) "Legend" else "Collector",
                rarity = if (i == 1) SkinRarity.LEGEND else SkinRarity.COLLECTOR,
                imageUrl = "",
                isMisprint = false,
                misprintAnomaly = null,
                serialCode = "#$i",
                pullRatePercent = 1.0,
                obtainedAtMs = 0L,
                isListedForTrade = false,
                isHolographic = true
            )
        }
        val eval = AchievementCatalog.evaluate(PlayerWalletState(totalPacksOpened = 1), fakeCards)
        val vault25 = eval.first { it.definition.id == "badge_vault_25" }
        assertTrue(vault25.isUnlocked)
        assertTrue(vault25.canClaim)
        assertEquals("CELESTIAL_GOLD", vault25.definition.rewardThemeId)
    }

    @Test
    fun `pack noir obsidian theme exposes dark obsidian canvas and purple-gold shine`() {
        val packNoir = ThemeAccentOption.OBSIDIAN_PACK
        assertTrue(packNoir.isUnlockedByDefault)
        assertTrue(packNoir.forcesDarkCanvas)
        assertTrue(packNoir.hasPackNoirShine)

        val palette = buildCollectorPalette(isDark = false, accent = packNoir)
        assertTrue(palette.isDark)
        assertTrue(palette.hasPackNoirShine)
        assertEquals(ObsidianPackCanvas, palette.canvas)
        assertEquals(ObsidianPackGold, palette.accentPrimary)

        assertFalse(ThemeAccentOption.CELESTIAL_GOLD.isUnlocked(emptySet()))
        assertTrue(ThemeAccentOption.CELESTIAL_GOLD.isUnlocked(setOf("CELESTIAL_GOLD")))
        assertFalse(ThemeAccentOption.MYTHIC_PRISM.isUnlocked(emptySet()))
        assertTrue(ThemeAccentOption.MYTHIC_PRISM.isUnlocked(setOf("MYTHIC_PRISM")))
    }
}
