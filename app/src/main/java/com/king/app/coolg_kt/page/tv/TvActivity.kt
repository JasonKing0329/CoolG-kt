package com.king.app.coolg_kt.page.tv

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.king.app.coolg_kt.BuildConfig
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityTvBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.page.download.OnDownloadListener
import com.king.app.coolg_kt.page.tv.player.SystemPlayerActivity
import com.king.app.coolg_kt.page.tv.popup.BgSelector
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.coolg_kt.view.dialog.TvDialogFragment
import java.io.File

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 15:06
 */
class TvActivity: BaseActivity<ActivityTvBinding, TvViewModel>() {

    var ftContent: ContentFragment? = null

    override fun getContentView(): Int = R.layout.activity_tv

    override fun createViewModel(): TvViewModel = generateViewModel(TvViewModel::class.java)

    override fun isFullScreen(): Boolean {
        return "tv" != BuildConfig.DEVICE_TYPE
    }

    override fun initView() {
        mBinding.tvSu.visibility = View.GONE
        mBinding.ivSocket.visibility = View.GONE
        mBinding.ivHome.setOnClickListener {
            ftContent = null
            showServerPage()
        }
        mBinding.ivUpload.setOnClickListener {
            mModel.uploadLog()
        }
        mBinding.ivChangeBg.setOnClickListener {
//            SystemPlayerActivity.startPage(this, "http://192.168.26.57:8080/JJGalleryServer/videos/d_scene/1.mp4", "")
            mModel.getBg()
        }
        mBinding.ivSu.setOnClickListener {
            SimpleDialogs()
                .openPasswordDialog(this, "Code", SimpleDialogs.OnDialogActionListener {
                    mModel.checkUserCode(it)
                    mBinding.tvSu.visibility = if (mModel.isSuperUser) View.VISIBLE else View.GONE
                    mBinding.ivSocket.visibility = if (mModel.isSuperUser) View.VISIBLE else View.GONE
                    onUserChanged()
                })
        }
        mBinding.ivSocket.setOnClickListener {
            SystemPlayerActivity.startPageAsServer(this)
        }

        showScreenInfo()

        showServerPage()
    }

    private fun showScreenInfo() {
        val small = resources.configuration.smallestScreenWidthDp
        DebugLog.e("screenWidth=" + ScreenUtils.getScreenWidth() + ", screenHeight=" + ScreenUtils.getScreenHeight())
        mBinding.tvScreen.text = "sw${small}dp"
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
        mModel.newVersionFound.observe(this, Observer {
            showConfirmCancelMessage(it,
                DialogInterface.OnClickListener { dialog, which -> downloadApp(mModel.getDownloadRequest()) },
                null)
        })
        mModel.localBg()
        mModel.checkAppUpdate()
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
        val dialogFragment = TvDialogFragment()
        dialogFragment.showConfirm = false
        dialogFragment.contentFragment = content
        dialogFragment.title = "更换背景图片"
        dialogFragment.setSize(ScreenUtils.getScreenWidth() * 4 / 5, ScreenUtils.getScreenHeight() * 2 / 3)
        dialogFragment.show(supportFragmentManager, "BgSelector")
    }

    private fun downloadApp(bean: DownloadDialogBean) {
        val content = DownloadFragmentTv()
        content.downloadDialogBean = bean
        content.onDownloadListener = object : OnDownloadListener {
            override fun onDownloadFinish(item: DownloadItem) {
                installApp(item.path)
            }

            override fun onDownloadFinish() {

            }
        }
        val dialogFragment = TvDialogFragment()
        dialogFragment.showConfirm = false
        dialogFragment.contentFragment = content
        dialogFragment.title = "Download"
        dialogFragment.setSize(ScreenUtils.getScreenWidth() / 2, ScreenUtils.getScreenHeight() * 2 / 3)
        dialogFragment.show(supportFragmentManager, "DownloadFragmentTv")
    }

    private fun installApp(path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri: Uri = FileProvider.getUriForFile(
                this,
                 "${BuildConfig.APPLICATION_ID}.fileProvider",
                File(path)
            )
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(
                Uri.fromFile(File(path)),
                "application/vnd.android.package-archive"
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        // 经实测，小米电视中，必须结束当前应用才能安装升级包
        finish()
    }
}