package com.king.app.coolg_kt.page.match.draw

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.databinding.ActivityMatchDrawBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.WildcardBean
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.match.h2h.H2hActivity
import com.king.app.coolg_kt.page.match.item.MatchActivity
import com.king.app.coolg_kt.page.match.rank.RankActivity
import com.king.app.coolg_kt.page.match.record.RecordMatchActivity
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 15:11
 */
class DrawActivity: BaseActivity<ActivityMatchDrawBinding, DrawViewModel>() {

    val ACTION_SAVE_DRAW = 1111111111

    var REQUEST_SELECT_WILDCARD = 11901

    var REQUEST_CHANGE_PLAYER = 11902

    var REQUEST_INSERT_SEED = 11903

    var REQUEST_SELECT_PRE_APPLY = 11904

    companion object {
        val EXTRA_MATCH_PERIOD_ID = "match_period_id"
        fun startPage(context: Context, matchPeriodId: Long) {
            var intent = Intent(context, DrawActivity::class.java)
            intent.putExtra(EXTRA_MATCH_PERIOD_ID, matchPeriodId)
            context.startActivity(intent)
        }
    }

    val adapter = DrawAdapter()

    var isEditing = false

    private var wildcardDialog: WildcardDialog? = null

    private var preApplyDialog: WildcardDialog? = null

    override fun getContentView(): Int = R.layout.activity_match_draw

    override fun createViewModel(): DrawViewModel = generateViewModel(DrawViewModel::class.java)

