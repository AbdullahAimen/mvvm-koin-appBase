package com.core.domain.network

import com.core.model.RecordInfo
import io.reactivex.Single

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class ObservableApiService(private val mIObservableApiService: IObservableApiService) :
    IObservableApiService {
    override fun getSomeData(lang: String?, token: String?): Single<RecordInfo?> {
        return mIObservableApiService.getSomeData(lang, token)
    }

}