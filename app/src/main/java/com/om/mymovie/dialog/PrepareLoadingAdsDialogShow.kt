package com.om.mymovie.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.om.mymovie.R

class PrepareLoadingAdsDialogShow(context: Context?, theme: Int) : Dialog(context!!, theme) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_ads_show_)
        setCancelable(false)
    }
}