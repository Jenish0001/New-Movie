package com.om.mymovie.ads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.om.mymovie.R
import com.om.mymovie.dialog.AdsCallBack
import com.om.mymovie.dialog.PrepareLoadingAdsDialogShow

object Google_inter_ads {
    var admobINter_GoogleInterAds: InterstitialAd? = null
    var aderror_GoogleInterAds = false
    var LOG_TAG = "Google_inter_ads----"

    var ginter_max_inter_ads_show_GoogleInterAds = 5

    var Origional_ads_show_GoogleInterAds = 0
    var adsclick = 0

    var ginter_gapbetweentwointer_GoogleInterAds = 5

    var ginter_CountDownTimer_GoogleInterAds = 10000

    var AdsShowIntervaltime_GoogleInterAds = false
    var firsttime_GoogleInterAds = false
    var dialog_GoogleInterAds: Dialog? = null
    var ads_Lodingdialogshow_GoogleInterAds = false

    fun GoogleIntrestial_GoogleInterAds(thiss: Activity?) {
        Glob.issplash_intercall_Glob = true
        Log.e(LOG_TAG, "VP_request_to_gInter_load")
        if (Origional_ads_show_GoogleInterAds == ginter_max_inter_ads_show_GoogleInterAds) {
            Log.e(LOG_TAG, "return_init")
            return
        }
        Log.d(LOG_TAG, "init")
        val extras = Bundle()
        extras.putString("max_ad_content_rating", Glob.max_ad_content_rating_Glob)
        val adRequest = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        InterstitialAd.load(
            thiss!!,
            Glob.GOOGLE_INSTER,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    aderror_GoogleInterAds = false
                    Log.e(LOG_TAG, "VP_GInter_loaded")
                    admobINter_GoogleInterAds = interstitialAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.d(LOG_TAG, "onAdFailedToLoad:::" + loadAdError.message)
                    admobINter_GoogleInterAds = null
                    aderror_GoogleInterAds = true
                    Log.e(LOG_TAG, "VP_GInter_AdFailedToLoad")
                }
            })
    }

    fun googleinter_show_GoogleInterAds(thiss: Activity, from: String, adsCallBack: AdsCallBack, isnext_call: Boolean) {
        Log.d(LOG_TAG, "req")
        val isadshowcall = false
        if (Origional_ads_show_GoogleInterAds == ginter_max_inter_ads_show_GoogleInterAds) {
            adsCallBack.adsCallBack()
            return
        }
        if (firsttime_GoogleInterAds == false && AdsShowIntervaltime_GoogleInterAds == false) {
            Log.d(LOG_TAG, "adclick_firsttime")
            Log.e(LOG_TAG, "GInter_req_$from")
            if (admobINter_GoogleInterAds != null) {
                if (Glob.ads_loader_Glob != null && Glob.ads_loader_Glob == "yes") {
                    try {
                        dialog_GoogleInterAds = PrepareLoadingAdsDialogShow(thiss, R.style.DialogTheme)
                        dialog_GoogleInterAds!!.setOnDismissListener {
                            ads_Lodingdialogshow_GoogleInterAds = false
                        }
                        dialog_GoogleInterAds!!.setOnCancelListener {
                            ads_Lodingdialogshow_GoogleInterAds = false
                        }
                        try {
                            if (!dialog_GoogleInterAds!!.isShowing()) {
                                Log.d("vvv---", "first-showww")
                                if (!ads_Lodingdialogshow_GoogleInterAds) {
                                    dialog_GoogleInterAds!!.show()
                                    ads_Lodingdialogshow_GoogleInterAds = true
                                }
                            }
                        } catch (e: Exception) {
                            return
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Handler().postDelayed({
                        if (!Glob.actpause_Glob) {
                            Log.e(LOG_TAG, "inter_show")
                            admobINter_GoogleInterAds!!.show(thiss)
                        } else {
                            Log.e(LOG_TAG, "dismiss:::222::: ")
                            Glob.actpause_Glob = false
                            if (dialog_GoogleInterAds != null && dialog_GoogleInterAds!!.isShowing) {
                                dialog_GoogleInterAds!!.dismiss()
                            }
                        }
                    }, 1000)
                    Handler().postDelayed({
                        if (dialog_GoogleInterAds != null && dialog_GoogleInterAds!!.isShowing) {
                            dialog_GoogleInterAds!!.dismiss()
                        }
                    }, 1200)
                } else {
                    Log.e(LOG_TAG, "inter_show")
                    admobINter_GoogleInterAds!!.show(thiss)
                }
                show_ads_inter_GoogleInterAds(thiss, adsCallBack, isnext_call)
                AppOpenManager.isShowingAd_AppOpenMan = true
                Log.e(LOG_TAG, "GInter_show_$from")
                Log.e(LOG_TAG, "GInter_Total_show_")
            } else {
                Log.e(LOG_TAG, "GInter_First_ex_show_")
                Log.e(LOG_TAG, "googleinter_show:::00::: ")
                Log.e(LOG_TAG, "========11 26========== ")
                adsCallBack.adsCallBack()
                if (isOnline_GoogleInterAds(thiss)) {
                    if (aderror_GoogleInterAds == true) {
                        Log.e(LOG_TAG, "GInter_show_$from")
                        GoogleIntrestial_GoogleInterAds(thiss)
                    }
                }
            }
        } else {
            adsclick++
            Log.d(LOG_TAG, "adclick_---" + adsclick)
            if (admobINter_GoogleInterAds != null) {
                if (AdsShowornot_GoogleInterAds()) {
                    Log.e(LOG_TAG, "GInter_show_else_$from")
                    if (Glob.ads_loader_Glob != null && Glob.ads_loader_Glob == "yes") {
                        try {
                            dialog_GoogleInterAds = PrepareLoadingAdsDialogShow(thiss, R.style.DialogTheme)
                            dialog_GoogleInterAds!!.setOnDismissListener {
                                ads_Lodingdialogshow_GoogleInterAds = false
                            }
                            dialog_GoogleInterAds!!.setOnCancelListener {
                                ads_Lodingdialogshow_GoogleInterAds = false
                            }
                            try {
                                if (!dialog_GoogleInterAds!!.isShowing()) {
                                    if (!ads_Lodingdialogshow_GoogleInterAds) {
                                        ads_Lodingdialogshow_GoogleInterAds = true
                                        dialog_GoogleInterAds!!.show()
                                    }
                                }
                            } catch (e: Exception) {
                                return
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Handler().postDelayed({
                            if (!Glob.actpause_Glob) {
                                Log.e(LOG_TAG, "inter_show")
                                admobINter_GoogleInterAds!!.show(thiss)
                            } else {
                                Log.e(LOG_TAG, "dismiss:::111::: " + Glob.actpause_Glob)
                                Glob.actpause_Glob = false
                                if (dialog_GoogleInterAds != null && dialog_GoogleInterAds!!.isShowing) {
                                    dialog_GoogleInterAds!!.dismiss()
                                }
                            }
                        }, 1000)
                        Handler().postDelayed({
                            if (dialog_GoogleInterAds != null && dialog_GoogleInterAds!!.isShowing) {
                                dialog_GoogleInterAds!!.dismiss()
                            }
                        }, 1200)
                    } else {
                        Log.e(LOG_TAG, "inter_show")
                        admobINter_GoogleInterAds!!.show(thiss)
                    }
                    show_ads_inter_GoogleInterAds(thiss, adsCallBack, isnext_call)
                    AppOpenManager.isShowingAd_AppOpenMan = true
                } else {
                    Log.e(LOG_TAG, "========11 25========== ")
                    adsCallBack.adsCallBack()
                }
            } else {
                if (isOnline_GoogleInterAds(thiss)) {
                    if (aderror_GoogleInterAds == true) {
                        Log.e(LOG_TAG, "GInter_rq_second_load_$from")
                        Log.e(LOG_TAG, "========11 23========== ")
                        adsCallBack.adsCallBack()
                        GoogleIntrestial_GoogleInterAds(thiss)
                    } else {
                        Log.e(LOG_TAG, "========11 22========== ")
                        adsCallBack.adsCallBack()
                    }
                } else {
                    if (!isadshowcall) {
                        Log.e(
                            LOG_TAG,
                            "googleinter_show:::AdsShowIntervaltime::: " + AdsShowIntervaltime_GoogleInterAds
                        )
                        Log.e(LOG_TAG, "========11 21========== ")
                        adsCallBack.adsCallBack()
                    }
                }
            }
        }
    }

    fun AdsShowornot_GoogleInterAds(): Boolean {
        return if (!AdsShowIntervaltime_GoogleInterAds && adsclick > ginter_gapbetweentwointer_GoogleInterAds && Origional_ads_show_GoogleInterAds != ginter_max_inter_ads_show_GoogleInterAds) {
            adsclick = 0
            Log.e(LOG_TAG, "...............true..........:")
            Log.e(LOG_TAG, "GInter_AdsShowornot_true")
            true
        } else {
            Log.e(LOG_TAG, "GInter_AdsShowornot_false")
            false
        }
    }

    fun isOnline_GoogleInterAds(thiss: Activity): Boolean {
        val netInfo =
            (thiss.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        return if (netInfo != null && netInfo.isConnected) {
            true
        } else {
            false
        }
    }

    fun show_ads_inter_GoogleInterAds(thiss: Activity?, adsCallBack: AdsCallBack, isnext_call: Boolean) {
        Log.e(LOG_TAG, "dshow 111:::2222::: ")
        admobINter_GoogleInterAds!!.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                AppOpenManager.isShowingAd_AppOpenMan = false
                Log.d(LOG_TAG, "ad closed")
                if (dialog_GoogleInterAds != null && dialog_GoogleInterAds!!.isShowing) {
                    dialog_GoogleInterAds!!.dismiss()
                }
                Glob.actpause_Glob = false
                Log.e(LOG_TAG, "========11 17========== ")
                adsCallBack.adsCallBack()
                starttimner_GoogleInterAds()
                Log.e(LOG_TAG, "VP_GInter_DismissedFullScreen")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(LOG_TAG, "ad failed to show.$adError")
                if (dialog_GoogleInterAds != null && dialog_GoogleInterAds!!.isShowing) {
                    dialog_GoogleInterAds!!.dismiss()
                }
                Glob.actpause_Glob = false
                Log.e(LOG_TAG, "========11 16========== ")
                adsCallBack.adsCallBack()
                admobINter_GoogleInterAds = null
                aderror_GoogleInterAds = true
                Log.e(LOG_TAG, "VP_GInter_FailedToShowFullScreen")
            }

            override fun onAdShowedFullScreenContent() {
                Log.e(LOG_TAG, "VP_GInter_AdShowedFullScreen")
                firsttime_GoogleInterAds = true
                Origional_ads_show_GoogleInterAds++
                AppOpenManager.Companion.isShowingAd_AppOpenMan = true
                Log.d(LOG_TAG, "ad show")
                GoogleIntrestial_GoogleInterAds(thiss)
                Log.d(LOG_TAG, "Origional_ads_show---" + Origional_ads_show_GoogleInterAds)
                Log.d(LOG_TAG, "ginter_max_inter_ads_show---" + ginter_max_inter_ads_show_GoogleInterAds)
            }
        }
    }

    fun starttimner_GoogleInterAds() {
        Log.d(LOG_TAG, "timer start")
        AdsShowIntervaltime_GoogleInterAds = true
        object : CountDownTimer(ginter_CountDownTimer_GoogleInterAds.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(LOG_TAG, "timer ----${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                Log.d(LOG_TAG, "timer stop")
                AdsShowIntervaltime_GoogleInterAds = false
            }
        }.start()
    }
}