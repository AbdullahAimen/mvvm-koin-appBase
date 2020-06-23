package com.koindroid.screens.landing

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.core.base.BaseViewModel
import com.core.model.RecordInfo
import com.core.repos.CommonRepo
import com.core.utilities.Event

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class LandingViewModel(
    private val mCommonRepo: CommonRepo
) : BaseViewModel() {
    var mLandingCommand: MutableLiveData<Event<LandingCommand>> = MutableLiveData()
    val typedLang = MutableLiveData<String>()
    private var lang = ""

    private val langObserver = Observer<String> { onLangChanged(it) }

    var retrievedLang = ObservableField<String>("")

    init {
        typedLang.observeForever(langObserver)
    }

    fun loadDataFromServer() {
        disposable().add(
            mCommonRepo
                .loadSomeData()
                .subscribe { response: RecordInfo?, throwable ->
                    if (response?.id!!.equals(0)) {
                        mLandingCommand.value =
                            Event(LandingCommand.AssignRecordInfo(response))
                    } else
                        mLandingCommand.value =
                            Event(LandingCommand.SessionExpired)

                })
    }

    private fun onLangChanged(newLang: String) {
        lang = newLang
    }

    fun read() {
        retrievedLang.set(mCommonRepo.getLang())
    }

    fun write() {
        mCommonRepo.setLang(lang)
    }

    override fun onCleared() {
        super.onCleared()
        typedLang.removeObserver(langObserver)
    }
}