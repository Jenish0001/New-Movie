package com.om.mymovie.activity

import android.Manifest
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.om.mymovie.MainApplication
import com.om.mymovie.R
import com.om.mymovie.VideoViewModel
import com.om.mymovie.activity.SplashActivity.Companion.sharedPreferencesSplash
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.Glob
import com.om.mymovie.ads.Google_inter_ads
import com.om.mymovie.databinding.ActivityMainBinding
import com.om.mymovie.dialog.AdsCallBack
import com.om.mymovie.fragment.FolderFragment
import com.om.mymovie.fragment.MovieFragment
import com.om.mymovie.fragment.VideoFragment

class MainActivity : AppCompatActivity() {
    private var dialog: Dialog? = null
    private var checkGrid: Boolean = false
    private var permisDial: Dialog? = null
    private lateinit var viewModel: VideoViewModel
    lateinit var binding: ActivityMainBinding

    companion object {
        var backgroundHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesSplash = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        checkGrid = sharedPreferencesSplash!!.getBoolean("GridLayout", false)

        dialog = Dialog(this, R.style.DialogTheme)
        AppUpdateDialogMainAct()
        if (!Glob.issplash_intercall_Glob) {
            Google_inter_ads.GoogleIntrestial_GoogleInterAds(this)
        }
        AppOpenManager.isShowingAd_AppOpenMan = false
        if (AppOpenManager.appOpenAd_AppOpenMan == null) {
            if (MainApplication.instance != null) {
                AppOpenManager(MainApplication.instance)
            }
        }

        if (permisAlreadyGrantedCheck()) {
            next()
        } else {
            requestPermissionDialog()
        }

    }

