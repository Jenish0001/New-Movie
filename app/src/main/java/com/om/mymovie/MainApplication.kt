package com.om.mymovie

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.om.mymovie.ads.Glob
import com.onesignal.OneSignal
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig

class MainApplication : Application() {
    private val APIKey = "069b844f-af19-4a52-8f8d-38fb3dc0200d"
    override fun onCreate() {
        super.onCreate()

        instance = this
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (instance!!.packageName != getProcessName()) {
                WebView.setDataDirectorySuffix(getProcessName())
            }
        }

        MobileAds.initialize(instance!!.applicationContext) { }
        Glob.init_Glob(this)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        val config = YandexMetricaConfig.newConfigBuilder(APIKey).build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)

    }

    companion object {
        var instance: MainApplication? = null
        var mFirebaseAnalytics: FirebaseAnalytics? = null
        private const val ONESIGNAL_APP_ID = "7f2fda59-67aa-42de-ad6a-4a33e170df4b"
    }
}