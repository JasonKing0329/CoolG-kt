package com.king.app.coolg_kt.page.tv

import android.view.View
import androidx.lifecycle.Observer
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.ActivityTvBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.page.match.rank.RankDialog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 15:06
 */
class TvActivity: BaseActivity<ActivityTvBinding, TvViewModel>() {

    var ftContent: ContentFragment? = null

    override fun getContentView(): Int = R.layout.activity_tv

    override fun createViewModel(): TvViewModel = generateViewModel(TvViewModel::class.java)

    override fun initView() {
        mBinding.tvSu.visibility = View.GONE
        mBinding.ivSetting.setOnClickListener {
            ftContent = null
            showServerPage()
        }
        mBinding.ivChangeBg.setOnClickListener {
            mModel.getBg()
        }
        mBinding.ivSu.setOnClickListener {
            SimpleDialogs()
                .openPasswordDialog(this, "Code", SimpleDialogs.OnDialogActionListener {
                    mModel.checkUserCode(it)
                    mBinding.tvSu.visibility = if (mModel.isSuperUser) View.VISIBLE else View.GONE
                    onUserChanged()
                })
        }

        showServerPage()
    }

    private fun showServerPage() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, ServerFragment(), "ServerFragment")
            .commit()
    }

    private fun onUserChanged() {
        ftContent?.let {
            if (it.isVisible) {
                it.onUserChanged()
            }
        }
    }

    override fun initData() {
        mModel.goToServer.observe(this, Observer {
            ftContent = ContentFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_ft, ftContent!!, "ContentFragment")
                .commit()
        })
        mModel.bgObserver.observe(this, Observer { showServerBg(it) })
        mModel.bgFilePath.observe(this, Observer {
            updateBg(it)
        })
        mModel.localBg()
    }

    private fun updateBg(url: String) {
        GlideApp.with(this@TvActivity)
            .load(url)
            .error(R.drawable.ic_tv_bg)
            .into(mBinding.ivBg)
    }

    private fun showServerBg(list: List<String>) {
        val content = BgSelector()
        content.list = list
        content.onSelectBgListener = object : BgSelector.OnSelectBgListener {
            override fun onSelectBg(url: String) {
                mModel.downloadBg(url)
                updateBg(url)
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Change Background")
        dialogFragment.fixedWidth = ScreenUtils.getScreenWidth() * 4 / 5
        dialogFragment.fixedHeight = ScreenUtils.getScreenHeight() *2 / 3
        dialogFragment.show(supportFragmentManager, "BgSelector")
    }
}