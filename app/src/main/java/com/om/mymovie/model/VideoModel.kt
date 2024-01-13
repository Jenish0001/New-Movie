package com.om.mymovie.model

import android.net.Uri

class VideoModel(
    var ID: String,
    var DISPLAY_NAME: String,
    var DATA: String,
    var BUCKET_ID: String,
    var BUCKET_DISPLAY_NAME: String,
    var DURATION: Long,
    var SIZE: String,
    var RESOLUTION: String,
    var DATE_MODIFIED: String,
    var MIME_TYPE: String,
    var TITLE: String,
    var CONTECT_URI: Uri
)