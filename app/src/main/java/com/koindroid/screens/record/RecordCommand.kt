package com.koindroid.screens.record

/**
 * @author Abdullah Ayman on 25/06/2020
 */
sealed class RecordCommand {
    object OpenDialog : RecordCommand()
    object Record : RecordCommand()
    object Pause : RecordCommand()
    class ShowErrorMessage(val test: String) : RecordCommand()
}