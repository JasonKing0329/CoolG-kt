package com.king.app.coolg_kt.page.match.draw

import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchDrawFinalBinding

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/19 17:47
 */
class FinalDrawActivity: BaseActivity<ActivityMatchDrawFinalBinding, FinalDrawViewModel>() {

    override fun getContentView(): Int = R.layout.activity_match_draw_final

    override fun createViewModel(): FinalDrawViewModel = generateViewModel(FinalDrawViewModel::class.java)

    override fun initView() {
        val manager = GridLayoutManager(this, 2)
        mBinding.rvList.layoutManager = manager
    }

    override fun initData() {

    }
}