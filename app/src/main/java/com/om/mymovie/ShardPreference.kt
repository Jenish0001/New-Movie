package com.jenish.videodownloader.util

import android.content.Context
import android.content.SharedPreferences

class ShardPreference(private val context: Context) {

    private val shardPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREF_NAME = "SmartVideoPlayer"

        fun setIntInPref(context: Context, key: String, value: Int) {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            pref.edit().putInt(key, value).apply()
        }

        fun getIntInPref(context: Context, key: String): Int {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getInt(key, -1)
        }

        fun setStringInPref(context: Context, key: String, value: String) {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            pref.edit().putString(key, value).apply()
        }

        fun getStringInPref(context: Context, key: String): String {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getString(key, "") ?: ""
        }

        fun setBooleanInPref(context: Context, key: String, value: Boolean) {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            pref.edit().putBoolean(key, value).apply()
        }

        fun getBooleanInPref(context: Context, key: String): Boolean {
            val pref: SharedPreferences =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getBoolean(key, false)
        }
    }
}