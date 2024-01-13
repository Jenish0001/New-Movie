package com.om.mymovie.activity


import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.om.mymovie.R
import com.om.mymovie.ResultsItem
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.Glob
import com.om.mymovie.databinding.ActivityDetailsOfMovieBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions

class DetailsOfMovieActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailsOfMovieBinding
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsOfMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GoogleNativeBannerHomeFragment(Glob.GOOGLE_NATIVE)

        val movieDetails = intent.getSerializableExtra("movieData") as? ResultsItem
        movieDetails.let {
            binding.tvTitleOfVideo.text = it?.title.toString()
            binding.tvDescription.text = it?.overview.toString()

            binding.tvReleseDate.text = "Release Date :- ${it?.releaseDate.toString()}"
        }
        val videoId = movieDetails!!.videoPath.toString()

        binding.youtubePlayerView.enableAutomaticInitialization = false

        lifecycle.addObserver(binding.youtubePlayerView)

        val listener: YouTubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0F)
            }

        }

        val options: IFramePlayerOptions =
            IFramePlayerOptions.Builder().controls(10).build()
        binding.youtubePlayerView.initialize(listener, options)

        binding.ivFullScreen.setOnClickListener {
            if (isFullScreen) {
                if (!isRotate) {
                    binding.youtubePlayerView.wrapContent()
                    binding.clAd.visibility = View.VISIBLE
                } else {
                    binding.youtubePlayerView.matchParent()
                    binding.clAd.visibility = View.GONE
                }


            } else {
                binding.youtubePlayerView.matchParent()
                binding.clAd.visibility = View.GONE
            }
            isFullScreen = !isFullScreen
        }

    }

    var isRotate = false
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Detect orientation change
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape orientation
            binding.youtubePlayerView.matchParent()
            binding.clAd.visibility = View.GONE

            isRotate = true

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Portrait orientation
            binding.youtubePlayerView.wrapContent()
            binding.clAd.visibility = View.VISIBLE
            isRotate = false
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
