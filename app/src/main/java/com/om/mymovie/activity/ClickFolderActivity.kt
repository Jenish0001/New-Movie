package com.om.mymovie.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.om.mymovie.VideoViewModel
import com.om.mymovie.adpter.FilterVideoAdpter
import com.om.mymovie.adpter.FilterVideoAdpter.Companion.AD_VIEW
import com.om.mymovie.adpter.FilterVideoAdpter.Companion.folderPo
import com.om.mymovie.databinding.ActivityClickFolderBinding
import com.om.mymovie.model.VideoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClickFolderActivity : AppCompatActivity() {
    private var filterAdpter: FilterVideoAdpter? = null
    lateinit var binding: ActivityClickFolderBinding

    companion object {
        var videoList = ArrayList<VideoModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClickFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { onBackPressed() }
        val setFolderName = intent.getStringExtra("SetFolderName")
        binding.tvFolderName.text = setFolderName.toString()
        CoroutineScope(Dispatchers.Main).launch {
            videoList.clear()
            for (i in VideoViewModel._videoList.value!!) {
                if (setFolderName == i.BUCKET_DISPLAY_NAME) {
                    videoList.addAll(
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
            }
            filterAdpter = FilterVideoAdpter(this@ClickFolderActivity, videoList)
            binding.rvFilterVideo.setHasFixedSize(true)
            val layoutManager = GridLayoutManager(this@ClickFolderActivity, 2)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (filterAdpter!!.getItemViewType(position)) {

                        FilterVideoAdpter.ITEM_VIEW -> 1

                        AD_VIEW -> 2

                        else -> 0
                    }
                }
            }

            val grid = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
            if (grid) {
                binding.rvFilterVideo.adapter = filterAdpter
                binding.rvFilterVideo.layoutManager = layoutManager

            } else {
                binding.rvFilterVideo.adapter = FilterVideoAdpter(this@ClickFolderActivity, videoList)
                binding.rvFilterVideo.layoutManager = GridLayoutManager(this@ClickFolderActivity, 1)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1232 && resultCode == -1) {
            videoList.removeAt(folderPo)
            binding.rvFilterVideo.setHasFixedSize(true)
            val layoutManager = GridLayoutManager(this@ClickFolderActivity, 2)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (filterAdpter!!.getItemViewType(position)) {

                        FilterVideoAdpter.ITEM_VIEW -> 1

                        AD_VIEW -> 2

                        else -> 0
                    }
                }
            }
            val grid = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
            if (grid) {
                binding.rvFilterVideo.adapter = FilterVideoAdpter(this@ClickFolderActivity, videoList)
                binding.rvFilterVideo.layoutManager = layoutManager
            } else {
                binding.rvFilterVideo.adapter = FilterVideoAdpter(this@ClickFolderActivity, videoList)
                binding.rvFilterVideo.layoutManager = GridLayoutManager(this@ClickFolderActivity, 1)
            }
        }
    }
}