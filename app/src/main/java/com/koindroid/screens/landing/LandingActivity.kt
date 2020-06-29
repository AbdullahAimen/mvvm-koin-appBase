package com.koindroid.screens.landing

import android.Manifest
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.lifecycle.Observer
import com.core.base.BaseActivity
import com.core.model.RecordInfo
import com.core.services.player.PlayerRemote
import com.core.services.player.PlayerServiceCommand
import com.core.utilities.AppConstants
import com.koindroid.BR
import com.koindroid.R
import com.koindroid.databinding.ActivityLandingBinding
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * @author Abdullah Ayman on 23/06/2020
 */
class LandingActivity : BaseActivity<ActivityLandingBinding, LandingViewModel>() {

    val mViewModel: LandingViewModel by inject()
    lateinit var mViewBinding: ActivityLandingBinding
    override val viewModel: LandingViewModel
        get() = mViewModel
    override val bindingVariable: Int
        get() = BR.viewModel
    override val layoutId: Int
        get() = R.layout.activity_landing


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = viewDataBinding!!

        mViewModel.mLandingCommand.observe(this, Observer {
            it.peekContent().let { it ->
                when (it) {
                    is LandingCommand.SessionExpired -> {
                        //TODO session expired
                    }
                    is LandingCommand.AssignRecordInfo -> {
                        Toast.makeText(this, it.record.recordName, Toast.LENGTH_LONG).show()

                    }
                    is LandingCommand.ShowErrorMessage -> {
                        Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })


        mViewBinding.button.setOnClickListener {
            //open dialog ()
        }
        mViewBinding.landingImgPlay.setOnClickListener {
            if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissionsSafely(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), REQUEST_CODE_PERMISSION_WRITE
                )
            } else {
                if (!PlayerRemote.isPlaying && !PlayerRemote.currentRecord.isPlaying)
                    playRecord()
                else
                    PlayerRemote.pauseOrResume()
            }
        }
        mViewBinding.landingSeekBarVolume.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                PlayerRemote.updateVolume(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        mViewBinding.landingSeekBarProgress.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                PlayerRemote.seekTo(seekBar.progress)
                Timber.d("progress: %s", seekBar.progress)
            }
        })
    }

    override fun onServiceConnected() {
        PlayerRemote.getPlayerServiceCommand()!!.observe(this, Observer {
            it.getContentIfNotHandled()?.let { it ->
                when (it) {
                    is PlayerServiceCommand.StopWatchUpdate -> {
                        mViewBinding.landingTvProgressTime.text =
                            String.format(
                                "%02d:%02d",
                                AppConstants.getMinutes(it.elapsedTime.toInt()),
                                AppConstants.getSeconds(it.elapsedTime.toInt())
                            )
                    }
                    is PlayerServiceCommand.RecordReady -> {
                        mViewBinding.landingTvRecordTitle.text =
                            PlayerRemote.currentRecord.recordName
                        mViewBinding.landingSeekBarVolume.progress =
                            PlayerRemote.getVolume().toInt()
                        mViewBinding.landingSeekBarVolume.max = PlayerRemote.getMaxVolume().toInt()
                        mViewBinding.landingSeekBarProgress.progress = 0
                        mViewBinding.landingSeekBarProgress.max = PlayerRemote.recordDuration
                        mViewBinding.landingImgPlay.setImageResource(R.drawable.ic_btn_pause)
                        mViewBinding.landingTvDurationTime.text =
                            String.format(
                                "%02d:%02d",
                                AppConstants.getMinutes(PlayerRemote.recordDuration),
                                AppConstants.getSeconds(PlayerRemote.recordDuration)

                            )
                        PlayerRemote.currentRecord.isPlaying = true
                    }
                    is PlayerServiceCommand.RecordResumed -> {
                        mViewBinding.landingImgPlay.setImageResource(R.drawable.ic_btn_pause)
                    }
                    is PlayerServiceCommand.RecordStop -> {
                        resetPlayer()
                    }
                    is PlayerServiceCommand.RecordPaused -> {
                        mViewBinding.landingImgPlay.setImageResource(R.drawable.ic_btn_play_active)
                    }
                    is PlayerServiceCommand.RecordProgressUpdate -> {
                        mViewBinding.landingSeekBarProgress.progress = it.position
                        Timber.d("%s", it.position)
                    }
                    is PlayerServiceCommand.UpdateVolume -> {
                        mViewBinding.landingSeekBarVolume.progress =
                            PlayerRemote.getVolume().toInt()
                    }

                }
            }
        })
    }

    private fun resetPlayer() {
        PlayerRemote.currentRecord.isPlaying = false
        mViewBinding.landingSeekBarProgress.progress = 0
        mViewBinding.landingImgPlay.setImageResource(R.drawable.ic_btn_play_active)
        mViewBinding.landingTvProgressTime.text = String.format("%02d:%02d", 0, 0)
    }

    override fun onServiceDisconnected() {
        PlayerRemote.getPlayerServiceCommand()!!.removeObservers(this)
    }

    private fun playRecord() {
        val record = RecordInfo(
            1,
            "/storage/emulated/0/Recorder/REC_Jun-24-2020_11-00-32.mp4",
            "REC_Jun-24-2020_11-00-32.mp4",
            "3211",
            "1",
            "20"
        )
        PlayerRemote.prepareRecord(record)
    }
}