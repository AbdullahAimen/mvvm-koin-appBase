package com.core.services.player

/**
 * @author Abdullah Ayman on 27/06/2020
 */
sealed class PlayerServiceCommand {
    object PlayerServiceCreated : PlayerServiceCommand()
    object PlayerServiceDestroyed : PlayerServiceCommand()
    object RecordReady : PlayerServiceCommand()
    object RecordPaused : PlayerServiceCommand()
    object RecordResumed : PlayerServiceCommand()
    object RecordStop : PlayerServiceCommand()
    object UpdateVolume : PlayerServiceCommand()
    class RecordProgressUpdate(val position: Int) : PlayerServiceCommand()
    class StopWatchUpdate(val elapsedTime: Long) : PlayerServiceCommand()
}