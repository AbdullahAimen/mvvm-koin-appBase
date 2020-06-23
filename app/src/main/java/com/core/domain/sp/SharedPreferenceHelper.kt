package com.core.domain.sp

import android.content.SharedPreferences

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class SharedPreferenceHelper(var mSharedPreference: SharedPreferences) : ISharedPreferenceHelper {

    private val PREF_KEY_FIREBASE_TOKEN = "PREF_KEY_FIREBASE_TOKEN"
    private val PREF_KEY_LANGUAGE = "PREF_KEY_LANGUAGE"
    private val PREF_KEY_REMEMBER_ME = "PREF_KEY_REMEMBER_ME"
    override fun setLanguage(lang: String?) {
        mSharedPreference.edit().putString(PREF_KEY_LANGUAGE, lang).apply()
    }

    override fun getLanguage(): String? {
        return mSharedPreference.getString(PREF_KEY_LANGUAGE, "")
    }

    override fun setFirebaseToken(token: String?) {
        mSharedPreference.edit().putString(PREF_KEY_FIREBASE_TOKEN, token).apply()
    }

    override fun getFirebaseToken(): String? {
        return mSharedPreference.getString(PREF_KEY_FIREBASE_TOKEN, "")
    }

    override fun setRememberMe(rememberMe: Boolean) {
        mSharedPreference.edit().putBoolean(PREF_KEY_REMEMBER_ME, rememberMe).apply()
    }

    override fun isRememberMe(): Boolean {
        return mSharedPreference.getBoolean(PREF_KEY_REMEMBER_ME, false)
    }

}