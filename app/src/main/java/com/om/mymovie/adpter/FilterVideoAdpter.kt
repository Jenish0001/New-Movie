package com.om.mymovie.adpter

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.om.mymovie.R
import com.om.mymovie.Utils.Util
import com.om.mymovie.activity.ClickFolderActivity
import com.om.mymovie.activity.SplashActivity
import com.om.mymovie.activity.VideoPlayerActivity
import com.om.mymovie.activity.VideoPlayerActivity.Companion.isCheckNewClick
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.Glob
import com.om.mymovie.ads.Google_inter_ads
import com.om.mymovie.dialog.AdsCallBack
import com.om.mymovie.model.VideoModel
import java.io.File

class FilterVideoAdpter(val activity: ClickFolderActivity, val folderVideoList: ArrayList<VideoModel>) : RecyclerView.Adapter<ViewHolder/*FilterVideoAdpter.ViewData*/>() {

    companion object {
        var folderPo: Int = 0
        val ITEM_VIEW = 0
        val AD_VIEW = 1
        val ITEM_FEED_COUNT = 20
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val sortList = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
        val layoutInflater = LayoutInflater.from(activity)
        return if (viewType == ITEM_VIEW) {
            val view: View
            if (sortList) {
                view = layoutInflater.inflate(R.layout.video_adpter_grid_item, parent, false)
            } else {
                view = layoutInflater.inflate(R.layout.video_adpter_item, parent, false)
            }
            ViewDataFolder(view)
        } else (if (viewType == AD_VIEW) {
            val view: View = layoutInflater.inflate(R.layout.folder_ads_adpter_item, parent, false)
            AdViewHolder(view)
        } else {
            null
        })!!
    }

