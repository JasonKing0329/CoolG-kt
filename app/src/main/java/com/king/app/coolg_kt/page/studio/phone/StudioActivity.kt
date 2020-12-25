package com.king.app.coolg_kt.page.studio.phone

import android.content.Context
import android.content.Intent
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.EmptyViewModel
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
    }

    private var ftList: StudioListFragment? = null

    override fun getContentView(): Int = R.layout.activity_record_studio

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView() {
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

    }

    override fun backToList() {

    }

    override fun sendSelectedOrderResult(id: Long?) {

    }
}