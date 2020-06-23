package com.core.domain.managers

import androidx.lifecycle.LiveData
import com.core.domain.db.AppDB
import com.core.domain.network.IObservableApiService
import com.core.domain.sp.ISharedPreferenceHelper
import com.core.model.RecordInfo
import com.core.utilities.AppExecutors
import com.core.utilities.AppSchedulers
import io.reactivex.Single

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class DomainManagerImpl(
    private val mIObservableApiService: IObservableApiService,
    private val mISharedPreferenceHelper: ISharedPreferenceHelper,
    private val mAppDB: AppDB,
    private val appExecutors: AppExecutors,
    private val appSchedulers: AppSchedulers
) : IDomainManager {
    override fun getAppExecutors(): AppExecutors {
        return appExecutors
    }

    override fun getAppSchedulers(): AppSchedulers {
        return appSchedulers
    }

    override fun getSomeData(lang: String?, token: String?): Single<RecordInfo?> {
        return mIObservableApiService.getSomeData(lang, token)
    }

    override fun setLanguage(lang: String?) {
        mISharedPreferenceHelper.setLanguage(lang)
    }

    override fun getLanguage(): String? {
        return mISharedPreferenceHelper.getLanguage()
    }

    override fun setFirebaseToken(token: String?) {
        mISharedPreferenceHelper.setFirebaseToken(token)
    }

    override fun getFirebaseToken(): String? {
        return mISharedPreferenceHelper.getFirebaseToken()
    }

    override fun setRememberMe(rememberMe: Boolean) {
        mISharedPreferenceHelper.setRememberMe(rememberMe)
    }

    override fun isRememberMe(): Boolean {
        return mISharedPreferenceHelper.isRememberMe()
    }

    override fun insertRecord(records: List<RecordInfo>) {
        mAppDB.RecordsDao().insertRecord(records)
    }

    override fun delete(record: RecordInfo) {
        mAppDB.RecordsDao().delete(record)
    }

    override fun retrieveRecords(): LiveData<RecordInfo> {
        return mAppDB.RecordsDao().retrieveRecords()
    }

    override fun deleteAllRecords() {
        mAppDB.RecordsDao().deleteAllRecords()
    }
}