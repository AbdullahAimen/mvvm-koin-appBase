package com.koindroid

import android.app.Application
import com.core.domain.domainModule
import com.core.repos.repoModule
import com.core.utilities.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        setUpTimber()
        setUpKoin()
    }

    /**
     * start Timber in case of debugging.
     */
    private fun setUpTimber() {
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }

    /**
     * initialize and start Koin.
     */
    private fun setUpKoin() {
        startKoin {
            if (BuildConfig.DEBUG)
                androidLogger()
            androidContext(this@AppClass)
            modules(
                domainModule, repoModule, appModule
            )
        }
    }
}