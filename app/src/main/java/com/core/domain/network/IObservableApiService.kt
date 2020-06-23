package com.core.domain.network

import com.core.model.RecordInfo
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * @author Abdullah Ayman on 23/06/2020
 */
interface IObservableApiService {
    @GET("/api/doSomeAction")
    fun getSomeData(
        @Header("lang") lang: String?,
        @Header("Authorization") token: String?
    ): Single<RecordInfo?>

}