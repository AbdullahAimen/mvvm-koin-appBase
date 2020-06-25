package com.koindroid.screens.record

import androidx.lifecycle.MutableLiveData
import com.core.base.BaseViewModel
import com.core.utilities.Event

/**
 * @author Abdullah Ayman on 25/06/2020
 */
class RecordViewModel : BaseViewModel() {
    var mLandingCommand: MutableLiveData<Event<RecordCommand>> = MutableLiveData()

    fun record() {
        mLandingCommand.value = Event(RecordCommand.OpenDialog)
    }

    fun pause() {
        mLandingCommand.value = Event(RecordCommand.Pause)
    }


}