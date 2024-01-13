package com.om.mymovie

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.om.mymovie.model.FolderStoreModel
import com.om.mymovie.model.VideoModel
import java.util.Date

var TAG = "VideoViewModel:::"

class VideoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val _folderList = ArrayList<FolderStoreModel>()
        val _videoList = MutableLiveData<ArrayList<VideoModel>>()
    }

    val videoListSet: LiveData<ArrayList<VideoModel>> = _videoList

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.e(TAG, "onChange::::::uri:::${uri}::\n::selfchange::${selfChange}")
            loadVideos()
        }
    }

    init {
        Log.e(TAG, "init::::")
        loadVideos()
        registerContentObserver()
    }

    @SuppressLint("Range")
    fun loadVideos() {
        Log.e(TAG, "loadVideos::::")
        val videoList = ArrayList<VideoModel>()
        videoList.clear()
        _folderList.clear()

        val queryUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = getApplication<Application>().contentResolver.query(queryUri, null, null, null, "date_added DESC")

        cursor?.use {
            while (it.moveToNext()) {

                val ID = it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val DISPLAY_NAME = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)) ?: Date().time.toString()
                val DATA = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                val BUCKET_ID = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
                val BUCKET_DISPLAY_NAME = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)) ?: "Internal Storage"
                val DURATION = it.getLong(it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
                val SIZE = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                val RESOLUTION = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)) ?: "720"
                val DATE_MODIFIED = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED))
                val MIME_TYPE = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE))
                val TITLE = it.getString(it.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))

                val CONTECT_URI: Uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ID)

                if (DURATION > 0 && SIZE!!.toLong() > 0) {
                    videoList.add(VideoModel(ID.toString(), DISPLAY_NAME, DATA, BUCKET_ID, BUCKET_DISPLAY_NAME, DURATION, SIZE, RESOLUTION, DATE_MODIFIED, MIME_TYPE, TITLE, CONTECT_URI))
                    val checkIf = CheckIF(BUCKET_DISPLAY_NAME)
                    if (checkIf) {
                        _folderList.add(
                            FolderStoreModel(
                                bucketID = BUCKET_ID,
                                bucketDisplayName = BUCKET_DISPLAY_NAME
                            )
                        )
                    }
                } else {
                    Log.e(TAG, "loadVideos:::::BUCKET_DISPLAY_NAME:::${BUCKET_DISPLAY_NAME}")
                    Log.e(TAG, "loadVideos:::::BUCKET_DISPLAY_NAME:::${DATA}")
                }
            }
        }
        _videoList.value = videoList
    }

    fun CheckIF(bucketDisplayName: String): Boolean {
        for (i in _folderList) {
            if (i.bucketDisplayName == bucketDisplayName) {
                return false
            }
        }
        return true
    }

    private fun registerContentObserver() {
        Log.e(TAG, "registerContentObserver::::")
        val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        getApplication<Application>().contentResolver.registerContentObserver(contentUri, true, contentObserver)
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "onCleared::::")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}