package com.example.model

import androidx.compose.ui.graphics.Color

enum class ProfileFrameStyle {
    CLASSIC_RING,
    ROYAL_CROWN,
    OBSIDIAN_LIGHTNING,
    EXALTED_FLAME,
    MEGA_AURUM,
    CYBER_PRISM,
    WORLD_SOVEREIGN,
    MYTHIC_LATTICE
}

data class ProfileFrameItem(
    val id: String,
    val name: String,
    val description: String,
    val style: ProfileFrameStyle,
    val diamondCost: Int = 0,
    val achievementUnlockHint: String? = null,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color
) {
    val isAchievementExclusive: Boolean get() = achievementUnlockHint != null
    val isFreeDefault: Boolean get() = diamondCost == 0 && !isAchievementExclusive
}

enum class BinderStyle {
    STUDIO_FROST,
    OBSIDIAN_PACK_EDITION,
    AURUM_GOLD,
    CRIMSON_VELVET,
    EMERALD_LEATHER,
    VOID_PRISM,
    WORLD_ARCHIVE
}

data class CardBinderDesign(
    val id: String,
    val name: String,
    val description: String,
    val style: BinderStyle,
    val diamondCost: Int = 0,
    val achievementUnlockHint: String? = null,
    val coverPrimary: Color,
    val coverSecondary: Color,
    val spineColor: Color,
    val trimGold: Color,
    val ringColor: Color
) {
    val isAchievementExclusive: Boolean get() = achievementUnlockHint != null
    val isFreeDefault: Boolean get() = diamondCost == 0 && !isAchievementExclusive
}

object CosmeticCatalog {
    const val DEFAULT_FRAME_ID = "frame_default"
    const val DEFAULT_BINDER_ID = "binder_default"

