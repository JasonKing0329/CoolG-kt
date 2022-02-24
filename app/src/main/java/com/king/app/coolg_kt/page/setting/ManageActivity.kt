package com.king.app.coolg_kt.page.setting

import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.Observer
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.conf.AppConfig
import com.king.app.coolg_kt.databinding.ActivityManageBinding
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.model.http.bean.response.AppCheckBean
import com.king.app.coolg_kt.page.download.DownloadFragment
import com.king.app.coolg_kt.page.download.OnDownloadListener
import com.king.app.coolg_kt.service.FileService
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.ProgressDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 11:06
 */
class ManageActivity: BaseActivity<ActivityManageBinding, ManageViewModel>() {

    private var detailProgress = ProgressDialogFragment()

    override fun getContentView(): Int = R.layout.activity_manage

    override fun createViewModel(): ManageViewModel = generateViewModel(ManageViewModel::class.java)

    override fun getStatusBarColor(): Int {
        return resources.getColor(R.color.white_dim)
    }

    override fun initView() {
        mBinding.model = mModel;

        mBinding.groupMoveStars.setOnClickListener { v -> warningMoveStar() }
        mBinding.groupMoveRecords.setOnClickListener { v -> warningMoveRecord() }

        mBinding.groupClearImages.setOnClickListener { v ->
            showMessageLong("Run on background...")
            startService(Intent().setClass(this@ManageActivity, FileService::class.java))
        }

        mBinding.tvZip.setOnClickListener {
            showConfirmCancelMessage("压缩过程可能需要几分钟，确定开始？",
                { dialog, which -> mModel.zipImages() },
                null)
        }
        mBinding.tvUnzip.setOnClickListener {
            showConfirmCancelMessage("确定解压到${AppConfig.GDB_IMG}？",
                { dialog, which -> mModel.unzipImages() },
                null)
        }
    }

    override fun initData() {

        mModel.imagesObserver.observe(this, Observer { bean -> imagesFound(bean) })

        mModel.gdbCheckObserver.observe(this, Observer { bean -> gdbFound(bean) })
        mModel.readyToDownloadObserver.observe(this, Observer { size -> downloadDatabase(size, false) })

        mModel.warningSync.observe(this, Observer { result -> warningSync() })
        mModel.warningUpload.observe(this, Observer { message -> warningUpload(message) })
        mModel.zipProgress.observe(this, {
            when(it.progress) {
                0 -> {
                    if (!detailProgress.isVisible) {
                        detailProgress.setMessage(it.message)
                        detailProgress.showAsNumProgress(0, supportFragmentManager, "DetailProgress")
                    }
                }
                else -> {
                    detailProgress.setProgress(it.progress)
                    detailProgress.updateMessage(it.message)
                }
            }

        })
        mModel.zipComplete.observe(this, {
            detailProgress.setProgress(100)
            detailProgress.dismissAllowingStateLoss()
            showMessageShort("success")
        })
        mModel.deleteZipSources.observe(this, {
            showConfirmCancelMessage(
                "是否删除zip文件？",
                { dialog, which -> mModel.deleteUnZipFiles() },
                null
                )
        })
    }

    private fun warningMoveStar() {
        AlertDialogFragment()
            .setMessage("Are you sure to move all files under /star ?")
            .setPositiveText(getString(R.string.ok))
            .setPositiveListener { dialogInterface, i -> mModel.moveStar() }
            .setNegativeText(getString(R.string.cancel))
            .show(supportFragmentManager, "AlertDialogFragmentV4")
    }

    private fun warningMoveRecord() {
        AlertDialogFragment()
            .setMessage("Are you sure to move all files under /record ?")
            .setPositiveText(getString(R.string.ok))
            .setPositiveListener { dialogInterface, i -> mModel.moveRecord() }
            .setNegativeText(getString(R.string.cancel))
            .show(supportFragmentManager, "AlertDialogFragmentV4")
    }

    private fun warningSync() {
        AlertDialogFragment()
            .setMessage("Synchronization will replace database. Please make sure you have uploaded changed data")
            .setPositiveText(getString(R.string.ok))
            .setPositiveListener { dialogInterface, i -> downloadDatabase(0, true) }
            .setNegativeText(getString(R.string.cancel))
            .show(supportFragmentManager, "AlertDialogFragmentV4")
    }

    private fun warningUpload(message: String) {
        AlertDialogFragment()
            .setMessage(message)
            .setPositiveText(getString(R.string.ok))
            .setPositiveListener { dialogInterface, i -> mModel.uploadDatabase() }
            .setNegativeText(getString(R.string.cancel))
            .show(supportFragmentManager, "AlertDialogFragmentV4")
    }

    private fun imagesFound(bean: DownloadDialogBean) {
        val content = DownloadFragment()
        content.downloadDialogBean = bean
        content.onDownloadListener = object : OnDownloadListener {
            override fun onDownloadFinish(item: DownloadItem) {

            }

            override fun onDownloadFinish() {
                showMessageLong(getString(R.string.gdb_download_done))
            }
        }
        val fragment = DraggableDialogFragment()
        fragment.contentFragment = content
        fragment.setTitle("Download")
        fragment.show(supportFragmentManager, "DownloadFragment")
    }

    private fun gdbFound(bean: AppCheckBean) {
        val msg = String.format(
            getString(R.string.gdb_update_found),
            bean.gdbDabaseVersion
        )
        SimpleDialogs().showWarningActionDialog(this, msg
            , resources.getString(R.string.yes), { dialog, which ->
                if (which === DialogInterface.BUTTON_POSITIVE) {
                    mModel.prepareUpgrade(bean)
                }
            }
            , resources.getString(R.string.no), null
            , null, null)
    }

    private fun downloadDatabase(size: Long, isUploadedDb: Boolean) {
        val content = DownloadFragment()
        content.downloadDialogBean = mModel.getDownloadDatabaseBean(size, isUploadedDb)
        content.onDownloadListener = object : OnDownloadListener {
            override fun onDownloadFinish(item: DownloadItem) {
                mModel.databaseDownloaded(isUploadedDb)
            }

            override fun onDownloadFinish() {

            }
        }
        val fragment = DraggableDialogFragment()
        fragment.contentFragment = content
        fragment.setTitle("Download")
        fragment.show(supportFragmentManager, "DownloadFragment")
    }

}