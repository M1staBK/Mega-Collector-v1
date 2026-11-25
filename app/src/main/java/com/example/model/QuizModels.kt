package com.example.model

enum class QuizQuestionType(val label: String) {
    SKIN_IDENTITY("Skin Knowledge"),
    MAP_KNOWLEDGE("Map Knowledge"),
    EQUIPMENT("Equipment"),
    GAME_BASICS("MLBB Basics")
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


    private fun basic(
        id: String,
        type: QuizQuestionType,
        prompt: String,
        correct: String,
        wrong1: String,
        wrong2: String,
        wrong3: String,
        explanation: String
    ) = QuizQuestion(
        id = id,
        type = type,
        prompt = prompt,
        options = listOf(correct, wrong1, wrong2, wrong3),
        correctAnswer = correct,
        explanation = explanation
    )

    private val equipmentPool = listOf(
        basic("eq_winter_crown", QuizQuestionType.EQUIPMENT,
            "Which equipment puts you in Stasis for a short time so you cannot be targeted?",
            "Winter Crown", "Wind of Nature", "Haas's Claws", "Berserker's Fury",
            "Winter Crown's active freezes you in Stasis, dodging damage while it lasts."),
        basic("eq_wind_of_nature", QuizQuestionType.EQUIPMENT,
            "Wind of Nature's active makes you immune to which damage type?",
            "Physical Damage", "Magic Damage", "Turret Damage only", "True Damage only",
            "Wind of Nature grants a short immunity to physical damage."),
        basic("eq_immortality", QuizQuestionType.EQUIPMENT,
            "Which equipment lets you come back to life after dying?",
            "Immortality", "Oracle", "Blade Armor", "Antique Cuirass",
            "Immortality revives the hero once after death, then goes on cooldown."),
        basic("eq_haas_claws", QuizQuestionType.EQUIPMENT,
            "Haas's Claws mainly gives which effect?",
            "Physical Lifesteal", "Cooldown Reduction", "Magic Penetration", "Extra Gold per kill",
            "Haas's Claws is the classic Physical Lifesteal item for sustain."),
        basic("eq_berserker", QuizQuestionType.EQUIPMENT,
            "Berserker's Fury is known for boosting what?",
            "Critical Damage", "Magic Power", "Mana Regen", "Movement Speed on boots",
            "Berserker's Fury adds Physical Attack, crit chance and extra critical damage."),
        basic("eq_blade_despair", QuizQuestionType.EQUIPMENT,
            "Blade of Despair mainly increases which stat?",
            "Physical Attack", "Magic Defense", "Mana", "Cooldown Reduction",
            "Blade of Despair is a big Physical Attack item."),
        basic("eq_holy_crystal", QuizQuestionType.EQUIPMENT,
            "Holy Crystal mainly increases which stat?",
            "Magic Power", "Physical Defense", "Attack Speed", "Physical Lifesteal",
            "Holy Crystal is a Magic Power item for mages."),
        basic("eq_boots_warrior", QuizQuestionType.EQUIPMENT,
            "Which boots give extra Physical Defense?",
            "Warrior Boots", "Arcane Boots", "Rapid Boots", "Magic Shoes",
            "Warrior Boots add Physical Defense on top of movement speed."),
        basic("eq_boots_tough", QuizQuestionType.EQUIPMENT,
            "Which boots give Magic Defense and shorten crowd-control effects on you?",
            "Tough Boots", "Warrior Boots", "Demon Shoes", "Rapid Boots",
            "Tough Boots are the anti-crowd-control boots with Magic Defense."),
        basic("eq_boots_rapid", QuizQuestionType.EQUIPMENT,
            "Rapid Boots give extra what?",
            "Attack Speed", "Magic Power", "Physical Defense", "Gold income",
            "Rapid Boots add Attack Speed, popular on marksmen."),
        basic("eq_boots_arcane", QuizQuestionType.EQUIPMENT,
            "Which boots give extra Magic Penetration?",
            "Arcane Boots", "Tough Boots", "Warrior Boots", "Rapid Boots",
            "Arcane Boots are the mage boots with Magic Penetration."),
        basic("eq_boots_magic_shoes", QuizQuestionType.EQUIPMENT,
            "Magic Shoes are best known for which bonus?",
            "Cooldown Reduction", "Physical Lifesteal", "Extra Attack Speed", "Bonus Gold",
            "Magic Shoes lower your skill cooldowns while adding movement speed."),
        basic("eq_oracle", QuizQuestionType.EQUIPMENT,
            "Oracle strengthens what kind of effects?",
            "Shields and healing", "Critical hits", "Turret damage", "Basic attack range",
            "Oracle boosts the power of shields and healing you give and receive."),
        basic("eq_blade_armor", QuizQuestionType.EQUIPMENT,
            "Blade Armor does what to physical damage?",
            "Reflects part of it back at the attacker", "Turns it into healing", "Doubles it", "Ignores it completely",
            "Blade Armor reflects a portion of incoming physical damage."),
        basic("eq_cursed_helmet", QuizQuestionType.EQUIPMENT,
            "Cursed Helmet damages enemies in what way?",
            "Burns nearby enemies over time", "Stuns them for 3 seconds", "Blinds them", "Steals their gold",
            "Cursed Helmet burns nearby enemies with a damage aura."),
        basic("eq_demon_hunter", QuizQuestionType.EQUIPMENT,
            "Demon Hunter Sword is useful against which kind of target?",
            "High-HP enemies, because it deals damage based on their max HP", "Invisible heroes", "Turrets only", "Low-HP minions",
            "Demon Hunter Sword adds damage that scales with the target's max HP."),
        basic("eq_categories", QuizQuestionType.EQUIPMENT,
            "Which of these is a category in the equipment shop?",
            "Defense", "Summoner", "Cosmetic", "Lord",
            "The shop sorts equipment into Attack, Magic, Defense, Movement, Jungle and Roam."),
        basic("eq_movement_cat", QuizQuestionType.EQUIPMENT,
            "Where do you find boots in the equipment shop?",
            "Movement", "Magic", "Roam", "Attack",
            "All boots are listed in the Movement category."),
        basic("eq_jungle_items", QuizQuestionType.EQUIPMENT,
            "Jungle equipment mainly helps you do what?",
            "Clear monsters faster and gain extra jungle gold", "Heal your turret", "Summon the Lord", "Remove the fog of war",
            "Jungle items such as Hunter-type gear boost jungle clearing and income.")
    )

