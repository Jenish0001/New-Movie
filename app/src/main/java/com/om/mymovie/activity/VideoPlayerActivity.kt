package com.om.mymovie.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Lifecycle
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.om.mymovie.R
import com.om.mymovie.Utils.Util
import com.om.mymovie.Utils.Util.Companion.setIntentNew
import com.om.mymovie.VideoViewModel.Companion._videoList
import com.om.mymovie.activity.ClickFolderActivity.Companion.videoList
import com.om.mymovie.activity.SearchActivity.Companion.SearchList
import com.om.mymovie.adpter.SearchAdapter.Companion.searchFilterList
import com.om.mymovie.databinding.ActivityVideoPlayerBinding
import com.om.mymovie.databinding.ExoplayerControlItemLayoutBinding
import com.om.mymovie.model.VideoModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class VideoPlayerActivity : AppCompatActivity(), GestureDetector.OnGestureListener, AudioManager.OnAudioFocusChangeListener {

    private var infoDialog: Dialog? = null

    private var speedSetVideo: Float = 1.0f

    private lateinit var _binding: ActivityVideoPlayerBinding

    private lateinit var binding: ExoplayerControlItemLayoutBinding

    private lateinit var mGestureDetectorCompat: GestureDetectorCompat

    private var playerList = ArrayList<VideoModel>()

    private lateinit var ivLockIcon: ImageView

    private lateinit var ivPlayAndPauseIcon: ImageView

    private lateinit var ivMuteSoundIcon: ImageView

    private lateinit var tvVideoTitle: TextView

    private lateinit var tvBattry: TextView

    private lateinit var ivFullScreenIcon: ImageView

    private lateinit var mAudioManager: AudioManager

    private var minSwipeYEngl: Float = 0f

    private var checkMuteSound = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val currentTime = Calendar.getInstance().time
            val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            val formattedTime = timeFormat.format(currentTime)
            findViewById<TextView>(R.id.tvTime).text = formattedTime
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        binding = ExoplayerControlItemLayoutBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        /*window.addFlags(67108864)
        window.addFlags(134217728)
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.decorView.systemUiVisibility = 2
            window.decorView.systemUiVisibility = 4096
        }*/

        tvVideoTitle = findViewById(R.id.tvVideoTitle)
        ivPlayAndPauseIcon = findViewById(R.id.ivPlayAndPauseIcon)
        ivFullScreenIcon = findViewById(R.id.ivFullScreenIcon)
        ivLockIcon = findViewById(R.id.ivLockIcon)
        ivMuteSoundIcon = findViewById(R.id.ivMuteSoundIcon)
        tvBattry = findViewById(R.id.tvBattry)

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeSet = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        _binding.btnVolumeIcon.text = volumeSet.toString()

        mGestureDetectorCompat = GestureDetectorCompat(this, this)
        showSystemUI(_binding.root)
        initVideoList()
        createExoPlayer()
        mDoubleTapEnableSet()

        registerReceiver(BatteryReceiver(), IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        handler.post(updateTimeRunnable)
    }

    private fun initVideoList() {
        if (setIntentNew == "ALL_Video") {
            playerList.clear()
            playerList = ArrayList()
            playerList.addAll(_videoList.value!!)
            createExoPlayer()
        } else if (setIntentNew == "Folder_Video") {
            playerList.clear()
            playerList = ArrayList()
            playerList.addAll(videoList)
            createExoPlayer()
        }else if (setIntentNew == "Search_Video") {
            playerList.clear()
            playerList = ArrayList()
            playerList.addAll(searchFilterList)
            createExoPlayer()
        }
    }


    @SuppressLint("NewApi")
    private fun createExoPlayer() {
        try {
            mExoPlayer.release()
            binding.tvVideoTitle.text = playerList[position].TITLE
        } catch (e: Exception) {
            Log.e("0990", "createPlySet: ${e.message}")
        }
        if (!isSpeedCheckedSet) {
            speedSet = 1.0f
        }
        binding.tvVideoTitle.isSelected = true
        mDefaultTrackSelectorCheck = DefaultTrackSelector(this)
        mExoPlayer = ExoPlayer.Builder(this).setTrackSelector(mDefaultTrackSelectorCheck).build()
        mDoubleTapEnableSet()

        mExoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(playerList[position].DATA))))
        mExoPlayer.setPlaybackSpeed(speedSetVideo)
        mExoPlayer.prepare()
        if (mRequestAudioFocusSet()) {
            mExoPlayer.playWhenReady = true
        }
        playVideo()

        mExoPlayer.addListener(object : Player.Listener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onPlaybackStateChanged(playbackStateInt: Int) {
                super.onPlaybackStateChanged(playbackStateInt)
                if (playbackStateInt == Player.STATE_ENDED) {
                    val ch = true
                    if (ch) {
                        nextPrepareVideoAndSet()
                    } else {
                        onBackPressed()
                        finish()
                    }
                }
                initBindingAndSetView()
            }
        })
        playInFullscreen(mEnable = isFullscreenCheck)
        mLoudnessEnhancerSet = LoudnessEnhancer(mExoPlayer.audioSessionId)
        mLoudnessEnhancerSet.enabled = true

        nowPlayingId = playerList[position].ID
        seekBarPrepareSet()
        initBindingAndSetView()

    }


    private fun nextPrepareVideoAndSet(isNext: Boolean = true) {

        if (isNext) {
            setSelectPosition()
            createExoPlayer()
            if (position == 0) {
                finish()
            }
        } else {
            setSelectPosition(isIncrSet = false)
            createExoPlayer()
            if (position == playerList.size - 1) {
                if (position > 0) {
                    finish()
                }
            }
        }
    }

    private fun setSelectPosition(isIncrSet: Boolean = true) {
        if (isIncrSet) {
            if (playerList.size - 1 == position) {
                position = 0
            } else {
                ++position
            }
        } else {
            if (position == 0) {
                position = playerList.size - 1
            } else {
                --position
            }
        }
    }

    private fun mRequestAudioFocusSet(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                ).setAcceptsDelayedFocusGain(true).setOnAudioFocusChangeListener(this).build()
            ) == AudioManager.AUDIOFOCUS_GAIN
        } else {
            mAudioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_GAIN
        }
    }

    private fun mDoubleTapEnableSet() {

        _binding.dtpvPlayerView.player = mExoPlayer
        _binding.ytol.performListener(object : YouTubeOverlay.PerformListener {
            override fun onAnimationEnd() {
                _binding.ytol.visibility = View.GONE
            }

            override fun onAnimationStart() {
                _binding.ytol.visibility = View.VISIBLE
            }
        })
        _binding.ytol.player(mExoPlayer)

        _binding.dtpvPlayerView.setOnTouchListener { _, motionEvent ->
            _binding.dtpvPlayerView.isDoubleTapEnabled = false
            if (!isLockedLayCheck) {
                _binding.dtpvPlayerView.isDoubleTapEnabled = true
                mGestureDetectorCompat.onTouchEvent(motionEvent)
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    _binding.btnBrightnessIcon.visibility = View.GONE
                    _binding.btnVolumeIcon.visibility = View.GONE
                }
            }
            return@setOnTouchListener false
        }
    }


    private fun seekBarPrepareSet() {

        findViewById<DefaultTimeBar>(R.id.exo_progress).addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                pauseVideo()
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                mExoPlayer.seekTo(position)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                playVideo()
            }
        })
    }

    private fun playVideo() {
        ivPlayAndPauseIcon.setImageResource(R.drawable.ic_pause_img)
        if (mRequestAudioFocusSet()) {
            mExoPlayer.play()
        }
    }

    private fun pauseVideo() {
        ivPlayAndPauseIcon.setImageResource(R.drawable.ic_play_vp_img)
        mExoPlayer.pause()
    }

    companion object {
        var isCheckNewClick: Boolean = false
        private lateinit var mExoPlayer: ExoPlayer
        var nowPlayingId: String = ""
        var position: Int = -1
        var isLockedLayCheck: Boolean = false
        lateinit var mDefaultTrackSelectorCheck: DefaultTrackSelector
        lateinit var mLoudnessEnhancerSet: LoudnessEnhancer
        var speedSet: Float = 1.0f
        var brightnessSet: Int = 0
        var volumeSet: Int = 0
        var isSpeedCheckedSet: Boolean = false
        var isFullscreenCheck: Boolean = false
    }

    override fun onDown(p0: MotionEvent): Boolean {
        minSwipeYEngl = 0f
        return false
    }

    override fun onShowPress(p0: MotionEvent) = Unit

    override fun onSingleTapUp(p0: MotionEvent): Boolean = false

    override fun onScroll(event: MotionEvent, event1: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        minSwipeYEngl += distanceY
        val sysWidth = Resources.getSystem().displayMetrics.widthPixels
        val sysHeight = Resources.getSystem().displayMetrics.heightPixels
        val borderSet = 100 * Resources.getSystem().displayMetrics.density.toInt()
        if (event.x < borderSet || event.y < borderSet || event.x > sysWidth - borderSet || event.y > sysHeight - borderSet) return false
        if (abs(distanceX) < abs(distanceY) && abs(minSwipeYEngl) > 50) {
            if (event.x < sysWidth / 2) {
                _binding.btnBrightnessIcon.visibility = View.VISIBLE
                _binding.btnVolumeIcon.visibility = View.GONE
                val increDis = distanceY > 0
                val newValueCre = if (increDis) brightnessSet + 1 else brightnessSet - 1
                if (newValueCre in 0..30) brightnessSet = newValueCre
                _binding.btnBrightnessIcon.text = brightnessSet.toString()
                setScreenBrightnesAndSet(brightnessSet)
            } else {
                _binding.btnBrightnessIcon.visibility = View.GONE
                _binding.btnVolumeIcon.visibility = View.VISIBLE
                val audioSet = this.getSystemService(AUDIO_SERVICE) as AudioManager
                val mMaxVolume = audioSet.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val iIncreSet = distanceY > 0
                val nNewValueSet = if (iIncreSet) volumeSet + 1 else volumeSet - 1
                if (nNewValueSet in 0..mMaxVolume) volumeSet = nNewValueSet
                _binding.btnVolumeIcon.text = volumeSet.toString()
                audioSet.setStreamVolume(AudioManager.STREAM_MUSIC, volumeSet, 0)
            }
            minSwipeYEngl = 0f
        }

        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                _binding.btnBrightnessIcon.visibility = View.GONE
                _binding.btnVolumeIcon.visibility = View.VISIBLE
                val audioSet = getSystemService(AUDIO_SERVICE) as AudioManager
                val mMaxVolume = audioSet.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val nNewValueSet = volumeSet - 1
                if (nNewValueSet in 0..mMaxVolume) volumeSet = nNewValueSet
                _binding.btnVolumeIcon.text = volumeSet.toString()
                audioSet.setStreamVolume(AudioManager.STREAM_MUSIC, volumeSet, 0)
                Handler(Looper.getMainLooper()).postDelayed({
                    _binding.btnVolumeIcon.visibility = View.GONE
                }, 1000)
            }

            KeyEvent.KEYCODE_VOLUME_UP -> {
                _binding.btnBrightnessIcon.visibility = View.GONE
                _binding.btnVolumeIcon.visibility = View.VISIBLE
                val audioSet = getSystemService(AUDIO_SERVICE) as AudioManager
                val mMaxVolume = audioSet.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val nNewValueSet = volumeSet + 1
                if (nNewValueSet in 0..mMaxVolume) volumeSet = nNewValueSet

                _binding.btnVolumeIcon.text = volumeSet.toString()
                audioSet.setStreamVolume(AudioManager.STREAM_MUSIC, volumeSet, 0)
                Handler(Looper.getMainLooper()).postDelayed({
                    _binding.btnVolumeIcon.visibility = View.GONE
                }, 1000)
            }

            KeyEvent.KEYCODE_BACK -> {
                onBackPressed()
            }
        }
        return true
    }

    private fun setScreenBrightnesAndSet(value: Int) {
        val display = 1.0f / 30
        val mattr = this.window.attributes
        mattr.screenBrightness = display * value
        this.window.attributes = mattr
    }

    override fun onLongPress(p0: MotionEvent) = Unit

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean = false

    private var mIsScreenOff: Boolean = false
    private var mPlayOnAudioFocus: Boolean = false
    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange != -2) {
            if (focusChange == -1) {
                mAudioManager.abandonAudioFocus(this)
                mPlayOnAudioFocus = false
                mExoPlayer.playWhenReady = false
                findViewById<ImageView>(R.id.ivPlayAndPauseIcon).setImageResource(R.drawable.ic_play_vp_img)
            } else if (focusChange == 1) {
                if (mPlayOnAudioFocus && !mExoPlayer.playWhenReady) {
                    if (mRequestAudioFocusSet()) {
                        mExoPlayer.playWhenReady = true
                    }
                } else if (mExoPlayer.playWhenReady) {
                    mExoPlayer.volume = 1.0f
                }
                this.mPlayOnAudioFocus = false
            }
        } else if (mExoPlayer.playWhenReady) {
            mPlayOnAudioFocus = true
            mExoPlayer.playWhenReady = false
            findViewById<ImageView>(R.id.ivPlayAndPauseIcon).setImageResource(R.drawable.ic_play_vp_img)
        }
    }

    @SuppressLint("NewApi")
    private fun initBindingAndSetView() {
        findViewById<ImageView>(R.id.tvback).setOnClickListener {
            finish()
        }
        ivPlayAndPauseIcon.setOnClickListener {
            if (mExoPlayer.isPlaying) {
                pauseVideo()
            } else {
                playVideo()
            }
        }
        findViewById<ImageView>(R.id.ivRotateIcon).setOnClickListener {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                hideAndFullScreenSystemUI(_binding.root)
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                showSystemUNFullUI(_binding.root)
            }
        }
        findViewById<ImageView>(R.id.ivPipIcon).setOnClickListener {
            goToPIPMode()
        }
        findViewById<ImageView>(R.id.ivSpeedVideoIcon).setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_speed_set_layout)
            dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            dialog.findViewById<TextView>(R.id.s1XTv).setOnClickListener {
                speedSetVideo = 1.0f
                mExoPlayer.setPlaybackSpeed(speedSetVideo)
                dialog.dismiss()
            }
            dialog.findViewById<TextView>(R.id.s15XTv).setOnClickListener {
                speedSetVideo = 1.5f
                mExoPlayer.setPlaybackSpeed(speedSetVideo)
                dialog.dismiss()
            }
            dialog.findViewById<TextView>(R.id.s2XTv).setOnClickListener {
                speedSetVideo = 2.0f
                mExoPlayer.setPlaybackSpeed(speedSetVideo)
                dialog.dismiss()
            }
            dialog.findViewById<TextView>(R.id.s25XTv).setOnClickListener {
                speedSetVideo = 2.5f
                mExoPlayer.setPlaybackSpeed(speedSetVideo)
                dialog.dismiss()
            }
            dialog.findViewById<TextView>(R.id.s3XTv).setOnClickListener {
                speedSetVideo = 3.0f
                mExoPlayer.setPlaybackSpeed(speedSetVideo)
                dialog.dismiss()
            }
        }
        findViewById<ImageView>(R.id.ivNextIcon).setOnClickListener {
            nextPrepareVideoAndSet()
        }
        findViewById<ImageView>(R.id.ivPrevIcon).setOnClickListener {
            nextPrepareVideoAndSet(false)
        }
        infoDialog = Dialog(this)
        findViewById<ImageView>(R.id.ivFileInfo).setOnClickListener {
            infoDialog!!.setContentView(R.layout.layout_info_dialog)
            infoDialog!!.show()
            pauseVideo()
            infoDialog?.findViewById<TextView>(R.id.tvName)?.text = playerList[position].DISPLAY_NAME
            infoDialog?.findViewById<TextView>(R.id.tvDate)?.text = Util().convertDateTimeToShortFormat(playerList[position].DATE_MODIFIED.toLong())
            infoDialog?.findViewById<TextView>(R.id.tvDuration)?.text = Util().convertDurationToShortFormat(playerList[position].DURATION)
            infoDialog?.findViewById<TextView>(R.id.tvSize)?.text = Formatter.formatShortFileSize(this, playerList[position].SIZE.toLong())
            infoDialog?.findViewById<TextView>(R.id.tvLocation)?.text = playerList[position].DATA
        }
        infoDialog!!.setOnDismissListener {
            playVideo()
        }
        ivFullScreenIcon.setOnClickListener {
            if (isFullscreenCheck) {
                isFullscreenCheck = false
                playInFullscreen(mEnable = false)
            } else {
                isFullscreenCheck = true
                playInFullscreen(mEnable = true)
            }
        }
        ivLockIcon.setOnClickListener {
            if (!isLockedLayCheck) {
                isLockedLayCheck = true
                _binding.dtpvPlayerView.hideController()
                _binding.dtpvPlayerView.useController = false
                ivLockIcon.visibility = View.GONE
                hideSystemUI(_binding.root)

            } else {
                isLockedLayCheck = false
                _binding.dtpvPlayerView.useController = true
                _binding.dtpvPlayerView.showController()
                ivLockIcon.visibility = View.GONE
                ivLockIcon.visibility = View.VISIBLE
                showSystemUI(_binding.root)
            }
        }
        _binding.ibUnlockIcon.setOnClickListener {
            isLockedLayCheck = false
            _binding.dtpvPlayerView.useController = true
            _binding.dtpvPlayerView.showController()
            ivLockIcon.visibility = View.VISIBLE
            _binding.ibUnlockIcon.visibility = View.GONE
        }
        _binding.dtpvPlayerView.setControllerVisibilityListener {
            when {
                isLockedLayCheck -> {
                    _binding.ibUnlockIcon.visibility = View.VISIBLE
                }

                _binding.dtpvPlayerView.isControllerVisible -> {
                    ivLockIcon.visibility = View.VISIBLE
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        showSystemUI(_binding.root)
                    } else {
                        hideAndFullScreenSystemUI(_binding.root)
                    }
                }

                !_binding.dtpvPlayerView.isControllerVisible -> {
                    hideSystemUI(_binding.root)
                }

                else -> {
                    ivLockIcon.visibility = View.INVISIBLE
                }
            }
        }
        findViewById<ImageView>(R.id.ivSubtitleIcon).setOnClickListener {

            pauseVideo()
            val subtitlelist = ArrayList<String>()
            val subtitlesListSec = ArrayList<String>()
            for (loop in mExoPlayer.currentTracksInfo.trackGroupInfos) {
                if (loop.trackType == C.TRACK_TYPE_TEXT) {
                    val mTrackGroup = loop.trackGroup
                    for (i in 0 until mTrackGroup.length) {
                        subtitlelist.add(mTrackGroup.getFormat(i).language.toString())
                        subtitlesListSec.add(
                            "${subtitlesListSec.size + 1}. " + Locale(mTrackGroup.getFormat(i).language.toString()).displayLanguage + "(${
                                mTrackGroup.getFormat(
                                    i
                                ).label
                            })"
                        )
                    }
                }
            }

            val tempTrackSize = subtitlesListSec.toArray(arrayOfNulls<CharSequence>(subtitlesListSec.size))
            val subDialog = MaterialAlertDialogBuilder(
                this, R.style.alertDialog
            ).setTitle("Select Subtitiles").setOnCancelListener {
                playVideo()
            }.setPositiveButton("Off Subtitle") { self, _ ->
                it.foreground = resources.getDrawable(R.drawable.shape_cornar)
                mDefaultTrackSelectorCheck.setParameters(
                    mDefaultTrackSelectorCheck.buildUponParameters().setRendererDisabled(
                        C.TRACK_TYPE_VIDEO, true
                    )
                )
                self.dismiss()
                playVideo()
            }.setItems(tempTrackSize) { _, position ->
                Snackbar.make(_binding.root, subtitlesListSec[position] + " Selected", 3000).show()
                mDefaultTrackSelectorCheck.setParameters(
                    mDefaultTrackSelectorCheck.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_VIDEO, false).setPreferredTextLanguage(subtitlelist[position])
                )
                playVideo()
            }.create()
            subDialog.show()

            subDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
            subDialog.window?.setBackgroundDrawable(ColorDrawable(0x99fff.toInt()))

            subDialog.setOnDismissListener {
                playVideo()
            }

        }
        findViewById<ImageView>(R.id.ivMusicIcon).setOnClickListener {
            pauseVideo()
            val mAudioTrackSet = ArrayList<String>()
            val mAudioTrakList = ArrayList<String>()
            for (loop in mExoPlayer.currentTracksInfo.trackGroupInfos) {
                if (loop.trackType == C.TRACK_TYPE_AUDIO) {
                    val mTrackGroup = loop.trackGroup
                    for (i in 0 until mTrackGroup.length) {
                        mAudioTrackSet.add(mTrackGroup.getFormat(i).language.toString())
                        mAudioTrakList.add(
                            "${mAudioTrakList.size + 1}. " + Locale(mTrackGroup.getFormat(i).language.toString()).displayLanguage + " (${
                                mTrackGroup.getFormat(
                                    i
                                ).label
                            })"
                        )
                    }
                }
            }

            try {
                if (mAudioTrakList[0].contains("null")) mAudioTrakList[0] = "1. Default Track"
            } catch (e: Exception) {
                Log.e("TAG", "initBlinding: ${e.message}")
            }

            val tempTracksize = mAudioTrakList.toArray(arrayOfNulls<CharSequence>(mAudioTrakList.size))
            val audioDefaulteDialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
                .setTitle("Select Audio").setOnCancelListener {
                    playVideo()
                }
                .setPositiveButton("Off Audio") { it, _ ->
                    mDefaultTrackSelectorCheck.setParameters(
                        mDefaultTrackSelectorCheck.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_AUDIO, true
                        )
                    )
                    checkMuteSound = true
                    ivMuteSoundIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_red_mute_icon))
                    mDefaultTrackSelectorCheck.setParameters(mDefaultTrackSelectorCheck.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_AUDIO, true))
                    it.dismiss()
                    playVideo()
                }
                .setItems(tempTracksize) { _, position ->
                    Snackbar.make(_binding.root, mAudioTrakList[position] + " Selected", 3000).show()
                    mDefaultTrackSelectorCheck.setParameters(
                        mDefaultTrackSelectorCheck.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_AUDIO, false).setPreferredAudioLanguage(mAudioTrackSet[position])
                    )
                    playVideo()
                }.create()

            audioDefaulteDialog.show()
            audioDefaulteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
            audioDefaulteDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))

        }
        ivMuteSoundIcon.setOnClickListener {
            if (checkMuteSound) {
                ivMuteSoundIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_mute_img))
                mDefaultTrackSelectorCheck.setParameters(
                    mDefaultTrackSelectorCheck.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                )
                checkMuteSound = false
            } else {
                ivMuteSoundIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_red_mute_icon))
                mDefaultTrackSelectorCheck.setParameters(
                    mDefaultTrackSelectorCheck.buildUponParameters().setRendererDisabled(C.TRACK_TYPE_AUDIO, true)
                )
                checkMuteSound = true
            }
        }
        findViewById<ImageView>(R.id.ivShareIcon).setOnClickListener {
            val mFileData = File(playerList[position].DATA)
            val muriList = ArrayList<Uri>()
            val mContentUri: Uri = Util().getVideoFileUri(this, mFileData)
            muriList.add(mContentUri)
            Util().shareVideoMediaItem(this, muriList)
        }
    }

    private fun hideSystemUI(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController?.hide(android.view.WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun hideAndFullScreenSystemUI(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = view.windowInsetsController
            controller?.hide(android.view.WindowInsets.Type.systemBars())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            view.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
//        hideSystemUI(view)
    }

    private fun showSystemUNFullUI(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = view.windowInsetsController
            controller?.show(android.view.WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_VISIBLE)
        }
//        showSystemUI(view)
    }

    private fun showSystemUI(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController?.show(android.view.WindowInsets.Type.navigationBars() or android.view.WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun playInFullscreen(mEnable: Boolean) {
        if (mEnable) {
            _binding.dtpvPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            mExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            ivFullScreenIcon.setImageResource(R.drawable.ic_vp_fs)
        } else {
            _binding.dtpvPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            mExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            ivFullScreenIcon.setImageResource(R.drawable.ic_vp_fullscreen_icon)
        }

    }

    @Suppress("DEPRECATION")
    private fun goToPIPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            _binding.dtpvPlayerView.useController = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
            } else {
                this.enterPictureInPictureMode()
            }
            Handler().postDelayed({ checkPIPModePermission() }, 500L)
        } else {
            Toast.makeText(this, "Pip mode is not supported For This Device !!", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkPIPModePermission() {
        if (!isInPictureInPictureMode) {
            onBackPressed()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        }
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            if (!isInPictureInPictureMode) {
                try {
                    finishAndRemoveTask()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
        if (lifecycle.currentState == Lifecycle.State.STARTED) {
            if (isInPictureInPictureMode) {
                findViewById<RelativeLayout>(R.id.rlMainView).visibility = View.GONE
            } else {
                findViewById<RelativeLayout>(R.id.rlMainView).visibility = View.VISIBLE
            }
        }
        if (isCheckNewClick) {
            isCheckNewClick = false
            initVideoList()
        }
        if (isInPictureInPictureMode) {
            findViewById<RelativeLayout>(R.id.rlMainView).visibility = View.GONE
            _binding.dtpvPlayerView.useController = false
            if (mRequestAudioFocusSet()) {
                mExoPlayer.playWhenReady = true
            }
        } else {
            findViewById<RelativeLayout>(R.id.rlMainView).visibility = View.VISIBLE
            _binding.dtpvPlayerView.useController = true
            mExoPlayer.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!mExoPlayer.isPlaying && mRequestAudioFocusSet()) {
                mExoPlayer.playWhenReady = true
                ivPlayAndPauseIcon.setImageResource(R.drawable.ic_pause_img)
            }
        }, 1000L)
        mIsScreenOff = false
    }

    override fun onPause() {
        super.onPause()
        mIsScreenOff = true
        pauseVideo()
        infoDialog?.dismiss()
    }

    override fun onStop() {
        super.onStop()
        mExoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        mExoPlayer.release()

    }

    inner class BatteryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    val batteryPercentage = (level.toFloat() / scale.toFloat() * 100).toInt()
                    tvBattry.text = "$batteryPercentage%"
                }
            }
        }
    }
}