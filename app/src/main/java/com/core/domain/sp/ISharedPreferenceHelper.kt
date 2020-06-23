package com.core.domain.sp

/**
 * @author Abdullah Ayman on 23/06/2020
 */
interface ISharedPreferenceHelper {

    fun setLanguage(lang: String?)

    fun getLanguage(): String?

    fun setFirebaseToken(token: String?)

    fun getFirebaseToken(): String?

    fun setRememberMe(rememberMe: Boolean)

    fun isRememberMe(): Boolean
}