    override fun initView() {

        mBinding.model = mModel
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_edit -> {
                    mBinding.actionbar.showConfirmStatus(it)
                    toggleEdit(true)
                }
                R.id.menu_history -> {
                    MatchActivity.startPage(this@DrawActivity, mModel.getMatchId())
                }
                R.id.menu_pre_apply -> {
                    if (mModel.isNotSupportPreApply()) {
                        showMessageShort("Pre-apply is not supported for current match")
                        return@setOnMenuItemListener
                    }
                    if (mModel.isDrawExist()) {
                        showMessageShort("This function is only available when draw is not created")
                        return@setOnMenuItemListener
                    }
                    showPreApplyDialog()
                }
                R.id.menu_insert_seed -> {
                    if (!mModel.isFirstRound()) {
                        showMessageShort("This function is only available for first round!")
                        return@setOnMenuItemListener
                    }
                    selectRecord(REQUEST_INSERT_SEED)
                }
                R.id.menu_create_draw -> {
                    if (mModel.isDrawExist()) {
                        showConfirmCancelMessage("Current match draw is already exist, do you want clear it?",
                            DialogInterface.OnClickListener { dialog, which -> startCreateDraw() },
                            null)
                    }
                    else {
                        startCreateDraw()
                    }
                }
                R.id.menu_create_score -> {
                    if (mModel.isDrawExist()) {
                        showConfirmCancelMessage("This action will clear all existed score data, continue?",
                            DialogInterface.OnClickListener { dialog, which -> mModel.createScore() },
                            null)
                    }
                    else {
                        mModel.createScore()
                    }
                }
                R.id.menu_studio_map -> {
                    showStudioMap()
                }
                R.id.menu_black_list -> {
                    BlackListActivity.startPage(this)
                }
            }
        }
        mBinding.actionbar.setOnConfirmListener {
            when(it) {
                ACTION_SAVE_DRAW -> mModel.saveDraw()
                R.id.menu_edit -> mModel.saveEdit()
            }
            false
        }
        mBinding.actionbar.setOnCancelListener {
            var autoCancel = when(it) {
                ACTION_SAVE_DRAW -> {
                    showConfirmCancelMessage("Are you sure to drop this draw?",
                        DialogInterface.OnClickListener { dialog, which -> mModel.cancelSaveDraw() },
                        null)
                    false
                }
                R.id.menu_edit -> {
                    if (mModel.isModified()) {
                        showConfirmCancelMessage("Are you sure to drop the edit?",
                            DialogInterface.OnClickListener { dialog, which ->
                                mModel.cancelEdit()
                            },
                            null)
                        false
                    }
                    else {
                        toggleEdit(false)
                        true
                    }
                }
                else -> {
                    toggleEdit(false)
                    true
                }
            }
            autoCancel
        }
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter.onDrawListener = object : DrawAdapter.OnDrawListener {
            override fun onClickPlayer(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?) {
                bean?.let {
                    when(it.bean.type) {
                        MatchConstants.MATCH_RECORD_NORMAL -> recordMatchPage(it.record)
                        MatchConstants.MATCH_RECORD_QUALIFY -> {
                            if (it.bean.recordId != 0L) {
                                recordMatchPage(it.record)
                            }
                        }
                        MatchConstants.MATCH_RECORD_WILDCARD -> {
                            if (isEditing && mModel.isFirstRound()) {
                                showWildcardDialog()
                            }
                            else {
                                recordMatchPage(it.record)
                            }
                        }
                    }
                }
            }

            override fun onEditPlayer(anchorView: View, position: Int, drawItem: DrawItem, bean: MatchRecordWrap?) {
                bean?.let {
                    val menu = PopupMenu(this@DrawActivity, anchorView)
                    menu.menuInflater.inflate(R.menu.match_draw_record, menu.menu)
                    menu.setOnMenuItemClickListener { item: MenuItem ->
                        when (item.itemId) {
                            R.id.menu_change -> {
                                if (isEditing && mModel.isFirstRound()) {
                                    selectChangeRecord(position, drawItem, it)
                                }
                            }
                            R.id.menu_detail -> recordPage(it.record)
                            R.id.menu_history -> recordMatchPage(it.record)
                        }
                        false
                    }
                    menu.show()
                }
            }

            override fun onPlayerDetail(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?) {
                bean?.record?.let {
                    recordPage(it)
                }
            }

            override fun onPlayerWin(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?) {
                if (isEditing) {
                    drawItem.winner = bean
                    drawItem.isChanged = true
                    adapter.notifyItemChanged(position)
                }
            }

            override fun onClickH2H(position: Int, drawItem: DrawItem) {
                kotlin.runCatching {
                    H2hActivity.startH2hPage(this@DrawActivity, drawItem.matchRecord1!!.bean.recordId, drawItem.matchRecord2!!.bean.recordId)
                }
            }
        }
        mBinding.rvList.adapter = adapter

        mBinding.spRound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                DebugLog.e()
                mModel.onRoundPositionChanged(position)
            }

        }

        mBinding.tvMain.isSelected = true
        mBinding.tvMain.setOnClickListener {
            if (mModel.warningIfModified()) {
                return@setOnClickListener
            }
            mModel.drawType = MatchConstants.DRAW_MAIN
            mModel.onDrawTypeChanged()
            mBinding.tvMain.isSelected = true
            mBinding.tvQualify.isSelected = false
        }
        mBinding.tvQualify.setOnClickListener {
            if (mModel.warningIfModified()) {
                return@setOnClickListener
            }
            mModel.drawType = MatchConstants.DRAW_QUALIFY
            mModel.onDrawTypeChanged()
            mBinding.tvMain.isSelected = false
            mBinding.tvQualify.isSelected = true
        }
        mBinding.tvRandomWin.setOnClickListener {
            if (isEditing) {
                showConfirmCancelMessage("Random results will overwrite the origin results, continue?",
                    { dialog, which ->
                        mModel.randomWin(mBinding.spPriority.selectedItemPosition)
                        adapter.notifyDataSetChanged()
                    },
                    null)
            }
            else {
                showMessageShort("It only works in edit mode")
            }
        }
    }

    private fun showStudioMap() {
        var content = StudioMapDialog()
        content.drawItems = mModel.itemsObserver.value
        var dialog = DraggableDialogFragment()
        dialog.setTitle("Count by studio")
        dialog.contentFragment = content
        dialog.maxHeight = ScreenUtils.getScreenHeight() * 2 / 3
        dialog.show(supportFragmentManager, "SeasonEditor")
    }

    private fun startCreateDraw() {
        if (mModel.matchPeriod.match.level == MatchConstants.MATCH_LEVEL_GS) {
            mModel.createDraw(SettingProperty.getDrawStrategy())
        }
        else {
            var content = DrawPlanDialog()
            content.matchPeriod = mModel.matchPeriod
            content.onDrawPlanListener = object : DrawPlanDialog.OnDrawPlanListener {
                override fun onSetPlan(drawStrategy: DrawStrategy) {
                    mModel.createDraw(drawStrategy)
                }
            }
            var dialog = DraggableDialogFragment()
            val title = "Plan"
            dialog.setTitle(title)
            dialog.contentFragment = content
            dialog.show(supportFragmentManager, "DrawPlanDialog")
        }
    }

    private fun selectChangeRecord(position: Int, drawItem: DrawItem, recordWrap: MatchRecordWrap) {
        mModel.mToSetWildCard = drawItem
        mModel.mToSetWildCardPosition = position
        mModel.mToSetWildCardRecord = recordWrap
        selectRecord(REQUEST_CHANGE_PLAYER)
    }

    private fun showWildcardDialog() {
        wildcardDialog = WildcardDialog()
        wildcardDialog?.apply {
            countText = "Wildcards"
            dataList = mModel.getWildcardList()
            wildcardListener = object : WildcardDialog.WildCardListener {
                override fun selectRecord() {
                    selectRecord(REQUEST_SELECT_WILDCARD)
                }

                override fun confirm(dataList: List<WildcardBean>) {
                    mModel.arrangeWildcards(dataList)
                }
            }

            var dialog = DraggableDialogFragment()
            dialog.setTitle("Wildcards")
            dialog.contentFragment = this
            dialog.show(supportFragmentManager, "wildcardDialog")
        }
    }

    private fun showPreApplyDialog() {
        preApplyDialog = WildcardDialog()
        preApplyDialog?.apply {
            countText = "Appliers"
            enableAddAndRemove = true
            dataList = mModel.preApplyList
            wildcardListener = object : WildcardDialog.WildCardListener {
                override fun selectRecord() {
                    selectRecord(REQUEST_SELECT_PRE_APPLY)
                }

                override fun confirm(dataList: List<WildcardBean>) {

                }
            }
            var dialog = DraggableDialogFragment()
            dialog.setTitle("Pre apply")
            dialog.contentFragment = this
            dialog.show(supportFragmentManager, "preApplyDialog")
        }
    }

    private fun selectRecord(requestCode: Int) {
        AlertDialogFragment()
            .setItems(
                arrayOf("Record List", "Rank List", "Studio Records", "Studio in rank", "Out of rank")
            ) { dialog, which ->
                when(which) {
                    0 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@DrawActivity, requestCode, matchLevel = mModel.matchPeriod.match.level)
                    1 -> RankActivity.startPageToSelect(this@DrawActivity, requestCode, mModel.getSelectFocusToRank(), mModel.matchPeriod.match.level)
                    2 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@DrawActivity, requestCode, studioId = mModel.findStudioId(), matchLevel = mModel.matchPeriod.match.level)
                    3 -> RankActivity.startPageToSelectStudioItem(this@DrawActivity, requestCode, mModel.findStudioId(), mModel.matchPeriod.match.level)
                    4 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@DrawActivity, requestCode, outOfRank = true, matchLevel = mModel.matchPeriod.match.level)
                }
            }
            .show(supportFragmentManager, "AlertDialogFragment")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_SELECT_WILDCARD -> {
                if (resultCode == Activity.RESULT_OK) {
                    val recordId = data?.getLongExtra(PhoneRecordListActivity.RESP_RECORD_ID, -1)
                    mModel.setWildCard(recordId!!)?.apply {
                        wildcardDialog?.setSelectedRecord(this)
                    }
                }
            }
            REQUEST_SELECT_PRE_APPLY -> {
                if (resultCode == Activity.RESULT_OK) {
                    val recordId = data?.getLongExtra(PhoneRecordListActivity.RESP_RECORD_ID, -1)
                    mModel.setPreApply(recordId!!)?.apply {
                        preApplyDialog?.setSelectedRecord(this)
                    }
                }
            }
            REQUEST_CHANGE_PLAYER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val recordId = data?.getLongExtra(PhoneRecordListActivity.RESP_RECORD_ID, -1)
                    if (mModel.setChangedRecord(recordId!!)) {
                        adapter.notifyItemChanged(mModel.mToSetWildCardPosition!!)
                    }
                }
            }
            REQUEST_INSERT_SEED -> {
                if (resultCode == Activity.RESULT_OK) {
                    val recordId = data?.getLongExtra(PhoneRecordListActivity.RESP_RECORD_ID, -1)
                    if (mModel.insertSeed(recordId!!)) {
                        mBinding.actionbar.showConfirmStatus(R.id.menu_edit)
                        adapter.notifyDataSetChanged()
                    }
                }

            }
        }
    }

    private fun recordPage(record: Record?) {
        record?.let {
            DetailActivity.startRecordPage(this, it.id!!)
        }
    }

    private fun recordMatchPage(record: Record?) {
        record?.let {
            RecordMatchActivity.startPage(this, it.id!!, mModel.getMatchId())
        }
    }

    override fun initData() {

        mModel.cancelConfirmCancelStatus.observe(this, Observer {
            mBinding.actionbar.cancelConfirmStatus()
            toggleEdit(false)
        })
        mModel.setRoundPosition.observe(this, Observer { mBinding.spRound.setSelection(it) })
        mModel.roundList.observe(this, Observer {
            mBinding.spRound.adapter = RoundAdapter(it)
        })
        mModel.newDrawCreated.observe(this, Observer {
            mBinding.actionbar.showConfirmStatus(ACTION_SAVE_DRAW)
        })
        mModel.itemsObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.imageChanged.observe(this, Observer {
            adapter.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.saveEditSuccess.observe(this, Observer {
            mBinding.actionbar.cancelConfirmStatus()
            toggleEdit(false)
        })
        mModel.loadMatch(intent.getLongExtra(EXTRA_MATCH_PERIOD_ID, -1))
    }

    private fun toggleEdit(edit: Boolean) {
        isEditing = edit
        adapter.isEditing = edit
        adapter.notifyDataSetChanged()
        mBinding.groupEdit.visibility = if (edit) View.VISIBLE else View.GONE
        mBinding.groupRound.visibility = if (edit) View.GONE else View.VISIBLE
        if (edit) {
            mBinding.spPriority.setSelection(mModel.randomPriority(mBinding.spPriority.count))
        }
    }

    override fun onBackPressed() {
        if (isEditing) {
            showConfirmMessage("Data is changed, please save or drop it first!", null)
        }
        else {
            super.onBackPressed()
        }
    }

    class RoundAdapter constructor(var list: List<RoundPack>): BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var contentView = convertView
                ?: LayoutInflater.from(parent.context).inflate(android.R.layout.simple_dropdown_item_1line, null)
            val textView = contentView.findViewById<TextView>(android.R.id.text1)
            textView.text = list[position].shortName
            return contentView
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return list[position].id.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

    }
}