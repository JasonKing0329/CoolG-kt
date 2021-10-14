package com.king.app.coolg_kt.view.dialog

import androidx.databinding.ViewDataBinding
import com.king.app.coolg_kt.base.BindingFragment

/**
 * Desc: xml里根布局如果使用match_parent，需要对DraggableDialogFragment设置fixedHeight
 * （不能通过bingding.root.layoutParams.height来判断是否是MATCH_PARENT，因为无论设置为match_parent还是wrap_content，
 * binding加载的根布局height都是MATCH_PARENT）
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

    /**
     * 根布局设置为match_parent时使用，子类自行覆盖
     */
    open fun fixHeightAsMaxHeight(): Boolean {
        return false
    }
}