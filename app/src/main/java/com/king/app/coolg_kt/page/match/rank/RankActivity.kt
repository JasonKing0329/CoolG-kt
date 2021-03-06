package com.king.app.coolg_kt.page.match.rank

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchRankBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.coolg_kt.page.match.score.ScoreActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.record.popup.SortDialogContent
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.entity.Star

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 16:28
 */
class RankActivity: BaseActivity<ActivityMatchRankBinding, RankViewModel>() {

    companion object {
        val EXTRA_SELECT_MODE = "select_mode"
        val RESP_RECORD_ID = "record_id"
        fun startPage(context: Context) {
            var intent = Intent(context, RankActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSelect(context: Activity, requestCode: Int) {
            var intent = Intent(context, RankActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            context.startActivityForResult(intent, requestCode)
        }
    }

    override fun getContentView(): Int = R.layout.activity_match_rank

    override fun createViewModel(): RankViewModel = generateViewModel(RankViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_create_rank -> createRank()
            }
        }

        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = 0
                outRect.bottom = 0
            }
        })

        mBinding.tvPeriod.setOnClickListener {
            mBinding.tvPeriod.isSelected = true
            mBinding.tvRtf.isSelected = false
            mModel.onPeriodOrRtfChanged(0)
        }
        mBinding.tvRtf.setOnClickListener {
            mBinding.tvPeriod.isSelected = false
            mBinding.tvRtf.isSelected = true
            mModel.onPeriodOrRtfChanged(1)
        }
        mBinding.ivNext.setOnClickListener { mModel.nextPeriod() }
        mBinding.ivPrevious.setOnClickListener { mModel.lastPeriod() }
    }

    /**
     *
    <item>Record-Period</item>
    <item>Record-RTF</item>
    <item>Star-Period</item>
    <item>Star-RTF</item>
     */
    private fun createRank() {
        when {
            mModel.periodOrRtf == 0 && mModel.recordOrStar == 0 -> {
                if (mModel.isLastRecordRankCreated()) {
                    showConfirmCancelMessage("Record ranks of last week have been already created, do you want to override it?",
                        DialogInterface.OnClickListener { dialog, which -> mModel.createRankRecord() },
                        null)
                }
                else {
                    mModel.createRankRecord()
                }
            }
            mModel.periodOrRtf == 1 && mModel.recordOrStar == 0 -> showMessageShort("Create record ranks can only be executed in Record-Period!")
            mModel.periodOrRtf == 0 && mModel.recordOrStar == 1 -> {
                if (mModel.isLastStarRankCreated()) {
                    showConfirmCancelMessage("Star Ranks of last week have been already created, do you want to override it?",
                        DialogInterface.OnClickListener { dialog, which -> mModel.createRankStar() },
                        null)
                }
                else {
                    mModel.createRankStar()
                }
            }
            mModel.periodOrRtf == 1 && mModel.recordOrStar == 0 -> showMessageShort("Create star ranks can only be executed in Star-Period!")
        }
    }

    override fun initData() {
        mModel.recordRanksObserver.observe(this, Observer {
            var adapter = RankAdapter<Record?>()
            adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RankItem<Record?>>{
                override fun onClickItem(view: View, position: Int, data: RankItem<Record?>) {
                    if (intent.getBooleanExtra(EXTRA_SELECT_MODE, false)) {
                        val intent = Intent()
                        intent.putExtra(RESP_RECORD_ID, data.bean?.id)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            })
            adapter.onItemListener = object : RankAdapter.OnItemListener<Record?> {
                override fun onClickScore(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        ScoreActivity.startRecordPage(this@RankActivity, record.id!!)
                    }
                }

                override fun onClickId(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        RecordActivity.startPage(this@RankActivity, record.id!!)
                    }
                }

                override fun onClickRank(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        showRankDialog(record)
                    }
                }
            }
            adapter.list = it
            mBinding.rvList.adapter = adapter
        })
        mModel.starRanksObserver.observe(this, Observer {
            var adapter = RankAdapter<Star?>()
            adapter.list = it
            mBinding.rvList.adapter = adapter
        })
        mModel.imageChanged.observe(this, Observer {
            mBinding.rvList.adapter?.notifyItemRangeChanged(it.start, it.count)
        })

        mBinding.tvPeriod.isSelected = true
        // spinner会自动触发onItemSelected 0
        mBinding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mModel.onRecordOrStarChanged(position)
            }
        }
    }

    private fun showRankDialog(record: Record) {
        val content = RankDialog()
        content.recordId = record.id!!
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Rank")
        dialogFragment.fixedHeight = ScreenUtils.getScreenHeight() *2 / 3
        dialogFragment.show(supportFragmentManager, "RankDialog")
    }
}