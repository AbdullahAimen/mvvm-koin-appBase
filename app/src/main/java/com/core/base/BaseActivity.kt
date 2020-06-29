package com.core.base

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.core.services.player.callBack.IServiceConnection
import com.core.services.player.PlayerRemote
import timber.log.Timber

/**
 * @author Abdullah Ayman on 23/06/2020
 */
/**
 * this is the basic activity for the project which will be extended from all new created activities
 * and contains the most common and needed functions for injecting views and parameters
 */
abstract class BaseActivity<T : ViewDataBinding, V : BaseViewModel> : AppCompatActivity(),
    IServiceConnection {

    private val TAG: String = "BaseActivity"
    var viewDataBinding: T? = null
        private set
    private var mViewModel: V? = null

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract val viewModel: V

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    abstract val bindingVariable: Int

    /**
     * @return layout resource id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    private var serviceToken: PlayerRemote.ServiceToken? = null
    var serviceConnected: Boolean = false

    fun finishApp() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
        performBindToPlayerService()
    }

    /**
     * function for doing PlayerService Binding
     */
    private fun performBindToPlayerService() {
        serviceToken = PlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                onServiceConnected()
                serviceConnected = true
                Timber.d(String.format("%s: %s %s", TAG, "onServiceConnected:", name.className))
                Timber.d(
                    String.format(
                        "%s: %s %b",
                        TAG,
                        "onServiceConnected: bind to service",
                        service.isBinderAlive
                    )
                )
            }

            override fun onServiceDisconnected(name: ComponentName) {
                onServiceDisconnected()
                serviceConnected = false
                Timber.d(
                    String.format(
                        "%s: %s %s",
                        TAG,
                        "onServiceDisconnected: unbind to service",
                        name.className
                    )
                )
            }
        })
    }

    /**
     * function for doing data Binding between XML and view
     */
    private fun performDataBinding() {
        this.viewDataBinding = DataBindingUtil.setContentView(this, layoutId)
        this.mViewModel = if (mViewModel == null) viewModel else mViewModel
        this.viewDataBinding!!.setVariable(bindingVariable, mViewModel)
        this.viewDataBinding!!.executePendingBindings()
    }


    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissionsSafely(permissions: Array<String>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun hasPermission(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    fun displayFragment(fragment: BaseFragment<*, *>, containerViewId: Int) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            containerViewId,
            fragment
        )
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onDestroy() {
        PlayerRemote.unbindFromService(serviceToken)
        super.onDestroy()
    }

    companion object {
        val REQUEST_CODE_PERMISSION_WRITE = 30
    }
}