    override fun getItemCount(): Int {
        return if (folderVideoList.size > 0) {
            folderVideoList.size + Math.round((folderVideoList.size / ITEM_FEED_COUNT).toFloat())
        } else folderVideoList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % ITEM_FEED_COUNT == 0) {
            AD_VIEW
        } else ITEM_VIEW
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == ITEM_VIEW) {
            val pos = (position - Math.round((position / ITEM_FEED_COUNT).toFloat()))
            (holder as ViewDataFolder).bindData(folderVideoList[pos], pos, activity, folderVideoList)
        } else if (holder.itemViewType == AD_VIEW) {
            (holder as AdViewHolder).bindAdData()
        }
    }

    private var shimmerLayout: ShimmerFrameLayout? = null
    private var mheight = 0
    var ad_media: LinearLayout? = null
    var unifiedNativeAds: NativeAd? = null

    inner class AdViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bindAdData() {
            //=========================================ad code==========================================================
            val flTxtSpacea = itemView.findViewById<FrameLayout>(R.id.flTxtSpacea)
            val rlNativeView = itemView.findViewById<RelativeLayout>(R.id.rlNativeView)
            shimmerLayout = itemView.findViewById(R.id.shimmerLayout)
            val space = itemView.findViewById<Space>(R.id.space)

            if (Glob.showNative.equals("yes")) {
                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                mheight = displayMetrics.heightPixels
                ad_media = shimmerLayout!!.findViewById(R.id.ad_media)
                val params = space!!.layoutParams
                flTxtSpacea!!.setVisibility(View.GONE)
                if (mheight / 4 > 300) {
                    params.height = (mheight / 4)
                } else {
                    params.height = 300
                }
                Log.e("TAG", "height-----mheight----------: $mheight")
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                space.layoutParams = params
                val params1 = ad_media!!.getLayoutParams()
                if (mheight / 4 > 300) {
                    params1.height = (mheight / 4)
                } else {
                    params1.height = 300
                }
                Log.e("TAG", "height-----mheight----------: $mheight")
                params1.width = ViewGroup.LayoutParams.MATCH_PARENT
                ad_media!!.setLayoutParams(params1)
                loadGoogleNativeAdsForRecy(Glob.GOOGLE_NATIVE, itemView)
            } else {
                rlNativeView.visibility = View.GONE
            }
        }

    }

    private fun loadGoogleNativeAdsForRecy(adsidNative: String, itemView: View) {

        Log.e("ads---", "native_native_ads")
        val builder = AdLoader.Builder(activity, adsidNative)
        val extras = Bundle()
        extras.putString("max_ad_content_rating", Glob.max_ad_content_rating_Glob)
        val build = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        builder.forNativeAd { nativeAd ->
            Log.e("ads---", "native_load")
            shimmerLayout!!.visibility = View.GONE
            unifiedNativeAds = nativeAd
            val adView = activity.layoutInflater.inflate(R.layout.google_native_layout, null)
            populateUnifiedNativeAdsViewForRecy(
                unifiedNativeAds!!,
                adView.findViewById<View>(R.id.native_ads) as NativeAdView
            )
            var nativeFrm = itemView.findViewById<FrameLayout>(R.id.nativeFrm)
            nativeFrm.removeAllViews()
            nativeFrm!!.addView(adView)
        }.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)

                unifiedNativeAds = null

                itemView.findViewById<FrameLayout>(R.id.flTxtSpacea).visibility = View.VISIBLE
                shimmerLayout!!.visibility = View.GONE

                val flTxtSpacea = itemView.findViewById<FrameLayout>(R.id.flTxtSpacea)
                shimmerLayout = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
                val space = itemView.findViewById<Space>(R.id.space)

                val displayMetrics = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                mheight = displayMetrics.heightPixels
                ad_media = shimmerLayout!!.findViewById(R.id.ad_media)
                val params = space!!.layoutParams
                flTxtSpacea!!.setVisibility(View.GONE)
                params.height = 0
                Log.e("TAG", "height-----mheight----------: $mheight")
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                space.layoutParams = params
                val params1 = ad_media!!.getLayoutParams()
                params1.height = 0
                Log.e("TAG", "height-----mheight----------: $mheight")
                params1.width = ViewGroup.LayoutParams.MATCH_PARENT
                ad_media!!.setLayoutParams(params1)

            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.e("ads---", "native_click")
                unifiedNativeAds = null
                AppOpenManager.isShowingAd_AppOpenMan = true
            }
        }).build().loadAd(build)
    }

    private fun populateUnifiedNativeAdsViewForRecy(nativeAdForRc: NativeAd, adViewForRc: NativeAdView) {
        adViewForRc.iconView = adViewForRc.findViewById(R.id.appinstall_app_icon)
        adViewForRc.headlineView = adViewForRc.findViewById(R.id.appinstall_headline1)
        adViewForRc.bodyView = adViewForRc.findViewById(R.id.appinstall_body)
        val mediaView = adViewForRc.findViewById<MediaView>(R.id.ad_media)
        val params = mediaView.layoutParams
        if (mheight / 4 > 300) {
            params.height = (mheight / 4)
        } else {
            params.height = 300
        }
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        mediaView.layoutParams = params

        mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                if (child is ImageView) {
                    child.adjustViewBounds = true
                }
            }

            override fun onChildViewRemoved(parent: View, child: View) {}
        })
        adViewForRc.mediaView = mediaView
        Log.e("TAG", "height-----mheight----------:$mheight")
        Log.e("TAG", "height---------------:${mediaView.layoutParams.height} ")
        adViewForRc.callToActionView = adViewForRc.findViewById(R.id.appinstall_call_to_action)
        if (nativeAdForRc.icon == null) {
            adViewForRc.iconView!!.visibility = View.GONE
        } else {
            (adViewForRc.iconView as ImageView?)!!.setImageDrawable(
                nativeAdForRc.icon!!.drawable
            )
            adViewForRc.iconView!!.visibility = View.VISIBLE
        }
        (adViewForRc.headlineView as TextView?)!!.text = nativeAdForRc.headline
        if (nativeAdForRc.body == null) {
            adViewForRc.bodyView!!.visibility = View.INVISIBLE
        } else {
            adViewForRc.bodyView!!.visibility = View.VISIBLE
            (adViewForRc.bodyView as TextView?)!!.text = nativeAdForRc.body
        }
        if (nativeAdForRc.callToAction == null) {
            adViewForRc.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adViewForRc.callToActionView!!.visibility = View.VISIBLE
            (adViewForRc.callToActionView as TextView?)!!.text = nativeAdForRc.callToAction
        }
        adViewForRc.setNativeAd(nativeAdForRc)
    }

    inner class ViewDataFolder(itemView: View) : ViewHolder(itemView) {
        fun bindData(videoModel: VideoModel, position: Int, activity: ClickFolderActivity, folderVideoList: ArrayList<VideoModel>) {

            var videoTitile = itemView.findViewById<TextView>(R.id.videoTitile)
            var videoImage = itemView.findViewById<ImageView>(R.id.videoImage)
            var mainVideoCLickRL = itemView.findViewById<RelativeLayout>(R.id.mainVideoCLickRL)
            var fileSizeTxt = itemView.findViewById<TextView>(R.id.fileSizeTxt)
            var durationTv = itemView.findViewById<TextView>(R.id.durationTv)
            var moreVideoImg = itemView.findViewById<ImageView>(R.id.moreVideoImg)

            Glide.with(itemView.context).load(videoModel.CONTECT_URI)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(videoImage)


            videoTitile.text = videoModel.TITLE
            fileSizeTxt.text = try {
                Formatter.formatShortFileSize(activity, videoModel.SIZE.toLong())
            } catch (e: java.lang.Exception) {
                "0"
            }
            durationTv.text = Util().convertDurationToShortFormat(videoModel.DURATION)

            mainVideoCLickRL.setOnClickListener {
                Util.setIntentNew = "Folder_Video"
                isCheckNewClick = true
                VideoPlayerActivity.position = position
                Google_inter_ads.googleinter_show_GoogleInterAds(activity, "FilterFolder", object : AdsCallBack {
                    override fun adsCallBack() {
                        ContextCompat.startActivity(activity, Intent(activity, VideoPlayerActivity::class.java), null)
                    }
                }, true)
            }



            moreVideoImg.setOnClickListener {
                val popupMoreDetailsMenu = PopupMenu(this@FilterVideoAdpter.activity, it)
                popupMoreDetailsMenu.inflate(R.menu.more_menu_video_item)
                popupMoreDetailsMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.shareItem -> {

                            val mvFile = File(this@FilterVideoAdpter.folderVideoList[position].DATA)
                            val mvuri = ArrayList<Uri>()
                            val mvContentUri: Uri = Util().getVideoFileUri(this@FilterVideoAdpter.activity, mvFile)
                            mvuri.add(mvContentUri)
                            Util().shareVideoMediaItem(this@FilterVideoAdpter.activity, mvuri)

                            true
                        }

                        R.id.deleteItem -> {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                Util().deleteFile(this@FilterVideoAdpter.activity, this@FilterVideoAdpter.folderVideoList[position].DATA, position)
                                notifyDataSetChanged()

                            } else {
                                val file = File(this@FilterVideoAdpter.folderVideoList[position].DATA)
                                AlertDialog.Builder(this@FilterVideoAdpter.activity).setTitle("Delete")
                                    .setMessage("Request Delete")
                                    .setNegativeButton(
                                        "Cancle"
                                    ) { dialogInterface, i -> dialogInterface.dismiss() }.setPositiveButton(
                                        "Ok"
                                    ) { dialogInterface, POSITION ->
                                        if (file.exists()) {
                                            file.delete()
                                        }
                                        this@FilterVideoAdpter.folderVideoList.removeAt(position)
                                        val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
                                        intent.data = Uri.fromFile(file)
                                        this@FilterVideoAdpter.activity.sendBroadcast(intent)
                                        dialogInterface.dismiss()

                                        folderPo = position
                                        notifyDataSetChanged()


                                    }.show()
                            }
                            true
                        }
                        else -> false
                    }
                }

                try {
                    val mPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                    mPopup.isAccessible = true
                    val mPopupMenuView = mPopup.get(popupMoreDetailsMenu)
                    mPopupMenuView.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopupMenuView, true)
                } catch (e: Exception) {
                    Log.e("Home_Act", "Error showing menu icons.", e)
                } finally {
                    popupMoreDetailsMenu.show()
                }
            }


        }

    }
}