package com.om.mymovie.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.om.mymovie.VideoViewModel
import com.om.mymovie.activity.MainActivity
import com.om.mymovie.activity.SplashActivity
import com.om.mymovie.adpter.FilterVideoAdpter
import com.om.mymovie.adpter.VideoAdpter
import com.om.mymovie.adpter.VideoAdpter.Companion.deletePosition
import com.om.mymovie.databinding.FragmentVideoBinding
import com.om.mymovie.model.VideoModel

class VideoFragment : Fragment() {

    private lateinit var viewModel: VideoViewModel
    lateinit var _binding: FragmentVideoBinding
    var io: ArrayList<VideoModel> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(VideoViewModel::class.java)
        viewModel.videoListSet.observe(viewLifecycleOwner) { videos ->
            io = videos
            changeUi()
        }

        MainActivity.backgroundHandler = Handler(Looper.getMainLooper()) { msg ->
            Log.e("Folder_Adpter", "onCreateView::Message->${msg.what}")
            if (msg.what == 1) {
                changeUi()
                msg.what = 0
            }
            false
        }
        return _binding.root
    }

    private fun changeUi() {
        var videoAdpter = VideoAdpter(requireActivity(), io)
        _binding.rvView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (videoAdpter.getItemViewType(position)) {

                    VideoAdpter.ITEM_VIEW_V -> 1

                    VideoAdpter.AD_VIEW_V -> 3

                    else -> 0
                }
            }
        }
        val grid = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
        if (grid) {
            _binding.rvView.layoutManager = layoutManager
            _binding.rvView.adapter = VideoAdpter(requireActivity(), io)
        } else {
            _binding.rvView.layoutManager = GridLayoutManager(requireContext(), 1)
            _binding.rvView.adapter = VideoAdpter(requireActivity(), io)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1232 && resultCode == -1) {
            io.removeAt(deletePosition!!)
            changeUi()
        }
    }
}