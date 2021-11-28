package com.king.app.coolg_kt.page.match.studio

import android.content.Context
import android.content.Intent
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.ActivityMatchStudiosBinding
import com.king.app.coolg_kt.page.studio.StudioHolder
import com.king.app.coolg_kt.page.studio.StudioListFragment
import com.king.app.jactionbar.JActionbar

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/28 15:46
 */
class MatchStudioActivity: BaseActivity<ActivityMatchStudiosBinding, EmptyViewModel>(), StudioHolder {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, MatchStudioActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var ftList: StudioListFragment? = null

    private var ftDetail: StudioDetailFragment? = null

    override fun getContentView(): Int = R.layout.activity_match_studios

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }

        ftList = StudioListFragment.newInstance(false, false)
        ftList!!.holder = this
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, ftList!!, "StudioListFragment")
            .commit()
    }

    override fun initData() {

    }

    override fun getJActionBar(): JActionbar {
        return mBinding.actionbar
    }

    override fun showStudioPage(studioId: Long, name: String?) {
        mBinding.actionbar.setTitle(name)
        setActionBarList(false)
        ftDetail = StudioDetailFragment.newInstance(studioId)
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_ft, ftDetail!!, "StudioDetailFragment")
            .addToBackStack(null)
            .commit()
    }

    private fun setActionBarList(isList: Boolean) {
        mBinding.actionbar.updateMenuItemVisible(R.id.menu_sort, isList)
        mBinding.actionbar.updateMenuItemVisible(R.id.menu_add, isList)
        mBinding.actionbar.updateMenuItemVisible(R.id.menu_delete, isList)
        mBinding.actionbar.updateMenuItemVisible(R.id.menu_mode, isList)
    }

    override fun backToList() {
        onBackPressed()
    }

    override fun onBackPressed() {
        setActionBarList(true)
        super.onBackPressed()
    }

    override fun sendSelectedOrderResult(id: Long?) {

    }
}