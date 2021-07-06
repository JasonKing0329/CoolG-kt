package com.king.app.coolg_kt.view.dialog

import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BindingDialogFragment
import com.king.app.coolg_kt.databinding.DialogBaseBinding
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * 描述: 可拖拽移动的base dialog框架
 *
 * 作者：景阳
 *
 * 创建时间: 2017/7/20 11:45
 */
open class DraggableDialogFragment : BindingDialogFragment<DialogBaseBinding>(), DraggableHolder {
    private var startPoint: Point? = null
    private var touchPoint: Point? = null
    private var title: String? = null
    private var backgroundColor = 0
    private var hideClose = false
    var contentFragment: DraggableContentFragment<*>? = null

    /**
     * 子类可选择覆盖
     * @return
     */
    var maxHeight = 0
        get() = if (field != 0) {
            field
        } else {
            ScreenUtils.getScreenHeight() * 3 / 5
        }

    /**
     * 固定高度
     */
    var fixedWidth = 0

    /**
     * 固定高度
     */
    var fixedHeight = 0
    
    var dismissListener: DialogInterface.OnDismissListener? = null
    
    override fun getBinding(inflater: LayoutInflater): DialogBaseBinding {
        return DialogBaseBinding.inflate(inflater)
    }

    override fun initView(view: View) {
        if (title != null) {
            mBinding.tvTitle.text = title
        }

        if (backgroundColor == 0) {
            backgroundColor = resources.getColor(R.color.dlg_base_bg)
        }
        val drawable = mBinding.groupDialog.background as GradientDrawable
        drawable.setColor(backgroundColor)

        if (hideClose) {
            mBinding.ivClose.visibility = View.GONE
        }
        initDragParams()
        contentFragment?.let {
            it.dialogHolder = this
            replaceContentFragment(it, "ContentView")
        }
        limitFixedSize()
        mBinding.flFt.post {
            DebugLog.e("groupFtContent height=" + mBinding.flFt.height)
            limitMaxHeight()
        }
        mBinding.ivClose.setOnClickListener { v: View? -> dismissAllowingStateLoss() }
    }

    protected fun replaceContentFragment(
        target: DraggableContentFragment<*>?,
        tag: String?
    ) {
        if (target != null) {
            val ft =
                childFragmentManager.beginTransaction()
            ft.replace(R.id.fl_ft, target, tag)
            ft.commit()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss(dialog)
    }

    /**
     * 固定宽高
     */
    override fun onResume() {
        super.onResume()
//        if (fixedHeight > 0 || fixedWidth > 0) {
//            var dm = DisplayMetrics()
//            requireActivity().windowManager.defaultDisplay.getMetrics(dm)
//            // 按比例可以这样设置：(int) (dm.widthPixels * 0.75)
//            var height = if (fixedHeight > 0) fixedHeight
//            else ViewGroup.LayoutParams.WRAP_CONTENT
//            var width = if (fixedWidth > 0) fixedWidth
//            else ViewGroup.LayoutParams.MATCH_PARENT
//            dialog?.window?.setLayout(width, height)
//        }
    }

    /**
     * 当dialog固定了宽高,contentFragment也需要固定高度
     */
    private fun limitFixedSize() {
        if (fixedHeight > 0 || fixedWidth > 0) {
            val params = mBinding.flFt.layoutParams
            if (fixedHeight > 0) {
                params.height = fixedHeight
            }
            if (fixedWidth > 0) {
                params.width = fixedWidth
            }
            mBinding.flFt.layoutParams = params
            mBinding.flFt.invalidate()
            mBinding.flFt.requestLayout()
        }
    }

    /**
     * 限制最大高度，fixedHeight > 0时不起作用
     */
    private fun limitMaxHeight() {
        if (fixedHeight == 0) {
            val maxContentHeight = maxHeight
            if (mBinding.flFt.height > maxContentHeight) {
                val params = mBinding.flFt.layoutParams
                params.height = maxContentHeight
                mBinding.flFt.layoutParams = params
            }
        }
    }

    private fun initDragParams() {
        touchPoint = Point()
        startPoint = Point()
        mBinding.groupDialog.setOnTouchListener(DialogTouchListener())
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun setBackgroundColor(backgroundColor: Int) {
        this.backgroundColor = backgroundColor
    }

    fun setHideClose(hideClose: Boolean) {
        this.hideClose = hideClose
    }

    override fun inflateToolbar(layout: Int): View {
        val view = layoutInflater.inflate(layout, null)
        mBinding.flToolbar.addView(view)
        return view
    }

    private inner class Point {
        var x = 0f
        var y = 0f
    }

    /**
     * move dialog
     */
    private inner class DialogTouchListener : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val action = event.action
            var x = 0f
            var y = 0f
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX //
                    y = event.rawY
                    startPoint!!.x = x
                    startPoint!!.y = y
                    DebugLog.d("ACTION_DOWN x=$x, y=$y")
                }
                MotionEvent.ACTION_MOVE -> {
                    x = event.rawX
                    y = event.rawY
                    touchPoint!!.x = x
                    touchPoint!!.y = y
                    val dx = touchPoint!!.x - startPoint!!.x
                    val dy = touchPoint!!.y - startPoint!!.y
                    move(dx.toInt(), dy.toInt())
                    startPoint!!.x = x
                    startPoint!!.y = y
                }
                MotionEvent.ACTION_UP -> {
                }
                else -> {
                }
            }
            return true
        }
    }
}