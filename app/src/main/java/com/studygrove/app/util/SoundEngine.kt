package com.studygrove.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundEngine(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var musicPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var currentTheme: String? = null
    private var musicVolume = 0f
    private var fadeJob: Job? = null

    fun updateMusic(enabled: Boolean, themeSong: String, timerActive: Boolean) {
        val safeTheme = themeSong.takeIf { it in ThemeSongs } ?: Theme1
        scope.launch {
            if (!enabled) {
                releaseMusic()
                return@launch
            }
            if (!ensureMusicPlayer(safeTheme)) return@launch
            if (timerActive) {
                setLowPassEnabled(true)
                fadeMusicTo(0f)
            } else {
                setLowPassEnabled(false)
                fadeMusicTo(DefaultMusicVolume)
            }
        }
    }

    fun click(enabled: Boolean) {
        // No dedicated click asset exists yet, so generic UI taps stay silent.
    }

    fun planting(enabled: Boolean) {
        if (enabled) playOneShot(PlantingSound)
    }

    fun takeOut(enabled: Boolean) {
        if (enabled) playOneShot(TakeOutSound)
    }

    fun cancelSession(enabled: Boolean) {
        if (enabled) playOneShot(CancelSessionSound)
    }

    fun levelUp(enabled: Boolean) {
        if (enabled) playOneShot(LevelUpSound)
    }

    fun sessionFinished(enabled: Boolean) {
        if (enabled) playOneShot(SessionFinishedSound)
    }

    fun reward(enabled: Boolean) {
        // Purchase reward sounds can be added later without reusing placement audio.
    }

    private fun playOneShot(assetName: String) {
        playOneShotAsset(context, assetName, enabled = true)
    }

    private fun ensureMusicPlayer(themeSong: String): Boolean {
        if (currentTheme == themeSong && musicPlayer != null) return true
        releaseMusic()
        val player = createAssetPlayer(context, "$themeSong.mp3", looping = true) ?: return false
        musicPlayer = player
        currentTheme = themeSong
        musicVolume = 0f
        player.setVolume(0f, 0f)
        player.start()
        runCatching {
            equalizer = Equalizer(0, player.audioSessionId).apply {
                enabled = false
            }
        }
        return true
    }

    private fun fadeMusicTo(targetVolume: Float) {
        val player = musicPlayer ?: return
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val start = musicVolume
            val steps = 56
            repeat(steps) { index ->
                val progress = (index + 1).toFloat() / steps
                val next = start + (targetVolume - start) * progress
                musicVolume = next.coerceIn(0f, DefaultMusicVolume)
                player.setVolume(musicVolume, musicVolume)
                delay(60)
            }
            musicVolume = targetVolume.coerceIn(0f, DefaultMusicVolume)
            player.setVolume(musicVolume, musicVolume)
        }
    }

    private fun setLowPassEnabled(enabled: Boolean) {
        val effect = equalizer ?: return
        runCatching {
            val range = effect.bandLevelRange
            val lowLevel = range[0]
            val flatLevel = 0.toShort()
            for (bandIndex in 0 until effect.numberOfBands.toInt()) {
                val band = bandIndex.toShort()
                val frequencyHz = effect.getCenterFreq(band) / 1000
                val level = if (enabled && frequencyHz >= 900) lowLevel else flatLevel
                effect.setBandLevel(band, level)
            }
            effect.enabled = enabled
        }
    }

    fun release() {
        fadeJob?.cancel()
        releaseMusic()
    }

    private fun releaseMusic() {
        fadeJob?.cancel()
        fadeJob = null
        equalizer?.release()
        equalizer = null
        musicPlayer?.release()
        musicPlayer = null
        currentTheme = null
        musicVolume = 0f
    }

    companion object {
        const val Theme1 = "theme_1"
        const val Theme2 = "theme_2"
        const val Theme3 = "theme_3"
        const val SessionNotificationSound = "session_notification_sound.mp3"

        private const val DefaultMusicVolume = 0.42f
        private const val CancelSessionSound = "cancel_session.mp3"
        private const val PlantingSound = "planting_sound.mp3"
        private const val TakeOutSound = "take_out_sound.mp3"
        private const val LevelUpSound = "level_up_sound.mp3"
        private const val SessionFinishedSound = "session_finished_sound.mp3"
        private val ThemeSongs = setOf(Theme1, Theme2, Theme3)
        private val activeOneShots = mutableSetOf<MediaPlayer>()

        fun playOneShotAsset(
            context: Context,
            assetName: String,
            enabled: Boolean,
            onFinished: (() -> Unit)? = null
        ) {
            if (!enabled) {
                onFinished?.invoke()
                return
            }
            val player = createAssetPlayer(context.applicationContext, assetName, looping = false)
            if (player == null) {
                onFinished?.invoke()
                return
            }
            synchronized(activeOneShots) {
                activeOneShots += player
            }
            fun finish(mediaPlayer: MediaPlayer) {
                synchronized(activeOneShots) {
                    activeOneShots -= mediaPlayer
                }
                mediaPlayer.release()
                onFinished?.invoke()
            }
            player.setOnCompletionListener { finish(it) }
            player.setOnErrorListener { mediaPlayer, _, _ ->
                finish(mediaPlayer)
                true
            }
            player.start()
        }

        private fun createAssetPlayer(context: Context, assetName: String, looping: Boolean): MediaPlayer? =
            runCatching {
                context.assets.openFd(assetName).use { descriptor ->
                    MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                        isLooping = looping
                        prepare()
                    }
                }
            }.getOrNull()
    }
}
