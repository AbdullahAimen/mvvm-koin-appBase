package com.core.domain.managers

import com.core.domain.db.RecordDAO
import com.core.domain.network.IObservableApiService
import com.core.domain.sp.ISharedPreferenceHelper
import com.core.utilities.AppExecutors
import com.core.utilities.AppSchedulers

/**
 * @author Abdullah Ayman on 23/06/2020
 */
interface IDomainManager : IObservableApiService, ISharedPreferenceHelper, RecordDAO {
    fun getAppExecutors(): AppExecutors
    fun getAppSchedulers(): AppSchedulers


    enum class LoggedInMode(val value: Int) {
        LOGGED_OUT(0),
        LOGGED_IN(1);
    }
}