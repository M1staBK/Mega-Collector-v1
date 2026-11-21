package com.example.model

/**
 * Provides authentic MOBA Legends hero and skin-theme voice-line quotes
 * displayed in the Pokemon TCG-inspired flavor strip on each collector card.
 */
object SkinVoiceLines {

    private val skinThemeQuotes: List<Pair<String, String>> = listOf(
        "cosmic gleam" to "Stars shatter beneath the cadence of twin cosmic blades.",
        "modena butterfly" to "Silver wings herald the eclipse of every challenger.",
        "obsidian blade" to "Even the deepest abyss recoils from this demonic edge.",
        "starfall knight" to "Every astral round locks onto fate itself.",
        "angelic agent" to "Precision judgment descends silently from high orbit.",
        "king of hell" to "Chains forged in brimstone never release their prey.",
        "infernal blaze" to "Ash and cinder bow before sovereign fire.",
        "psion of tomorrow" to "Telekinetic resonance amplified beyond all limits.",
        "galactic ticket" to "Next stop: victory across uncharted constellations.",
        "kof" to "Arcade fighting spirit ignites with every combo!",
        "aspirants" to "Mecha-wing synchronization locked at maximum output.",
        "exorcists" to "Ancient seals awaken to banish every phantom.",
        "lightborn" to "Stand firm behind the radiant shield of the empire.",
        "s.a.b.e.r" to "Tactical enforcement unit deployed. Target locked.",
        "v.e.n.o.m" to "Neurotoxin primed. Shadows never strike twice.",
        "dragon tamer" to "Ancient wyrmfire answers only to the fearless.",
        "zodiac" to "Written in starlight, sealed by celestial power.",
        "starlight" to "Spotlight on the arena's brightest legend.",
        "collector" to "A rare masterwork forged only for true legends.",
        "luckybox" to "Fortune favors the boldest warrior on the rift.",
        "summer" to "Sun-drenched waves can't cool down this battle heat!",
        "valentine" to "A swift strike straight through the heart of battle.",
        "christmas" to "Winter bells chime as a festive miracle unfolds.",
        "halloween" to "Lanterns flicker—tonight belongs to the phantoms.",
        "venom" to "Silent fangs strike before the alarm even sounds.",
        "blazing" to "Wildfire spirit rides across the frontier dust.",
        "stun" to "Turn up the beat and electrify the entire stage!",
        "m1" to "Crowned in championship gold on the world stage.",
        "m2" to "Forged in world-class fire for supreme glory.",
        "m3" to "Echoes of champions resound across the Land of Dawn.",
        "m4" to "Rise above the storm to claim the world trophy.",
        "m5" to "Victory blazes brightest under championship spotlights.",
        "prime" to "Peak evolution unlocked—unmatched in every lane."
    )

