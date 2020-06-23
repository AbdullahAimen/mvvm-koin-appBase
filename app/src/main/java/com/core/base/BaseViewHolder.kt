package com.core.base

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Abdullah Ayman on 23/06/2020
 */
abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBind(position: Int)

    //you can add what-ever custom bind view and override inside ur adapter
    open fun onBind(mContext: Context?, position: Int) {
    }
}