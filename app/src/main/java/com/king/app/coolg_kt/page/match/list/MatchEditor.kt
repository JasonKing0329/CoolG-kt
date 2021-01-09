package com.king.app.coolg_kt.page.match.list

import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.FragmentMatchEditorBinding
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 14:08
 */
class MatchEditor: DraggableContentFragment<FragmentMatchEditorBinding>() {

    var match: Match? = null

    var onMatchListener: OnMatchListener? = null

    override fun getBinding(inflater: LayoutInflater): FragmentMatchEditorBinding = FragmentMatchEditorBinding.inflate(inflater)

    override fun initData() {
        mBinding.tvOk.setOnClickListener { saveMatch() }

        match?.let {
            mBinding.etOrder.setText(it.orderInPeriod.toString())
            mBinding.etDraw.setText(it.draws.toString())
            mBinding.etQualifyDraw.setText(it.qualifyDraws.toString())
            mBinding.etByeDraw.setText(it.byeDraws.toString())
            mBinding.etWildcard.setText(it.wildcardDraws.toString())
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
    }

    private fun saveMatch() {
        val order = mBinding.etOrder.text.toString().toInt()
        val name = mBinding.etName.text.toString()
        val level = mBinding.spLevel.selectedItemPosition
        val draw = mBinding.etDraw.text.toString().toInt()
        val qualifyDraw = mBinding.etQualifyDraw.text.toString().toInt()
        val byeDraw = mBinding.etByeDraw.text.toString().toInt()
        val wildcardDraw = mBinding.etWildcard.text.toString().toInt()
        if (match == null) {
            match = Match(0, level, draw, byeDraw, qualifyDraw, wildcardDraw, order, name, "")
            var list = listOf(match!!)
            CoolApplication.instance.database!!.getMatchDao().insertMatches(list)
        }
        else {
            match?.let {
                it.level = level
                it.draws = draw
                it.byeDraws = byeDraw
                it.qualifyDraws = qualifyDraw
                it.wildcardDraws = wildcardDraw
                it.orderInPeriod = order
                it.name = name
                CoolApplication.instance.database!!.getMatchDao().updateMatch(it)
            }
        }
        onMatchListener?.onUpdated(match!!)
        dismissAllowingStateLoss()
    }

    interface OnMatchListener {
        fun onUpdated(match: Match)
    }
}