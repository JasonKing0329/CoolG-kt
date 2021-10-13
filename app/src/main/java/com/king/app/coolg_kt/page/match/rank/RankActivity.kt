package com.king.app.coolg_kt.page.match.rank

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchRankBinding
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.ProgressDialogFragment
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
        val EXTRA_SELECT_ALL_VALID = "select_all_valid"
        val EXTRA_SELECT_MATCH_LEVEL = "select_match_level"
        val EXTRA_FOCUS_TO_RANK = "focus_to_rank"
        val RESP_RECORD_ID = "record_id"
        val EXTRA_INIT_STUDIO = "studio_id"
        fun startPage(context: Context) {
            var intent = Intent(context, RankActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSelect(context: Activity, requestCode: Int) {
            var intent = Intent(context, RankActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            intent.putExtra(EXTRA_SELECT_ALL_VALID, true)
            context.startActivityForResult(intent, requestCode)
        }
        fun startPageToSelect(context: Activity, requestCode: Int, focusToRank: Int, matchLevel: Int) {
            var intent = Intent(context, RankActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            intent.putExtra(EXTRA_FOCUS_TO_RANK, focusToRank)
            intent.putExtra(EXTRA_SELECT_MATCH_LEVEL, matchLevel)
            context.startActivityForResult(intent, requestCode)
        }
        fun startPageToSelectStudioItem(context: Activity, requestCode: Int, studioId: Long, matchLevel: Int) {
            var intent = Intent(context, RankActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            intent.putExtra(EXTRA_INIT_STUDIO, studioId)
            intent.putExtra(EXTRA_SELECT_MATCH_LEVEL, matchLevel)
            context.startActivityForResult(intent, requestCode)
        }
    }

    private var detailProgress = ProgressDialogFragment()

    override fun getContentView(): Int = R.layout.activity_match_rank

    override fun createViewModel(): RankViewModel = generateViewModel(RankViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_create_rank -> createRank()
                R.id.menu_create_rank_detail -> mModel.createRankDetails()
                R.id.menu_p_end -> mModel.loadPeriodFinalRank()
                R.id.menu_high_rank -> highRank()
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
        mBinding.tvWeek.setOnClickListener { selectWeek() }
    }

    private fun selectWeek() {
        var content = PeriodFragment()
        content.period = mModel.showPeriod.period
        content.onPeriodSelectListener = object : PeriodFragment.OnPeriodSelectListener {
            override fun onSelectPeriod(period: Int, orderInPeriod: Int) {
                mModel.targetPeriod(period, orderInPeriod)
            }
        }
        var dialog = DraggableDialogFragment()
        val title = "Select week"
        dialog.setTitle(title)
        dialog.contentFragment = content
        dialog.maxHeight = ScreenUtils.getScreenHeight() * 2 / 3
        dialog.show(supportFragmentManager, "PeriodFragment")
    }

    private fun showScoreStructure(recordId: Long) {
        var content = RankScoresFragment()
        content.recordId = recordId
        content.period = mModel.showPeriod.period
        content.orderInPeriod = mModel.showPeriod.orderInPeriod
        var dialog = DraggableDialogFragment()
        val title = "Score Structure"
        dialog.setTitle(title)
        dialog.contentFragment = content
        dialog.maxHeight = ScreenUtils.getScreenHeight() * 4 / 5
        dialog.show(supportFragmentManager, "RankScoresFragment")
    }

    private fun highRank() {
        HighRankActivity.startPage(this)
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

    private fun isSelectMode(): Boolean {
        return intent.getBooleanExtra(EXTRA_SELECT_MODE, false)
    }

    private fun isSelectAllValid(): Boolean {
        return intent.getBooleanExtra(EXTRA_SELECT_ALL_VALID, false)
    }

    private fun getFocusToRank(): Int {
        return intent.getIntExtra(EXTRA_FOCUS_TO_RANK, 0)
    }

    private fun getSelectMatchLevel(): Int {
        return intent.getIntExtra(EXTRA_SELECT_MATCH_LEVEL, 0)
    }

    private fun getInitStudioId(): Long {
        return intent.getLongExtra(EXTRA_INIT_STUDIO, 0)
    }

    override fun initData() {
        mModel.isSelectMode = isSelectMode()
        mModel.isSelectAllValid = isSelectAllValid()
        mModel.mMatchSelectLevel = getSelectMatchLevel()
        mModel.initPeriod()

        mModel.recordRanksObserver.observe(this, Observer {
            var adapter = RankAdapter<Record?>()
            adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RankItem<Record?>>{
                override fun onClickItem(view: View, position: Int, data: RankItem<Record?>) {
                    if (isSelectMode()) {
                        if (data.canSelect) {
                            val intent = Intent()
                            intent.putExtra(RESP_RECORD_ID, data.bean?.id)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                        else {
                            showMessageShort("This record is already in draws of current week")
                        }
                    }
                }
            })
            adapter.onItemListener = object : RankAdapter.OnItemListener<Record?> {
                override fun onClickScore(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        showScoreStructure(record.id!!)
                    }
                }

                override fun onClickImage(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        RecordActivity.startPage(this@RankActivity, record.id!!)
                    }
                }

                override fun onClickRank(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        showRankDialog(record)
                    }
                }

                override fun onClickMatchCount(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        DetailActivity.startRecordPage(this@RankActivity, record.id!!)
                    }
                }

                override fun onClickStudio(bean: RankItem<Record?>) {
                    bean.bean?.let { record ->
                        DetailActivity.startRecordPage(this@RankActivity, record.id!!)
                    }
                }
            }
            adapter.list = it
            mBinding.rvList.adapter = adapter
            if (isSelectMode() && getFocusToRank() > 0) {
                mBinding.rvList.scrollToPosition(getFocusToRank())
            }
        })
        mModel.starRanksObserver.observe(this, Observer {
            var adapter = RankAdapter<Star?>()
            adapter.list = it
            mBinding.rvList.adapter = adapter
        })
        mModel.imageChanged.observe(this, Observer {
            mBinding.rvList.adapter?.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.detailProgressError.observe(this, Observer { detailProgress.dismissAllowingStateLoss() })
        mModel.detailProgressing.observe(this, Observer {
            when(it) {
                0 -> {
                    if (!detailProgress.isVisible) {
                        detailProgress.setMessage("Creating rank details...")
                        detailProgress.showAsNumProgress(0, supportFragmentManager, "DetailProgress")
                    }
                }
                100 -> {
                    detailProgress.setProgress(it)
                    detailProgress.dismissAllowingStateLoss()
                    showMessageShort("success")
                }
                else -> detailProgress.setProgress(it)
            }
        })

        mBinding.tvPeriod.isSelected = true
        mModel.onRecordOrStarChanged(0)

        mModel.studiosObserver.observe(this, Observer {
            var adapter = ArrayAdapter<String>(this@RankActivity, android.R.layout.simple_dropdown_item_1line, it)
            mBinding.spStudio.adapter = adapter
            // spinner会自动触发onItemSelected 0
            mBinding.spStudio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    mModel.filterByStudio(position - 1)
                }
            }
        })
        mModel.mOnlyStudioId = getInitStudioId()
        if (getInitStudioId() > 0) {
            mBinding.spStudio.visibility = View.INVISIBLE
        }
        mModel.loadStudios()
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