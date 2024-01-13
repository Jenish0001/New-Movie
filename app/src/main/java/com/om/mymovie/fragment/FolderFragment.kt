package com.om.mymovie.fragment

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
import com.om.mymovie.VideoViewModel.Companion._folderList
import com.om.mymovie.activity.MainActivity
import com.om.mymovie.activity.SplashActivity
import com.om.mymovie.adpter.FolderAdpter
import com.om.mymovie.adpter.FolderAdpter.Companion.AD_VIEW
import com.om.mymovie.adpter.FolderAdpter.Companion.ITEM_VIEW
import com.om.mymovie.databinding.FragmentFolderBinding

class FolderFragment : Fragment() {

    private lateinit var _binding: FragmentFolderBinding
    private lateinit var viewModel: VideoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentFolderBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(VideoViewModel::class.java)
        viewModel.videoListSet.observe(viewLifecycleOwner) {
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
        _folderList.forEach {
            Log.e("TAG----", "changeUi:${it.bucketDisplayName}")
        }
        val folder = FolderAdpter(requireActivity(), _folderList)
        _binding.rvFolderView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 3)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (folder.getItemViewType(position)) {
                    ITEM_VIEW -> 1
                    AD_VIEW -> 3
                    else -> 0
                }
            }
        }
        val grid = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
        if (grid) {
            _binding.rvFolderView.layoutManager = layoutManager
            _binding.rvFolderView.adapter = folder
        } else {
            _binding.rvFolderView.layoutManager = GridLayoutManager(requireContext(), 1)
            _binding.rvFolderView.adapter = folder
        }
    }
}

