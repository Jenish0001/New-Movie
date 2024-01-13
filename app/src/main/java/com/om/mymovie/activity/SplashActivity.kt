package com.om.mymovie.activity

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.om.mymovie.MainApplication
import com.om.mymovie.R
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.GetadsKey
import com.om.mymovie.ads.Glob
import com.om.mymovie.ads.Google_inter_ads
import com.om.mymovie.ads.SplashOpenAds
import com.om.mymovie.databinding.ActivitySplashBinding
import com.yandex.metrica.YandexMetrica

class SplashActivity : AppCompatActivity() {

    var mHandlerSplash: Handler? = null
    var runnableSplash: Runnable? = null
    var milliSecSplash = 5000
    private var requestCodeSplash = -1
    private var resultHandlerSplash: ActivityResultLauncher<Intent>? = null

    companion object {
        var animatorSplash: ValueAnimator? = null
        var sharedPreferencesSplash: SharedPreferences? = null
        lateinit var splashScreenThis: SplashActivity // no
        var mHandler_splashAd: Handler? = null
        var runnable_splashAd: Runnable? = null
        var pau_res_Splash = false
        var issplashshowedSplash = false
        var isnetwork_no_Splash = false
        var splash_activity: SplashActivity? = null
        var adloadedd_Splash = false
        var progress_dialog_Splash: Dialog? = null
        var dialog_view_Splash: RelativeLayout? = null

        fun loader_progress_dialog_splash(activity: Activity?) {
            Log.e("TAG", "openAppUpdateDialogSplash::::::::" + progress_dialog_Splash)
            progress_dialog_Splash = Dialog(activity!!, R.style.DialogTheme)
            progress_dialog_Splash!!.setContentView(R.layout.dialog_ads_show_)
            progress_dialog_Splash!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progress_dialog_Splash!!.setCancelable(false)
            dialog_view_Splash = progress_dialog_Splash!!.findViewById(R.id.dialog_view)
        }

        fun dismiss_progres_sdialog_splash() {
            if (progress_dialog_Splash != null && progress_dialog_Splash!!.isShowing) {
                progress_dialog_Splash!!.dismiss()
            }
        }
    }

    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesSplash = getSharedPreferences("MyPreferences", MODE_PRIVATE)

        animatorSplash = ValueAnimator.ofFloat(-100f, 200f) // Animate from 0 to 200 pixels vertically
        animatorSplash!!.duration = 2000 // Animation duration in milliseconds
        animatorSplash!!.repeatCount = ValueAnimator.INFINITE // Infinite loop
        animatorSplash!!.repeatMode = ValueAnimator.REVERSE // Reverse animation on each repeat

