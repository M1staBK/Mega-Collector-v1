package com.example

import com.example.data.remote.SkinJsonParser
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
    fun `parses top-level array with standard fields`() {
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
              }
            ]
        """.trimIndent()

        val result = SkinJsonParser.parseSkins(json)
        assertEquals(2, result.size)
        assertEquals("Cosmic Gleam", result[0].skinName)
        assertEquals("Gusion", result[0].heroName)
        assertEquals(SkinRarity.LEGEND, result[0].rarity)
        assertEquals("https://mlbbdex.com/images/gusion_cosmic.jpg", result[0].imageUrl)

        assertEquals("Night Owl", result[1].skinName)
        assertEquals(SkinRarity.COLLECTOR, result[1].rarity)
        assertEquals("https://cdn.mlbbdex.com/night_owl.png", result[1].imageUrl)
    }

    @Test
    fun `parses wrapped data envelope with nested hero and tier objects`() {
        val json = """
            {
              "data": [
                {
                  "skin_id": 42,
                  "title": "S.A.B.E.R. Breacher",
                  "hero": { "name": "Johnson", "role": "Tank" },
                  "tier": { "name": "Epic" },
                  "images": { "splash": "https://mlbbdex.com/saber.webp" }
                }
              ]
            }
        """.trimIndent()

        val result = SkinJsonParser.parseSkins(json)
        assertEquals(1, result.size)
        assertEquals("42", result[0].id)
        assertEquals("Johnson", result[0].heroName)
        assertEquals("Tank", result[0].role)
        assertEquals(SkinRarity.EPIC, result[0].rarity)
        assertEquals("https://mlbbdex.com/saber.webp", result[0].imageUrl)
    }

    @Test
    fun `handles empty payload gracefully`() {
        assertTrue(SkinJsonParser.parseSkins("").isEmpty())
        assertTrue(SkinJsonParser.parseSkins("{}").isEmpty())
    }
}
