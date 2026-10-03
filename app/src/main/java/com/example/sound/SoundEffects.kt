package com.example.sound

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

object SoundEffects {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
        } catch (e: Exception) {
            Log.w("SoundEffects", "Could not initialize ToneGenerator", e)
        }
    }

    fun playSentChime(enabled: Boolean = true) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playReceivedChime(enabled: Boolean = true) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playRingtone(enabled: Boolean = true) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun playActionTick(enabled: Boolean = true) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
