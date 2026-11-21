package com.example

import com.example.data.remote.SkinJsonParser
import com.example.model.MegaPackConfig
import com.example.model.PackBundleOption
import com.example.model.QuestCatalog
import com.example.model.SkinRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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
    fun `parses paginated skins envelope and hero detail embedded skins`() {
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
                "role": "Marksman",
                "skins": [
                  { "id": "miya_1", "name": "Modena Butterfly", "tier": "Legend" },
                  { "id": "miya_2", "name": "Moonlight Archer", "tier": "Basic" }
                ]
              }
            }
        """.trimIndent()

        val heroSkins = SkinJsonParser.parseHeroDetailSkins(heroDetailJson)
        assertEquals(2, heroSkins.size)
        assertEquals("Miya", heroSkins[0].heroName)
        assertEquals(SkinRarity.LEGEND, heroSkins[0].rarity)
        assertEquals(SkinRarity.COMMON, heroSkins[1].rarity)
    }

    @Test
    fun `mega pack odds, bundle discounts, and quests remain valid`() {
        val total = MegaPackConfig.RARITY_WEIGHTS.values.sum()
        assertEquals(100.0, total, 0.001)
        assertEquals(0.005, MegaPackConfig.MISPRINT_CHANCE_PERCENT, 0.0001)
        // Ensure a skin with rawTier Basic stays COMMON even if skinName contains "Grand"
        assertEquals(SkinRarity.COMMON, SkinRarity.fromApiTier("Basic", "Grand Blade"))

        // Bundle discount assertions: 5 packs = 5% off, 10 packs = 10% off
        assertEquals(200, PackBundleOption.SINGLE.coinCost)
        assertEquals(950, PackBundleOption.FIVE_PACK.coinCost)
        assertEquals(5, PackBundleOption.FIVE_PACK.discountPercent)
        assertEquals(1800, PackBundleOption.TEN_PACK.coinCost)
        assertEquals(10, PackBundleOption.TEN_PACK.discountPercent)

        // Quest catalog completeness
        assertTrue(QuestCatalog.dailyQuests.isNotEmpty())
        assertTrue(QuestCatalog.weeklyQuests.isNotEmpty())
        assertTrue(QuestCatalog.communityQuests.isNotEmpty())
    }

    @Test
    fun `handles empty payload gracefully`() {
        assertTrue(SkinJsonParser.parseSkins("").isEmpty())
        assertTrue(SkinJsonParser.parseSkins("{}").isEmpty())
    }
}
