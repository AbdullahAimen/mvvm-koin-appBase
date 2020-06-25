package com.core.repos

import com.core.domain.managers.IDomainManager
import com.core.model.RecordInfo
import io.reactivex.Single

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class CommonRepo(private val mIDomainManager: IDomainManager) {

    fun loadSomeData(): Single<RecordInfo?> {
        return mIDomainManager.getSomeData(
            mIDomainManager.getLanguage(),
            mIDomainManager.getFirebaseToken()
        )
            .subscribeOn(mIDomainManager.getAppSchedulers().ioScheduler())
            .observeOn(mIDomainManager.getAppSchedulers().uiScheduler())
    }

    fun updateRememberMe(value: Boolean) {
        mIDomainManager.setRememberMe(value)
    }

    fun insertRecords(records: ArrayList<RecordInfo>) {
        mIDomainManager.getAppExecutors().diskIO().execute {
            mIDomainManager.insertRecord(records)
        }
    }

    fun deleteRecords() {
        mIDomainManager.getAppExecutors().diskIO().execute {
            mIDomainManager.deleteAllRecords()
        }
    }

    fun getLang() = mIDomainManager.getLanguage()
    fun setLang(value: String) {
        mIDomainManager.setLanguage(value)
    }
}
