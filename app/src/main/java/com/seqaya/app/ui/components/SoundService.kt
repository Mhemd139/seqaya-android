package com.seqaya.app.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the signature wooden-chime sound for success moments.
 *
 * Loads a single wood-marimba sample (`res/raw/chime_wood`) into a SoundPool and
 * triggers it at three pitches:
 *   - C5 (rate 1.00) — NFC transfer success (Add, Locate, Hold, Resume, Reprogram)
 *   - E5 (rate 1.26) — Dry-mapping success (ascending one step)
 *   - G5 (rate 1.50) — Wet-mapping success (top of the CEG triad)
 *
 * Respects silent-mode via [AudioManager.getRingerMode]. Callers can also pass
 * an explicit gate (e.g., reduced-motion preference).
 *
 * **Asset:** `res/raw/chime_wood.ogg` is expected to exist. If the resource is
 * missing at runtime, [play] logs and no-ops rather than throwing — the UX
 * degrades gracefully to silent success. Mo to drop in a CC0 marimba sample
 * (freesound.org) before release; 500 ms mono OGG Vorbis, 48 kHz, ~8 KB.
 */
@Singleton
class SoundService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var chimeSoundId: Int = 0
    private var loaded: Boolean = false

    init {
        // Dynamic lookup: if res/raw/chime_wood.ogg is missing the app still builds,
        // and SoundService simply no-ops at play() time. Replace the resource with a
        // CC0 marimba sample before release.
        val resId = context.resources.getIdentifier("chime_wood", "raw", context.packageName)
        if (resId != 0) {
            try {
                chimeSoundId = pool.load(context, resId, 1)
                pool.setOnLoadCompleteListener { _, _, status ->
                    loaded = status == 0
                    if (!loaded) Log.w(TAG, "Chime load failed, status=$status")
                }
            } catch (t: Throwable) {
                Log.w(TAG, "chime_wood failed to load; sound effects disabled", t)
            }
        } else {
            Log.w(TAG, "res/raw/chime_wood missing; sound effects disabled until asset is added")
        }
    }

    /** Plays the wooden chime at the requested pitch, or no-ops if silenced or unavailable. */
    fun play(pitch: Pitch = Pitch.C5) {
        if (!loaded || chimeSoundId == 0) return
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        pool.play(chimeSoundId, VOLUME, VOLUME, PRIORITY, 0, pitch.rate)
    }

    enum class Pitch(val rate: Float) {
        /** Root — NFC transfer success. */
        C5(1.00f),
        /** Major third — dry mapping success. */
        E5(1.26f),
        /** Perfect fifth — wet mapping success, resolves the CEG triad. */
        G5(1.50f),
    }

    fun shutdown() {
        pool.release()
    }

    private companion object {
        const val TAG = "SoundService"
        const val VOLUME = 0.6f
        const val PRIORITY = 1
    }
}
