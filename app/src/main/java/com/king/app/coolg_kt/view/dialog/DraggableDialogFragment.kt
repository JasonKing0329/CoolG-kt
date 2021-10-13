package com.king.app.coolg_kt.view.dialog

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
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
     * 最大高度
     * @return
     */
    var maxHeight = ScreenUtils.getScreenHeight() * 4 / 5

    /**
     * 最小高度
     */
    var minHeight = 0

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
        mBinding.viewBg.setBackgroundColor(backgroundColor)

        if (hideClose) {
            mBinding.ivClose.visibility = View.GONE
        }
        initDragParams()
        contentFragment?.let {
            it.dialogHolder = this
            replaceContentFragment(it, "ContentView")
        }

        // 固定宽高
        if (fixedWidth > 0 || fixedHeight > 0) {
            limitFixedSize()
        }
        else {
            // 限制最大最小宽高
            mBinding.flFt.viewTreeObserver
                .addOnGlobalLayoutListener { limitMaxMinHeight() }
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
     * 当dialog固定了宽高,contentFragment也需要固定高度
     */
    private fun limitFixedSize() {
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

    /**
     * 限制最大最小高度
     */
    private fun limitMaxMinHeight() {
        val contentHeight: Int = mBinding.flFt.height
        DebugLog.e("contentHeight=$contentHeight")
        // 最大高度
        if (maxHeight in 1 until contentHeight) {
            val params: ViewGroup.LayoutParams = mBinding.flFt.layoutParams
            params.height = maxHeight
            mBinding.flFt.layoutParams = params
        }
        // 最小高度
        else if (contentHeight < minHeight) {
            val params: ViewGroup.LayoutParams = mBinding.flFt.layoutParams
            params.height = minHeight
            mBinding.flFt.layoutParams = params
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