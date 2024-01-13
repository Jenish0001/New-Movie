package com.om.mymovie.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.om.mymovie.R
import com.om.mymovie.VideoViewModel
import com.om.mymovie.VideoViewModel.Companion._videoList
import com.om.mymovie.adpter.SearchAdapter
import com.om.mymovie.adpter.SearchAdapter.Companion.searchDeletePosition
import com.om.mymovie.adpter.SearchAdapter.Companion.searchFilterList
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.Glob
import com.om.mymovie.databinding.ActivitySearchBinding
import com.om.mymovie.model.VideoModel

class SearchActivity : AppCompatActivity() {

    companion object {
        var SearchList: ArrayList<VideoModel> = ArrayList()
    }

    private var mSearchAdapter: SearchAdapter? = null
    private lateinit var binding: ActivitySearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GoogleNativeBannerHomeFragment(Glob.GOOGLE_NATIVE)

        binding.ivBack.setOnClickListener { onBackPressed() }
        val viewModel = ViewModelProvider(this).get(VideoViewModel::class.java)
        viewModel.videoListSet.observe(this) { videos ->
            SearchList = videos
            changeUi()
        }
        searchFilterList.clear()
        for (i in _videoList.value!!) {
            searchFilterList.addAll(
                listOf(
                    VideoModel(
                        i.ID,
                        i.DISPLAY_NAME,
                        i.DATA,
                        i.BUCKET_ID,
                        i.BUCKET_DISPLAY_NAME,
                        i.DURATION,
                        i.SIZE,
                        i.RESOLUTION,
                        i.DATE_MODIFIED,
                        i.MIME_TYPE,
                        i.TITLE,
                        i.CONTECT_URI
                    )
                )
            )
        }
        binding.edtSearchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                refreshList(s.toString(), SearchList)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.rvSearchView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Scroll has stopped, hide the side scrollbar view
                    binding.fastscroller.handleVisibilityDuration = 1000

                } else {
                    // RecyclerView is scrolling, show the side scrollbar view
                    binding.fastscroller.handleVisibilityDuration = 1000
                }
            }
        })

    }

    private fun refreshList(char: String, searchList: ArrayList<VideoModel>) {
        val filteredArray: ArrayList<VideoModel> = ArrayList()
        val videoArray: ArrayList<VideoModel> = searchList
        for (i in videoArray) {
            if (i.TITLE.toLowerCase().contains(char.toLowerCase())) {
                filteredArray.add(i)
            }
        }
        mSearchAdapter!!.filterList(filteredArray)
        mSearchAdapter!!.notifyDataSetChanged()
    }

    private fun changeUi() {

        val grid = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
        if (grid) {
            mSearchAdapter = SearchAdapter(this, SearchList)
            binding.rvSearchView.layoutManager = GridLayoutManager(this, 3)
            binding.rvSearchView.adapter = mSearchAdapter
        } else {
            mSearchAdapter = SearchAdapter(this, SearchList)
            binding.rvSearchView.layoutManager = GridLayoutManager(this, 1)
            binding.rvSearchView.adapter = mSearchAdapter
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1232 && resultCode == -1) {
            searchFilterList.removeAt(searchDeletePosition)
            changeUi()
        }
    }

    private var unifiedNativeAds: NativeAd? = null
    private fun GoogleNativeBannerHomeFragment(adsId: String) {
        val builder = AdLoader.Builder(this, adsId).forNativeAd { nativeAd ->
            unifiedNativeAds = nativeAd

            Log.e("NativeBanner----", "NativeBanner_show")
            ShowNativeBanner(binding.flSmallNative, this)
        }


        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e("NativeBanner----", "NativeBanner_failed")
                unifiedNativeAds = null
                binding.tvAdsText.setVisibility(View.VISIBLE)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e("NativeBanner----", "NativeBanner_failed")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.e("NativeBanner----", "NativeBanner_click")
                AppOpenManager.isShowingAd_AppOpenMan = true
                unifiedNativeAds = null
            }
        }).build()
        val extras = Bundle()
        extras.putString("max_ad_content_rating", Glob.max_ad_content_rating_Glob)
        val adReques =
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        adLoader.loadAd(adReques)


    }


    //show google nativebanner130
    private fun ShowNativeBanner(framelayout: FrameLayout, context: Activity) {
        Log.e("NativeBanner----", "NativeBanner_show")
        binding.tvAdsText.setVisibility(View.GONE)
        val adView = context.layoutInflater.inflate(
            R.layout.google_native_view_small,
            null
        ) as NativeAdView
        populateAppsInstallAdViewAndSet(unifiedNativeAds!!, adView)
        framelayout.removeAllViews()
        framelayout.addView(adView)
    }

    private fun populateAppsInstallAdViewAndSet(nativeAd: NativeAd, adView: NativeAdView) {
        adView.iconView = adView.findViewById(R.id.ivAppImage)
        adView.headlineView = adView.findViewById(R.id.tvHeadline1)
        adView.bodyView = adView.findViewById(R.id.tvInstallBody)
        adView.callToActionView = adView.findViewById(R.id.tvAppInstallText)
        (adView.headlineView as TextView?)!!.text = nativeAd.headline
        if (nativeAd.body == null) {
            adView.bodyView!!.visibility = View.INVISIBLE
        } else {
            adView.bodyView!!.visibility = View.VISIBLE
            (adView.bodyView as TextView?)!!.text = nativeAd.body
        }
        if (nativeAd.icon == null) {
            adView.iconView!!.visibility = View.GONE
        } else {
            (adView.iconView as ImageView?)!!.setImageDrawable(nativeAd.icon!!.drawable)
            adView.iconView!!.visibility = View.VISIBLE
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView!!.visibility = View.INVISIBLE
        } else {
            adView.callToActionView!!.visibility = View.VISIBLE
            (adView.callToActionView as TextView?)!!.text = nativeAd.callToAction
        }
        adView.setNativeAd(nativeAd)
    }
}