package com.king.app.coolg_kt.view.dialog

import android.view.View

/**
 * @desc
 * @auth 景阳
 * @time 2018/3/24 0024 23:18
 */
interface DraggableHolder {
    fun dismiss()
    fun dismissAllowingStateLoss()
    fun inflateToolbar(layout: Int): View
}