package com.core.utilities

import io.reactivex.Scheduler
/**
 * @author Abdullah Ayman on 23/06/2020
 */
open class AppSchedulers(
    private val ui: Scheduler,
    private val io: Scheduler,
    private val computation: Scheduler
) {
    fun uiScheduler(): Scheduler {
        return ui
    }

    fun ioScheduler(): Scheduler {
        return io
    }

    fun computationScheduler(): Scheduler {
        return computation
    }
}