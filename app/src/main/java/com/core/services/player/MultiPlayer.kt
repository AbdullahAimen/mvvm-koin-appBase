package com.core.services.player

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.PowerManager
import android.widget.Toast
import com.core.services.player.callBack.Playback

/**
 * @author Abdullah Ayman on 22/06/2020
 */
class MultiPlayer(context: Context?) : Playback, MediaPlayer.OnErrorListener, OnCompletionListener {

    private var mCurrentMediaPlayer = MediaPlayer()
    private var mContext: Context? = null
    private var callbacks: Playback.PlaybackCallbacks? = null
    private var mIsInitialized = false
    private var audioManager: AudioManager
    private var maxVolume = 1f
    private var curVolume = 6f

    /**
     * Constructor of 'MultiPlayer'
     */
    init {
        this.mContext = context
        mCurrentMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        audioManager = mContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()

    }

    /**
     * Sets the callbacks
     *
     * @param callbacks The callbacks to use
     */
    override fun setCallbacks(callbacks: Playback.PlaybackCallbacks?) {
        this.callbacks = callbacks
    }

    /**
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     * @return True if the player has been prepared and is
     * ready to play, false otherwise
     */
    override fun setDataSource(path: String?): Boolean {
        mIsInitialized = false
        mCurrentMediaPlayer = MediaPlayer()
        mCurrentMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path!!)
        return mIsInitialized
    }

    /**
     * @param player The [MediaPlayer] to use
     * @param path   The path of the file, or the http/rtsp URL of the stream
     * you want to play
     * @return True if the `player` has been prepared and is
     * ready to play, false otherwise
     */
    private fun setDataSourceImpl(player: MediaPlayer, path: String): Boolean {
        if (mContext == null) {
            return false
        }
        try {
            player.reset()
            if (path.startsWith("content://")) {
                player.setDataSource(mContext!!, Uri.parse(path))
            } else {
                player.setDataSource(path)
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            player.prepare()
        } catch (e: Exception) {
            return false
        }
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
        val intent = Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId())
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext!!.packageName)
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        mContext!!.sendBroadcast(intent)
        return true
    }

    /**
     * @return True if the player is ready to go, false otherwise
     */
    override fun isInitialized(): Boolean {
        return mIsInitialized
    }

    /**
     * Starts or resumes playback.
     */
    override fun start(): Boolean {
        return try {
            mCurrentMediaPlayer.start()
            true
        } catch (e: java.lang.IllegalStateException) {
            false
        }
    }

    /**
     * Resets the MediaPlayer to its uninitialized state.
     */
    override fun stop() {
        mCurrentMediaPlayer.reset()
        mIsInitialized = false
    }

    /**
     * Releases resources associated with this MediaPlayer object.
     */
    override fun release() {
        stop()
        mCurrentMediaPlayer.release()
    }

    /**
     * Pauses playback. Call start() to resume.
     */
    override fun pause(): Boolean {
        return try {
            mCurrentMediaPlayer.pause()
            true
        } catch (e: java.lang.IllegalStateException) {
            false
        }
    }

    /**
     * Checks whether the MultiPlayer is playing.
     */
    override fun isPlaying(): Boolean {
        return mIsInitialized && mCurrentMediaPlayer.isPlaying
    }

    /**
     * Gets the duration of the file.
     * @return The duration in milliseconds
     */
    override fun duration(): Int {
        return if (!mIsInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.duration
        } catch (e: java.lang.IllegalStateException) {
            -1
        }
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    override fun position(): Int {
        return if (!mIsInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.currentPosition
        } catch (e: java.lang.IllegalStateException) {
            -1
        }
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     * @return The offset in milliseconds from the start to seek to
     */
    override fun seek(whereto: Int): Int {
        return try {
            mCurrentMediaPlayer.seekTo(whereto)
            whereto
        } catch (e: java.lang.IllegalStateException) {
            -1
        }
    }

    override fun setVolume(v: Float): Boolean {
        return try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, v.toInt(), 0)
            true
        } catch (e: java.lang.IllegalStateException) {
            false
        }
    }

    override fun getMaxVolume(): Float {
        return maxVolume
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    override fun setAudioSessionId(sessionId: Int): Boolean {
        return try {
            mCurrentMediaPlayer.audioSessionId = sessionId
            true
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: IllegalStateException) {
            false
        }
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    override fun getAudioSessionId(): Int {
        return mCurrentMediaPlayer.audioSessionId
    }

    /**
     * {@inheritDoc}
     */
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mIsInitialized = false
        mCurrentMediaPlayer.release()
        mCurrentMediaPlayer = MediaPlayer()
        mCurrentMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK)
        if (mContext != null) {
            Toast.makeText(
                mContext,
                "Couldn't play this song.",
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }

    /**
     * {@inheritDoc}
     */
    override fun onCompletion(mp: MediaPlayer?) {
        if (mp === mCurrentMediaPlayer) {
            mCurrentMediaPlayer.release()
            if (callbacks != null) callbacks!!.onTrackEnded()
        } else {
            if (callbacks != null) callbacks!!.onTrackEnded()
        }
        mIsInitialized = false
    }
}