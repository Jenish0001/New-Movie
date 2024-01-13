package com.om.mymovie.adpter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.om.mymovie.R
import com.om.mymovie.activity.ClickFolderActivity
import com.om.mymovie.activity.SplashActivity
import com.om.mymovie.model.FolderStoreModel

class FolderAdpterExtra(val activity: FragmentActivity, val _folderList: ArrayList<FolderStoreModel>) : RecyclerView.Adapter<FolderAdpterExtra.ViewData>() {

    private var view: ViewData? = null

    class ViewData(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var folderTitile = itemView.findViewById<TextView>(R.id.folderTitile)
        var mainVideoCLickRL = itemView.findViewById<RelativeLayout>(R.id.mainVideoCLickRL)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewData {

        val sortList = SplashActivity.sharedPreferencesSplash!!.getBoolean("GridLayout", false)

        if (sortList) {
            view = ViewData(LayoutInflater.from(activity).inflate(R.layout.folder_adpter_grid_item, parent, false))
        } else {
            view = ViewData(LayoutInflater.from(activity).inflate(R.layout.folder_adpter_item, parent, false))
        }
        return view!!
    }

    override fun getItemCount(): Int {
        return _folderList.size
    }

    override fun onBindViewHolder(holder: ViewData, position: Int) {
        holder.folderTitile.text = _folderList[position].bucketDisplayName
        holder.mainVideoCLickRL.setOnClickListener {
            activity.startActivity(Intent(activity, ClickFolderActivity::class.java).putExtra("SetFolderName", _folderList[position].bucketDisplayName))
        }
    }
}