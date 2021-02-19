package com.king.app.coolg_kt.view.dialog

import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BindingDialogFragment
import com.king.app.coolg_kt.databinding.DialogBaseBinding
import com.king.app.coolg_kt.databinding.DialogBaseTvBinding
import com.king.app.coolg_kt.utils.ColorUtil
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * 描述: 可拖拽移动的base dialog框架
 *
 * 作者：景阳
 *
 * 创建时间: 2017/7/20 11:45
 */
open class TvDialogFragment : BindingDialogFragment<DialogBaseTvBinding>(), TvDialogHolder {

    var title: String = ""
    var backgroundColor = 0
    var contentFragment: TvDialogContentFragment<*>? = null
    var showConfirm = true
    var showCancel = true

    /**
     * 固定高度
     */
    var fixedWidth = 0

    /**
     * 固定高度
     */
    var fixedHeight = 0
    
    var dismissListener: DialogInterface.OnDismissListener? = null
    
    override fun getBinding(inflater: LayoutInflater): DialogBaseTvBinding {
        return DialogBaseTvBinding.inflate(inflater)
    }

    override fun initView(view: View) {

        mBinding.tvTitle.text = title

        if (!showConfirm) {
            mBinding.tvOk.visibility = View.GONE
            mBinding.tvCancel.visibility = View.GONE
        }
        if (!showCancel) {
            mBinding.tvCancel.visibility = View.GONE
        }

        if (backgroundColor != 0) {
            val drawable = mBinding.groupDialog.background as GradientDrawable
            drawable.setColor(backgroundColor)
        }
        contentFragment?.let {
            it.dialogHolder = this
            replaceContentFragment(it, "ContentView")
        }
        mBinding.ivClose.setOnClickListener { dismissAllowingStateLoss() }
        mBinding.tvCancel.setOnClickListener {
            contentFragment?.let {
                if (!it.onCancel()) {
                    dismissAllowingStateLoss()
                }
            }
        }
        mBinding.tvOk.setOnClickListener {
            contentFragment?.let {
                if (!it.onConfirm()) {
                    dismissAllowingStateLoss()
                }
            }
        }
    }

    private fun replaceContentFragment(target: TvDialogContentFragment<*>, tag: String) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, target, tag)
            .commit()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss(dialog)
    }

    fun setSize(width: Int, height: Int): TvDialogFragment {
        fixedWidth = width
        fixedHeight = height
        return this
    }

    /**
     * 固定宽高
     */
    override fun onResume() {
        super.onResume()
        if (fixedHeight > 0 || fixedWidth > 0) {
            var dm = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(dm)
            // 按比例可以这样设置：(int) (dm.widthPixels * 0.75)
            var height = if (fixedHeight > 0) fixedHeight
            else ViewGroup.LayoutParams.WRAP_CONTENT
            var width = if (fixedWidth > 0) fixedWidth
            else ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.window?.setLayout(width, height)
            mBinding.flFt.invalidate()
            mBinding.flFt.requestLayout()
        }
    }
}