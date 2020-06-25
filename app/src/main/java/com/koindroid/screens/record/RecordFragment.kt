package com.koindroid.screens.record

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.core.base.BaseFragment
import com.koindroid.BR
import com.koindroid.R
import com.koindroid.databinding.FragmentRecordBinding
import org.koin.android.ext.android.inject

/**
 * @author Abdullah Ayman on 25/06/2020
 */
class RecordFragment : BaseFragment<FragmentRecordBinding, RecordViewModel>() {
    val mViewModel: RecordViewModel by inject()
    lateinit var mViewBinding: FragmentRecordBinding
    override fun getLayoutId(): Int {
        return R.layout.fragment_record
    }

    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getViewModel(): RecordViewModel {
        return mViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding = getViewDataBinding()
        mViewModel.mLandingCommand.observe(this, Observer {
            it.getContentIfNotHandled().let {
                when (it) {
                    is RecordCommand.OpenDialog -> {
                    }
                    is RecordCommand.Record -> {
                    }
                    is RecordCommand.Pause -> {
                    }
                    is RecordCommand.ShowErrorMessage -> {
                        Toast.makeText(context, it.test, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}