    private val heroQuotes: Map<String, String> = mapOf(
        "miya" to "Wait till the moonlight pierces through the shadows.",
        "balmond" to "Taste the fury of my crimson axe!",
        "saber" to "Move like the wind, be steady as a rock.",
        "alice" to "Watch your back, or be consumed by scarlet night.",
        "nana" to "Do you want to make friends with Nana?",
        "tigreal" to "A real man never hides in the brush!",
        "alucard" to "Nothing lasts forever—we can change the future.",
        "karina" to "A shadow blade strikes before you hear the whisper.",
        "akai" to "Let's go grab a bite after rolling through battle!",
        "franco" to "I love the smell of gunpowder in the morning!",
        "bane" to "Raise the black flag—the abyssal seas obey me!",
        "bruno" to "Speed is everything when the golden striker attacks!",
        "clint" to "Justice arrives at the speed of a silver bullet.",
        "rafaela" to "Healing prayers fall softly like radiant feathers.",
        "eudora" to "Feel the wrath of heaven's sharpest lightning!",
        "zilong" to "Heroes never fade—spears ready for the charge!",
        "fanny" to "Sir, what's your command? Watch the cables fly!",
        "layla" to "Keep it up until you're as brilliant as the malefic gun!",
        "minotaur" to "My warhammer shakes the very stones of the maze!",
        "lolita" to "This energy shield protects everyone I hold dear.",
        "hayabusa" to "Let me clear the way for my master in shadow.",
        "freya" to "By the northern valkyries, none shall breach our line!",
        "gord" to "Arcane energy surges strictly under my command.",
        "natalia" to "Nothing escapes an unseen assassin in the mist.",
        "kagura" to "One umbrella holds a hundred silent spirits.",
        "chou" to "Wipe out injustice with one decisive dragon kick!",
        "sun" to "Every cloud hides a thousand golden clones!",
        "alpha" to "Sometimes I think about girls—wait, protocol active.",
        "ruby" to "Don't fool with the red hood or meet my scythe!",
        "yi sun-shin" to "Our fleet sails steady—no wave breaks our resolve.",
        "moskov" to "Fear the spear pierced straight from the abyss.",
        "johnson" to "Buckle up! Full throttle straight down mid lane!",
        "cyclops" to "Hey, look at the stars—even giants kneel to magic!",
        "estes" to "May the moonlight mend every wound on the field.",
        "hilda" to "Wilderness warriors never back down from a duel!",
        "aurora" to "Winter is coming, and my frost bows to no one.",
        "lapu-lapu" to "Twin blades unite for the freedom of our shores!",
        "vexana" to "Rise from forgotten ashes to heed my sovereign call.",
        "roger" to "Don't run from the full moon—you can't outrun a wolf!",
        "karrie" to "Lightwheels spin faster than any mortal eye can trace.",
        "gatotkaca" to "Bones of iron, skin of brass—strike if you dare!",
        "harley" to "Watch closely—the greatest trick happens in a flash!",
        "irithel" to "Leo and I hunt as one unstoppable roar!",
        "grock" to "Sleepy stone awakens to crush every siege tower!",
        "argus" to "Immortality belongs to those strong enough to seize it!",
        "odette" to "Sword and melody glide cleanly across Swan Lake.",
        "lancelot" to "Time to witness true handsome swordsmanship!",
        "diggie" to "Hoot-hoot! Time's ticking—watch out for the owls!",
        "hylos" to "Only fools stand in the path of the centaur guard.",
        "zhask" to "The swarm expands until whole worlds bow to Kastiya.",
        "helcurt" to "Night falls—and with it, your final breath.",
        "pharsa" to "My raven's eyes miss nothing from above the clouds.",
        "lesley" to "One shot, one life—never step into my crosshairs.",
        "jawhead" to "System online! Ejecting trouble in three, two, one!",
        "angela" to "Papi-papi-poom! Love and shields are on the way!",
        "gusion" to "Break the limits—every dagger returns on cue!",
        "valir" to "Ignite the sky with unstoppable arcane flames!",
        "martis" to "Three thousand worlds, and not a single worthy foe!",
        "uranus" to "Guardian of the celestial palace—eternally unbroken.",
        "hanabi" to "Scarlet petals bloom brightest behind the shadow ninja.",
        "chang'e" to "Jade rabbit, let's shower them with starlight!",
        "kaja" to "Lightning whips spare none who defy the celestial court.",
        "selena" to "Abyss and moon—both whispers answer only to me.",
        "aldous" to "One punch carries the weight of fallen empires.",
        "claude" to "Dexter, grab the loot—we're vanishing before dawn!",
        "vale" to "Wind carries both gentle songs and fierce tempests.",
        "leomord" to "Barbiel and I ride even beyond death itself.",
        "lunox" to "Order and chaos dance in delicate twilight balance.",
        "hanzo" to "The strongest forbidden ninjutsu hungers for shadows.",
        "belerick" to "Ancient roots guard every sacred spark of life.",
        "kimmy" to "Wait till you see what my chemical cannon mixes next!",
        "thamuz" to "Burn everything to cinders in abyssal lava!",
        "harith" to "Time and space bend right at my fingertips!",
        "minsitthar" to "Royal guards assemble—glory to the Golden Kingdom!",
        "kadita" to "The roaring ocean swallows anyone mocking the deep.",
        "faramis" to "Death is merely an open doorway for my alchemy.",
        "badang" to "Fists like roaring thunder shatter every defense!",
        "khufra" to "The desert tyrant awakens—bow before crimson bandages!",
        "granger" to "Listen closely to the requiem played on twin strings.",
        "guinevere" to "Gravity bends gracefully around true royal magic!",
        "esmeralda" to "Starmoon casket shines brightest when destinies collide.",
        "terizla" to "Every swing of this hammer echoes forgotten vengeance.",
        "x.borg" to "Turn up the heat! Time to ignite the whole lane!",
        "ling" to "Wall to wall, light as mist—try catching the wind!",
        "dyrroth" to "Abyssal fury tears weaklings apart in seconds!",
        "lylia" to "Gloom, hop out and make a booming surprise!",
        "baxia" to "Spinning shields roll straight through enemy formations!",
        "masha" to "Bear claws tear clean through cold northern iron!",
        "wanwan" to "Crossbow whistling—catch me if you even can!",
        "silvanna" to "Light of the Moniyan Empire guides every lance thrust!",
        "cecilion" to "Under the blood moon, eternity awaits with Carmilla.",
        "carmilla" to "Crimson blossoms bloom strongest when hearts unite.",
        "atlas" to "Tectonic chains drag monsters straight from the trench.",
        "popol and kupa" to "Kupa and I are the fiercest duo in the frozen north!",
        "yu zhong" to "I would rather betray the world than let it betray me!",
        "luo yi" to "Yin and Yang turn chaos cleanly into harmony.",
        "benedetta" to "This blade guards what shadows try to erase.",
        "khaleed" to "Golden sands rise cleanly at the desert prince's call.",
        "barats" to "Detona's hungry—keep those snacks coming!",
        "brody" to "Abyssal pain fuels every unstoppable strike.",
        "yve" to "The cosmos aligns purely under my starlight board.",
        "mathilda" to "Soaring like free spirits across the emerald plains.",
        "paquito" to "One clean uppercut ends every heavy brawl!",
        "gloo" to "Stick together! The more of us, the merrier!",
        "beatrix" to "Four weapons loaded—which melody do you prefer?",
        "natan" to "Through twisted timelines, hope still finds a path.",
        "aulus" to "A small warrior swings the heaviest battle axe!",
        "aamon" to "Shards cloak every step of House Paxley's quiet heir.",
        "valentina" to "Your finest ultimate looks much better in my hands.",
        "edith" to "Primal lightning awakens inside ancient titan armor!",
        "floryn" to "Hold onto Hope—every tiny sprout brings new light!",
        "yin" to "Step into my domain and settle this one-on-one!",
        "melissa" to "Cuddles, Muddles—let's stitch this duel together!",
        "xavier" to "Light pierces furthest when darkness grows deepest.",
        "julian" to "Chains bind the past, never the resolve ahead.",
        "fredrinn" to "Every treasure has a price—are you ready to pay?",
        "joy" to "Catch the rhythm! Lightning sparkles on every dash!",
        "arlott" to "Twin spears strike where demons dare not tread.",
        "novaria" to "Astral spheres reveal secrets scattered among the stars.",
        "ixia" to "Sand-blaster calibrated—time for an explosive showcase!",
        "nolan" to "A cosmic rift opens whenever this blade cuts space.",
        "cici" to "Spin the yo-yo—the spotlight belongs to the stage!",
        "chip" to "Portal open! Shortcut delivery straight to the teamfight!",
        "zhuxin" to "Butterfly lanterns carry dreams drifting in midnight mist.",
        "suyou" to "Immortal spirit sharpens both mortal blade and resolve.",
        "lukas" to "Roar of the sacred beast echoes across the valley!"
    )

