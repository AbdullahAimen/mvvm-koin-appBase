package com.core.utilities

import androidx.recyclerview.widget.LinearLayoutManager
import com.koindroid.screens.landing.LandingViewModel
import com.koindroid.screens.record.RecordViewModel
import io.reactivex.disposables.CompositeDisposable
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * @author Abdullah Ayman on 23/06/2020
 */

val appModule = module {
    /**
     * Single
     * */
    single { CompositeDisposable() }

    /**
     * Factory
     * */
    factory { LinearLayoutManager(get()) }

    /**
     * ViewModel
     * */
    viewModel { LandingViewModel(get()) }
    viewModel { RecordViewModel() }

}