package com.core.services.player

import java.util.*

/**
 * Simple thread safe stop watch.
 *
 * @author Abdullah Ayman on 22/06/2020
 */
class StopWatch {
    /**
     * The time the stop watch was last started.
     */
    private var startTime: Long = 0
    /**
     * The time elapsed before the current [.startTime].
     */
    private var previousElapsedTime: Long = 0
    /**
     * Whether the stop watch is currently running or not.
     */
    private var isRunning = false

    /**
     * Starts or continues the stop watch.
     *
     * @see .pause
     * @see .reset
     */
    fun start() {
        synchronized(this) {
            startTime = System.currentTimeMillis()
            isRunning = true
        }
    }

    /**
     * Pauses the stop watch. It can be continued later from [.start].
     *
     * @see .start
     * @see .reset
     */
    fun pause() {
        synchronized(this) {
            previousElapsedTime += System.currentTimeMillis() - startTime
            isRunning = false
        }
    }

    /**
     * Stops and resets the stop watch to zero milliseconds.
     *
     * @see .start
     * @see .pause
     */
    fun reset() {
        synchronized(this) {
            startTime = 0
            previousElapsedTime = 0
            isRunning = false
        }
    }

    /**
     * @return the total elapsed time in milliseconds
     */
    val elapsedTime: Long
        get() {
            synchronized(this) {
                var currentElapsedTime: Long = 0
                if (isRunning) {
                    currentElapsedTime = System.currentTimeMillis() - startTime
                }
                return previousElapsedTime + currentElapsedTime
            }
        }

    override fun toString(): String {
        return String.format(Locale.getDefault(), "%d millis", elapsedTime)
    }
}