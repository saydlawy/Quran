package com.saydlawy.ultimatemushaf.audio

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.CommandButton
import androidx.media3.session.SessionCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PlaybackSnapshot(
    val verseKey:String?=null,
    val positionMs:Long=0,
    val playing:Boolean=false,
    val speed:Float=1f,
    val repeatMode:Int=0
)

class AudioStateStore(private val context:Context){
    private val prefs=context.getSharedPreferences("audio_state",Context.MODE_PRIVATE)
    private val _state=MutableStateFlow(load())
    val state:StateFlow<PlaybackSnapshot> = _state
    fun save(s:PlaybackSnapshot){
        _state.value=s
        prefs.edit().putString("verse",s.verseKey).putLong("position",s.positionMs)
            .putBoolean("playing",s.playing).putFloat("speed",s.speed)
            .putInt("repeat",s.repeatMode).apply()
    }
    private fun load()=PlaybackSnapshot(
        prefs.getString("verse",null),prefs.getLong("position",0),
        prefs.getBoolean("playing",false),prefs.getFloat("speed",1f),
        prefs.getInt("repeat",0))
}

class QuranAudioService:MediaSessionService(){
    private lateinit var player:ExoPlayer
    private lateinit var session:MediaSession
    private lateinit var store:AudioStateStore

    override fun onCreate(){
        super.onCreate()
        store=AudioStateStore(this)
        player=ExoPlayer.Builder(this).build().apply{
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA).build(),true)
            setHandleAudioBecomingNoisy(true)
            setPlaybackSpeed(store.state.value.speed)
            repeatMode=store.state.value.repeatMode
        }
        session=MediaSession.Builder(this,player)
            .setId("ultimate-mushaf-audio")
            .build()
    }

    fun playVerse(verseKey:String,url:String){
        val item=MediaItem.Builder().setMediaId(verseKey)
            .setUri(url).setMediaMetadata(
                MediaMetadata.Builder().setTitle("Quran $verseKey").build()
            ).build()
        player.setMediaItem(item)
        player.prepare()
        player.play()
        store.save(PlaybackSnapshot(verseKey,0,true,player.playbackParameters.speed,player.repeatMode))
    }

    override fun onGetSession(controllerInfo:MediaSession.ControllerInfo)=session

    override fun onDestroy(){
        store.save(PlaybackSnapshot(
            player.currentMediaItem?.mediaId,player.currentPosition,
            player.isPlaying,player.playbackParameters.speed,player.repeatMode))
        session.release()
        player.release()
        super.onDestroy()
    }
}