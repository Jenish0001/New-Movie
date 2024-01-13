package com.om.mymovie.adpter

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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
import com.om.mymovie.activity.ClickFolderActivity
import com.om.mymovie.activity.SplashActivity.Companion.sharedPreferencesSplash
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.Glob
import com.om.mymovie.ads.Glob.Companion.showNative
import com.om.mymovie.ads.Google_inter_ads
import com.om.mymovie.dialog.AdsCallBack
import com.om.mymovie.model.FolderStoreModel

class FolderAdpter(val activity: FragmentActivity, val _folderList: ArrayList<FolderStoreModel>) : RecyclerView.Adapter<ViewHolder>() {

    companion object {
        const val ITEM_VIEW = 0
        const val AD_VIEW = 1
        const val ITEM_FEED_COUNT = 20
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val sortList = sharedPreferencesSplash!!.getBoolean("GridLayout", false)
        val layoutInflater = LayoutInflater.from(activity)
        return if (viewType == ITEM_VIEW) {
            val view: View
            if (sortList) {
                view = layoutInflater.inflate(R.layout.folder_adpter_grid_item, parent, false)
            } else {
                view = layoutInflater.inflate(R.layout.folder_adpter_item, parent, false)
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
        return if (_folderList.size > 0) {
            _folderList.size + Math.round((_folderList.size / ITEM_FEED_COUNT).toFloat())
        } else _folderList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % ITEM_FEED_COUNT == 0) {
            AD_VIEW
        } else ITEM_VIEW
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == ITEM_VIEW) {
            val pos = (position - Math.round((position / ITEM_FEED_COUNT).toFloat()))
            (holder as ViewDataFolder).bindData(_folderList[pos])
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

            if (showNative.equals("yes")) {
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

        var folderTitile = itemView.findViewById<TextView>(R.id.folderTitile)
        var mainVideoCLickRL = itemView.findViewById<RelativeLayout>(R.id.mainVideoCLickRL)

        fun bindData(folderStoreModel: FolderStoreModel) {
            folderTitile.text = folderStoreModel.bucketDisplayName
            mainVideoCLickRL.setOnClickListener {
                Google_inter_ads.googleinter_show_GoogleInterAds(activity, "FolderAdapter", object : AdsCallBack {
                    override fun adsCallBack() {
                        activity.startActivity(Intent(activity, ClickFolderActivity::class.java).putExtra("SetFolderName", folderStoreModel.bucketDisplayName))
                    }
                }, true)
            }
        }


    }

}
