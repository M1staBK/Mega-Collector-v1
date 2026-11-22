package com.example

import com.example.data.local.PlayerWalletState
import com.example.data.remote.SkinJsonParser
import com.example.model.AchievementCatalog
import com.example.model.BazaarMarketCatalog
import com.example.model.MegaPackConfig
import com.example.model.MisprintAnomaly
import com.example.model.PackBundleOption
import com.example.model.PackSatinTheme
import com.example.model.QuestCatalog
import com.example.model.QuizQuestionBank
import com.example.model.QuizQuestionType
import com.example.model.SkinCatalogItem
import com.example.model.SkinRarity
import com.example.model.SkinVoiceLines
import com.example.ui.components.CharacterFocusSquareTransformation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkinJsonParserTest {
    @Test
    fun `parses top-level array with 7-tier rarity fields`() {
        val json = """
            [
              {
                "id": "skin_01",
                "name": "Cosmic Gleam",
                "hero": "Gusion",
                "role": "Assassin",
                "tier": "Legend",
                "image": "/images/gusion_cosmic.jpg"
              },
              {
                "id": "skin_02",
                "skin_name": "Night Owl",
                "hero_name": "Gusion",
                "rarity": "Collector",
                "image_url": "https://cdn.mlbbdex.com/night_owl.png"
              },
              {
                "id": "skin_03",
                "name": "Wizardry Teacher",
                "hero": "Alice",
                "tier": "Luckybox"
              },
              {
                "id": "skin_04",
                "name": "Dark Guardian",
                "hero": "Tigreal",
                "tier": "Elite"
              }
            ]
        """.trimIndent()

        val result = SkinJsonParser.parseSkins(json)
        assertEquals(4, result.size)
        assertEquals(SkinRarity.LEGEND, result[0].rarity)
        assertEquals("https://mlbbdex.com/images/gusion_cosmic.jpg", result[0].imageUrl)
        assertEquals(SkinRarity.COLLECTOR, result[1].rarity)
        assertEquals(SkinRarity.LUCKYBOX, result[2].rarity)
        assertEquals(SkinRarity.UNCOMMON, result[3].rarity)
    }

    @Test
    fun `parses paginated skins envelope and hero detail with illustrations and images maps`() {
        val pageJson = """
            {
              "data": [
                {
                  "skin_id": 42,
                  "title": "S.A.B.E.R. Breacher",
                  "hero": { "name": "Johnson", "role": "Tank" },
                  "tier": { "name": "Epic" },
                  "images": { "splash": "https://mlbbdex.com/saber.webp" }
                }
              ],
              "meta": {
                "total": 1150,
                "totalPages": 12,
                "hasNext": true
              }
            }
        """.trimIndent()

        val page = SkinJsonParser.parseSkinsPage(pageJson)
        assertEquals(1, page.items.size)
        assertEquals(SkinRarity.EPIC, page.items[0].rarity)
        assertEquals(1150, page.totalCount)
        assertEquals(12, page.totalPages)
        assertEquals(true, page.hasNextPage)

        val heroDetailJson = """
            {
              "data": {
                "name": "Miya",
                "roles": ["Marksman"],
                "images": {
                  "portrait": "/visuels/heros/miya/portrait.webp",
                  "skins": {
                    "miya_2": "/visuels/heros/miya/skins/miya_2.webp",
                    "miya_3": "/visuels/heros/miya/skins/miya_3.webp"
                  }
                },
                "illustrations": {
                  "Modena Butterfly": "/visuels/heros/miya/illustrations/modena.webp"
                },
                "skins": [
                  { "id": "miya_1", "name": "Modena Butterfly", "tier": "Supreme", "label": "Legend" },
                  { "id": "miya_2", "name": "Doom Catalyst", "rarity": "Exquisite", "series": "Collector" },
                  { "id": "miya_3", "name": "Christmas Carnival", "tier": "Basic", "series": "Luckybox" },
                  { "id": "miya_4", "name": "Moonlight Archer", "tier": "Basic" }
                ]
              }
            }
        """.trimIndent()

        val heroSkins = SkinJsonParser.parseHeroDetailSkins(heroDetailJson)
        assertEquals(4, heroSkins.size)
        assertEquals("Miya", heroSkins[0].heroName)
        assertEquals("Marksman", heroSkins[0].role)
        assertEquals(SkinRarity.LEGEND, heroSkins[0].rarity)
        assertEquals("https://mlbbdex.com/visuels/heros/miya/illustrations/modena.webp", heroSkins[0].imageUrl)
        assertEquals(SkinRarity.COLLECTOR, heroSkins[1].rarity)
        assertEquals("https://mlbbdex.com/visuels/heros/miya/skins/miya_2.webp", heroSkins[1].imageUrl)
        assertEquals(SkinRarity.LUCKYBOX, heroSkins[2].rarity)
        assertEquals("https://mlbbdex.com/visuels/heros/miya/skins/miya_3.webp", heroSkins[2].imageUrl)
        assertEquals(SkinRarity.COMMON, heroSkins[3].rarity)
        assertEquals("https://mlbbdex.com/visuels/heros/miya/portrait.webp", heroSkins[3].imageUrl)
    }

    @Test
    fun `hourly charges refill every hour and cap at stack of 3`() {
        val anchor = 10_000_000L
        val oneHour = PlayerWalletState.ONE_HOUR_MS

        val after30m = PlayerWalletState.computeHourlyChargeStatus(0, anchor, anchor + oneHour / 2)
        assertEquals(0, after30m.charges)
        assertEquals(oneHour / 2, after30m.msUntilNextCharge)

        val after2h15m = PlayerWalletState.computeHourlyChargeStatus(0, anchor, anchor + oneHour * 2 + oneHour / 4)
        assertEquals(2, after2h15m.charges)
        assertEquals((oneHour * 3) / 4, after2h15m.msUntilNextCharge)

        val after10h = PlayerWalletState.computeHourlyChargeStatus(1, anchor, anchor + oneHour * 10)
        assertEquals(3, after10h.charges)
        assertEquals(0L, after10h.msUntilNextCharge)

        val wallet = PlayerWalletState(quizLastCompletedMs = anchor)
        assertEquals(false, wallet.darkModeEnabled)
        assertEquals(true, wallet.vibrationEnabled)
        assertEquals(false, wallet.soundMuted)
        assertEquals(oneHour / 2, wallet.currentQuizCooldownRemainingMs(anchor + oneHour / 2))
        assertEquals(0L, wallet.currentQuizCooldownRemainingMs(anchor + oneHour))
    }

    @Test
    fun `hourly quiz always builds 5 questions including map knowledge`() {
        val emptyQuiz = QuizQuestionBank.buildHourlyQuiz(emptyList())
        assertEquals(QuizQuestionBank.QUIZ_QUESTION_COUNT, emptyQuiz.size)
        assertTrue(emptyQuiz.all { it.type == QuizQuestionType.MAP_KNOWLEDGE })

        val sampleCatalog = listOf("Gusion", "Miya", "Fanny", "Layla", "Ling").mapIndexed { idx, hero ->
            SkinCatalogItem(
                id = "skin_$idx",
                skinName = "Skin $idx",
                heroName = hero,
                role = "Assassin",
                rawTier = "Epic",
                rarity = SkinRarity.EPIC,
                imageUrl = "https://mlbbdex.com/visuels/$idx.webp"
            )
        }
        val mixedQuiz = QuizQuestionBank.buildHourlyQuiz(sampleCatalog)
        assertEquals(5, mixedQuiz.size)
        assertEquals(3, mixedQuiz.count { it.type == QuizQuestionType.SKIN_IDENTITY })
        assertEquals(2, mixedQuiz.count { it.type == QuizQuestionType.MAP_KNOWLEDGE })
        assertTrue(mixedQuiz.all { it.options.size == 4 && it.correctAnswer in it.options })
    }

    @Test
    fun `mega pack odds, 50-coin bundle discounts, recycle values, misprints, achievements, and bazaar remain valid`() {
        val total = MegaPackConfig.RARITY_WEIGHTS.values.sum()
        assertEquals(100.0, total, 0.001)
        assertEquals(0.005, MegaPackConfig.MISPRINT_CHANCE_PERCENT, 0.0001)
        assertEquals(SkinRarity.COMMON, SkinRarity.fromApiTier("Basic", "Grand Blade"))

        assertEquals(50, PackBundleOption.SINGLE.coinCost)
        assertEquals(237, PackBundleOption.FIVE_PACK.coinCost)
        assertEquals(5, PackBundleOption.FIVE_PACK.discountPercent)
        assertEquals(450, PackBundleOption.TEN_PACK.coinCost)
        assertEquals(10, PackBundleOption.TEN_PACK.discountPercent)
        assertEquals(5, PackBundleOption.SINGLE.diamondCost)
        assertEquals(23, PackBundleOption.FIVE_PACK.diamondCost)
        assertEquals(45, PackBundleOption.TEN_PACK.diamondCost)

        assertEquals(1, SkinRarity.COMMON.recycleGold)
        assertEquals(2, SkinRarity.UNCOMMON.recycleGold)
        assertEquals(3, SkinRarity.SPECIAL.recycleGold)
        assertEquals(5, SkinRarity.EPIC.recycleGold)
        assertEquals(6, SkinRarity.LUCKYBOX.recycleGold)
        assertEquals(7, SkinRarity.COLLECTOR.recycleGold)
        assertEquals(10, SkinRarity.LEGEND.recycleGold)

        assertTrue(MisprintAnomaly.entries.size <= MisprintAnomaly.MAX_VARIANTS)
        assertEquals(10, MisprintAnomaly.entries.size)
        assertTrue(PackSatinTheme.ALL.size >= 7)

        assertTrue(QuestCatalog.dailyQuests.isNotEmpty())
        assertTrue(QuestCatalog.weeklyQuests.isNotEmpty())
        assertTrue(QuestCatalog.communityQuests.isNotEmpty())

        val badges = AchievementCatalog.evaluate(
            wallet = PlayerWalletState(totalPacksOpened = 1, dailyStreakCount = 3),
            ownedCards = emptyList()
        )
        assertEquals(12, badges.size)
        assertTrue(badges.first { it.definition.id == "badge_first_rip" }.isUnlocked)
        assertTrue(badges.first { it.definition.id == "badge_streak_3" }.isUnlocked)

        val sampleSkins = SkinRarity.entries.mapIndexed { idx, rarity ->
            SkinCatalogItem(
                id = "bz_skin_$idx",
                skinName = "Royal ${rarity.displayName}",
                heroName = "Hero$idx",
                role = "Mage",
                rawTier = rarity.displayName,
                rarity = rarity,
                imageUrl = "https://mlbbdex.com/visuels/$idx.webp"
            )
        }
        val listings = BazaarMarketCatalog.buildDailyBazaarListings(sampleSkins, epochDay = 20000L, rotationSeed = 1)
        assertEquals(6, listings.size)
        assertTrue(listings.all { it.trader.showcaseSkins.size == 3 })
    }

    @Test
    fun `character focus crop returns exact square anchored on character face or saliency peak`() {
        // Landscape 1280x720 with face centered on the right side (78% across)
        val faceRightRect = CharacterFocusSquareTransformation.computeFocusCropBounds(
            width = 1280,
            height = 720,
            faceNormX = 0.78f,
            faceNormY = 0.35f
        )
        assertEquals(720, faceRightRect.width())
        assertEquals(720, faceRightRect.height())
        assertTrue(faceRightRect.left > 400)

        // Landscape 1280x720 with visual energy concentrated on the left (columns 2..8 of 32)
        val leftEnergyCrop = CharacterFocusSquareTransformation.computeFocusCropBounds(
            width = 1280,
            height = 720,
            energyProvider = { gridW, gridH ->
                FloatArray(gridW * gridH).also { arr ->
                    for (y in 2 until gridH / 2) {
                        for (x in 2..8) {
                            arr[y * gridW + x] = 100f
                        }
                    }
                }
            }
        )
        assertEquals(720, leftEnergyCrop.width())
        assertEquals(720, leftEnergyCrop.height())
        assertTrue(leftEnergyCrop.left < 180)

        // Portrait 720x1280 fallback favors upper torso rather than bottom edge
        val portraitCrop = CharacterFocusSquareTransformation.computeFocusCropBounds(
            width = 720,
            height = 1280
        )
        assertEquals(720, portraitCrop.width())
        assertEquals(720, portraitCrop.height())
        assertTrue(portraitCrop.top in 100..220)
    }

    @Test
    fun `skin voice lines resolve signature themes heroes and fallbacks`() {
        val gusionQuote = SkinVoiceLines.quoteFor("Gusion", "Cosmic Gleam", SkinRarity.LEGEND, "Assassin")
        val miyaQuote = SkinVoiceLines.quoteFor("Miya", "Modena Butterfly", SkinRarity.LEGEND, "Marksman")
        val heroFallbackQuote = SkinVoiceLines.quoteFor("Tigreal", "Fallen Guard", SkinRarity.EPIC, "Tank")
        val genericFallback = SkinVoiceLines.quoteFor("CustomHero", "Crimson Dawn", SkinRarity.COLLECTOR, "Fighter")
        val explicitApiQuote = SkinVoiceLines.quoteFor(
            heroName = "Miya",
            skinName = "Moonlight",
            rarity = SkinRarity.COMMON,
            role = "Marksman",
            apiQuote = "Arrows sing in the pale moonlight."
        )

        assertTrue(gusionQuote.contains("cosmic", ignoreCase = true))
        assertTrue(miyaQuote.contains("wings", ignoreCase = true))
        assertNotEquals(gusionQuote, miyaQuote)
        assertTrue(heroFallbackQuote.isNotBlank())
        assertTrue(genericFallback.contains("CustomHero"))
        assertEquals("Arrows sing in the pale moonlight.", explicitApiQuote)
    }

    @Test
    fun `handles empty payload gracefully`() {
        assertTrue(SkinJsonParser.parseSkins("").isEmpty())
        assertTrue(SkinJsonParser.parseSkins("{}").isEmpty())
    }
}
