package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.model.SkinRarity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

class PackSoundEngine(context: Context) {
    private val appContext = context.applicationContext
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    @Volatile
    private var isMuted: Boolean = false

    @Volatile
    private var isLoaded: Boolean = false

    private var clickId: Int = 0
    private var plasticCrinkleId: Int = 0
    private var packTearId: Int = 0
    private var cardFlipId: Int = 0
    private var rareBassId: Int = 0
    private var mythicBassId: Int = 0
    private var rewardChimeId: Int = 0

    @Volatile
    private var lastCrinkleMs: Long = 0L

    init {
        ioScope.launch {
            prepareSoundFiles()
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun playClick() {
        if (isMuted || !isLoaded || clickId == 0) return
        soundPool.play(clickId, 0.42f, 0.42f, 1, 0, 1.0f)
    }

    fun playPlasticCrinkle() {
        if (isMuted || !isLoaded || plasticCrinkleId == 0) return
        val now = System.currentTimeMillis()
        if (now - lastCrinkleMs < 200L) return
        lastCrinkleMs = now
        val rate = 0.94f + Random.nextFloat() * 0.14f
        soundPool.play(plasticCrinkleId, 0.65f, 0.65f, 2, 0, rate)
    }

    fun playPackTear() {
        if (isMuted || !isLoaded || packTearId == 0) return
        soundPool.play(packTearId, 0.85f, 0.85f, 3, 0, 1.0f)
    }

    fun playCardFlip(rarity: SkinRarity, isMisprint: Boolean) {
        if (isMuted || !isLoaded) return
        if (cardFlipId != 0) {
            soundPool.play(cardFlipId, 0.72f, 0.72f, 3, 0, 1.0f)
        }
        when {
            isMisprint || rarity.tierRank >= SkinRarity.COLLECTOR.tierRank -> {
                if (mythicBassId != 0) {
                    soundPool.play(mythicBassId, 0.96f, 0.96f, 5, 0, 1.0f)
                }
            }
            rarity.tierRank >= SkinRarity.EPIC.tierRank -> {
                if (rareBassId != 0) {
                    soundPool.play(rareBassId, 0.88f, 0.88f, 4, 0, 1.0f)
                }
            }
        }
    }

    fun playRewardChime() {
        if (isMuted || !isLoaded || rewardChimeId == 0) return
        soundPool.play(rewardChimeId, 0.65f, 0.65f, 2, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }

    private fun prepareSoundFiles() {
        try {
            val sfxDir = File(appContext.cacheDir, "sfx_v2").apply { mkdirs() }

            val clickFile = ensureWav(sfxDir, "sfx_click.wav") { synthSoftClick() }
            val crinkleFile = ensureWav(sfxDir, "sfx_plastic_crinkle.wav") { synthPlasticCrinkle() }
            val tearFile = ensureWav(sfxDir, "sfx_pack_tear.wav") { synthPackTear() }
            val flipFile = ensureWav(sfxDir, "sfx_card_flip.wav") { synthCardFlip() }
            val rareBassFile = ensureWav(sfxDir, "sfx_rare_bass.wav") { synthRareBass(mythic = false) }
            val mythicBassFile = ensureWav(sfxDir, "sfx_mythic_bass.wav") { synthRareBass(mythic = true) }
            val chimeFile = ensureWav(sfxDir, "sfx_reward_chime.wav") { synthRewardChime() }

            clickId = soundPool.load(clickFile.absolutePath, 1)
            plasticCrinkleId = soundPool.load(crinkleFile.absolutePath, 1)
            packTearId = soundPool.load(tearFile.absolutePath, 1)
            cardFlipId = soundPool.load(flipFile.absolutePath, 1)
            rareBassId = soundPool.load(rareBassFile.absolutePath, 1)
            mythicBassId = soundPool.load(mythicBassFile.absolutePath, 1)
            rewardChimeId = soundPool.load(chimeFile.absolutePath, 1)
            isLoaded = true
        } catch (_: Throwable) {
            isLoaded = false
        }
    }

    private inline fun ensureWav(dir: File, name: String, generator: () -> ShortArray): File {
        val file = File(dir, name)
        if (!file.exists() || file.length() < 64L) {
            val samples = generator()
            writeMono16BitWav(file, samples, SAMPLE_RATE)
        }
        return file
    }

    private fun synthSoftClick(): ShortArray {
        val durationSec = 0.042
        val count = (SAMPLE_RATE * durationSec).toInt()
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 115.0)
            val freq = 1050.0 - 650.0 * (t / durationSec)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val body = sin(phase) * 0.65 + sin(phase * 2.1) * 0.15
            val sample = (body * env * 0.45 * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out[i] = sample.toShort()
        }
        return out
    }

    private fun synthPlasticCrinkle(): ShortArray {
        val durationSec = 0.28
        val count = (SAMPLE_RATE * durationSec).toInt()
        val out = ShortArray(count)
        var prev1 = 0.0
        var prev2 = 0.0
        val crackleCenters = doubleArrayOf(0.02, 0.06, 0.105, 0.145, 0.195, 0.235)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val white = Random.nextDouble(-1.0, 1.0)
            // High-pass differentiated noise for crisp plastic foil texture
            val hp = white - 0.72 * prev1 + 0.18 * prev2
            prev2 = prev1
            prev1 = white

            var burstEnv = 0.08
            for (c in crackleCenters) {
                val dist = (t - c)
                burstEnv += exp(-dist * dist / 0.00022)
            }
            val shimmer = sin(2.0 * PI * 3400.0 * t) * 0.16 * burstEnv
            val overallEnv = sin(PI * (t / durationSec)).coerceAtLeast(0.0)
            val valNorm = (hp * 0.42 * burstEnv + shimmer) * overallEnv
            out[i] = (valNorm.coerceIn(-1.0, 1.0) * 0.65 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun synthPackTear(): ShortArray {
        val durationSec = 0.42
        val count = (SAMPLE_RATE * durationSec).toInt()
        val out = ShortArray(count)
        var lp = 0.0
        var prev = 0.0
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val white = Random.nextDouble(-1.0, 1.0)
            // Band-pass rip sweep
            val hp = white - 0.68 * prev
            prev = white
            val cutoff = 0.22 + 0.55 * progress
            lp += cutoff * (hp - lp)

            // Rapid zipper/foil tear serrations
            val serration = 0.55 + 0.45 * sin(2.0 * PI * (38.0 + 30.0 * progress) * t)
            // Pop release near end
            val popEnv = exp(-((t - 0.34).pow(2)) / 0.0008)
            phase += 2.0 * PI * (240.0 + 180.0 * progress) / SAMPLE_RATE
            val popTone = sin(phase) * popEnv * 0.6

            val attack = (t / 0.025).coerceIn(0.0, 1.0)
            val release = exp(-((t - 0.30).coerceAtLeast(0.0)) * 16.0)
            val valNorm = (lp * serration * 1.15 + popTone) * attack * release
            out[i] = (valNorm.coerceIn(-1.0, 1.0) * 0.85 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun synthCardFlip(): ShortArray {
        val durationSec = 0.19
        val count = (SAMPLE_RATE * durationSec).toInt()
        val out = ShortArray(count)
        var lp = 0.0
        var snapPhase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec
            val white = Random.nextDouble(-1.0, 1.0)
            val alpha = 0.08 + 0.32 * sin(PI * progress)
            lp += alpha * (white - lp)
            val whooshEnv = sin(PI * progress).pow(1.2)

            // Crisp card snap transient near start
            val snapFreq = 190.0 - 110.0 * (t / 0.045).coerceAtMost(1.0)
            snapPhase += 2.0 * PI * snapFreq / SAMPLE_RATE
            val snap = sin(snapPhase) * exp(-t * 65.0) * 0.55

            val valNorm = lp * whooshEnv * 0.9 + snap
            out[i] = (valNorm.coerceIn(-1.0, 1.0) * 0.78 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun synthRareBass(mythic: Boolean): ShortArray {
        val durationSec = if (mythic) 1.35 else 0.95
        val count = (SAMPLE_RATE * durationSec).toInt()
        val out = ShortArray(count)
        var subPhase = 0.0
        var punchPhase = 0.0
        var shimmerPhaseA = 0.0
        var shimmerPhaseB = 0.0
        var shimmerPhaseC = 0.0

        val startSub = if (mythic) 68.0 else 64.0
        val endSub = if (mythic) 31.0 else 35.0

        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / durationSec

            // Deep sub-bass pitch drop
            val subFreq = startSub - (startSub - endSub) * (1.0 - exp(-t * 4.5))
            subPhase += 2.0 * PI * subFreq / SAMPLE_RATE
            val subEnv = (t / 0.012).coerceIn(0.0, 1.0) * exp(-t * if (mythic) 2.2 else 3.2)
            // Fundamental + warm octave/2nd harmonic so mobile speakers feel the bass clearly
            val subWave = (sin(subPhase) * 0.78 + sin(subPhase * 2.0) * 0.32 + sin(subPhase * 3.0) * 0.12) * subEnv

            // Impact punch transient
            val punchFreq = 155.0 * exp(-t * 28.0) + 45.0
            punchPhase += 2.0 * PI * punchFreq / SAMPLE_RATE
            val punchWave = sin(punchPhase) * exp(-t * 32.0) * 0.55

            // Secondary bass pulse for Mythic / Collector / Legend / 1-of-1 Misprint
            val secondBoom = if (mythic && t > 0.22) {
                val t2 = t - 0.22
                sin(subPhase * 0.98) * (t2 / 0.015).coerceIn(0.0, 1.0) * exp(-t2 * 2.5) * 0.45
            } else 0.0

            // Ethereal chime flare behind the bass
            shimmerPhaseA += 2.0 * PI * 523.25 / SAMPLE_RATE
            shimmerPhaseB += 2.0 * PI * (if (mythic) 783.99 else 659.25) / SAMPLE_RATE
            shimmerPhaseC += 2.0 * PI * (if (mythic) 1046.50 else 987.77) / SAMPLE_RATE
            val chimeEnv = (t / 0.06).coerceIn(0.0, 1.0) * exp(-t * if (mythic) 2.6 else 3.8)
            val chime = (sin(shimmerPhaseA) + sin(shimmerPhaseB) * 0.75 + sin(shimmerPhaseC) * 0.5) *
                chimeEnv * if (mythic) 0.18 else 0.12

            val mixed = (subWave + punchWave + secondBoom + chime) * (1.0 - progress * 0.05)
            out[i] = (mixed.coerceIn(-1.0, 1.0) * 0.92 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun synthRewardChime(): ShortArray {
        val durationSec = 0.36
        val count = (SAMPLE_RATE * durationSec).toInt()
        val out = ShortArray(count)
        var phaseA = 0.0
        var phaseB = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            phaseA += 2.0 * PI * 987.77 / SAMPLE_RATE
            val envA = (t / 0.01).coerceIn(0.0, 1.0) * exp(-t * 14.0)
            var noteB = 0.0
            if (t >= 0.09) {
                val tb = t - 0.09
                phaseB += 2.0 * PI * 1479.98 / SAMPLE_RATE
                val envB = (tb / 0.01).coerceIn(0.0, 1.0) * exp(-tb * 11.0)
                noteB = (sin(phaseB) + 0.3 * sin(phaseB * 2.0)) * envB * 0.65
            }
            val noteA = (sin(phaseA) + 0.25 * sin(phaseA * 2.0)) * envA * 0.5
            out[i] = ((noteA + noteB).coerceIn(-1.0, 1.0) * 0.7 * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun writeMono16BitWav(file: File, samples: ShortArray, sampleRate: Int) {
        val dataSize = samples.size * 2
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16) // PCM chunk size
        buffer.putShort(1.toShort()) // PCM format
        buffer.putShort(1.toShort()) // Mono
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2) // Byte rate
        buffer.putShort(2.toShort()) // Block align
        buffer.putShort(16.toShort()) // Bits per sample
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)
        for (s in samples) {
            buffer.putShort(s)
        }
        FileOutputStream(file).use { fos ->
            fos.write(buffer.array())
        }
    }

    companion object {
        private const val SAMPLE_RATE = 44100
    }
}
