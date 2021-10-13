package com.king.app.coolg_kt.view.dialog

import androidx.databinding.ViewDataBinding
import com.king.app.coolg_kt.base.BindingFragment

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/6/7 10:05
 */
abstract class DraggableContentFragment<T : ViewDataBinding> : BindingFragment<T>() {

    var dialogHolder: DraggableHolder? = null

    protected fun dismiss() {
        dialogHolder?.dismiss()
    }

    protected fun dismissAllowingStateLoss() {
        dialogHolder?.dismissAllowingStateLoss()
    }
}