        animatorSplash!!.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            binding.main.translationY = value // Update the Y translation of the view
        }

        animatorSplash!!.start()

        splashScreenThis = this
        AppOpenManager.isShowingAd_AppOpenMan = true
        AppOpenManager.appOpenAd_AppOpenMan = null
        SplashOpenAds.isShowingAd_SplashOpenAds = false
        issplashshowedSplash = false
        Glob.issplash_opensucess_Glob = false
        GetadsKey.isloaded_adsid_getAndKey = false
        pau_res_Splash = false
        isnetwork_no_Splash = false
        Google_inter_ads.ginter_max_inter_ads_show_GoogleInterAds = 0

        if (!Glob.isdebug_mode_Glob) {
            Glob.SPLASH_APPOPENADS = "ca-app-pub-3940256099942544/3419835294"
            Glob.APPOPENADS = "ca-app-pub-3940256099942544/3419835294"
            Glob.GOOGLE_INSTER = "ca-app-pub-3940256099942544/1033173712"
            Glob.GOOGLE_BANNER = "ca-app-pub-3940256099942544/6300978111"
            Glob.GOOGLE_NATIVE = "ca-app-pub-3940256099942544/2247696110"
        }

        loader_progress_dialog_splash(this)

        try {
            try {
                var networkRequestSplash: NetworkRequest? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    networkRequestSplash = NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .build()
                }
                val connectivityManagerSplash =
                    getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    connectivityManagerSplash.requestNetwork(networkRequestSplash!!, networkCallbackSplash)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                registerForActivityResultSplash()
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResulttSplash(intent, 100)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            registerForActivityResultSplash()
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResulttSplash(intent, 100)
        }
        showinternetdialogSplash()
        if (isConnectedSplash(this@SplashActivity)) {
            starttimer_Splash(milliSecSplash)
        } else {
            showdialogSplash()
        }
    }

    fun startActivityForResulttSplash(intent: Intent, requestCode: Int) {
        this.requestCodeSplash = requestCode
        if (resultHandlerSplash != null) {
            resultHandlerSplash!!.launch(intent)
        }
    }

    private fun registerForActivityResultSplash() {
        resultHandlerSplash =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    RESULT_OK -> {
                        this@SplashActivity.onActivityResult(
                            requestCodeSplash, result.resultCode, result.data
                        )
                        requestCodeSplash = -1
                    }
                }
            }
    }

    private val networkCallbackSplash: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.e("network", "onAvailable: $network")
            if (!isFinishing) {
                runOnUiThread {
                    dismissdialogSplash()
                    isshowingdismissSplash = false
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.e("network", "onLost: $network")
            if (!isFinishing) {
                runOnUiThread {
                    Log.e("TAG", "showdialog")
                    showdialogSplash()
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            val unmeteredSplash =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            Log.e("network", "onCapabilitiesChanged: $unmeteredSplash")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("splashscreen", "onActivityResult:-----------requestCode---------- $requestCode")
        if (requestCode == 100) {
            try {
                try {
                    var networkRequest: NetworkRequest? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        networkRequest = NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                            .build()
                    }
                    val connectivityManager11 =
                        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        connectivityManager11.requestNetwork(networkRequest!!, networkCallbackSplash)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResulttSplash(intent, 100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResulttSplash(intent, 100)
            }
        }
    }

    fun isConnectedSplash(context: Context): Boolean {
        val cm: ConnectivityManager
        var netInfo: NetworkInfo? = null
        try {
            cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            netInfo = cm.activeNetworkInfo
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return netInfo != null && netInfo.isConnectedOrConnecting
    }

    var alertDialogSplash: Dialog? = null
    var isshowingdismissSplash = false

    private fun showinternetdialogSplash() {
        alertDialogSplash = Dialog(this)
        alertDialogSplash!!.setContentView(R.layout.no_internet_dialog)
        alertDialogSplash!!.window!!.setBackgroundDrawable(ColorDrawable(0))
        alertDialogSplash!!.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        alertDialogSplash!!.window!!.attributes.windowAnimations = R.style.DialogTheme
        alertDialogSplash!!.setCanceledOnTouchOutside(false)
        val btn_wifi_on = alertDialogSplash!!.findViewById<TextView>(R.id.ttWifiTxt)
        val btn_mobile_data_on = alertDialogSplash!!.findViewById<TextView>(R.id.ttDataTxt)
        btn_wifi_on.setOnClickListener {
            alertDialogSplash!!.dismiss()
            tuenonwifiSplash(this@SplashActivity)
        }
        btn_mobile_data_on.setOnClickListener {
            alertDialogSplash!!.dismiss()
            tuenon_mobiledata_Splash(this@SplashActivity)
        }
        alertDialogSplash!!.setOnDismissListener {
            Log.e("splashscreen", "pau_res:::isshowingdismiss: $isshowingdismissSplash")
            Log.e("DemoEvent----", "Internet_dialog_dismiss")
            if (!isshowingdismissSplash) {
                isshowingdismissSplash = false
                if (isConnectedSplash(this@SplashActivity)) {
                    starttimer_Splash(milliSecSplash)
                } else {
                    Log.e("splashscreen", "alertDialog---mHandler------$mHandlerSplash")
                    Log.e("splashscreen", "alertDialog---runnable------$runnableSplash")
                    if (mHandlerSplash == null || runnableSplash == null) {
                        mHandlerSplash = Handler()
                        runnableSplash = Runnable {
                            if (isConnectedSplash(this@SplashActivity)) {
                                Log.e("splashscreen", "alertDialog---33------$runnableSplash")
                                starttimer_Splash(milliSecSplash)
                            } else {
                                Log.e("splashscreen", "alertDialog---0000------$runnableSplash")
                                if (!isFinishing) {
                                    Log.e("splashscreen", "alertDialog---1111------$runnableSplash")
                                    showdialogSplash()
                                }
                            }
                        }
                    }
                    mHandlerSplash!!.postDelayed(runnableSplash!!, 3000)
                }
            }
        }
    }


    fun tuenonwifiSplash(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "It cannot open settings!", Toast.LENGTH_LONG).show()
        }
    }


    fun tuenon_mobiledata_Splash(context: Context) {
        try {
            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "It cannot open settings!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun starttimer_Splash(milli: Int) {
        Log.e("splashscreen---", "starttimer: ")
        if (!Glob.first_offline_splash_Glob) {
            Glob.first_offline_splash_Glob = true
            SplashOpenAds(this)
        }
        GetadsKey.updateAppDialog_getAndKey = Dialog(this)
        Log.e("splashscreen---", "starttimer:::000::")
        GetadsKey.LoadAdsData_getAndKey(this@SplashActivity)
        mHandlerSplash = Handler()
        runnableSplash = Runnable {
            if (isConnectedSplash(this@SplashActivity)) {
                Log.e("splashscreen", "alertDialog---33------$runnableSplash")
                if (Glob.updatenow_Glob == "yes") {
                    SplashOpenAds.isShowingAd_SplashOpenAds = true
                    Log.e("splashscreen", "starttimer::1111:222::")
                    GetadsKey.openAppUpdateDialog_getAndKey()
                } else if (SplashOpenAds.splash_ad_SplashOpenAds != null && SplashOpenAds.splash_ad_SplashOpenAds!!.isFailedtoLoad_SplashOpenAds) {
                    Log.e("splashscreen", "starttimer:::3333::")
                    startMainActivitySplash()
                } else {
                    if (SplashOpenAds.splash_ad_SplashOpenAds != null && SplashOpenAds.splash_ad_SplashOpenAds!!.isAdAvailable_SplashOpenAds) {
                        Log.e("splashscreen", "starttimer:::5555::")
                        if (Glob.Splash_appopn_show_Glob.equals("yes")) {
                            Log.e("splashscreen", "starttimer:::6666::")
                            if (!Glob.issplash_opensucess_Glob) {
                                Log.e(
                                    "splashscreen",
                                    "starttimer:::7777::" + Glob.updatenow_Glob
                                )
                                SplashOpenAds.splash_ad_SplashOpenAds!!.showAdIfAvailable_SplashOpenAds(this@SplashActivity,
                                    object : OnShowAdCompleteListener {
                                        override fun onShowAdComplete() {}
                                    })
                            } else {
                                Log.e("splashscreen", "starttimer:::888::")
                                startMainActivitySplash()
                            }
                        } else {
                            Log.e("splashscreen", "starttimer:::999::")
                            startMainActivitySplash()
                        }
                    } else {
                        Log.e("splashscreen", "starttimer:::999111::")
                        startMainActivitySplash()
                    }
                }
            } else {
                Log.e("splashscreen", "alertDialog---0000--0000----$runnableSplash")
                if (!isFinishing) {
                    Log.e("splashscreen", "alertDialog---1111--111----$runnableSplash")
                    showdialogSplash()
                }
            }
        }
        Log.e("splashscreen", "starttimer:::63636363::")
        mHandlerSplash!!.postDelayed(runnableSplash!!, milli.toLong())
    }

    private fun startMainActivitySplash() {
        animatorSplash!!.pause()
        SplashOpenAds.isShowingAd_SplashOpenAds = true
        Log.e("splashscreen", "starttimer:::issplashshowed::" + issplashshowedSplash)
        if (!issplashshowedSplash) {
            issplashshowedSplash = true
            Log.e("DemoEvent----", "SplashAct_intent_Mainact")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("DemoEvent----", "SplashAct_onDestroy")
        MainApplication.mFirebaseAnalytics!!.logEvent("SplashAct_onDestroy", Bundle())
        YandexMetrica.reportEvent("SplashAct_onDestroy")
        SplashOpenAds.appOpenAd_SplashOpenAds = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        SplashOpenAds.appOpenAd_SplashOpenAds = null
        Log.e("DemoEvent----", "SplashAct_onBack")
        MainApplication.mFirebaseAnalytics!!.logEvent("SplashAct_onBack", Bundle())
        YandexMetrica.reportEvent("SplashAct_onBack")


    }

    override fun onResume() {
        super.onResume()
        Log.e("DemoEvent----", "SplashAct_onResume")
        MainApplication.mFirebaseAnalytics!!.logEvent("SplashAct_onResume", Bundle())
        YandexMetrica.reportEvent("SplashAct_onResume")
        if (pau_res_Splash) {
            pau_res_Splash = false
            Log.e("splashscreen", "pau_res:::isloaded_adsid: " + GetadsKey.isloaded_adsid_getAndKey)
            if (GetadsKey.isloaded_adsid_getAndKey) {
                if (mHandlerSplash != null && runnableSplash != null) {
                    mHandlerSplash!!.postDelayed(runnableSplash!!, 3000)
                } else {
                    Log.e("splashscreen", "pau_res:::isloaded_adsid:111111111 ")
                    starttimer_Splash(milliSecSplash)
                }
            } else {
                if (isConnectedSplash(this@SplashActivity)) {
                    dismissdialogSplash()
                    starttimer_Splash(milliSecSplash)
                } else {
                    isshowingdismissSplash = false
                    showdialogSplash()
                }
            }
        }
    }

    fun dismissdialogSplash() {
        if (alertDialogSplash != null && alertDialogSplash!!.isShowing) {
            isnetwork_no_Splash = false
            alertDialogSplash!!.dismiss()
            isshowingdismissSplash = true
        }
    }


    fun showdialogSplash() {
        if (alertDialogSplash != null && !alertDialogSplash!!.isShowing) {
            isnetwork_no_Splash = true
            dismiss_progres_sdialog_splash()
            if (!adloadedd_Splash) {
                alertDialogSplash!!.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e("DemoEvent----", "SplashAct_onpause")
        MainApplication.mFirebaseAnalytics!!.logEvent("SplashAct_onpause", Bundle())
        YandexMetrica.reportEvent("SplashAct_onpause")
        if (GetadsKey.updateAppDialog_getAndKey != null) {
            if (GetadsKey.updateAppDialog_getAndKey!!.isShowing) {
                GetadsKey.updateAppDialog_getAndKey!!.dismiss()
            }
        }
        dismissdialogSplash()
        pau_res_Splash = true
        if (mHandlerSplash != null && runnableSplash != null) {
            mHandlerSplash!!.removeCallbacks(runnableSplash!!)
        }
        if (mHandler_splashAd != null && runnable_splashAd != null) {
            mHandler_splashAd!!.removeCallbacks(runnable_splashAd!!)
        }
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

}