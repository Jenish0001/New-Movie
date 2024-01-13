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
import androidx.fragment.app.FragmentActivity
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
import com.om.mymovie.Utils.Util.Companion.setIntentNew
import com.om.mymovie.activity.SplashActivity
import com.om.mymovie.activity.VideoPlayerActivity
import com.om.mymovie.activity.VideoPlayerActivity.Companion.isCheckNewClick
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.Glob
import com.om.mymovie.ads.Google_inter_ads
import com.om.mymovie.dialog.AdsCallBack
import com.om.mymovie.model.VideoModel
import java.io.File

class VideoAdpter(var _actvity: FragmentActivity, val videos: ArrayList<VideoModel>) : RecyclerView.Adapter<ViewHolder>() {

    companion object {
        var deletePosition: Int? = 0
        const val ITEM_VIEW_V = 0
        const val AD_VIEW_V = 1
        const val ITEM_FEED_COUNT_V = 20
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val sortList = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
        val layoutInflater = LayoutInflater.from(_actvity)
        return if (viewType == ITEM_VIEW_V) {
            val view: View
            if (sortList) {
                view = layoutInflater.inflate(R.layout.video_adpter_grid_item, parent, false)
            } else {
                view = layoutInflater.inflate(R.layout.video_adpter_item, parent, false)
            }
            ViewDataFolder(view)
        } else (if (viewType == AD_VIEW_V) {
            val view: View = layoutInflater.inflate(R.layout.folder_ads_adpter_item, parent, false)
            AdViewHolder(view)
        } else {
            null
        })!!
    }

    override fun getItemCount(): Int {
        return if (videos.size > 0) {
            videos.size + Math.round((videos.size / FolderAdpter.ITEM_FEED_COUNT).toFloat())
        } else videos.size
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % FolderAdpter.ITEM_FEED_COUNT == 0) {
            AD_VIEW_V
        } else ITEM_VIEW_V
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == ITEM_VIEW_V) {
            val pos = (position - Math.round((position / ITEM_FEED_COUNT_V).toFloat()))
            (holder as ViewDataFolder).bindData(videos[pos], pos)
        } else if (holder.itemViewType == AD_VIEW_V) {
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
                _actvity.windowManager.defaultDisplay.getMetrics(displayMetrics)
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
        val builder = AdLoader.Builder(_actvity, adsidNative)
        val extras = Bundle()
        extras.putString("max_ad_content_rating", Glob.max_ad_content_rating_Glob)
        val build = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
        builder.forNativeAd { nativeAd ->
            Log.e("ads---", "native_load")
            shimmerLayout!!.visibility = View.GONE
            unifiedNativeAds = nativeAd
            val adView = _actvity.layoutInflater.inflate(R.layout.google_native_layout, null)
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
                _actvity.windowManager.defaultDisplay.getMetrics(displayMetrics)
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

        var videoTitile = itemView.findViewById<TextView>(R.id.videoTitile)
        var videoImage = itemView.findViewById<ImageView>(R.id.videoImage)
        var mainVideoCLickRL = itemView.findViewById<RelativeLayout>(R.id.mainVideoCLickRL)
        var fileSizeTxt = itemView.findViewById<TextView>(R.id.fileSizeTxt)
        var durationTv = itemView.findViewById<TextView>(R.id.durationTv)
        var moreVideoImg = itemView.findViewById<ImageView>(R.id.moreVideoImg)

        fun bindData(videoModel: VideoModel, pos: Int) {

            val po = pos
            Glide.with(_actvity).load(videos[po].CONTECT_URI)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(videoImage)
            videoTitile.text = videos[pos].TITLE
            fileSizeTxt.text = try {
                Formatter.formatShortFileSize(_actvity, videos[pos].SIZE.toLong())
            } catch (e: java.lang.Exception) {
                "0"
            }
            durationTv.text = Util().convertDurationToShortFormat(videos[pos].DURATION)

            mainVideoCLickRL.setOnClickListener {
                VideoPlayerActivity.position = pos
                Google_inter_ads.googleinter_show_GoogleInterAds(_actvity, "VideoAdpter", object : AdsCallBack {
                    override fun adsCallBack() {
                        ContextCompat.startActivity(_actvity, Intent(_actvity, VideoPlayerActivity::class.java), null)
                        setIntentNew = "ALL_Video"
                        isCheckNewClick = true
                    }
                }, true)

            }

            moreVideoImg.setOnClickListener {


                val popupMoreDetailsMenu = PopupMenu(_actvity, it)
                popupMoreDetailsMenu.inflate(R.menu.more_menu_video_item)
                popupMoreDetailsMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.shareItem -> {

                            val mvFile = File(videos[pos].DATA)
                            val mvuri = ArrayList<Uri>()
                            val mvContentUri: Uri = Util().getVideoFileUri(_actvity, mvFile)
                            mvuri.add(mvContentUri)
                            Util().shareVideoMediaItem(_actvity, mvuri)

                            true
                        }

                        R.id.deleteItem -> {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                Util().deleteFile(_actvity, videos[pos].DATA, pos)
                                notifyDataSetChanged()
                            } else {
                                val file = File(videos[pos].DATA)
                                AlertDialog.Builder(_actvity).setTitle("Delete")
                                    .setMessage("Request Delete")
                                    .setNegativeButton(
                                        "Cancle"
                                    ) { dialogInterface, i -> dialogInterface.dismiss() }.setPositiveButton(
                                        "Ok"
                                    ) { dialogInterface, POSITION ->
                                        if (file.exists()) {
                                            file.delete()
                                        }
                                        videos.removeAt(pos)
                                        val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
                                        intent.data = Uri.fromFile(file)
                                        _actvity.sendBroadcast(intent)
                                        dialogInterface.dismiss()
                                        notifyDataSetChanged()

                                        deletePosition = pos

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