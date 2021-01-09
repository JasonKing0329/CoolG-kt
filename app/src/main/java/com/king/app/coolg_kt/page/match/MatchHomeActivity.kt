package com.king.app.coolg_kt.page.match

import android.content.Context
import android.content.Intent
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.ActivityMatchHomeBinding
import com.king.app.coolg_kt.page.match.list.MatchListActivity

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:10
 */
class MatchHomeActivity: BaseActivity<ActivityMatchHomeBinding, EmptyViewModel>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, MatchHomeActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_match_home

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView() {
        mBinding.tvMatch.setOnClickListener {
            MatchListActivity.startPage(this@MatchHomeActivity)
        }
    }

    override fun initData() {

    }
}