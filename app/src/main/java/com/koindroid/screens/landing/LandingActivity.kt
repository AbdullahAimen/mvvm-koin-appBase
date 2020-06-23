package com.koindroid.screens.landing

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.core.base.BaseActivity
import com.koindroid.BR
import com.koindroid.R
import com.koindroid.databinding.ActivityLandingBinding
import org.koin.android.ext.android.inject

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
            it.peekContent()?.let { it ->
                when (it) {
                    is LandingCommand.SessionExpired -> {
                        //TODO session expired
                    }
                    is LandingCommand.AssignRecordInfo -> {
                        Toast.makeText(this, it.record.recordName, Toast.LENGTH_LONG).show()

                    }
                }
            }
        })
    }
}