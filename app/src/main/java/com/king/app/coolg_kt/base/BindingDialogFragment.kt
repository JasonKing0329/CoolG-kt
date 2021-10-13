package com.king.app.coolg_kt.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * 描述:DialogFragment基类
 *
 * <br></br>创建时间: 2020/1/21
 */
abstract class BindingDialogFragment<T : ViewDataBinding> : DialogFragment() {

    protected lateinit var mBinding: T

    private var windowParams: WindowManager.LayoutParams? = null

    /**
     * 是否可以点击空白处取消
     *
     * @return
     */
    val cancelable: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = cancelable
        setStyle(STYLE_NORMAL, R.style.BaseDialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = super.onCreateDialog(savedInstanceState)

        windowParams = dialog.window?.attributes
        windowParams?.apply {
            width = ScreenUtils.getScreenWidth() - ScreenUtils.dp2px(16f) * 2
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = getBinding(inflater)
        val view = mBinding.root
        initView(view)
        return mBinding.root
    }

    protected abstract fun getBinding(inflater: LayoutInflater): T

    protected abstract fun initView(view: View)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun setWidth(w: Int) {
        windowParams?.apply {
            width = w
            dialog?.window?.attributes = this
        }
    }

    fun setHeight(h: Int) {
        windowParams?.apply {
            height = h
            dialog?.window?.attributes = this
        }
    }

    /**
     * 设置dialog的偏移位置
     *
     * @param x 负数向左，正数向右
     * @param y 负数向上，正数向下
     */
    fun setPositionOffset(x: Int, y: Int) {
        windowParams?.apply {
            this.x = x
            this.y = y
            dialog?.window?.attributes = this
        }
    }

    /**
     * album_selector dialog
     *
     * @param x
     * @param y
     */
    protected fun move(x: Int, y: Int) {

        windowParams?.apply {
            this.x += x
            this.y += y
            dialog?.window?.attributes = this
        }
    }

    fun<VM: BaseViewModel> generateViewModel(vm: Class<VM>) = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(vm)

}
