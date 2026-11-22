package com.example.model

enum class QuizQuestionType(val label: String) {
    SKIN_IDENTITY("Skin Knowledge"),
    MAP_KNOWLEDGE("Map Knowledge")
}

data class QuizQuestion(
    val id: String,
    val type: QuizQuestionType,
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val imageUrl: String = "",
    val skinRarity: SkinRarity? = null,
    val explanation: String = ""
)

object QuizQuestionBank {
    private val mapKnowledgePool = listOf(
        QuizQuestion(
            id = "map_turtle_spawn",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "At what match timestamp does the first Turtle spawn on the Land of Dawn?",
            options = listOf("1:00", "2:00", "3:00", "5:00"),
            correctAnswer = "2:00",
            explanation = "The first Turtle spawns at 2:00 in the river near the EXP Lane side."
        ),
        QuizQuestion(
            id = "map_lord_spawn",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "When does the first Lord replace the Turtle pit?",
            options = listOf("6:00", "8:00", "10:00", "12:00"),
            correctAnswer = "8:00",
            explanation = "The Neutral Lord spawns at 8:00, replacing the Turtle on the map."
        ),
        QuizQuestion(
            id = "map_lord_evolve",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "At what match time does the Luminous Lord evolve into an Enhanced Lord with lightning attacks?",
            options = listOf("8:00", "10:00", "12:00", "18:00"),
            correctAnswer = "12:00",
            explanation = "At 12:00 the Lord becomes Enhanced; at 18:00 it becomes the Evolved Lord with stronger siege buffs."
        ),
        QuizQuestion(
            id = "map_lithowanderer",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "What bonus does defeating the Lithowanderer (River Sprite) provide in the mid river?",
            options = listOf(
                "Mana regen, movement speed & a walking river ward",
                "Permanent physical lifesteal",
                "Instant level 4 unlock",
                "Outer turret shield restoration"
            ),
            correctAnswer = "Mana regen, movement speed & a walking river ward",
            explanation = "Slaying Lithowanderer grants Walkie Grass (vision ward) plus mana & river speed buff."
        ),
        QuizQuestion(
            id = "map_gold_lane",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Why is the Gold Lane placed opposite the Turtle spawn?",
            options = listOf(
                "Siege minions grant extra Gold & Marksmen farm safely early",
                "Minions there give double EXP instead of Gold",
                "Turrets have no energy shield",
                "Jungle buffs spawn inside Gold Lane"
            ),
            correctAnswer = "Siege minions grant extra Gold & Marksmen farm safely early",
            explanation = "Gold Lane siege minions drop extra Gold; the first Turtle spawns closer to the EXP Lane."
        ),
        QuizQuestion(
            id = "map_turret_shield",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "How long does the Outer Turret Gold Energy Shield stay active in the early game?",
            options = listOf("First 3 minutes", "First 5 minutes", "First 8 minutes", "First 10 minutes"),
            correctAnswer = "First 5 minutes",
            explanation = "Outer Turret Gold Shields reduce early damage and reward Gold on hit for the first 5:00."
        ),
        QuizQuestion(
            id = "map_purple_buff",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Which benefits are granted by the Purple (Molten/Runed) Buff in the Jungle?",
            options = listOf(
                "Cooldown reduction & reduced Mana/Energy cost",
                "True damage burn & slow on basic attacks",
                "25% Attack Speed & Critical Damage",
                "Extra Magic Defense & HP Shield"
            ),
            correctAnswer = "Cooldown reduction & reduced Mana/Energy cost",
            explanation = "Purple Buff cuts skill cooldowns and Mana/Energy costs, plus heals on hero takedowns."
        ),
        QuizQuestion(
            id = "map_orange_buff",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "What effect does the Orange Buff (Fiend) give to your attacks?",
            options = listOf(
                "Adaptive Attack, bonus True Damage & slow on hit",
                "Extra Turret Armor penetration",
                "Faster Recall channel time",
                "40% Cooldown Reduction"
            ),
            correctAnswer = "Adaptive Attack, bonus True Damage & slow on hit",
            explanation = "Orange Buff boosts Adaptive Attack and applies a burning True Damage + slow effect."
        ),
        QuizQuestion(
            id = "map_roam_boots",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "When a Roamer equips Roaming Boots (Conceal/Favor/Encourage), how does Gold/EXP sharing work for the first 8 minutes?",
            options = listOf(
                "Roamer doesn't take Gold/EXP from nearby minions or creeps",
                "Roamer absorbs 50% of the jungler's EXP",
                "Roamer gains double minion last-hit Gold",
                "Nearby allies receive zero EXP"
            ),
            correctAnswer = "Roamer doesn't take Gold/EXP from nearby minions or creeps",
            explanation = "Thriving lets the lowest-gold teammate gain passive Gold/EXP without taxing laners."
        ),
        QuizQuestion(
            id = "map_cyclone_eye",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Where does the Cyclone Eye / Wind Flow launch pad spawn on the Sanctuary map?",
            options = listOf(
                "Outside the allied & enemy Jungle walls for fast rotation",
                "Directly under the Enemy Fountain",
                "Only inside the Lord pit",
                "Exclusive to Brawl mode"
            ),
            correctAnswer = "Outside the allied & enemy Jungle walls for fast rotation",
            explanation = "Stepping on the Cyclone Eye propels heroes rapidly across jungle lanes."
        ),
        QuizQuestion(
            id = "map_exp_lane",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Which lane receives boosted Experience from Siege Minions to hit Level 4 before the first Turtle fight?",
            options = listOf("EXP Lane", "Gold Lane", "Mid Lane", "River Mid"),
            correctAnswer = "EXP Lane",
            explanation = "EXP Lane Siege Minions grant bonus EXP so Fighters reach Level 4 for the 2:00 Turtle."
        ),
        QuizQuestion(
            id = "map_inhibitor_super",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "What happens when an enemy Inhibitor (Base) Turret is destroyed?",
            options = listOf(
                "Enhanced Super Minions spawn in that lane",
                "All jungle creeps disappear permanently",
                "The Lord immediately respawns",
                "Turrets gain temporary invulnerability"
            ),
            correctAnswer = "Enhanced Super Minions spawn in that lane",
            explanation = "Destroying an Inhibitor Turret upgrades allied waves in that lane into tanky Super Minions."
        ),
        QuizQuestion(
            id = "map_jungle_invade",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "During the first 2 minutes, what protective aura applies inside your own Jungle?",
            options = listOf(
                "Allied Junglers gain extra damage reduction inside their own jungle",
                "Enemy invaders are stunned for 3 seconds",
                "All jungle monsters deal zero damage",
                "Purple buff turns invisible"
            ),
            correctAnswer = "Allied Junglers gain extra damage reduction inside their own jungle",
            explanation = "Early Jungle protection reduces incoming damage on home turf to curb level-1 cheese invades."
        ),
        QuizQuestion(
            id = "map_retribution",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "How many Jungle creep/hero charges are needed to unlock the upgraded Blessing on Retribution?",
            options = listOf("5 stacks", "10 stacks", "15 stacks", "25 stacks"),
            correctAnswer = "5 stacks",
            explanation = "Slaying 5 creeps or scoring kill/assist stacks upgrades Retribution into Ice, Flame, or Bloody."
        ),
        QuizQuestion(
            id = "map_sanctuary_lord",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Which lane does an allied Lord automatically march down after being summoned?",
            options = listOf(
                "The lane with the fewest intact enemy turrets (weakest lane)",
                "Always the Mid Lane",
                "Always the EXP Lane",
                "Randomly chosen at 8:00"
            ),
            correctAnswer = "The lane with the fewest intact enemy turrets (weakest lane)",
            explanation = "Lord targets the enemy lane with the lowest remaining turret defense."
        ),
        QuizQuestion(
            id = "map_turtle_shield",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Besides teamwide Gold and EXP, what immediate reward does the Turtle slayer receive?",
            options = listOf(
                "A personal Shield & Adaptive Attack/Defense buff",
                "Free Blink spell reset",
                "Instant Inhibitor turret destruction",
                "Permanent double movement speed"
            ),
            correctAnswer = "A personal Shield & Adaptive Attack/Defense buff",
            explanation = "Every teammate gets Gold/EXP/small shield, while the slayer gets a stronger Turtle shield."
        ),
        QuizQuestion(
            id = "map_rickety_crab",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "What drops in the side lane near the Crabs (Gold Buff) to help sustain or earn early gold?",
            options = listOf(
                "Gold Coins bonus over time",
                "Permanent True Damage",
                "Free Level 15 upgrade",
                "Lord summon scroll"
            ),
            correctAnswer = "Gold Coins bonus over time",
            explanation = "Slaying the side-lane Crab grants continuous bonus Gold ticking every second."
        ),
        QuizQuestion(
            id = "map_mid_bush",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Why do Mid Laners clear the very first minion wave as fast as possible?",
            options = listOf(
                "To rotate early for the Lithowanderer fight and side ganks",
                "To destroy the Base before 1:00",
                "To spawn the Lord at 2:00",
                "To disable enemy Recall permanently"
            ),
            correctAnswer = "To rotate early for the Lithowanderer fight and side ganks",
            explanation = "Mid wave priority frees Mages & Roamers to secure river vision and contest Lithowanderer."
        ),
        QuizQuestion(
            id = "map_base_regen",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "How long does the Healing Spring inside your Base Fountain stay effective when taking lethal chase?",
            options = listOf(
                "It rapidly restores HP & Mana and grants a brief speed boost exiting base",
                "It teleports allies straight to Lord pit",
                "It doubles Attack Speed across the map",
                "It locks jungle buffs permanently"
            ),
            correctAnswer = "It rapidly restores HP & Mana and grants a brief speed boost exiting base",
            explanation = "Fountain heals HP & Mana quickly while giving a short Homeguard burst of speed on exit."
        ),
        QuizQuestion(
            id = "map_healing_river_cabbage",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Which neutral creep in the mid river heals nearby allied heroes upon defeat?",
            options = listOf("Lithowanderer", "Infernal Lord", "Molten Fiend", "Scaled Lizard"),
            correctAnswer = "Lithowanderer",
            explanation = "Alongside Walkie Grass and speed, Lithowanderer restores a burst of HP & Mana to the team."
        ),
        QuizQuestion(
            id = "map_inner_turret_def",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "What defense activates on nearby allied turrets if multiple enemy heroes siege together early?",
            options = listOf(
                "Anti-gank Defense Shield reducing siege damage when no allied minions are around",
                "Turrets turn invisible for 15 seconds",
                "Turrets summon a second Turtle",
                "Minions transform into Jungle Creeps"
            ),
            correctAnswer = "Anti-gank Defense Shield reducing siege damage when no allied minions are around",
            explanation = "Without allied minions in range, turrets gain massive damage reduction against backdooring."
        ),
        QuizQuestion(
            id = "map_flameshot_steal",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Why is Retribution crucial during Turtle & Lord pit contests?",
            options = listOf(
                "It deals instant scaled True Damage to secure objectives over normal skills",
                "It makes the Lord fight for the enemy",
                "It freezes turret attacks for 30 seconds",
                "It grants 500 Gold per basic attack"
            ),
            correctAnswer = "It deals instant scaled True Damage to secure objectives over normal skills",
            explanation = "Retribution executes Turtle & Lord cleanly when timed with the objective's HP threshold."
        ),
        QuizQuestion(
            id = "map_winter_crown",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Which active equipment freezes your hero in Stasis for 2 seconds to dodge fatal bursts?",
            options = listOf("Winter Crown", "Wind of Nature", "Immortality", "Rose Gold Meteor"),
            correctAnswer = "Winter Crown",
            explanation = "Winter Crown puts your hero in untargetable Stasis while allowing ongoing channels to continue."
        ),
        QuizQuestion(
            id = "map_wind_of_nature",
            type = QuizQuestionType.MAP_KNOWLEDGE,
            prompt = "Wind of Nature active grants immunity against which damage type?",
            options = listOf(
                "Physical Damage",
                "True Damage",
                "Magic Damage",
                "Turret Laser only"
            ),
            correctAnswer = "Physical Damage",
            explanation = "Wind Chant grants brief immunity to all Physical Damage (lasting longer for Marksmen)."
        )
    )

