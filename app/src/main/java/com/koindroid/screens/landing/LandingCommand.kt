package com.koindroid.screens.landing

import com.core.model.RecordInfo

/**
 * @author Abdullah Ayman on 23/06/2020
 */
sealed class LandingCommand {
    object SessionExpired : LandingCommand()
    class AssignRecordInfo(val record: RecordInfo) : LandingCommand()

}