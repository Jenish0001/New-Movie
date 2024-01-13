package com.om.mymovie.Utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class Util {
    companion object {
        var setIntentNew: String = ""
    }

    fun getVideoFileUri(context: Context, filePath: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", filePath)
    }

    fun shareVideoMediaItem(context: Activity, uripath: ArrayList<Uri>) {
        try {
            val videofileDirPath =
                File(Environment.getExternalStorageDirectory(), context.packageName + ".provider")
            videofileDirPath.mkdirs()
            val innt = Intent(Intent.ACTION_SEND_MULTIPLE)
            innt.type = "*/*"
            innt.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uripath)
            innt.putExtra(Intent.EXTRA_SUBJECT, "VideoPlayer")
            context.startActivityForResult(Intent.createChooser(innt, "Share via "), 1212)
        } catch (e: IOException) {
            throw RuntimeException("Error generating file", e)
        }
    }

    fun convertDurationToShortFormat(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        val stringBuilder = StringBuilder()
        if (hours > 0) {
            stringBuilder.append(hours)
            stringBuilder.append(":")
        }
        if (minutes > 0) {
            stringBuilder.append(minutes)
            stringBuilder.append(":")
        }
        if (seconds > 0) {
            stringBuilder.append(seconds)
            stringBuilder.append("s")
        }
        return stringBuilder.toString().trim()
    }

    fun convertDateTimeToShortFormat(dateTimeInMillis: Long): String {
        Log.e("787878", "convertDateTimeToShortFormat: ${dateTimeInMillis}")
        val epoch = dateTimeInMillis
        val date = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Date(epoch * 1000))
        Log.e("787878", "convertDateTimeToShortFormat: ${date}")
        return date.toString()
    }

    fun deleteFile(tthis: Activity, str: String?, k: Int) {
        val File = File(str)
        val mediaScannerClient: MediaScannerConnection.MediaScannerConnectionClient =
            object : MediaScannerConnection.MediaScannerConnectionClient {
                private var mediasccaner: MediaScannerConnection? = null
                override fun onMediaScannerConnected() {
                    mediasccaner!!.scanFile(File.toString(), null)
                }

                override fun onScanCompleted(path: String, strMain: Uri) {
                    val urilist: MutableCollection<Uri> = java.util.ArrayList()
                    urilist.add(strMain)
                    Log.e("0000", "onScanCompleted:22222 $path")
                    Log.e("000--", "path--$path")
                    var pendingIntent: PendingIntent? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        pendingIntent =
                            MediaStore.createDeleteRequest(tthis.contentResolver, urilist)
                    }
                    try {
                        tthis.startIntentSenderForResult(
                            pendingIntent!!.intentSender, 1232, null, 0, 0, 0
                        )
                        Log.e("dddd", "onScanCompleted:....44444 $tthis")
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("dddd", "onScanCompleted:...333333 $path")
                        Log.e("ff--", "path--" + e.message)
                        e.printStackTrace()
                    }
                    mediasccaner!!.disconnect()
                }

                init {
                    mediasccaner = MediaScannerConnection(
                        tthis, this
                    )
                    mediasccaner!!.connect()
                }
            }

    }

}