package com.king.app.coolg_kt.base

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.utils.AppUtil
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.ProgressDialogFragment

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/21 13:05
 */
abstract class RootActivity : AppCompatActivity() {

    private var progressDialogFragment: ProgressDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        // full screen
        if (isFullScreen()) {
            setFullScreen()
        }
        // status bar
        if (updateStatusBarColor()) {
            ScreenUtils.setStatusBarColor(this, getStatusBarColor())
        }

        //prevent from task manager take screenshot
        //also prevent from system screenshot
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        super.onCreate(savedInstanceState)
    }

    private fun setFullScreen() {
        if (AppUtil.isAndroidP()) {
            var uiOptions = (
//                // 顶部状态栏
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            // 当状态栏隐藏的时候，手动调出状态栏导航栏，显示一会儿随后就会隐藏掉。设置该属性后不会清除flag
                            // SYSTEM_UI_FLAG_IMMERSIVE 会在手动调出状态栏后立马清除flag
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
            // 底部导航栏
            if (!keepNavWhenFullScreen()) {
                uiOptions = uiOptions or (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        )
            }
            window.decorView.systemUiVisibility = uiOptions
        }
        else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    /**
     * systemUiVisibility的属性集在离开/暂时离开当前页面后都会清除flag
     * 所以在onResume()里重新设置这些属性
     */
    override fun onResume() {
        super.onResume()
        if (isFullScreen()) {
            setFullScreen()
        }
    }

    /**
     * 只有PlayerActivity需要全屏，覆盖方法
     */
    open fun isFullScreen(): Boolean {
        return false
    }

    /**
     * 全屏时保留底部栏，子类自行覆盖
     */
    open fun keepNavWhenFullScreen(): Boolean {
        return false
    }

    /**
     * 仅LoginActivity不应用，单独覆写
     * @return
     */
    open fun updateStatusBarColor(): Boolean = true

    open fun getStatusBarColor():Int = resources.getColor(R.color.white)

    fun showConfirmMessage(msg: String, listener: DialogInterface.OnClickListener?) {
        AlertDialog.Builder(this)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), listener)
            .show()
    }

    fun showConfirmCancelMessage(
        msg: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(this)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), okListener)
            .setNegativeButton(getString(R.string.cancel), cancelListener)
            .show()
    }

    fun showConfirmCancelMessage(
        msg: String,
        okText: String,
        okListener: DialogInterface.OnClickListener,
        cancelText: String,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(this)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(okText, okListener)
            .setNegativeButton(cancelText, cancelListener)
            .show()
    }

    fun showNeutralMessage(
        msg: String,
        okText: String,
        okListener: DialogInterface.OnClickListener,
        neutralText: String,
        neutralListener: DialogInterface.OnClickListener,
        cancelText: String,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(this)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(okText, okListener)
            .setNeutralButton(neutralText, neutralListener)
            .setNegativeButton(cancelText, cancelListener)
            .show()
    }

    fun showYesNoMessage(
        msg: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(this)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.yes), okListener)
            .setNegativeButton(getString(R.string.no), cancelListener)
            .show()
    }

    fun showProgress(msg: String) {
        var msg = msg
        progressDialogFragment = ProgressDialogFragment()
        if (TextUtils.isEmpty(msg)) {
            msg = resources.getString(R.string.loading)
        }
        progressDialogFragment!!.setMessage(msg)
        progressDialogFragment!!.show(supportFragmentManager, "ProgressDialogFragment")
    }

    fun dismissProgress() {
        if (progressDialogFragment != null) {
            progressDialogFragment!!.dismissAllowingStateLoss()
        }
    }

    fun showMessageShort(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showMessageLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * 完全全屏，隐藏状态栏与导航栏
     */
    fun fullscreen() {
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
    }

    /**
     * 只全屏上半部分，隐藏状态栏，显示导航栏
     */
    fun fullscreenTop() {
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
            navigationBarColor(R.color.actionbar_bg_trans)
        }
    }

    /**
     * 上班部分沉浸式，显示状态栏，显示导航栏
     * @param marginTopView 需要显示在状态栏下方的view
     */
    fun immersiveTop(marginTopView: View) {
        immersionBar {
            fullScreen(true)
            titleBarMarginTop(marginTopView)
            navigationBarColor(R.color.actionbar_bg_trans)
        }
    }

    /**
     * 上班部分沉浸式，显示状态栏，显示导航栏。状态栏字体颜色为深色（appbar收起时，适配其白色的背景）
     * @param marginTopView 需要显示在状态栏下方的view
     */
    fun immersiveTopDarkFont(marginTopView: View) {
        immersionBar {
            fullScreen(true)
            titleBarMarginTop(marginTopView)
            statusBarDarkFont(true)
            navigationBarColor(R.color.actionbar_bg_trans)
        }
    }

    /**
     * ImmersiveTopXX模式下，由于设置了titleBarMarginTop，会导致滚动视图页面滑不到最底部
     * 对于RecyclerView的处理方案如下：将statusBar的高度作为最后一个item进行填充，保证数据展示完整
     */
    @Deprecated("暂时废弃，采用将导航栏背景颜色设置为透明色处理")
    fun immersiveDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val count = recyclerView.adapter?.itemCount?:0
                val position = parent.getChildLayoutPosition(view)
                if (count - 1 == position) {
                    outRect.bottom = ImmersionBar.getStatusBarHeight(this@RootActivity)
                }
                else {
                    outRect.bottom = 0
                }
            }
        })
    }

}
