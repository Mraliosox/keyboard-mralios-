package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import com.example.R

class SoundManager(private val context: Context) {

    enum class SoundEvent {
        CHARACTER,
        SPACE,
        DELETE,
        ENTER
    }

    companion object {
        const val PROFILE_SYSTEM = "system"
        const val PROFILE_CLICK = "click"
        const val PROFILE_POP = "pop"
        const val PROFILE_TYPEWRITER = "typewriter"
        const val PROFILE_BUBBLE = "bubble"
        const val PROFILE_SAMSUNG = "samsung"
        const val PROFILE_CUSTOM = "custom"
    }

    private var soundPool: SoundPool? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val soundIdMap = mutableMapOf<SoundEvent, Int>()
    private var customSoundId: Int = 0

    private var currentProfile: String = PROFILE_POP
    private var currentCustomUri: String? = null
    private var volume: Float = 0.8f
    private var isMuted: Boolean = false
    private var pendingPreviewEvent: SoundEvent? = null

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attributes)
            .build().apply {
                setOnLoadCompleteListener { sp, sampleId, status ->
                    if (status == 0 && pendingPreviewEvent != null) {
                        val expectedId = soundIdMap[pendingPreviewEvent] ?: customSoundId
                        if (expectedId == sampleId) {
                            sp.play(sampleId, volume, volume, 1, 0, 1.0f)
                            pendingPreviewEvent = null
                        }
                    }
                }
            }
    }

    fun configure(enabled: Boolean, profile: String, volumePercent: Int, customUri: String? = null) {
        this.isMuted = !enabled
        this.volume = (volumePercent.coerceIn(0, 100) / 100f)

        val profileChanged = profile != currentProfile
        val uriChanged = customUri != currentCustomUri

        if (profileChanged || uriChanged || soundIdMap.isEmpty()) {
            this.currentProfile = profile
            this.currentCustomUri = customUri
            loadProfileSounds(profile, customUri)
        }
    }

    private fun loadProfileSounds(profile: String, customUri: String?) {
        val sp = soundPool ?: return

        for (id in soundIdMap.values) {
            try { sp.unload(id) } catch (_: Exception) {}
        }
        soundIdMap.clear()

        if (customSoundId != 0) {
            try { sp.unload(customSoundId) } catch (_: Exception) {}
            customSoundId = 0
        }

        if (profile == PROFILE_SYSTEM) {
            return
        }

        if (profile == PROFILE_CUSTOM && !customUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(customUri)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    customSoundId = sp.load(pfd.fileDescriptor, 0, pfd.statSize, 1)
                    pfd.close()
                }
            } catch (_: Exception) {
                loadProfileSounds(PROFILE_POP, null)
            }
            return
        }

        val resMap = when (profile) {
            PROFILE_CLICK -> mapOf(
                SoundEvent.CHARACTER to R.raw.sound_click_char,
                SoundEvent.SPACE to R.raw.sound_click_space,
                SoundEvent.DELETE to R.raw.sound_click_delete,
                SoundEvent.ENTER to R.raw.sound_click_enter
            )
            PROFILE_POP -> mapOf(
                SoundEvent.CHARACTER to R.raw.sound_pop_char,
                SoundEvent.SPACE to R.raw.sound_pop_space,
                SoundEvent.DELETE to R.raw.sound_pop_delete,
                SoundEvent.ENTER to R.raw.sound_pop_enter
            )
            PROFILE_TYPEWRITER -> mapOf(
                SoundEvent.CHARACTER to R.raw.sound_typewriter_char,
                SoundEvent.SPACE to R.raw.sound_typewriter_space,
                SoundEvent.DELETE to R.raw.sound_typewriter_delete,
                SoundEvent.ENTER to R.raw.sound_typewriter_enter
            )
            PROFILE_BUBBLE -> mapOf(
                SoundEvent.CHARACTER to R.raw.sound_bubble_char,
                SoundEvent.SPACE to R.raw.sound_bubble_space,
                SoundEvent.DELETE to R.raw.sound_bubble_delete,
                SoundEvent.ENTER to R.raw.sound_bubble_enter
            )
            PROFILE_SAMSUNG -> mapOf(
                SoundEvent.CHARACTER to R.raw.sound_samsung_char,
                SoundEvent.SPACE to R.raw.sound_samsung_space,
                SoundEvent.DELETE to R.raw.sound_samsung_delete,
                SoundEvent.ENTER to R.raw.sound_samsung_enter
            )
            else -> mapOf(
                SoundEvent.CHARACTER to R.raw.sound_pop_char,
                SoundEvent.SPACE to R.raw.sound_pop_space,
                SoundEvent.DELETE to R.raw.sound_pop_delete,
                SoundEvent.ENTER to R.raw.sound_pop_enter
            )
        }

        for ((event, resId) in resMap) {
            try {
                val id = sp.load(context, resId, 1)
                soundIdMap[event] = id
            } catch (_: Exception) {}
        }
    }

    fun playSound(event: SoundEvent) {
        if (isMuted) return

        if (currentProfile == PROFILE_SYSTEM) {
            val effect = when (event) {
                SoundEvent.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
                SoundEvent.DELETE -> AudioManager.FX_KEYPRESS_DELETE
                SoundEvent.ENTER -> AudioManager.FX_KEYPRESS_RETURN
                SoundEvent.CHARACTER -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audioManager?.playSoundEffect(effect, volume)
            return
        }

        val sp = soundPool ?: return

        if (currentProfile == PROFILE_CUSTOM && customSoundId != 0) {
            sp.play(customSoundId, volume, volume, 1, 0, 1.0f)
            return
        }

        val soundId = soundIdMap[event] ?: 0
        if (soundId != 0) {
            sp.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun previewSound(profile: String, event: SoundEvent, volumePercent: Int, customUri: String? = null) {
        val needsReload = profile != currentProfile || (profile == PROFILE_CUSTOM && customUri != currentCustomUri)
        configure(enabled = true, profile = profile, volumePercent = volumePercent, customUri = customUri)

        if (profile == PROFILE_SYSTEM) {
            val effect = when (event) {
                SoundEvent.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
                SoundEvent.DELETE -> AudioManager.FX_KEYPRESS_DELETE
                SoundEvent.ENTER -> AudioManager.FX_KEYPRESS_RETURN
                SoundEvent.CHARACTER -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audioManager?.playSoundEffect(effect, volume)
            return
        }

        if (needsReload) {
            pendingPreviewEvent = event
        } else {
            playSound(event)
        }
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Exception) {}
        soundPool = null
        soundIdMap.clear()
        customSoundId = 0
        pendingPreviewEvent = null
    }
}
