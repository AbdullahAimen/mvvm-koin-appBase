package com.core.services.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.audiofx.AudioEffect
import android.os.*
import android.os.PowerManager.WakeLock
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.core.model.RecordInfo
import com.core.model.RecordInfo.Companion.emptyRecord
import com.core.services.player.callBack.Playback
import com.core.services.player.callBack.Playback.PlaybackCallbacks
import com.core.services.player.notification.PlayingNotification
import com.core.services.player.notification.PlayingNotificationImpl
import com.core.utilities.Event
import com.koindroid.BuildConfig
import com.koindroid.R
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * @author Abdullah Ayman on 22/06/2020
 */
class PlayerService : Service(), PlaybackCallbacks {
    private val playerBinder: IBinder = PlayerBinder()

    private var pausedByTransientLossOfFocus = false
    private var playingNotification: PlayingNotification? = null
    private var wakeLock: WakeLock? = null
    private var playerHandler: PlaybackHandler? = null
    private var musicPlayerHandlerThread: HandlerThread? = null
    private var becomingNoisyReceiverRegistered = false
    private var notHandledMetaChangedForCurrentTrack = false
    private var currentPosition: Int = 0
    private val becomingNoisyReceiverIntentFilter =
        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    var playback: Playback? = null
        private set
    var currentRecord = emptyRecord()
        private set
    var mCurrentVolume: Float = 8f
    var mPlayerServiceCommand: MutableLiveData<Event<PlayerServiceCommand>> = MutableLiveData()

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val action = intent.action
            if (action != null && action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    private val audioFocusListener =
        OnAudioFocusChangeListener { focusChange ->
            playerHandler!!.obtainMessage(FOCUS_CHANGE, focusChange, 0)
                .sendToTarget()
        }

    private fun audioManager(): AudioManager? =
        getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
        wakeLock?.setReferenceCounted(false)
        musicPlayerHandlerThread = HandlerThread("PlaybackHandler")
        musicPlayerHandlerThread!!.start()
        playerHandler = PlaybackHandler(this, musicPlayerHandlerThread!!.looper)
        playback = MultiPlayer(this)
        playback?.setCallbacks(this)
        //TODO add this action to the command
        mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.PlayerServiceCreated))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action != null) {
                when (intent.action.toString()) {
                    ACTION_PLAYER_PLAY -> {
                        recordPlay()
                    }
                    ACTION_PLAYER_PAUSE -> {
                        recordPause()
                    }
                    ACTION_PLAYER_RESUME -> {
                        recordResume()
                    }
                    ACTION_PLAYER_STOP -> {
                        recordStop()
                    }
                }
            }

        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (becomingNoisyReceiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver)
            becomingNoisyReceiverRegistered = false
        }
        quit()
        releaseResources()
        wakeLock!!.release()
        //TODO add this action to the command
        mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.PlayerServiceDestroyed))
    }

    override fun onBind(intent: Intent): IBinder? {
        return playerBinder
    }

    /**
     * Player Remote fun recordPrepare
     * */
    fun recordPrepare(recordInfo: RecordInfo) {
        playerHandler!!.removeMessages(RECORD_RESET)
        currentRecord = recordInfo
        playerHandler!!.obtainMessage(RECORD_RESET).sendToTarget()
    }

    /**
     * Player Remote fun setVolume
     * */
    fun setVolume(volume: Float) {
        mCurrentVolume = volume
        playerHandler!!.obtainMessage(RECORD_VOLUME_UPDATE).sendToTarget()
    }

    /**
     * Player Remote fun recordPlay
     * */
    fun recordPlay() {
        playerHandler!!.obtainMessage(RECORD_PLAY).sendToTarget()
    }

    /**
     * Player Remote fun recordResume
     * */
    fun recordResume() {
        playerHandler!!.obtainMessage(RECORD_RESUME).sendToTarget()
    }

    /**
     * Player Remote fun recordPause
     * */
    fun recordPause() {
        playerHandler!!.obtainMessage(RECORD_PAUSE).sendToTarget()
    }

    /**
     * Player Remote fun recordStop
     * */
    fun recordStop() {
        playerHandler!!.obtainMessage(RECORD_STOP).sendToTarget()
    }

    /**
     * Player Remote fun pause
     * */
    fun pause() {
        playerHandler!!.obtainMessage(RECORD_PAUSE).sendToTarget()
    }

    /**
     * Player Remote fun seek
     * */
    fun seek(millis: Int): Int {
        currentPosition = millis
        playerHandler!!.obtainMessage(RECORD_SEEK).sendToTarget()
        return millis
    }

    /**
     * Player Remote fun seek
     * */
    fun getMaxVolume(): Float = playback!!.getMaxVolume()

    val isPlaying: Boolean
        get() = playback != null && playback!!.isPlaying()
    val recordDurationMillis: Int
        get() = playback!!.duration()
    val audioSessionId: Int
        get() = playback!!.getAudioSessionId()

    private fun quit() {
        pause()
        playingNotification!!.stop()
        closeAudioEffectSession()
        audioManager()!!.abandonAudioFocus(audioFocusListener)
        stopSelf()
    }

    private fun releaseResources() {
        playerHandler!!.removeCallbacksAndMessages(null)
        if (Build.VERSION.SDK_INT >= 18) {
            musicPlayerHandlerThread!!.quitSafely()
        } else {
            musicPlayerHandlerThread!!.quit()
        }
        playback!!.release()
        playback = null
    }

    private fun openCurrent(): Boolean {
        synchronized(this) {
            return try {
                playback!!.setDataSource(getTrackUri(currentRecord))
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun closeAudioEffectSession() {
        val audioEffectsIntent =
            Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playback!!.getAudioSessionId())
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        sendBroadcast(audioEffectsIntent)
    }

    private fun requestFocus(): Boolean {
        return audioManager()!!.requestAudioFocus(
            audioFocusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun initNotification() {
        playingNotification = PlayingNotificationImpl()
        playingNotification!!.init(this)
    }

    private fun updateNotification() {
        if (playingNotification != null) {
            playingNotification!!.update()
        }
    }

    private fun play() {
        //TODO needs refactor
        synchronized(this) {
            if (requestFocus()) {
                if (!playback!!.isPlaying()) {
                    if (!playback!!.isInitialized()) {
                        if (isPlaying) playback!!.release()
                    } else {
                        playback!!.start()
                        if (!becomingNoisyReceiverRegistered) {
                            registerReceiver(
                                becomingNoisyReceiver,
                                becomingNoisyReceiverIntentFilter
                            )
                            becomingNoisyReceiverRegistered = true
                        }
                        if (notHandledMetaChangedForCurrentTrack) {
                            handleChangeInternal(META_CHANGED)
                            notHandledMetaChangedForCurrentTrack = false
                        }
                        notifyChange(PLAY_STATE_CHANGED)
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.audio_focus_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateVolume() {
        try {
            playback!!.setVolume(mCurrentVolume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateStopWatch() {
        mPlayerServiceCommand.postValue(
            Event(
                PlayerServiceCommand.StopWatchUpdate(
                    playback!!.position().toLong()
                )
            )
        )
    }

    private fun notifyChange(what: String) {
        handleChangeInternal(what)
        sendChangeInternal(what)
    }

    private fun sendChangeInternal(what: String) {
        sendBroadcast(Intent(what))
    }

    private fun handleChangeInternal(what: String) {
        when (what) {
            PLAY_STATE_CHANGED -> {
                updateNotification()
            }
            META_CHANGED -> {
                updateNotification()
            }
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
    }

    override fun onTrackEnded() {
        wakeLock!!.acquire(30000)
        playerHandler!!.sendEmptyMessage(RECORD_ENDED)
    }


    private class PlaybackHandler(service: PlayerService, looper: Looper) :
        Handler(looper) {
        private val mService: WeakReference<PlayerService> = WeakReference(service)
        override fun handleMessage(msg: Message) {
            val mMusicService = mService.get() ?: return
            when (msg.what) {
                RECORD_RESET -> {
                    mMusicService.openCurrent()
                    mMusicService.initNotification()
                    mMusicService.mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.RecordReady))
                    mMusicService.updateNotification(ACTION_PLAYER_PREPARED)
                }
                RECORD_RESUME, RECORD_PLAY -> {
                    mMusicService.play()
                    obtainMessage(STOP_WATCH_UPDATE).sendToTarget()
                    obtainMessage(RECORD_PROGRESS_UPDATE).sendToTarget()
                    mMusicService.mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.RecordResumed))
                    mMusicService.updateNotification(ACTION_PLAYER_PLAY)
                }
                RECORD_PAUSE -> if (mMusicService.playback!!.isPlaying()) {
                    mMusicService.playback!!.pause()
                    //TODO add this action to the command
                    mMusicService.mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.RecordPaused))
                    mMusicService.notifyChange(PLAY_STATE_CHANGED)
                    mMusicService.updateNotification(ACTION_PLAYER_PAUSE)
                }
                RECORD_STOP -> {
                    if (mMusicService.playback!!.isPlaying()) mMusicService.playback!!.stop()
                    mMusicService.mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.RecordStop))
                    mMusicService.notifyChange(META_CHANGED)
                    mMusicService.updateNotification(ACTION_PLAYER_IDLE)
                }
                RECORD_SEEK -> {
                    Timber.d("Seek arg: %s", mMusicService.currentPosition)
                    synchronized(this) {
                        try {
                            mMusicService.playback!!.seek(mMusicService.currentPosition)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    obtainMessage(STOP_WATCH_UPDATE).sendToTarget()
                }
                RECORD_ENDED -> {
                    if (mMusicService.playback!!.isPlaying()) mMusicService.playback!!.isPlaying()
                    mMusicService.mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.RecordStop))
                    mMusicService.notifyChange(META_CHANGED)
                    mMusicService.updateNotification(ACTION_PLAYER_IDLE)
                }
                RECORD_VOLUME_UPDATE -> {
                    mMusicService.updateVolume()
                    mMusicService.mPlayerServiceCommand.postValue(Event(PlayerServiceCommand.UpdateVolume))
                }
                RELEASE_WAKELOCK -> mMusicService.releaseWakeLock()
                STOP_WATCH_UPDATE -> {
                    mMusicService.updateStopWatch()
                    sendEmptyMessageDelayed(STOP_WATCH_UPDATE, 500)
                }
                RECORD_PROGRESS_UPDATE -> if (mMusicService.playback!!.isPlaying()) {
                    mMusicService.mPlayerServiceCommand.postValue(
                        Event(
                            PlayerServiceCommand.RecordProgressUpdate(
                                mMusicService.playback!!.position()
                            )
                        )
                    )
                    sendEmptyMessageDelayed(RECORD_PROGRESS_UPDATE, 50)
                }
                FOCUS_CHANGE -> when (msg.arg1) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        Timber.d("AUDIOFOCUS_GAIN: %s", mMusicService.currentPosition)
                        if (!mMusicService.isPlaying && mMusicService.pausedByTransientLossOfFocus) {
                            mMusicService.pause()
                            mMusicService.pausedByTransientLossOfFocus = false
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        val wasPlaying = mMusicService.isPlaying
                        Timber.d("AUDIOFOCUS_LOSS_TRANSIENT: %s", mMusicService.currentPosition)
                        mMusicService.pause()
                        mMusicService.pausedByTransientLossOfFocus = wasPlaying
                    }
                }
            }
        }
    }

    fun updateNotification(action: String) {
        serviceStatus = action
        updateNotification()
    }

    inner class PlayerBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    companion object {
        private const val PACKAGE_NAME = BuildConfig.APPLICATION_ID
        private const val RECORD_RESET = 720
        private const val RECORD_PLAY = 721
        private const val RECORD_PAUSE = 722
        private const val RECORD_RESUME = 723
        private const val RECORD_STOP = 724
        private const val RECORD_ENDED = 725
        private const val RECORD_SEEK = 726
        private const val RECORD_PROGRESS_UPDATE = 727
        private const val STOP_WATCH_UPDATE = 728
        private const val RECORD_VOLUME_UPDATE = 729
        private const val FOCUS_CHANGE = 730
        private const val RELEASE_WAKELOCK = 731

        const val ACTION_PLAYER_PLAY = "$PACKAGE_NAME.PLAYER_PLAY"
        const val ACTION_PLAYER_PAUSE = "$PACKAGE_NAME.PLAYER_PAUSE"
        const val ACTION_PLAYER_RESUME = "$PACKAGE_NAME.PLAYER_RESUME"
        const val ACTION_PLAYER_STOP = "$PACKAGE_NAME.PLAYER_STOP"
        const val ACTION_PLAYER_PREPARED = "$PACKAGE_NAME.PLAYER_PREPARED"
        const val ACTION_PLAYER_IDLE = "$PACKAGE_NAME.PLAYER_IDLE"
        const val ACTION_QUIT = "$PACKAGE_NAME.quitservice"
        const val META_CHANGED = "$PACKAGE_NAME.metachanged"
        const val PLAY_STATE_CHANGED = "$PACKAGE_NAME.playstatechanged"
        const val ACTION_ON_CLICK_NOTIFICATION = "$PACKAGE_NAME.on_click_notification"

        var serviceStatus = ACTION_PLAYER_IDLE

        private fun getTrackUri(record: RecordInfo): String {
            return record.recordUri
        }


    }
}