    private val basicsPool = listOf(
        basic("basic_team_size", QuizQuestionType.GAME_BASICS,
            "How many players are on each team in a classic match?",
            "5", "3", "4", "6",
            "Classic MLBB is a 5v5 battle."),
        basic("basic_lanes", QuizQuestionType.GAME_BASICS,
            "How many lanes are on the Land of Dawn map?",
            "3", "2", "4", "5",
            "There are three lanes: Gold, Mid and EXP."),
        basic("basic_roles", QuizQuestionType.GAME_BASICS,
            "Which of these is NOT a hero role?",
            "Summoner", "Tank", "Mage", "Marksman",
            "The roles are Tank, Fighter, Assassin, Mage, Marksman and Support."),
        basic("basic_tigreal", QuizQuestionType.GAME_BASICS,
            "Tigreal is which role?",
            "Tank", "Marksman", "Mage", "Assassin",
            "Tigreal is a durable crowd-control Tank."),
        basic("basic_layla", QuizQuestionType.GAME_BASICS,
            "Layla is which role?",
            "Marksman", "Tank", "Support", "Fighter",
            "Layla is a long-range Marksman."),
        basic("basic_eudora", QuizQuestionType.GAME_BASICS,
            "Eudora is which role?",
            "Mage", "Tank", "Fighter", "Support",
            "Eudora is a burst Mage known for her lightning."),
        basic("basic_saber", QuizQuestionType.GAME_BASICS,
            "Saber is which role?",
            "Assassin", "Support", "Marksman", "Tank",
            "Saber is a burst Assassin."),
        basic("basic_estes", QuizQuestionType.GAME_BASICS,
            "Estes is which role?",
            "Support", "Fighter", "Assassin", "Marksman",
            "Estes is a healing Support."),
        basic("basic_franco", QuizQuestionType.GAME_BASICS,
            "Which hero is famous for the Iron Hook?",
            "Franco", "Fanny", "Gusion", "Kagura",
            "Franco's Iron Hook pulls enemies toward him."),
        basic("basic_fanny", QuizQuestionType.GAME_BASICS,
            "Which hero uses Steel Cables to swing around the map?",
            "Fanny", "Franco", "Lancelot", "Natalia",
            "Fanny is the cable-swinging Assassin."),
        basic("basic_roles_gold", QuizQuestionType.GAME_BASICS,
            "Which role most often plays in the Gold Lane?",
            "Marksman", "Tank", "Support", "Assassin",
            "Marksmen farm the Gold Lane to buy their core items."),
        basic("basic_exp_lane", QuizQuestionType.GAME_BASICS,
            "Which role most often plays in the EXP Lane?",
            "Fighter", "Support", "Marksman", "Mage",
            "Fighters usually take the EXP Lane."),
        basic("basic_retribution", QuizQuestionType.GAME_BASICS,
            "Which battle spell is meant for the Jungler?",
            "Retribution", "Flicker", "Sprint", "Purify",
            "Retribution is the Jungler's spell for clearing monsters and securing objectives."),
        basic("basic_flicker", QuizQuestionType.GAME_BASICS,
            "Which battle spell teleports you a short distance?",
            "Flicker", "Purify", "Sprint", "Inspire",
            "Flicker instantly blinks your hero a short distance."),
        basic("basic_purify", QuizQuestionType.GAME_BASICS,
            "Which battle spell removes negative effects from your hero?",
            "Purify", "Flicker", "Retribution", "Sprint",
            "Purify clears debuffs and gives brief immunity to them."),
        basic("basic_sprint", QuizQuestionType.GAME_BASICS,
            "Which battle spell gives a burst of movement speed?",
            "Sprint", "Purify", "Retribution", "Flicker",
            "Sprint boosts your movement speed for a few seconds."),
        basic("basic_maniac", QuizQuestionType.GAME_BASICS,
            "A Maniac announcement means you got how many kills in a row quickly?",
            "4", "2", "3", "5",
            "Double Kill is 2, Triple Kill is 3, Maniac is 4 and Savage is 5."),
        basic("basic_savage", QuizQuestionType.GAME_BASICS,
            "A Savage announcement means you got how many quick kills?",
            "5", "3", "4", "6",
            "Savage is five kills in quick succession."),
        basic("basic_first_rank", QuizQuestionType.GAME_BASICS,
            "Which is the lowest ranked-mode tier?",
            "Warrior", "Elite", "Master", "Epic",
            "The ranked ladder starts at Warrior."),
        basic("basic_rank_after_epic", QuizQuestionType.GAME_BASICS,
            "Which ranked tier comes right after Epic?",
            "Legend", "Grandmaster", "Master", "Elite",
            "Order: Warrior, Elite, Master, Grandmaster, Epic, Legend, Mythic."),
        basic("basic_kda", QuizQuestionType.GAME_BASICS,
            "What does KDA stand for?",
            "Kills, Deaths, Assists", "Kills, Damage, Armor", "Kings, Defense, Attack", "Kills, Deaths, Armor",
            "KDA is your Kills, Deaths and Assists score."),
        basic("basic_mvp", QuizQuestionType.GAME_BASICS,
            "What does MVP mean after a match?",
            "Most Valuable Player", "Max Victory Points", "Minion Vision Pack", "Most Visible Player",
            "MVP is awarded to the best performing player."),
        basic("basic_gank", QuizQuestionType.GAME_BASICS,
            "What is a gank?",
            "A surprise attack on an enemy by teammates leaving their lane", "Buying equipment", "Recalling to base", "Destroying a turret",
            "Ganking means sneaking up with teammates to ambush an enemy."),
        basic("basic_roam", QuizQuestionType.GAME_BASICS,
            "A Roamer usually does what?",
            "Moves between lanes to help teammates", "Stays in the Jungle all game", "Never leaves the base", "Only farms the Gold Lane",
            "Roamers travel the map to protect and set up kills."),
        basic("basic_cooldown", QuizQuestionType.GAME_BASICS,
            "What does CD mean in game chat?",
            "Cooldown", "Critical Damage", "Combo Defense", "Camp Delay",
            "CD means cooldown, the wait before a skill can be used again.")
    )

    const val QUIZ_QUESTION_COUNT = 5

    /** Skin questions are gone: every quiz is MLBB basics, equipment and map knowledge. */
    @Suppress("UNUSED_PARAMETER")
    fun buildHourlyQuiz(catalog: List<SkinCatalogItem> = emptyList()): List<QuizQuestion> {
        val equipment = equipmentPool.shuffled().take(2)
        val rest = (basicsPool + mapKnowledgePool).shuffled().take(QUIZ_QUESTION_COUNT - equipment.size)
        return (equipment + rest)
            .map { q -> q.copy(options = q.options.shuffled()) }
            .shuffled()
            .take(QUIZ_QUESTION_COUNT)
    }
}
