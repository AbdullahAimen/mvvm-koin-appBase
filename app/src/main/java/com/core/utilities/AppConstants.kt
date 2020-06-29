package com.core.utilities

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class AppConstants {

    companion object {
        const val REQUEST_TIMEOUT = 20L
        /**
         *  indicator for life data service
         * */
        const val LIFE_DATA_RETROFIT = "lifeDataRetrofitService"
        const val OBSERVABLE_RETROFIT = "ObservableRetrofitService"

        const val DB_NAME = "WeatherDB"

        const val KEY_FORECAST_LIST = "KEY_FORECAST_LIST"


        const val NULL_INDEX = -1L
        const val SHARED_PREFERENCE_NAME = "_sp"

        fun getSeconds(milliseconds: Int): Int {
            return (milliseconds / 1000) % 60
        }

        fun getMinutes(milliseconds: Int): Int {
            return (milliseconds / 1000) / 60
        }
    }
}