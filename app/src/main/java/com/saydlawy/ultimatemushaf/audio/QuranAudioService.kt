package com.saydlawy.ultimatemushaf.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AudioPlaybackState {
    IDLE, PREPARING, PLAYING, PAUSED, BUFFERING, COMPLETED, ERROR
}

data class PlaybackSnapshot(
    val verseKey: String? = null,
    val mediaUri: String? = null,
    val positionMs: Long = 0,
    val playing: Boolean = false,
    val speed: Float = 1f,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

class AudioStateStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("audio_state_v2", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(load())
    val state: StateFlow<PlaybackSnapshot> = _state

    fun save(snapshot: PlaybackSnapshot) {
        _state.value = snapshot
        prefs.edit()
            .putString("verse", snapshot.verseKey)
            .putString("uri", snapshot.mediaUri)
            .putLong("position", snapshot.positionMs)
            .putBoolean("playing", snapshot.playing)
            .putFloat("speed", snapshot.speed)
            .putInt("repeat", snapshot.repeatMode)
            .apply()
    }

    private fun load(): PlaybackSnapshot = PlaybackSnapshot(
        verseKey = prefs.getString("verse", null),
        mediaUri = prefs.getString("uri", null),
        positionMs = prefs.getLong("position", 0L),
        playing = prefs.getBoolean("playing", false),
        speed = prefs.getFloat("speed", 1f),
        repeatMode = prefs.getInt("repeat", Player.REPEAT_MODE_OFF)
    )
}

/**
 * Process-independent Quran playback owner.
 *
 * The Activity never owns ExoPlayer. The service owns the player and MediaSession,
 * so process recreation, notification controls, Bluetooth controls and audio
 * focus are all handled through the same playback authority.
 */
class QuranAudioService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var session: MediaSession
    private lateinit var store: AudioStateStore

    private val _playbackState = MutableStateFlow(AudioPlaybackState.IDLE)
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState

    override fun onCreate() {
        super.onCreate()
        store = AudioStateStore(this)

        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
                setPlaybackSpeed(store.state.value.speed)
                repeatMode = store.state.value.repeatMode
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        _playbackState.value = when (state) {
                            Player.STATE_BUFFERING -> AudioPlaybackState.BUFFERING
                            Player.STATE_READY -> if (isPlaying) {
                                AudioPlaybackState.PLAYING
                            } else {
                                AudioPlaybackState.PAUSED
                            }
                            Player.STATE_ENDED -> AudioPlaybackState.COMPLETED
                            else -> AudioPlaybackState.IDLE
                        }
                        persist()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playbackState.value = if (isPlaying) {
                            AudioPlaybackState.PLAYING
                        } else if (_playbackState.value != AudioPlaybackState.COMPLETED) {
                            AudioPlaybackState.PAUSED
                        } else {
                            AudioPlaybackState.COMPLETED
                        }
                        persist()
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        _playbackState.value = AudioPlaybackState.ERROR
                        persist()
                    }
                })
            }

        session = MediaSession.Builder(this, player)
            .setId("ultimate-mushaf-audio")
            .build()

        restoreLastPlayback()
    }

    private fun restoreLastPlayback() {
        val saved = store.state.value
        val uri = saved.mediaUri ?: return
        val verse = saved.verseKey ?: return

        val item = MediaItem.Builder()
            .setMediaId(verse)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("القرآن الكريم • $verse")
                    .setArtist("Ultimate Mushaf")
                    .build()
            )
            .build()

        player.setMediaItem(item, saved.positionMs)
        player.prepare()

        // Do not unexpectedly start audio merely because the service was recreated.
        // Explicit system playback-resumption can start it later.
        if (saved.playing) {
            player.pause()
        }
    }

    fun playVerse(verseKey: String, url: String) {
        _playbackState.value = AudioPlaybackState.PREPARING
        val item = MediaItem.Builder()
            .setMediaId(verseKey)
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("القرآن الكريم • $verseKey")
                    .setArtist("Ultimate Mushaf")
                    .build()
            )
            .build()

        player.setMediaItem(item, 0L)
        player.prepare()
        player.play()
        persist()
    }

    fun seekTo(positionMs: Long) = player.seekTo(positionMs.coerceAtLeast(0L))

    fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed.coerceIn(0.5f, 2f))
        persist()
    }

    fun setRepeatMode(mode: Int) {
        player.repeatMode = mode
        persist()
    }

    private fun persist() {
        if (!::player.isInitialized || !::store.isInitialized) return
        store.save(
            PlaybackSnapshot(
                verseKey = player.currentMediaItem?.mediaId,
                mediaUri = player.currentMediaItem?.localConfiguration?.uri?.toString(),
                positionMs = player.currentPosition,
                playing = player.isPlaying,
                speed = player.playbackParameters.speed,
                repeatMode = player.repeatMode
            )
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        // Keep active playback alive; MediaSessionService manages the foreground
        // service while playback is ongoing.
        if (!player.isPlaying) stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        persist()
        session.release()
        player.release()
        super.onDestroy()
    }
}