    const val QUIZ_QUESTION_COUNT = 5

    fun buildHourlyQuiz(catalog: List<SkinCatalogItem>): List<QuizQuestion> {
        val uniqueHeroes = catalog
            .map { it.heroName }
            .distinct()
            .filter { it.isNotBlank() && !it.equals("Legend", ignoreCase = true) }

        val validSkins = catalog
            .filter {
                it.imageUrl.isNotBlank() &&
                    it.heroName.isNotBlank() &&
                    !it.heroName.equals("Legend", ignoreCase = true)
            }
            .shuffled()

        val skinQuestionCount = if (validSkins.size >= 3 && uniqueHeroes.size >= 4) 3 else 0
        val skinQuestions = mutableListOf<QuizQuestion>()

        if (skinQuestionCount > 0) {
            val usedSkinIds = HashSet<String>()
            for (skin in validSkins) {
                if (skinQuestions.size >= skinQuestionCount) break
                if (!usedSkinIds.add(skin.id)) continue
                val distractors = uniqueHeroes
                    .filter { !it.equals(skin.heroName, ignoreCase = true) }
                    .shuffled()
                    .take(3)
                if (distractors.size < 3) continue
                val options = (distractors + skin.heroName).shuffled()
                skinQuestions += QuizQuestion(
                    id = "skin_${skin.id}",
                    type = QuizQuestionType.SKIN_IDENTITY,
                    prompt = "Which hero wears the \"${skin.skinName}\" skin?",
                    options = options,
                    correctAnswer = skin.heroName,
                    imageUrl = skin.imageUrl,
                    skinRarity = skin.rarity,
                    explanation = "${skin.skinName} is a ${skin.rarity.displayName} (${skin.rawTier}) skin for ${skin.heroName}."
                )
            }
        }

        val mapNeeded = QUIZ_QUESTION_COUNT - skinQuestions.size
        val mapQuestions = mapKnowledgePool
            .shuffled()
            .take(mapNeeded)
            .map { q -> q.copy(options = q.options.shuffled()) }

        return (skinQuestions + mapQuestions).shuffled().take(QUIZ_QUESTION_COUNT)
    }
}
