package com.om.mymovie.ads

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class Glob(context: Context) {

    var mSharedPreferences_Glob: SharedPreferences?
    var activity_Glob: Context

    init {
        mSharedPreferences_Glob = PreferenceManager.getDefaultSharedPreferences(context)
        activity_Glob = context
    }

    fun Setstring(context: Context?, key: String?, str: String?) {
        if (mSharedPreferences_Glob == null) {
            mSharedPreferences_Glob = PreferenceManager.getDefaultSharedPreferences(context)
        }
        val edit = mSharedPreferences_Glob!!.edit()
        edit.putString(key, str)
        edit.commit()
    }

    fun Getstring(context: Context?, key: String?): String? {
        if (mSharedPreferences_Glob == null) {
            mSharedPreferences_Glob = PreferenceManager.getDefaultSharedPreferences(context)
        }
        return mSharedPreferences_Glob!!.getString(key, "")
    }

    fun SetBoolean_Glob(context: Context?, key: String?, str: Boolean) {
        if (mSharedPreferences_Glob == null) {
            mSharedPreferences_Glob = PreferenceManager.getDefaultSharedPreferences(context)
        }
        val edit = mSharedPreferences_Glob!!.edit()
        edit.putBoolean(key, str)
        edit.commit()
    }

    fun GetBoolean_Glob(context: Context?, key: String?, def: Boolean): Boolean {
        if (mSharedPreferences_Glob == null) {
            mSharedPreferences_Glob = PreferenceManager.getDefaultSharedPreferences(context)
        }
        return mSharedPreferences_Glob!!.getBoolean(key, def)
    }

    companion object {
        var isdebug_mode_Glob = true

        var SPLASH_APPOPENADS = "ca-app-pub-3940256099942544/3419835294_"
        var APPOPENADS = "ca-app-pub-3940256099942544/3419835294_"
        var GOOGLE_INSTER = "ca-app-pub-3940256099942544/1033173712_"
        var GOOGLE_BANNER = "ca-app-pub-3940256099942544/6300978111_"
        var GOOGLE_NATIVE = "ca-app-pub-3940256099942544/2247696110_"

        var actpause_Glob = false
        var creatdialog_Glob = false
        var instance_Glob: Glob? = null
        var updatenow_Glob = "no"
        var playstore_link_Glob = ""
        var issplash_opensucess_Glob = false
        var issplash_intercall_Glob = false
        var first_offline_splash_Glob = false
        var online_splash_appopn_Glob = "no"
        var Splash_appopn_show_Glob = "yes"
        var max_ad_content_rating_Glob = "PG"
        var splosh_close_timer_Glob = "no"
        var ads_loader_Glob: String? = "yes"


        var showNative: String? = "yes"
        var api: String? = ""
        var apiKey: String? = ""

        fun init_Glob(context: Context): Glob? {
            if (instance_Glob == null) {
                instance_Glob = Glob(context)
            }
            return instance_Glob
        }
    }
}