package com.core.domain

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.core.domain.db.AppDB
import com.core.domain.managers.DomainManagerImpl
import com.core.domain.managers.IDomainManager
import com.core.domain.network.IObservableApiService
import com.core.domain.network.ObservableApiService
import com.core.domain.sp.ISharedPreferenceHelper
import com.core.domain.sp.SharedPreferenceHelper
import com.core.utilities.AppConstants
import com.core.utilities.AppExecutors
import com.core.utilities.AppSchedulers
import com.koindroid.BuildConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

/**
 * @author Abdullah Ayman on 23/06/2020
 */


val domainModule = module {

    /**
     * providing AppExecutors for limited threads
     * */
    single {
        AppExecutors(
            Executors.newSingleThreadExecutor(),
            Executors.newFixedThreadPool(3),
            AppExecutors.MainThreadExecutor()
        )
    }

    /**
     * providing AppScheduler for limited threads
     * */
    single {
        AppSchedulers(
            AndroidSchedulers.mainThread(),
            Schedulers.io(),
            Schedulers.computation()
        )

    }

    /**
     * providing db
     * */
    single { provideAppDB(androidApplication()) }

    /**
     * providing sp
     * */
    single { provideSharedPref(androidApplication()) }
    single<ISharedPreferenceHelper> { SharedPreferenceHelper(get()) }

    single<IDomainManager> {
        DomainManagerImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    /**
     * Observable Retrofit service for the project
     * [StringQualifier] to specify name for the definition to distinguish it.
     * This is the main app [Retrofit] resolved  by name default
     */
    single<Retrofit>(StringQualifier(AppConstants.OBSERVABLE_RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    single<OkHttpClient> {
        val httpClient = OkHttpClient().newBuilder()
            .connectTimeout(AppConstants.REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(AppConstants.REQUEST_TIMEOUT, TimeUnit.SECONDS)
            .hostnameVerifier { hostname, session ->
                val hv = HttpsURLConnection.getDefaultHostnameVerifier()
                hv.verify(hostname, session)
            }
            .writeTimeout(AppConstants.REQUEST_TIMEOUT, TimeUnit.SECONDS)
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        httpClient.addInterceptor(interceptor)
        httpClient.addInterceptor { chain ->

            val request: Request = chain.request()
            val response: Response = chain.proceed(request)
            when (response.code()) {
                //TODO handle Api response Errors
                200 -> {
                }
                404 -> {
                    Timber.d("Client error: ${response.body().toString()}")
                }
                500 -> {
                }

            }
            return@addInterceptor response
        }
        httpClient.build()

    }

    /**
     * provide Observable Service
     * */
    single {
        provideObservableRetrofitService(get(StringQualifier(AppConstants.OBSERVABLE_RETROFIT)))
    }

    /**
     * provide Observable Service implementation
     * */
    single { ObservableApiService(get()) }
}

fun provideAppDB(app: Application): AppDB {
    return Room
        .databaseBuilder(app, AppDB::class.java, AppConstants.DB_NAME)
        .fallbackToDestructiveMigration()
        .build()
}

fun provideSharedPref(app: Application): SharedPreferences {
    return app.applicationContext.getSharedPreferences(
        AppConstants.SHARED_PREFERENCE_NAME,
        Context.MODE_PRIVATE
    )
}

fun provideObservableRetrofitService(retrofit: Retrofit): IObservableApiService =
    retrofit.create(IObservableApiService::class.java)