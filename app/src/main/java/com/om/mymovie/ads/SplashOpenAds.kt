package com.om.mymovie.ads

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.om.mymovie.MainApplication
import com.om.mymovie.activity.MainActivity
import com.om.mymovie.activity.SplashActivity
import com.om.mymovie.activity.SplashActivity.Companion.adloadedd_Splash
import com.om.mymovie.activity.SplashActivity.Companion.dialog_view_Splash
import com.om.mymovie.activity.SplashActivity.Companion.issplashshowedSplash
import com.om.mymovie.activity.SplashActivity.Companion.mHandler_splashAd
import com.om.mymovie.activity.SplashActivity.Companion.pau_res_Splash
import com.om.mymovie.activity.SplashActivity.Companion.progress_dialog_Splash
import com.om.mymovie.activity.SplashActivity.Companion.runnable_splashAd
import java.util.Date

class SplashOpenAds(activity: Activity?) : ActivityLifecycleCallbacks, LifecycleObserver {
    var ispaued_SplashOpenAds = false
    private val currentActivity_SplashOpenAds: Activity
    var isFailedtoLoad_SplashOpenAds = false

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected fun onMoveToForeground() {
        Log.e("onResume", "onMoveToForeground: .........................")
        ispaued_SplashOpenAds = false
        splash_ad_SplashOpenAds = this
        if (!issplashshowedSplash) {
            showAdIfAvailable_SplashOpenAds(currentActivity_SplashOpenAds, object : SplashActivity.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                }
            })
        }
        if (MainApplication.mFirebaseAnalytics != null) {
            Log.e(LOG_TAG, "Splashopen_movetoforeground")
        }
    }

    private var loadTime_SplashOpenAds: Long = 0


    init {
        currentActivity_SplashOpenAds = activity!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(this)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun loadAd_SplashOpenAds(context: Context?) {
        Log.d(LOG_TAG, "load req_")
        if (isLoadingAd_SplashOpenAds || isAdAvailable_SplashOpenAds) {
            return
        }
        isLoadingAd_SplashOpenAds = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context!!, Glob.SPLASH_APPOPENADS, request,
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(LOG_TAG, "Ad was loaded.")
                    appOpenAd_SplashOpenAds = ad
                    isLoadingAd_SplashOpenAds = false
                    isFailedtoLoad_SplashOpenAds = false
                    adloadedd_Splash = true
                    loadTime_SplashOpenAds = Date().time
                    if (!Glob.online_splash_appopn_Glob.equals("yes")) {
                        Log.e(LOG_TAG, "issplashshowed----." + issplashshowedSplash)
                        if (!issplashshowedSplash) {
                            if (!Glob.updatenow_Glob.equals("yes")) {
                                Log.e(LOG_TAG, "currentActivity----.$currentActivity_SplashOpenAds")
                                if (currentActivity_SplashOpenAds != null) {
                                    Log.e(LOG_TAG, "isAdAvailable()----." + issplashshowedSplash)
                                    if (isAdAvailable_SplashOpenAds) {
                                        Log.e(LOG_TAG, "ispaued----.$ispaued_SplashOpenAds")
                                        if (!ispaued_SplashOpenAds) {
                                            Log.e(LOG_TAG, "Splashopen_show_from_load")
                                            showAdIfAvailable_SplashOpenAds(
                                                currentActivity_SplashOpenAds,
                                                object : SplashActivity.OnShowAdCompleteListener {
                                                    override fun onShowAdComplete() {
                                                        Log.e(LOG_TAG, "onShowAdComplete.")
                                                        issplashshowedSplash = true
                                                    }
                                                })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(LOG_TAG, "Splash_failedtoload")
                    Log.d(LOG_TAG, loadAdError.message)
                    Log.d(LOG_TAG, "onAdFailedToLoad.....")
                    adloadedd_Splash = false
                    isLoadingAd_SplashOpenAds = false
                    isFailedtoLoad_SplashOpenAds = true
                }
            })
    }

    private fun wasLoadTimeLessThanNHoursAgo_SplashOpenAds(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime_SplashOpenAds
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    val isAdAvailable_SplashOpenAds: Boolean
        get() = appOpenAd_SplashOpenAds != null && wasLoadTimeLessThanNHoursAgo_SplashOpenAds(4)

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd_SplashOpenAds) {
        }
    }

    override fun onActivityResumed(activity: Activity) {
        ispaued_SplashOpenAds = false
        Log.e(LOG_TAG, "Splashopen_onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        ispaued_SplashOpenAds = true
        Log.e(LOG_TAG, "Splashopen_onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.e(LOG_TAG, "Splashopen_onActivityPaused")
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        Log.e(LOG_TAG, "Splashopen_onActivityDestroyed")
    }

    fun showAdIfAvailable_SplashOpenAds(activity: Activity, onShowAdCompleteListener: SplashActivity.OnShowAdCompleteListener) {
        if (isShowingAd_SplashOpenAds) {
            Log.d(LOG_TAG, "The app open ad is already showing.")
            return
        }
        if (!isAdAvailable_SplashOpenAds) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowAdComplete()
            if (activity != null) loadAd_SplashOpenAds(activity)
            return
        }
        Log.e("TAG", "showAdIfAvailable:::::::::::: $activity")
        if (Glob.ads_loader_Glob!!.equals("yes")) {
            Log.e(LOG_TAG, "Splashopen_show_from_loadd")
            show_progressdialog_SplashOpenAds()
            mHandler_splashAd = Handler()
            runnable_splashAd = Runnable {
                Log.e(LOG_TAG, "show_ad________")
                isShowingAd_SplashOpenAds = true
                appOpenAd_SplashOpenAds!!.show(activity)
            }
            mHandler_splashAd!!.postDelayed(
                runnable_splashAd!!,
                1000
            )
        } else {
            Log.e(LOG_TAG, "Splashopen_show")
            show_progressdialog_SplashOpenAds()
            hideLoadingAdsText_SplashOpenAds()
            Handler().postDelayed({
                isShowingAd_SplashOpenAds = true
                appOpenAd_SplashOpenAds!!.show(activity)
            }, 0)
        }
        appOpenAd_SplashOpenAds!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd_SplashOpenAds = null
                Log.e(LOG_TAG, "onAdDismissedFullScreenContent.")
                Glob.issplash_opensucess_Glob = true
                adloadedd_Splash = false
                isShowingAd_SplashOpenAds = true
                Log.e(LOG_TAG, "Splashopen_ad_closed")
                issplashshowedSplash = true
                dismiss_progressdialog_SplashOpenAds()
                intet_main_SplashOpenAds(activity)
                if (Glob.splosh_close_timer_Glob.equals("yes")) {
                    Google_inter_ads.starttimner_GoogleInterAds()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd_SplashOpenAds = null
                isShowingAd_SplashOpenAds = false
                adloadedd_Splash = false
                dismiss_progressdialog_SplashOpenAds()
                Log.e(LOG_TAG, "Splashopen_failed_toshow")
                if (!pau_res_Splash) {
                    if (!issplashshowedSplash) {
                        issplashshowedSplash = true
                        intet_main_SplashOpenAds(activity)
                    }
                }
            }

            override fun onAdShowedFullScreenContent() {
                Log.e(LOG_TAG, "onAdShowedFullScreenContent.......")
                isShowingAd_SplashOpenAds = true
                Glob.issplash_opensucess_Glob = true
                Log.e(LOG_TAG, "Splashopen_Showed_full")
                if (!Glob.issplash_intercall_Glob) {
                    Google_inter_ads.GoogleIntrestial_GoogleInterAds(activity)
                }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.e(LOG_TAG, "Splashopen_onAdImpression")
                hideLoadingAdsText_SplashOpenAds()
            }
        }
    }

    companion object {
        var LOG_TAG = "splashscreen_ad"
        var appOpenAd_SplashOpenAds: AppOpenAd? = null
        var isLoadingAd_SplashOpenAds = false
        var isShowingAd_SplashOpenAds = false
        var splash_ad_SplashOpenAds: SplashOpenAds? = null
        fun show_progressdialog_SplashOpenAds() {
            if (progress_dialog_Splash != null && !progress_dialog_Splash!!.isShowing()) {
                progress_dialog_Splash!!.show()
            }
        }

        fun hideLoadingAdsText_SplashOpenAds() {
            if (dialog_view_Splash != null) {
                dialog_view_Splash!!.setVisibility(View.INVISIBLE)
            }
        }

        fun dismiss_progressdialog_SplashOpenAds() {
            if (progress_dialog_Splash != null && progress_dialog_Splash!!.isShowing()) {
                progress_dialog_Splash!!.dismiss()
            }
        }

        fun intet_main_SplashOpenAds(activity: Activity) {
            SplashActivity.animatorSplash!!.pause()
            activity.startActivity(Intent(activity, MainActivity::class.java))
            Handler().postDelayed({ activity.finish() }, 500)
        }
    }
}