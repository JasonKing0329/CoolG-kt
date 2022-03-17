package com.king.app.coolg_kt.page.match.list

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.FragmentMatchEditorBinding
import com.king.app.coolg_kt.page.studio.phone.StudioActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:08
 */
class MatchEditor: DraggableContentFragment<FragmentMatchEditorBinding>() {

    var match: Match? = null

    var initLevel = 0

    var onMatchListener: OnMatchListener? = null

    private var isApplyStudioCover = false

    private var studioCoverUrl: String? = null

    override fun getBinding(inflater: LayoutInflater): FragmentMatchEditorBinding = FragmentMatchEditorBinding.inflate(inflater)

    override fun initData() {
        mBinding.tvOk.setOnClickListener { saveMatch() }

        match.let {
            mBinding.spLevel.setSelection(initLevel)
        }
        match?.let {
            mBinding.etOrder.setText(it.orderInPeriod.toString())
            mBinding.etDraw.setText(it.draws.toString())
            mBinding.etQualifyDraw.setText(it.qualifyDraws.toString())
            mBinding.etByeDraw.setText(it.byeDraws.toString())
            mBinding.etName.setText(it.name)
            mBinding.spLevel.setSelection(it.level)
        }

        mBinding.spLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                var stdDraw = MatchConstants.MATCH_LEVEL_DRAW_STD[position]
//                mBinding.etDraw.setText(stdDraw.draw.toString())
//                mBinding.etByeDraw.setText(stdDraw.byeDraw.toString())
//                mBinding.etQualifyDraw.setText(stdDraw.qualifyDraw.toString())
            }
        }

        mBinding.btnStudio.setOnClickListener {
            showConfirmCancelMessage(
                "Apply studio's cover as match cover?",
                getString(R.string.yes),
                { dialog, which ->
                    isApplyStudioCover = true
                    StudioActivity.startPageToSelectAsMatch(this@MatchEditor, 0)
                },
                getString(R.string.no),
                { dialog, which ->
                    isApplyStudioCover = false
                    StudioActivity.startPageToSelectAsMatch(this@MatchEditor, 0)
                }
            )
        }

        if (MatchConstants.MATCH_LEVEL_FINAL == match?.level) {
            mBinding.llScorePlan.visibility = View.GONE
        }
        mBinding.ivScorePlan.setOnClickListener {
            showScorePlan()
        }
    }

    private fun showScorePlan() {
        var content = ScorePlanDialog()
        content.matchId = match!!.id
        var dialog = DraggableDialogFragment()
        dialog.contentFragment = content
        dialog.maxHeight = ScreenUtils.getScreenHeight()
        dialog.show(childFragmentManager, "ScorePlanDialog")
    }

    private fun saveMatch() {
        val order = mBinding.etOrder.text.toString().toInt()
        val name = mBinding.etName.text.toString()
        val level = mBinding.spLevel.selectedItemPosition
        val draw = mBinding.etDraw.text.toString().toInt()
        val qualifyDraw = mBinding.etQualifyDraw.text.toString().toInt()
        val byeDraw = mBinding.etByeDraw.text.toString().toInt()
        if (match == null) {
            match = Match(0, level, draw, byeDraw, qualifyDraw, 0, order, name, "")
            var list = listOf(match!!)
            if (isApplyStudioCover) {
                match!!.imgUrl = studioCoverUrl?:""
            }
            CoolApplication.instance.database!!.getMatchDao().insertMatches(list)
        }
        else {
            match?.let {
                it.level = level
                it.draws = draw
                it.byeDraws = byeDraw
                it.qualifyDraws = qualifyDraw
                it.orderInPeriod = order
                it.name = name
                if (isApplyStudioCover) {
                    it.imgUrl = studioCoverUrl?:""
                }
                CoolApplication.instance.database!!.getMatchDao().updateMatch(it)
            }
        }
        onMatchListener?.onUpdated(match!!)
        dismissAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val orderId = data!!.getLongExtra(AppConstants.RESP_ORDER_ID, -1)
            CoolApplication.instance.database!!.getFavorDao().getFavorRecordOrderBy(orderId)?.let {
                mBinding.etName.setText(it.name)
                if (isApplyStudioCover) {
                    studioCoverUrl = it.coverUrl
                }
            }
        }
    }

    interface OnMatchListener {
        fun onUpdated(match: Match)
    }
}