    fun quoteFor(
        heroName: String,
        skinName: String,
        rarity: SkinRarity,
        role: String,
        apiQuote: String? = null
    ): String {
        val cleanedApi = apiQuote?.trim().orEmpty()
        if (cleanedApi.length >= 12) return cleanedApi

        val skinLower = skinName.lowercase()
        for ((keyword, quote) in skinThemeQuotes) {
            if (skinLower.contains(keyword)) return quote
        }

        val heroLower = heroName.trim().lowercase()
        heroQuotes[heroLower]?.let { return it }
        heroQuotes.entries.firstOrNull { heroLower.contains(it.key) }?.let { return it.value }

        val cleanRole = role.substringBefore("/").trim().ifBlank { "Legend" }
        return when (rarity) {
            SkinRarity.LEGEND -> "Mythic ${heroName} awakens as a pinnacle ${cleanRole} legend."
            SkinRarity.COLLECTOR -> "Masterwork armor crafted exclusively for ${heroName}'s legacy."
            SkinRarity.LUCKYBOX -> "Rare vault treasure unleashing ${heroName}'s hidden might."
            SkinRarity.EPIC -> "Battlefield radiance surges with every ${cleanRole} strike."
            SkinRarity.SPECIAL -> "Signature ${skinName} flair illuminates the Land of Dawn."
            SkinRarity.UNCOMMON -> "Tempered steel ready for ${heroName}'s next clash."
            SkinRarity.COMMON -> "True courage shines brightest in the heart of combat."
        }
    }
}
