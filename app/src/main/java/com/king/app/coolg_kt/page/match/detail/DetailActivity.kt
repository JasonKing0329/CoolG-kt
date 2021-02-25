package com.king.app.coolg_kt.page.match.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchRecordDetailBinding
import com.king.app.coolg_kt.page.match.rank.RankDialog
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 9:36
 */
class DetailActivity: BaseActivity<ActivityMatchRecordDetailBinding, DetailViewModel>() {

    val MATCH_RECORD_DETAIL_TAB = arrayOf(
        "Basic", "Score", "Champions", "Grand Slam", "GM1000"
    )
    lateinit var pagerAdapter: DetailPagerAdapter

    companion object {
        val EXTRA_RECORD_ID = "record_id"
        fun startRecordPage(context: Context, recordId: Long) {
            var intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            context.startActivity(intent)
        }
    }

    override fun isFullScreen(): Boolean = true

    override fun getContentView(): Int = R.layout.activity_match_record_detail

    override fun createViewModel(): DetailViewModel = generateViewModel(DetailViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.actionbar.setOnBackListener { onBackPressed() }

        mBinding.ivHead.setOnClickListener {
            RecordActivity.startPage(this, getRecordId())
        }
        mBinding.tvRank.setOnClickListener {
            showRankDialog(getRecordId())
        }
    }

    override fun initData() {
        mModel.showRankDialog.observe(this, Observer { showRankDialog(getRecordId()) })
        mModel.loadRecord(getRecordId())
        initTabs()
    }

    private fun initTabs() {

        val list = mutableListOf<AbsDetailChildFragment<*, *>>()
        list.add(BasicFragment())
        list.add(ScoreFragment())
        list.add(ChampionFragment())
        list.add(GsFragment())
        list.add(Gm1000Fragment())
        pagerAdapter = DetailPagerAdapter(this, list)
        mBinding.viewpager.adapter = pagerAdapter

        var mediator = TabLayoutMediator(mBinding.tabLayout, mBinding.viewpager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position -> tab.text = MATCH_RECORD_DETAIL_TAB[position] })
        mediator.attach()
    }

    private fun getRecordId(): Long {
        return intent.getLongExtra(EXTRA_RECORD_ID, -1)
    }

    private fun showRankDialog(recordId: Long) {
        val content = RankDialog()
        content.recordId = recordId
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Rank")
        dialogFragment.fixedHeight = ScreenUtils.getScreenHeight() *2 / 3
        dialogFragment.show(supportFragmentManager, "RankDialog")
    }

}