    private fun next() {

        viewModel = ViewModelProvider(this).get(VideoViewModel::class.java)

        viewModel.loadVideos()

        setFragment(FolderFragment())

        binding.bootmNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.folderItem -> {

                    Google_inter_ads.googleinter_show_GoogleInterAds(
                        this, "MainAct", object : AdsCallBack {
                            override fun adsCallBack() {
                                binding.searchImg.visibility = View.VISIBLE
                                binding.ivSortDataImg.visibility = View.VISIBLE
                                setFragment(FolderFragment())
                            }
                        }, true
                    )

                    true
                }

                R.id.videoItem -> {
                    binding.searchImg.visibility = View.VISIBLE
                    binding.ivSortDataImg.visibility = View.VISIBLE
                    Google_inter_ads.googleinter_show_GoogleInterAds(this, "MainAct", object : AdsCallBack {
                        override fun adsCallBack() {
                            setFragment(VideoFragment())
                        }
                    }, true)
                    true
                }

                R.id.movieItem -> {

                    Google_inter_ads.googleinter_show_GoogleInterAds(
                        this, "MainAct", object : AdsCallBack {
                            override fun adsCallBack() {
                                binding.searchImg.visibility = View.GONE
                                binding.ivSortDataImg.visibility = View.GONE
                                setFragment(MovieFragment())
                            }
                        }, true
                    )

                    true
                }

                else -> {
                    setFragment(FolderFragment())
                    false
                }

            }
        }
        if (checkGrid) {
            binding.ivSortDataImg.setImageDrawable(getDrawable(R.drawable.ic_row_icon))
        } else {
            binding.ivSortDataImg.setImageDrawable(getDrawable(R.drawable.ic_collage_img))
        }
        binding.ivSortDataImg.setOnClickListener {
            checkGrid = !checkGrid
            changeGridIcon(checkGrid)
        }
        binding.searchImg.setOnClickListener {
            Google_inter_ads.googleinter_show_GoogleInterAds(
                this, "MainAct", object : AdsCallBack {
                    override fun adsCallBack() {
                        startActivity(Intent(this@MainActivity, SearchActivity::class.java))
                    }
                }, true
            )


        }

        binding.moreMenuImg.setOnClickListener {
            val popupMenuOpen = PopupMenu(this, it)
            popupMenuOpen.inflate(R.menu.more_menu_main_item)
            try {
                val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldPopup.isAccessible = true
                val mPopupMenu = fieldPopup.get(popupMenuOpen)
                mPopupMenu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopupMenu, true)
            } catch (e: Exception) {
                Log.e("Home_Act", "Error showing menu icons.", e)
            } finally {
                popupMenuOpen.show()
            }
            popupMenuOpen.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.shareAppItem -> {

                        appShare(this)
                        true
                    }

                    R.id.rateAppItem -> {

                        rateUsApp(this)
                        true
                    }

                    R.id.privacyItem -> {
                        appPolicy(this)

                        true
                    }

                    else -> false
                }
            }


        }
    }

    private fun changeGridIcon(checkGrid: Boolean) {
        if (checkGrid) {
            binding.ivSortDataImg.setImageDrawable(getDrawable(R.drawable.ic_row_icon))
            sharedPreferencesSplash!!.edit().putBoolean("GridLayout", true).apply()
            val message = Message()
            message.what = 1
            backgroundHandler.sendMessage(message)
        } else {
            binding.ivSortDataImg.setImageDrawable(getDrawable(R.drawable.ic_collage_img))
            sharedPreferencesSplash!!.edit().putBoolean("GridLayout", false).apply()
            val message = Message()
            message.what = 1
            backgroundHandler.sendMessage(message)
        }
    }

    private fun permisAlreadyGrantedCheck(): Boolean {

        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            return ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissionDialog() {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.READ_MEDIA_VIDEO
                ), 1
            )
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isEmpty()) {
                return
            } else {
                val permsVar: MutableMap<String, Int> = HashMap()
                for (i in permissions.indices) {
                    permsVar[permissions[i]] = grantResults[i]
                }
                try {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (permsVar.isNotEmpty() && permsVar[Manifest.permission.POST_NOTIFICATIONS] == PackageManager.PERMISSION_GRANTED && permsVar[Manifest.permission.READ_MEDIA_VIDEO] == PackageManager.PERMISSION_GRANTED) {/*"Permission granted successfully"*/
                            Toast.makeText(
                                this, "Permission granted successfully", Toast.LENGTH_SHORT
                            ).show()

                            next()

                        } else {

                            val showRationale1 = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                            val showRationale2 = shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO)

                            if (!showRationale1 && !showRationale2) {
                                openSettiDia()
                            } else {
                                permissiondialog()
                            }
                        }
                    } else {

                        if (permsVar.isNotEmpty() && permsVar[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED && permsVar[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED

                        ) {

                            Toast.makeText(
                                this, "Permission granted successfully", Toast.LENGTH_SHORT
                            ).show()
                            next()
                        } else {
                            val showRationale = shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
                            val showRationale1 = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            if (!showRationale && !showRationale1) {
                                openSettiDia()
                            } else {
                                permissiondialog()
                            }
                        }
                    }

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun permissiondialog() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            requestPermissionDialog()
            if (permisAlreadyGrantedCheck()) {
                return
            }
        } else {
            requestPermissionDialog()
            if (permisAlreadyGrantedCheck()) {
                return
            }
        }

    }

    private fun openSettiDia() {

        permisDial = Dialog(this@MainActivity)
        permisDial!!.setContentView(R.layout.permission_setting_dialog)
        permisDial!!.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        permisDial!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        permisDial!!.setCancelable(false)

        val textCancel: TextView = permisDial!!.findViewById(R.id.textCancel)
        val textGoSetting: TextView = permisDial!!.findViewById(R.id.textGoSetting)

        textCancel.setOnClickListener { v: View? ->
            permisDial!!.dismiss()
            finishAffinity()
        }
        textGoSetting.setOnClickListener { v: View? ->
            permisDial!!.dismiss()
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, 101)
            } else {
                val intent = Intent()
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", packageName)
                intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
                startActivityForResult(intent, 101)
            }
        }
        permisDial!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            if (permisAlreadyGrantedCheck()) {
                next()
                return
            } else {
                requestPermissionDialog()
            }
        }
    }

    private fun setFragment(frm: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, frm).commit()
    }


    //========================================= Rate  Us App========================================
    fun rateUsApp(context: Context) {
        val na = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        "market://details?id=$na"
                    )
                )
            )
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=$na"
                    )
                )
            )
        }
    }

    //========================================= App Share=====================================
    fun appShare(context: Context) {
        try {
            val intentShare = Intent(Intent.ACTION_SEND)
            intentShare.type = "text/plain"
            intentShare.putExtra(
                Intent.EXTRA_SUBJECT, context.resources.getString(R.string.app_name)
            )
            val shareMsgPlay: String = """
            https://play.google.com/store/apps/details?id=${packageName}
            """.trimIndent()
            intentShare.putExtra(Intent.EXTRA_TEXT, shareMsgPlay)
            context.startActivity(Intent.createChooser(intentShare, "choose one"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //     ==================================== App Police and Privecy============================
    fun appPolicy(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://www.google.com/")
        context.startActivity(intent)
    }


    //    ===============default app update dialog===========================
    var MY_REQUEST_CODE = 111
    var updateManagerMainAct: AppUpdateManager? = null
    fun AppUpdateDialogMainAct() {
        updateManagerMainAct = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTaskMainAct = updateManagerMainAct!!.appUpdateInfo
        appUpdateInfoTaskMainAct.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) || appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE
                ))
            ) {
                try {
                    updateManagerMainAct!!.startUpdateFlowForResult(
                        appUpdateInfo, this, AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).setAllowAssetPackDeletion(true).build(), MY_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("TAG", "appUpdateInfoTask:::::::: $e")
                    e.printStackTrace()
                }
            }
        }
        updateManagerMainAct!!.registerListener(stateUpdatedListenerMainAct)
    }

    var stateUpdatedListenerMainAct = InstallStateUpdatedListener { state: InstallState ->
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
        }
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            ComplateUpdate_Msg_MainAct()
        }
    }

    private fun ComplateUpdate_Msg_MainAct() {
        val mainview = findViewById<RelativeLayout>(R.id.mainRv)
        val snackbar = Snackbar.make(mainview, "App Update Alomost done.", Snackbar.LENGTH_LONG).setAction("RESTART") { view: View? -> updateManagerMainAct!!.completeUpdate() }
        snackbar.setActionTextColor(resources.getColor(R.color.black))
        snackbar.show()
    }

    override fun onStop() {
        super.onStop()
        updateManagerMainAct!!.unregisterListener(stateUpdatedListenerMainAct)
    }

    override fun onResume() {
        super.onResume()

        AppOpenManager.isShowingAd_AppOpenMan = false

        if (permisAlreadyGrantedCheck()) {
            try {
                if (permisDial!!.isShowing) {
                    permisDial!!.dismiss()
                }
            } catch (e: Exception) {
                if (permisDial != null && permisDial!!.isShowing) {
                    permisDial!!.dismiss()
                    next()
                }
            }
        }

        if (updateManagerMainAct != null) {
            updateManagerMainAct!!.appUpdateInfo.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        updateManagerMainAct!!.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE, this, MY_REQUEST_CODE
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                }
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    ComplateUpdate_Msg_MainAct()
                }
                Log.e("TAG", "update::::::::::::: ")
            }
        }
    }

    override fun onBackPressed() {
//        exitDialog()
    }

    private fun exitDialog() {
        dialog!!.setContentView(R.layout.exit_dialog_view)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.show()
    }

}