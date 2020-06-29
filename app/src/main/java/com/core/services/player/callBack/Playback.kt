package com.core.services.player.callBack

/**
 * @author Abdullah Ayman on 22/06/2020
 */
interface Playback {
    fun setDataSource(path: String?): Boolean

    fun setCallbacks(callbacks: PlaybackCallbacks?)

    fun isInitialized(): Boolean

    fun start(): Boolean

    fun stop()

    fun release()

    fun pause(): Boolean

    fun isPlaying(): Boolean

    fun duration(): Int

    fun position(): Int

    fun seek(whereto: Int): Int

    fun setVolume(volume: Float): Boolean

    fun getMaxVolume(): Float

    fun setAudioSessionId(sessionId: Int): Boolean

    fun getAudioSessionId(): Int


    interface PlaybackCallbacks {
        fun onTrackEnded()
    }
}