package com.om.mymovie.ads

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.om.mymovie.MainApplication
import com.om.mymovie.R
import com.om.mymovie.dialog.PrepareLoadingAdsDialogShow
import java.util.Date

class AppOpenManager(myApplication: MainApplication?) : LifecycleObserver,
    ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private var loadTime_AppOpenMan: Long = 0
    private var loadCallback_AppOpenMan: AppOpenAdLoadCallback? = null
    private val myApplication_AppOpenMan: MainApplication?
    var dialog_AppOpenMan: PrepareLoadingAdsDialogShow? = null
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    private var currentActivity_AppOpenMan: Activity? = null
    override fun onActivityStarted(activity: Activity) {
        currentActivity_AppOpenMan = activity
        if (MainApplication.mFirebaseAnalytics != null) {
            Log.e(LOG_TAG, "appopen_onActivityStarted")
        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity_AppOpenMan = activity
        if (MainApplication.mFirebaseAnalytics != null) {
            Log.e(LOG_TAG, "appopen_onActivityResumed")
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {
        if (MainApplication.mFirebaseAnalytics != null) {
            Log.e(LOG_TAG, "appopen_onActivityPaused")
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        currentActivity_AppOpenMan = null
    }

    init {
        Log.d(LOG_TAG, "callll")
        this.myApplication_AppOpenMan = myApplication
        this.myApplication_AppOpenMan!!.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if (MainApplication.mFirebaseAnalytics != null) {
            Log.e(LOG_TAG, "appopen_init")
        }
    }

    private val adRequest_AppOpenMan: AdRequest
        private get() {
            val extras = Bundle()
            extras.putString("max_ad_content_rating", Glob.max_ad_content_rating_Glob)
            return AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
        }
    val isAdAvailable_AppOpenMan: Boolean
        get() = appOpenAd_AppOpenMan != null && wasLoadTimeLessThanNHoursAgo(4)

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime_AppOpenMan
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    fun fetchAd_AppOpenMan(adsid: String?) {
        if (isAdAvailable_AppOpenMan) {
            return
        }
        if (MainApplication.mFirebaseAnalytics != null) {
            Log.e(LOG_TAG, "appopen_fetch_ad")
        }
        loadCallback_AppOpenMan = object : AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd_AppOpenMan = ad
                loadTime_AppOpenMan = Date().time
                if (MainApplication.mFirebaseAnalytics != null) {
                    Log.e(LOG_TAG, "appopen__ad_loaded")
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (MainApplication.mFirebaseAnalytics != null) {
                    Log.e(LOG_TAG, "appopen__failed_load")
                }
            }
        }
        val request_AppOpenMan = adRequest_AppOpenMan
        AppOpenAd.load(
            myApplication_AppOpenMan!!, adsid!!, request_AppOpenMan,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback_AppOpenMan!!
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected fun onMoveToForeground() {
        showAdIfAvailable_AppOpenMan()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        showAdIfAvailable_AppOpenMan()
    }

    fun showAdIfAvailable_AppOpenMan() {
        Log.e(LOG_TAG, "isAdAvailable--" + isAdAvailable_AppOpenMan)
        Log.e(LOG_TAG, "isShowingAd--" + isShowingAd_AppOpenMan)
        if (!isShowingAd_AppOpenMan && isAdAvailable_AppOpenMan) {
            dialog_AppOpenMan = PrepareLoadingAdsDialogShow(currentActivity_AppOpenMan, R.style.DialogTheme)
            Log.e("hhhh", "==============open ads dialog create================")
            Glob.creatdialog_Glob = true
            Log.d(LOG_TAG, "Will show ad.")
            if (MainApplication.mFirebaseAnalytics != null) {
                Log.e(LOG_TAG, "appopen_show_ad_if")
            }
            if (Glob.ads_loader_Glob!!.equals("yes")) {
                Log.e(LOG_TAG, "Splashopen_show_from_load")
                if (dialog_AppOpenMan != null && !dialog_AppOpenMan!!.isShowing) {
                    dialog_AppOpenMan!!.show()
                }
                Log.e("hhhh", "==============open ads dialog show================")
                Handler().postDelayed({
                    isShowingAd_AppOpenMan = true
                    if (currentActivity_AppOpenMan != null) {
                        Log.e("hhhh", "==============open ads dialog 11111================")
                        if (appOpenAd_AppOpenMan != null) {
                            Log.e("hhhh", "==============open ads dialog 22222================")
                            appOpenAd_AppOpenMan!!.show(currentActivity_AppOpenMan!!)
                        } else {
                            if (dialog_AppOpenMan != null) {
                                Log.e("hhhh", "==============dialog dialog 11111================")
                                if (dialog_AppOpenMan!!.isShowing) {
                                    Log.e(
                                        "hhhh",
                                        "==============dialog dialog 2222================"
                                    )
                                    dialog_AppOpenMan!!.dismiss()
                                }
                            }
                        }
                    } else {
                        if (dialog_AppOpenMan != null) {
                            Log.e("hhhh", "==============dialog dialog 3333================")
                            if (dialog_AppOpenMan!!.isShowing) {
                                Log.e("hhhh", "==============dialog dialog 4444================")
                                dialog_AppOpenMan!!.dismiss()
                            }
                        }
                    }
                }, 1000)

                Handler().postDelayed({
                    if (dialog_AppOpenMan != null && dialog_AppOpenMan!!.isShowing) {
                        dialog_AppOpenMan!!.dismiss()
                    }
                }, 2000)

            } else {
                Handler().postDelayed({
                    if (Glob.ads_loader_Glob!!.equals("no")) {
                        isShowingAd_AppOpenMan = true
                        appOpenAd_AppOpenMan!!.show(currentActivity_AppOpenMan!!)
                    }
                }, 0)
            }
            appOpenAd_AppOpenMan!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(LOG_TAG, "onAdDismissedFullScreenContent: $dialog_AppOpenMan")
                    Log.d(LOG_TAG, "onAdDismissedFullScreenContent: " + currentActivity_AppOpenMan!!.isFinishing)
                    Log.d(LOG_TAG, "onAdDismissedFullScreenContent: $currentActivity_AppOpenMan")
                    appOpenAd_AppOpenMan = null
                    isShowingAd_AppOpenMan = false
                    Log.e("hhhh", "==============open dialog dismiss=========333=======")
                    if (dialog_AppOpenMan != null) {
                        Log.e("hhhh", "==============open dialog dismiss=========444=======")
                        Glob.creatdialog_Glob = false
                        dialog_AppOpenMan!!.dismiss()
                    }
                    fetchAd_AppOpenMan(Glob.APPOPENADS)
                    if (MainApplication.mFirebaseAnalytics != null) {
                        Log.e(LOG_TAG, "appopen_ad_dismiss")
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e("hhhh", "==============open dialog dismiss=========555=======")
                    if (dialog_AppOpenMan != null) {
                        Log.e("hhhh", "==============open dialog dismiss=========6666=======")
                        Glob.creatdialog_Glob = false
                        dialog_AppOpenMan!!.dismiss()
                    }
                    if (MainApplication.mFirebaseAnalytics != null) {
                        Log.e(LOG_TAG, "appopen_failed_to_show$adError")
                    }
                    Log.e(LOG_TAG, "onAdFailedToShowFullScreenContent: ")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(LOG_TAG, "onAdShowedFullScreenContent: ")
                    isShowingAd_AppOpenMan = true
                    if (MainApplication.mFirebaseAnalytics != null) {
                        Log.e(LOG_TAG, "appopen_show_full")
                    }
                }
            }
        } else {
            Log.d(LOG_TAG, "Can not show ad.")
            fetchAd_AppOpenMan(Glob.APPOPENADS)
            if (MainApplication.mFirebaseAnalytics != null) {
                Log.e(LOG_TAG, "appopen_reload_ad")
            }
        }
    }

    companion object {
        private const val LOG_TAG = "appopen----"
        var appOpenAd_AppOpenMan: AppOpenAd? = null
        var isShowingAd_AppOpenMan = false
    }
}