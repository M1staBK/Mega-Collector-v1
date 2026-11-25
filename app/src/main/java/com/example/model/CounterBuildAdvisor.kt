package com.example.model

/**
 * Counter-item rules used by the Meta tab. Every rule below follows widely published MLBB item guidance:
 *  - anti-heal: Sea Halberd (physical), Glowing Wand (magic), Dominance Ice (aura for frontliners)
 *  - armor / magic defense: Malefic Roar or Malefic Gun (physical), Divine Glaive (magic)
 *  - burst: Athena's Shield (magic), Wind of Nature / Immortality (physical)
 *  - sustained magic damage: Radiant Armor
 *  - crowd control: Tough Boots
 * Necklace of Durance is NOT suggested: it was removed from the game and its anti-heal moved to Glowing Wand.
 */
object CounterBuildAdvisor {

    fun plan(
        heroName: String,
        roles: List<String>,
        specialities: List<String>,
        skillTags: List<String>
    ): CounterBuildPlan {
        val roleSet = roles.map { it.lowercase() }.toSet()
        val traits = (specialities + skillTags).map { it.lowercase() }

        fun has(vararg keys: String) = traits.any { t -> keys.any { k -> t.contains(k) } }

        val isTank = "tank" in roleSet
        val isMage = "mage" in roleSet
        val isMarksman = "marksman" in roleSet
        val isAssassin = "assassin" in roleSet
        val sustain = has("regen", "heal", "lifesteal", "sustain")
        val burst = has("burst", "finisher", "reap") || isAssassin
        val crowdControl = has("crowd control", "initiator") || isTank || skillTags.any { it.equals("CC", true) }
        val tanky = isTank || has("guard", "damage reduction")
        val magicDamage = isMage || has("magic")

        val traitLabels = buildList {
            add(if (magicDamage) "Magic damage" else "Physical damage")
            if (tanky) add("Tanky")
            if (sustain) add("Heavy sustain")
            if (burst) add("Burst damage")
            if (crowdControl) add("Crowd control")
            if (isMarksman) add("Basic-attack carry")
        }

        val items = LinkedHashMap<String, String>()

        if (sustain) {
            items["Sea Halberd"] = "Physical anti-heal: cuts the target's shields and HP regen to 60% for 3s after each hit."
            items["Glowing Wand"] = "Magic anti-heal: same 3s effect for mages (it replaced Necklace of Durance)."
            items["Dominance Ice"] = "Frontline anti-heal aura (within 5 units) that also slows enemy attack speed."
        }
        if (tanky) {
            items["Malefic Roar"] = "Physical penetration that scales against heavy armor: the standard answer to tanks."
            items["Divine Glaive"] = "Magic penetration for mages (about 40%, more against high magic defense)."
        }
        if (burst) {
            if (magicDamage) {
                items["Athena's Shield"] = "Magic shield that reduces magic damage after you are hit: the main answer to magic burst."
            } else {
                items["Wind of Nature"] = "Active makes you immune to physical damage for a moment: breaks physical burst and auto-attacks."
                items["Antique Cuirass"] = "Large Physical Defense for surviving physical burst and basic-attack heroes."
            }
            items["Immortality"] = "Revives you once: insurance against a late-game dive."
        }
        if (magicDamage && !burst) {
            items["Radiant Armor"] = "Magic Defense that is strongest against sustained or damage-over-time magic."
        }
        if (isMarksman) {
            items["Dominance Ice"] = "Reduces nearby enemies' attack speed, which blunts basic-attack carries."
            items["Blade Armor"] = "Reflects part of incoming physical damage back at the attacker."
        }
        if (crowdControl) {
            items["Tough Boots"] = "Boots with Magic Defense that shorten crowd-control effects on you."
        }

        val picked = items.entries.take(6).map { CounterBuildItem(it.key, it.value) }

        val headline = when {
            picked.isEmpty() -> "No special counter items needed against $heroName"
            else -> "How to build against $heroName"
        }
        return CounterBuildPlan(
            headline = headline,
            traits = traitLabels,
            items = picked,
            note = "Rule-based advice from the hero's role and skill tags, not live match data. " +
                "Pick one or two of these to fit your own build."
        )
    }
}
