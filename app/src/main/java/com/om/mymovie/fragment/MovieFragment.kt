package com.om.mymovie.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.om.mymovie.MovieAdapter
import com.om.mymovie.R
import com.om.mymovie.ResponseMovie
import com.om.mymovie.ads.AppOpenManager
import com.om.mymovie.ads.Glob
import com.om.mymovie.api.RetrofitClient
import com.om.mymovie.databinding.FragmentMovieBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MovieFragment : Fragment() {

    private var processDialog: ProgressDialog? = null
    lateinit var binding: FragmentMovieBinding

    lateinit var mActivity: Activity

    private lateinit var movieAdapter: MovieAdapter

    companion object {
        var isEmptyView: ConstraintLayout? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { it ->
            mActivity = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovieBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment

        processDialog = ProgressDialog.show(mActivity, "Loading", "Please wait...", true, false)
        movieAdapter = MovieAdapter()

        isEmptyView = binding.clFoundView
        prepareRecyclerView()

        val call = RetrofitClient(mActivity).instance.getMovies(1)
        call.enqueue(object : Callback<ResponseMovie> {
            override fun onResponse(call: Call<ResponseMovie>, response: Response<ResponseMovie>) {
                if (response.isSuccessful) {
                    processDialog?.dismiss()
                    Log.e("TAG---", "onResponse:::${response.body()?.results}")
                    movieAdapter.submitList(response.body()?.results!!.toMutableList())
                    googleNativeBanner60Dp(Glob.GOOGLE_NATIVE)
                } else {
                    loopTimeSendRequsetByServer()
                }
            }

            override fun onFailure(call: Call<ResponseMovie>, t: Throwable) {
                Log.e("TAG---", "onFailure::::OnFailure::::${t.message}")
                processDialog?.dismiss()
                if (isConnected(mActivity)) {
                    binding.clFoundView.visibility = View.GONE
                    loopTimeSendRequsetByServer()
                } else {
                    binding.clFoundView.visibility = View.VISIBLE
                }
            }
        })

        binding.tvInternetBtn.setOnClickListener {
            try {
                context?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Please Try Again", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root

    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager: ConnectivityManager
        var networkInfo: NetworkInfo? = null
        try {
            connectivityManager =
                context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            networkInfo = connectivityManager.activeNetworkInfo
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private var currentPage = 2
    private fun prepareRecyclerView() {
        binding.rvMovieFrag.apply {
            adapter = movieAdapter
            layoutManager = GridLayoutManager(mActivity, 3)
            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                        val firstVisibleItemPosition =
                            layoutManager!!.findFirstVisibleItemPosition()
                        val isAtTop =
                            firstVisibleItemPosition == 0 && layoutManager.findViewByPosition(0)!!.top == 0
                        if (isAtTop) {
                            binding.tvLoading.visibility = View.GONE
                        } else {
                            if (isConnected(mActivity)) {
                                binding.tvLoading.visibility = View.VISIBLE
                                loadMoreData()
                            } else {
                                binding.tvLoading.visibility = View.GONE
                            }
                        }
                    }
                }
            })
        }
    }

    private fun loopTimeSendRequsetByServer() {
        val call = RetrofitClient(mActivity).instance.getMovies(1)
        call.enqueue(object : Callback<ResponseMovie> {
            override fun onResponse(call: Call<ResponseMovie>, response: Response<ResponseMovie>) {
                if (response.isSuccessful) {
                    processDialog?.dismiss()
                    Log.e("TAG---", "onResponse:::${response.body()?.results}")
                    movieAdapter.submitList(response.body()?.results!!.toMutableList())
                }
            }

            override fun onFailure(call: Call<ResponseMovie>, t: Throwable) {
                Log.e("TAG---", "onFailure::::OnFailure::::${t.message}")
                if (isConnected(mActivity)) {
                    processDialog?.show()
                    binding.clFoundView.visibility = View.GONE
                    loopTimeSendRequsetByServer()
                } else {
                    binding.clFoundView.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun loadMoreData() {
        val call = RetrofitClient(mActivity).instance.getMovies(currentPage)
        call.enqueue(object : Callback<ResponseMovie> {
            override fun onResponse(call: Call<ResponseMovie>, response: Response<ResponseMovie>) {
                if (response.isSuccessful) {
                    Log.e("TAG---", "onResponse:::${response.body()?.results}")
                    movieAdapter.addItems(response.body()?.results!!.toMutableList())
                    movieAdapter.isEmptyView()
                    currentPage++
                    binding.tvLoading.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<ResponseMovie>, t: Throwable) {
                Log.e("TAG---", "onFailure::::OnFailure::::${t.message}")
                binding.tvLoading.visibility = View.GONE
            }
        })
    }


    //load google nativebanner60
    private var unifiedNativeAds: NativeAd? = null
    private fun googleNativeBanner60Dp(adsId: String?) {
        Log.e("NativeBanner60----", "NativeBanner_call")
        val builder = AdLoader.Builder(mActivity, adsId!!).forNativeAd { nativeAd ->
            Log.e("NativeBanner60----", "NativeBanner_show")
            unifiedNativeAds = nativeAd
            showNativeBanner60Dp(binding.flSmallFrame, mActivity)
        }
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e("NativeBanner60----", "NativeBanner_failed")
                unifiedNativeAds = null
                binding.llAdsTextBookmark.setVisibility(View.VISIBLE)
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.e("NativeBanner60----", "NativeBanner_load")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.e("NativeBanner60----", "NativeBanner_click")
                AppOpenManager.isShowingAd_AppOpenMan = true
                unifiedNativeAds = null
            }
        }).build()
        val extras = Bundle()
        extras.putString("max_ad_content_rating", Glob.max_ad_content_rating_Glob)
        val adreques = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        adLoader.loadAd(adreques)
    }

    //show google NativeBanner60
    private fun showNativeBanner60Dp(framelayout: FrameLayout, context: Activity) {
        Log.e("NativeBanner60---", "NativeBanner_show")

        binding.llAdsTextBookmark.setVisibility(View.GONE)
        val adView = context.layoutInflater.inflate(R.layout.small_native_banner_ad_60, null) as NativeAdView
        populateAppInstallAdView60Dp(unifiedNativeAds!!, adView)
        framelayout.removeAllViews()
        framelayout.addView(adView)
    }

    //setview google NativeBanner60
    private fun populateAppInstallAdView60Dp(nativeAd: NativeAd, adView: NativeAdView) {
        adView.iconView = adView.findViewById(R.id.ivAppInstallAppIcon)
        adView.headlineView = adView.findViewById(R.id.tvAppInstallHeadLine1)
        adView.bodyView = adView.findViewById(R.id.tvAppInstallBody)
        adView.callToActionView = adView.findViewById(R.id.tvAppInstallCallToAction)
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