    val FRAMES: List<ProfileFrameItem> = listOf(
        ProfileFrameItem(
            id = DEFAULT_FRAME_ID,
            name = "Studio Crest",
            description = "Clean studio bezel matched to your active theme accent.",
            style = ProfileFrameStyle.CLASSIC_RING,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF2557EB),
            glowColor = Color(0xFF93C5FD)
        ),
        ProfileFrameItem(
            id = "frame_royal_crown",
            name = "Gold Sovereign",
            description = "Brushed 24K arena filigree with corner crown crests.",
            style = ProfileFrameStyle.ROYAL_CROWN,
            diamondCost = 14,
            primaryColor = Color(0xFFFACC15),
            secondaryColor = Color(0xFFB45309),
            glowColor = Color(0xFFFEF08A)
        ),
        ProfileFrameItem(
            id = "frame_obsidian_bolt",
            name = "Pack Noir Lightning",
            description = "Obsidian forged bezel crackling with purple & gold shine.",
            style = ProfileFrameStyle.OBSIDIAN_LIGHTNING,
            diamondCost = 22,
            primaryColor = Color(0xFF9333EA),
            secondaryColor = Color(0xFF09070F),
            glowColor = Color(0xFFFACC15)
        ),
        ProfileFrameItem(
            id = "frame_exalted_rose",
            name = "Exalted Rose Wing",
            description = "Radiant rose-magenta wings inspired by Exalted Collector.",
            style = ProfileFrameStyle.EXALTED_FLAME,
            diamondCost = 18,
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFF9D174D),
            glowColor = Color(0xFFFBCFE8)
        ),
        ProfileFrameItem(
            id = "frame_cyber_prism",
            name = "Holo Prism Halo",
            description = "Refractive cyan-violet prism ring for Holo collectors.",
            style = ProfileFrameStyle.CYBER_PRISM,
            diamondCost = 28,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF7C3AED),
            glowColor = Color(0xFFA5F3FC)
        ),
        ProfileFrameItem(
            id = "frame_mega_aurum",
            name = "Mega Yellow Flare",
            description = "Blazing Mega Collector gold-yellow starburst border.",
            style = ProfileFrameStyle.MEGA_AURUM,
            achievementUnlockHint = "Unlock via Hero Roster (15 Unique Heroes) achievement",
            primaryColor = Color(0xFFFACC15),
            secondaryColor = Color(0xFFEAB308),
            glowColor = Color(0xFFFFFBEB)
        ),
        ProfileFrameItem(
            id = "frame_mythic_lattice",
            name = "Mythic Starlight",
            description = "Geometric celestial diamond lattice border.",
            style = ProfileFrameStyle.MYTHIC_LATTICE,
            achievementUnlockHint = "Unlock via Dex Pathfinder (50 Unique Skins) achievement",
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF38BDF8),
            glowColor = Color(0xFFFDE68A)
        ),
        ProfileFrameItem(
            id = "frame_world_sovereign",
            name = "World Collector Crown",
            description = "Crimson and molten gold insignia reserved for elite vaults.",
            style = ProfileFrameStyle.WORLD_SOVEREIGN,
            achievementUnlockHint = "Unlock via Legend Conqueror achievement",
            primaryColor = Color(0xFFDC2626),
            secondaryColor = Color(0xFFF59E0B),
            glowColor = Color(0xFFFEF08A)
        )
    )

    val BINDERS: List<CardBinderDesign> = listOf(
        CardBinderDesign(
            id = DEFAULT_BINDER_ID,
            name = "Sky Studio Folio",
            description = "Clean frosted glass binder synced to your studio theme.",
            style = BinderStyle.STUDIO_FROST,
            coverPrimary = Color(0xFF2557EB),
            coverSecondary = Color(0xFF1E3A5F),
            spineColor = Color(0xFF17264D),
            trimGold = Color(0xFF93C5FD),
            ringColor = Color(0xFFE2E8F0)
        ),
        CardBinderDesign(
            id = "binder_obsidian_pack",
            name = "Obsidian Pack Folio",
            description = "Matte black foil binder laced with violet lightning & gold trim.",
            style = BinderStyle.OBSIDIAN_PACK_EDITION,
            diamondCost = 18,
            coverPrimary = Color(0xFF140F22),
            coverSecondary = Color(0xFF07050C),
            spineColor = Color(0xFF2E1065),
            trimGold = Color(0xFFFACC15),
            ringColor = Color(0xFFFDE047)
        ),
        CardBinderDesign(
            id = "binder_aurum_gold",
            name = "Mega Yellow Vault",
            description = "Stitched gold-yellow tournament leather with metallic clasps.",
            style = BinderStyle.AURUM_GOLD,
            diamondCost = 26,
            coverPrimary = Color(0xFF854D0E),
            coverSecondary = Color(0xFF422006),
            spineColor = Color(0xFFCA8A04),
            trimGold = Color(0xFFFDE047),
            ringColor = Color(0xFFFEF08A)
        ),
        CardBinderDesign(
            id = "binder_crimson_velvet",
            name = "Imperial Crimson",
            description = "Deep ruby velvet sleeve folio with antique bronze hardware.",
            style = BinderStyle.CRIMSON_VELVET,
            diamondCost = 16,
            coverPrimary = Color(0xFF7F1D1D),
            coverSecondary = Color(0xFF3F0D12),
            spineColor = Color(0xFF991B1B),
            trimGold = Color(0xFFFBBF24),
            ringColor = Color(0xFFFDE68A)
        ),
        CardBinderDesign(
            id = "binder_emerald_leather",
            name = "Emerald Grimoire",
            description = "Forest emerald leather binder with silver runic spine.",
            style = BinderStyle.EMERALD_LEATHER,
            diamondCost = 24,
            coverPrimary = Color(0xFF065F46),
            coverSecondary = Color(0xFF022C22),
            spineColor = Color(0xFF047857),
            trimGold = Color(0xFF6EE7B7),
            ringColor = Color(0xFFD1FAE5)
        ),
        CardBinderDesign(
            id = "binder_void_prism",
            name = "Holo Prism Album",
            description = "Iridescent holographic weave engineered for Collector+ foils.",
            style = BinderStyle.VOID_PRISM,
            achievementUnlockHint = "Unlock via Holo Specialist achievement",
            coverPrimary = Color(0xFF3B0764),
            coverSecondary = Color(0xFF0F172A),
            spineColor = Color(0xFF7E22CE),
            trimGold = Color(0xFF38BDF8),
            ringColor = Color(0xFFF0ABFC)
        ),
        CardBinderDesign(
            id = "binder_world_archive",
            name = "Dex Sovereign Archive",
            description = "Royal museum-grade obsidian & gold binder for master collectors.",
            style = BinderStyle.WORLD_ARCHIVE,
            achievementUnlockHint = "Unlock via Dex Sovereign (150 Unique Skins) achievement",
            coverPrimary = Color(0xFF1A1205),
            coverSecondary = Color(0xFF09070B),
            spineColor = Color(0xFFD97706),
            trimGold = Color(0xFFFACC15),
            ringColor = Color(0xFFFFFBEB)
        )
    )

    fun frameById(id: String?): ProfileFrameItem =
        FRAMES.firstOrNull { it.id == id } ?: FRAMES.first()

    fun binderById(id: String?): CardBinderDesign =
        BINDERS.firstOrNull { it.id == id } ?: BINDERS.first()
}
