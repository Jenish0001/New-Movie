package com.om.mymovie.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jenish.videodownloader.util.ShardPreference
import com.om.mymovie.R
import com.om.mymovie.activity.SplashActivity.Companion.issplashshowedSplash
import java.util.Locale

@SuppressLint("StaticFieldLeak")
object GetadsKey {

    var context_getAndKey: Activity? = null
    var seconds_getAndKey = 0
    var running_getAndKey = true
    val adshandler_getAndKey: Handler = Handler()
    var adsrunnable_getAndKey: Runnable? = null
    var LOG_TAG = GetadsKey::class.java.name
    var isloaded_adsid_getAndKey = false


    fun LoadAdsData_getAndKey(mcontext: Activity?) {

        running_getAndKey = true
        Log.e("Get----", "GETADSKEY_LOADDATA")
        context_getAndKey = mcontext

        starttime_getAndKey()

        val myRef_getAndKey = Firebase.database.reference.child("adsIds")
        myRef_getAndKey.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value_getAndKey = dataSnapshot.getValue().toString()
                Log.e("Data", "onDataChange: $value_getAndKey")

                if (dataSnapshot.exists()) {

                    val editor_getAndKey = Glob(mcontext!!).mSharedPreferences_Glob!!.edit()

                    val dataMappings = mapOf(
                        "SPLASH_APPOPENADS" to "SPLASH_APPOPENADS",
                        "ads_loader" to "ads_loader",
                        "firsttime" to "firsttime",
                        "ginter_CountDownTimer" to "ginter_CountDownTimer",
                        "ginter_gapbetweentwointer" to "ginter_gapbetweentwointer",
                        "ginter_max_inter_ads_show" to "ginter_max_inter_ads_show",
                        "google_appopen" to "google_appopen",
                        "google_banner" to "google_banner",
                        "google_interstitial" to "google_interstitial",
                        "google_native" to "google_native",
                        "max_ad_content_rating" to "max_ad_content_rating",
                        "online_splash_appopen" to "online_splash_appopen",
                        "playstore_link" to "playstore_link",
                        "splash_appopen_show" to "splash_appopen_show",
                        "splash_close_timer" to "splash_close_timer",
                        "updatenow" to "updatenow",
                        "showNative" to "showNative",
                        "api" to "api",
                        "apiKey" to "apiKey"
                    )

                    for ((dataKey, prefKey) in dataMappings) {
                        val value = dataSnapshot.child(dataKey).getValue()?.toString() ?: ""
                        editor_getAndKey.putString(prefKey, value)
                        ShardPreference.setStringInPref(mcontext, prefKey, value)
                        Log.d("TAG---", "onDataChange:::::value::::${value}")
                        Log.d("TAG---", "onDataChange:::::prefKey::::${prefKey}")
                    }

                    editor_getAndKey.apply()
                    storeVariableData_getAndKey()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG-----", "onCancelled::::Error::::${error}")
                stoptime_getAndKey()
            }
        })
        Log.e(LOG_TAG, "LoadAdsData::: " + System.currentTimeMillis())
    }

    fun storeVariableData_getAndKey() {
        val mSharedPreferences_getAndKey = Glob(context_getAndKey!!).mSharedPreferences_Glob
        Glob.SPLASH_APPOPENADS = mSharedPreferences_getAndKey!!.getString("SPLASH_APPOPENADS", "")!!
        Glob.ads_loader_Glob = mSharedPreferences_getAndKey.getString("ads_loader", "")!!
        Google_inter_ads.firsttime_GoogleInterAds =
            mSharedPreferences_getAndKey.getString("firsttime", "")!!.toBoolean()
        Google_inter_ads.ginter_CountDownTimer_GoogleInterAds =
            mSharedPreferences_getAndKey.getString("ginter_CountDownTimer", "")!!.toInt()
        Google_inter_ads.ginter_gapbetweentwointer_GoogleInterAds =
            mSharedPreferences_getAndKey.getString("ginter_gapbetweentwointer", "")!!.toInt()
        Google_inter_ads.ginter_max_inter_ads_show_GoogleInterAds =
            mSharedPreferences_getAndKey.getString("ginter_max_inter_ads_show", "")!!.toInt()
        Glob.APPOPENADS = mSharedPreferences_getAndKey.getString("google_appopen", "")!!
        Glob.GOOGLE_BANNER = mSharedPreferences_getAndKey.getString("google_banner", "")!!
        Glob.GOOGLE_INSTER = mSharedPreferences_getAndKey.getString("google_interstitial", "")!!
        Glob.GOOGLE_NATIVE = mSharedPreferences_getAndKey.getString("google_native", "")!!
        Glob.max_ad_content_rating_Glob =
            mSharedPreferences_getAndKey.getString("max_ad_content_rating", "")!!
        Glob.online_splash_appopn_Glob =
            mSharedPreferences_getAndKey.getString("online_splash_appopen", "")!!
        Glob.playstore_link_Glob = mSharedPreferences_getAndKey.getString("playstore_link", "")!!
        Glob.Splash_appopn_show_Glob =
            mSharedPreferences_getAndKey.getString("splash_appopen_show", "")!!
        Glob.splosh_close_timer_Glob =
            mSharedPreferences_getAndKey.getString("splash_close_timer", "")!!
        Glob.updatenow_Glob = mSharedPreferences_getAndKey.getString("updatenow", "")!!
        Glob.showNative = mSharedPreferences_getAndKey.getString("showNative", "")!!
        Glob.showNative = mSharedPreferences_getAndKey.getString("showNative", "")!!
        Glob.api = mSharedPreferences_getAndKey.getString("api", "")!!
        Glob.apiKey = mSharedPreferences_getAndKey.getString("apiKey", "")!!

        defaulte_getAndKey()
    }

    fun defaulte_getAndKey() {

        if (!Glob.instance_Glob!!.GetBoolean_Glob(context_getAndKey, "firsttime_load", false)) {
            Glob.instance_Glob!!.SetBoolean_Glob(context_getAndKey, "firsttime_load", true)
        }

        if (Glob.updatenow_Glob == "yes") {
            SplashOpenAds.isShowingAd_SplashOpenAds = true
            Log.e("LOG", "starttimer:::issplash_opensucess::" + Glob.issplash_opensucess_Glob)
            Log.e("LOG", "starttimer:::222::" + issplashshowedSplash)
            if (!context_getAndKey!!.isFinishing && !Glob.issplash_opensucess_Glob && !issplashshowedSplash) {
                openAppUpdateDialog_getAndKey()
            }
        }

        stoptime_getAndKey()

        Log.e(LOG_TAG, "getdata:::000:::11:: " + Glob.online_splash_appopn_Glob)

        if (Glob.online_splash_appopn_Glob.equals("yes")) {
            SplashOpenAds.appOpenAd_SplashOpenAds = null
            if (SplashOpenAds.appOpenAd_SplashOpenAds == null) {
                Log.e("gggggggggggggggg", "getdata:::000:::22:: " + context_getAndKey)
                SplashOpenAds.isLoadingAd_SplashOpenAds = false
                SplashOpenAds(context_getAndKey)
            }
        }
    }

    var updateAppDialog_getAndKey: Dialog? = null

    fun openAppUpdateDialog_getAndKey() {
        if (updateAppDialog_getAndKey == null) {
            updateAppDialog_getAndKey = Dialog(context_getAndKey!!)
        }
        updateAppDialog_getAndKey!!.setContentView(R.layout.dialog_update)
        updateAppDialog_getAndKey!!.window!!.setGravity(Gravity.CENTER)
        updateAppDialog_getAndKey!!.window!!
            .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        updateAppDialog_getAndKey!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        updateAppDialog_getAndKey!!.setCancelable(false)
        Log.e("TAG", "openAppUpdateDialog:::0::: " + updateAppDialog_getAndKey!!.isShowing)
        if (!updateAppDialog_getAndKey!!.isShowing) {
            updateAppDialog_getAndKey!!.show()
        }
        updateAppDialog_getAndKey!!.findViewById<View>(R.id.ll_update_app)
            .setOnClickListener { v: View? ->
                updateAppDialog_getAndKey!!.dismiss()
                gotoPlayStore_getAndKey()
            }
    }

    //goto playstore update app
    fun gotoPlayStore_getAndKey() {
        try {
            var intent2_getAndKey: Intent? = null
            intent2_getAndKey = Intent("android.intent.action.VIEW")
            intent2_getAndKey.data = Uri.parse(Glob.playstore_link_Glob)
            context_getAndKey!!.startActivity(intent2_getAndKey)
        } catch (e: Exception) {
            e.printStackTrace()
            val data_getAndKey: String = Glob.playstore_link_Glob
            val defaultBrowser_getAndKey =
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
            defaultBrowser_getAndKey.data = Uri.parse(data_getAndKey)
            context_getAndKey!!.startActivity(defaultBrowser_getAndKey)
        }
    }

    fun starttime_getAndKey() {
        adsrunnable_getAndKey = object : Runnable {
            override fun run() {
                val secs = seconds_getAndKey % 60
                val time = String.format(Locale.getDefault(), "%02d", secs)
                Log.e(LOG_TAG, "run.....time :::: $time")
                if (seconds_getAndKey == 120) {
                    Toast.makeText(context_getAndKey, "Not Get Data!", Toast.LENGTH_SHORT).show()
                    Log.e("DemoEvent----", "GETADSKEY_120_TIMEOUT")
                    stoptime_getAndKey()
                    return
                }
                if (running_getAndKey) {
                    seconds_getAndKey++
                }
                adshandler_getAndKey.postDelayed(this, 1000)
            }
        }
        adshandler_getAndKey.post(adsrunnable_getAndKey!!)
    }

    fun stoptime_getAndKey() {
        if (adshandler_getAndKey != null && adsrunnable_getAndKey != null) {
            running_getAndKey = false
            adshandler_getAndKey.removeCallbacks(adsrunnable_getAndKey!!)
        }
    }
}