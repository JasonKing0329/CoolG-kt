package com.king.app.coolg_kt.page.studio.phone

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityRecordStudioBinding
import com.king.app.coolg_kt.page.studio.StudioHolder
import com.king.app.coolg_kt.page.studio.StudioListFragment
import com.king.app.jactionbar.JActionbar

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/25 17:37
 */
class StudioActivity: BaseActivity<ActivityRecordStudioBinding, EmptyViewModel>(), StudioHolder {

    companion object {
        val EXTRA_SELECT_MODE = "select_mode"
        fun startPage(context: Context) {
            var intent = Intent(context, StudioActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSelect(activity: Activity, requestCode: Int) {
            var intent = Intent(activity, StudioActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    private var ftList: StudioListFragment? = null

    private var ftPage: StudioPageFragment? = null

    override fun getContentView(): Int = R.layout.activity_record_studio

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView() {

        mBinding.actionbar.setOnBackListener { onBackPressed() }

        ftList = StudioListFragment.newInstance(intent.getBooleanExtra(EXTRA_SELECT_MODE, false))
        ftList!!.holder = this
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, ftList!!, "StudioListFragment")
            .commit()
    }

    override fun initData() {

    }

    override fun getJActionBar(): JActionbar = mBinding.actionbar

    override fun showStudioPage(studioId: Long, name: String?) {
        mBinding.actionbar.setTitle(name)
        setActionBarList(false)
        ftPage = StudioPageFragment.newInstance(studioId)
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_ft, ftPage!!, "StudioPageFragment")
            .addToBackStack(null)
            .commit()
    }

    private fun setActionBarList(isList: Boolean) {
        mBinding.actionbar.updateMenuItemVisible(R.id.menu_sort, isList)
        mBinding.actionbar.updateMenuItemVisible(R.id.menu_mode, isList)
    }

    override fun backToList() {
        onBackPressed()
    }

    override fun onBackPressed() {
        setActionBarList(true)
        super.onBackPressed()
    }

    override fun sendSelectedOrderResult(orderId: Long?) {
        val intent = Intent()
        intent.putExtra(AppConstants.RESP_ORDER_ID, orderId)
        setResult(RESULT_OK, intent)
        finish()
    }
}