package com.core.services.player

import android.app.Activity
import android.content.*
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.core.model.RecordInfo
import com.core.utilities.Event
import java.util.*

/**
 * @author Abdullah Ayman on 22/06/2020
 */
object PlayerRemote {
    private val TAG = PlayerRemote::class.java.simpleName
    private var musicService: PlayerService? = null
    private val mConnectionMap =
        WeakHashMap<Context, ServiceBinder>()

    fun bindToService(
        context: Context,
        callback: ServiceConnection?
    ): ServiceToken? {
        var realActivity = (context as Activity).parent
        if (realActivity == null) {
            realActivity = context
        }
        val contextWrapper = ContextWrapper(realActivity)
        contextWrapper.startService(Intent(contextWrapper, PlayerService::class.java))
        val binder = ServiceBinder(callback)
        if (contextWrapper.bindService(
                Intent().setClass(contextWrapper, PlayerService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            mConnectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            musicService = null
        }
    }

    fun getPlayerServiceCommand(): MutableLiveData<Event<PlayerServiceCommand>>? {
        if (musicService != null) {
            return musicService!!.mPlayerServiceCommand
        }
        return null
    }

    fun prepareRecord(recordInfo: RecordInfo?) {
        if (musicService != null) {
            musicService!!.recordPrepare(recordInfo!!)
            musicService!!.recordPlay()
        }
    }

    fun pauseOrResume() {
        if (isPlaying) musicService!!.recordPause() else {
            if (musicService != null) musicService!!.recordResume()
        }
    }

    fun stopRecord() {
        if (musicService != null) {
            musicService!!.recordStop()
        }
    }

    fun seekTo(millis: Int): Int {
        return if (musicService != null) {
            musicService!!.seek(millis)
        } else -1
    }

    fun updateVolume(value: Float) {
        musicService!!.setVolume(value)
    }

    fun getVolume(): Float = musicService!!.mCurrentVolume

    fun getMaxVolume(): Float = musicService!!.getMaxVolume()

    val isPlaying: Boolean
        get() = musicService != null && musicService!!.isPlaying

    val currentRecord: RecordInfo
        get() = musicService!!.currentRecord

    val recordDuration: Int
        get() = if (musicService != null) {
            musicService!!.recordDurationMillis
        } else -1

    val audioSessionId: Int
        get() = if (musicService != null) {
            musicService!!.audioSessionId
        } else -1

    val isServiceConnected: Boolean
        get() = musicService != null

    class ServiceBinder(private val mCallback: ServiceConnection?) : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder = service as PlayerService.PlayerBinder
            musicService = binder.service
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            musicService = null
        }

    }

    class ServiceToken(var mWrappedContext: ContextWrapper)
}