package com.core.repos

import org.koin.dsl.module

/**
 * @author Abdullah Ayman on 23/06/2020
 */

val repoModule = module {
    /**
     * provide Forecast repo with koin
     * */
    single { CommonRepo(get()) }
}