package com.studygrove.app.util

import android.media.AudioManager
import android.media.ToneGenerator

class SoundEngine {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 35)

    fun click(enabled: Boolean) {
        if (enabled) toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
    }

    fun reward(enabled: Boolean) {
        if (enabled) toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 120)
    }

    fun complete(enabled: Boolean) {
        if (enabled) toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180)
    }

    fun release() {
        toneGenerator.release()
    }
}
