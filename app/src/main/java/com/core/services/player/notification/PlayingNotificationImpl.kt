package com.core.services.player.notification

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.core.services.player.PlayerService
import com.koindroid.R
import com.koindroid.screens.landing.LandingActivity


/**
 * @author Abdullah Ayman on 22/06/2020
 */
class PlayingNotificationImpl : PlayingNotification() {
    @Synchronized
    override fun update() {
        stopped = false
        val isPlaying = service!!.isPlaying
        val action = Intent(service, LandingActivity::class.java)
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        action.action = PlayerService.ACTION_ON_CLICK_NOTIFICATION
        val clickIntent = PendingIntent.getActivity(service, 0, action, 0)
        val serviceName = ComponentName(service!!, PlayerService::class.java)
        val intent = Intent(PlayerService.ACTION_QUIT)
        intent.component = serviceName
        val deleteIntent = PendingIntent.getService(service, 0, intent, 0)
        val bitmap =
            BitmapFactory.decodeResource(service!!.resources, R.drawable.ic_launcher_foreground)
        var builder = NotificationCompat.Builder(service!!, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(bitmap)
            .setContentIntent(clickIntent)
            .setDeleteIntent(deleteIntent)
            .setContentTitle(service!!.resources.getString(R.string.app_name))
            .setOngoing(isPlaying)
            .setShowWhen(false)
        builder = addActions(builder)
        if (stopped) return  // notification has been stopped before loading was finished
        updateNotifyModeAndPostNotification(builder.build())


    }

    private fun retrievePlaybackAction(action: String): PendingIntent {
        val serviceName = ComponentName(service!!, PlayerService::class.java)
        val intent = Intent(action)
        intent.component = serviceName
        return PendingIntent.getService(service, 0, intent, 0)
    }

    private fun addActions(builder: NotificationCompat.Builder): NotificationCompat.Builder {
        when (PlayerService.serviceStatus) {
            PlayerService.ACTION_PLAYER_IDLE -> {
                //no action to be added
            }
            PlayerService.ACTION_PLAYER_PREPARED -> {
                builder.addAction(
                    NotificationCompat.Action(
                        0,
                        service!!.getString(R.string.action_play),
                        retrievePlaybackAction(PlayerService.ACTION_PLAYER_PLAY)
                    )
                )
                builder.setContentText("Player is Ready To Play. Tab to open")

            }
            PlayerService.ACTION_PLAYER_PLAY -> {
                builder.addAction(
                    NotificationCompat.Action(
                        0,
                        service!!.getString(R.string.action_pause),
                        retrievePlaybackAction(PlayerService.ACTION_PLAYER_PAUSE)
                    )
                )
                builder.addAction(
                    NotificationCompat.Action(
                        0,
                        service!!.getString(R.string.action_stop),
                        retrievePlaybackAction(PlayerService.ACTION_PLAYER_STOP)
                    )
                )
                builder.setContentText("Player is Playing. Tab to open")
            }
            PlayerService.ACTION_PLAYER_PAUSE -> {
                builder.addAction(
                    NotificationCompat.Action(
                        0,
                        service!!.getString(R.string.action_resume),
                        retrievePlaybackAction(PlayerService.ACTION_PLAYER_RESUME)
                    )
                )
                builder.addAction(
                    NotificationCompat.Action(
                        0,
                        service!!.getString(R.string.action_stop),
                        retrievePlaybackAction(PlayerService.ACTION_PLAYER_STOP)
                    )
                )
                builder.setContentText("Player is Paused. Tab to open")
            }
        }
        return builder
    }

}