package com.king.app.coolg_kt.page.setting

import androidx.lifecycle.Observer
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityManageBinding
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.model.http.bean.response.AppCheckBean
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 11:06
 */
class ManageActivity: BaseActivity<ActivityManageBinding, ManageViewModel>() {
    override fun getContentView(): Int = R.layout.activity_manage

    override fun createViewModel(): ManageViewModel = generateViewModel(ManageViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel;

        mBinding.groupMoveStars.setOnClickListener { v -> warningMoveStar() }
        mBinding.groupMoveRecords.setOnClickListener { v -> warningMoveRecord() }

        mBinding.groupClearImages.setOnClickListener { v ->
//            showMessageLong("Run on background...")
//            startService(Intent().setClass(this@ManageActivity, FileService::class.java))
        }
    }

    override fun initData() {

        mModel.imagesObserver.observe(this, Observer { bean -> imagesFound(bean) });

        mModel.gdbCheckObserver.observe(this, Observer { bean -> gdbFound(bean) });
        mModel.readyToDownloadObserver.observe(this, Observer { size -> downloadDatabase(size, false) });

        mModel.warningSync.observe(this, Observer { result -> warningSync() });
        mModel.warningUpload.observe(this, Observer { message -> warningUpload(message) });
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

    }

    private fun gdbFound(bean: AppCheckBean) {

    }

    private fun downloadDatabase(size: Long, isUploadedDb: Boolean) {

    }

}