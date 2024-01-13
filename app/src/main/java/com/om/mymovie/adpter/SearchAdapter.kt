package com.om.mymovie.adpter

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.om.mymovie.R
import com.om.mymovie.Utils.Util
import com.om.mymovie.Utils.Util.Companion.setIntentNew
import com.om.mymovie.activity.SearchActivity
import com.om.mymovie.activity.SplashActivity
import com.om.mymovie.activity.VideoPlayerActivity
import com.om.mymovie.activity.VideoPlayerActivity.Companion.isCheckNewClick
import com.om.mymovie.ads.Google_inter_ads
import com.om.mymovie.dialog.AdsCallBack
import com.om.mymovie.model.VideoModel
import java.io.File

class SearchAdapter(var _actvity: SearchActivity, var videos: ArrayList<VideoModel>) : RecyclerView.Adapter<SearchAdapter.ViewData>() {

    companion object {
        val searchFilterList: ArrayList<VideoModel> = ArrayList()
        var searchDeletePosition: Int = 0
    }

    private var view: ViewData? = null

    class ViewData(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoTitile = itemView.findViewById<TextView>(R.id.videoTitile)
        var videoImage = itemView.findViewById<ImageView>(R.id.videoImage)
        var mainVideoCLickRL = itemView.findViewById<RelativeLayout>(R.id.mainVideoCLickRL)
        var fileSizeTxt = itemView.findViewById<TextView>(R.id.fileSizeTxt)
        var durationTv = itemView.findViewById<TextView>(R.id.durationTv)
        var moreVideoImg = itemView.findViewById<ImageView>(R.id.moreVideoImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewData {
        val sortList = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)
        if (sortList) {
            view = ViewData(LayoutInflater.from(_actvity).inflate(R.layout.video_adpter_grid_item, parent, false))
        } else {
            view = ViewData(LayoutInflater.from(_actvity).inflate(R.layout.video_adpter_item, parent, false))
        }
        return view!!
    }

    override fun getItemCount(): Int {
        return searchFilterList.size
    }

    override fun onBindViewHolder(holder: ViewData, position: Int) {

        val po = position
        Glide.with(_actvity).load(searchFilterList[po].CONTECT_URI).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.videoImage)
        holder.videoTitile.text = searchFilterList[position].TITLE
        holder.fileSizeTxt.text = try {
            Formatter.formatShortFileSize(_actvity, searchFilterList[position].SIZE.toLong())
        } catch (e: java.lang.Exception) {
            "0"
        }
        holder.durationTv.text = Util().convertDurationToShortFormat(searchFilterList[position].DURATION)

        holder.mainVideoCLickRL.setOnClickListener {

            Google_inter_ads.googleinter_show_GoogleInterAds(_actvity, "SearchAdapter", object : AdsCallBack {
                override fun adsCallBack() {
                    ContextCompat.startActivity(_actvity, Intent(_actvity, VideoPlayerActivity::class.java), null)
                }
            }, true)

            VideoPlayerActivity.position = position
            setIntentNew = "Search_Video"
            isCheckNewClick = true
        }
        holder.moreVideoImg.setOnClickListener {
            val popupMoreDetailsMenu = PopupMenu(_actvity, it)
            popupMoreDetailsMenu.inflate(R.menu.more_menu_video_item)
            popupMoreDetailsMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.shareItem -> {

                        val mvFile = File(searchFilterList[position].DATA)
                        val mvuri = ArrayList<Uri>()
                        val mvContentUri: Uri = Util().getVideoFileUri(_actvity, mvFile)
                        mvuri.add(mvContentUri)
                        Util().shareVideoMediaItem(_actvity, mvuri)

                        true
                    }

                    R.id.deleteItem -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Util().deleteFile(_actvity, searchFilterList[position].DATA, position)
                            notifyDataSetChanged()
                        } else {
                            val file = File(searchFilterList[position].DATA)
                            AlertDialog.Builder(_actvity).setTitle("Delete").setMessage("Request Delete").setNegativeButton(
                                "Cancle"
                            ) { dialogInterface, i -> dialogInterface.dismiss() }.setPositiveButton(
                                "Ok"
                            ) { dialogInterface, POSITION ->
                                if (file.exists()) {
                                    file.delete()
                                }
                                searchFilterList.removeAt(position)
                                searchDeletePosition = position
                                val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
                                intent.data = Uri.fromFile(file)
                                _actvity.sendBroadcast(intent)
                                dialogInterface.dismiss()
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
                mPopupMenuView.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(mPopupMenuView, true)
            } catch (e: Exception) {
                Log.e("Home_Act", "Error showing menu icons.", e)
            } finally {
                popupMoreDetailsMenu.show()
            }
        }

    }

    //== ================================= Search View Method Filter ====================
    fun filterList(filteredCourseAry: ArrayList<VideoModel>) {
        this.videos = filteredCourseAry
        searchFilterList.clear()
        for (i in filteredCourseAry) {
            Log.e("Search", "filterList: ${i.TITLE}")
            searchFilterList.addAll(
                listOf(
                    VideoModel(
                        i.ID, i.DISPLAY_NAME, i.DATA, i.BUCKET_ID, i.BUCKET_DISPLAY_NAME, i.DURATION, i.SIZE, i.RESOLUTION, i.DATE_MODIFIED, i.MIME_TYPE, i.TITLE, i.CONTECT_URI
                    )
                )
            )
        }
        notifyDataSetChanged()
    }

}