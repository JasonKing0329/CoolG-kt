package com.king.app.coolg_kt.page.match

import android.content.Context
import android.content.Intent
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchHomeBinding
import com.king.app.coolg_kt.page.match.h2h.H2hActivity
import com.king.app.coolg_kt.page.match.titles.FinalListActivity
import com.king.app.coolg_kt.page.match.list.MatchListActivity
import com.king.app.coolg_kt.page.match.rank.RankActivity
import com.king.app.coolg_kt.page.match.season.SeasonActivity

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:10
 */
class MatchHomeActivity: BaseActivity<ActivityMatchHomeBinding, MatchHomeViewModel>() {

    companion object {
        val EXTRA_COVER_PATH = "cover_path"
        fun startPage(context: Context) {
            var intent = Intent(context, MatchHomeActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSetCover(context: Context, path: String) {
            var intent = Intent(context, MatchHomeActivity::class.java)
            intent.putExtra(EXTRA_COVER_PATH, path)
            context.startActivity(intent)
        }
    }

    override fun isFullScreen(): Boolean = true

    override fun getContentView(): Int = R.layout.activity_match_home

    override fun createViewModel(): MatchHomeViewModel = generateViewModel(MatchHomeViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

        mBinding.ivBack.setOnClickListener { onBackPressed() }

        mBinding.tvMatch.setOnClickListener {
            if (setCoverPath == null) {
                MatchListActivity.startPage(this@MatchHomeActivity)
            }
            else {
                mModel.updateMatchUrl(setCoverPath!!)
            }
        }
        mBinding.tvSeason.setOnClickListener {
            if (setCoverPath == null) {
                SeasonActivity.startPage(this@MatchHomeActivity)
            }
            else {
                mModel.updateSeasonUrl(setCoverPath!!)
            }
        }
        mBinding.tvRank.setOnClickListener {
            if (setCoverPath == null) {
                RankActivity.startPage(this@MatchHomeActivity)
            }
            else {
                mModel.updateRankUrl(setCoverPath!!)
            }
        }
        mBinding.tvH2h.setOnClickListener {
            if (setCoverPath == null) {
                H2hActivity.startPage(this@MatchHomeActivity)
            }
            else {
                mModel.updateH2hUrl(setCoverPath!!)
            }
        }
        mBinding.tvFinal.setOnClickListener {
            if (setCoverPath == null) {
                FinalListActivity.startPage(this@MatchHomeActivity)
            }
            else {
                mModel.updateFinalUrl(setCoverPath!!)
            }
        }
    }

    override fun initData() {

    }

    private val setCoverPath: String?
        get() = intent.getStringExtra(EXTRA_COVER_PATH)

}