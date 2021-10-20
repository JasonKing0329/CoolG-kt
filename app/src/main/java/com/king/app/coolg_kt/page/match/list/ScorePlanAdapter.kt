package com.king.app.coolg_kt.page.match.list

import android.R
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.databinding.AdapterScorePlanItemBinding
import com.king.app.coolg_kt.page.match.ScoreItem
import com.king.app.coolg_kt.page.match.ScoreQualifyItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/10/20 13:54
 */
class ScorePlanAdapter: BaseBindingAdapter<AdapterScorePlanItemBinding, ScoreItem>() {

    var roundList = listOf<RoundPack>()
    var roundTextList = listOf<String>()

    var textWatchers = mutableMapOf<AdapterScorePlanItemBinding, WatcherPack>()
    var onDeleteItemListener: OnDeleteItemListener? = null

    class WatcherPack {
        var winWatcher: SimpleWatcher? = null
        var winQWatcher: SimpleWatcher? = null
        var loseWatcher: SimpleWatcher? = null
        var loseQWatcher: SimpleWatcher? = null
    }

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterScorePlanItemBinding = AdapterScorePlanItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterScorePlanItemBinding, position: Int, bean: ScoreItem) {
        binding.etWin.removeTextChangedListener(textWatchers[binding]?.winWatcher)
        binding.etLose.removeTextChangedListener(textWatchers[binding]?.loseWatcher)
        binding.etWinQ.removeTextChangedListener(textWatchers[binding]?.winQWatcher)
        binding.etLoseQ.removeTextChangedListener(textWatchers[binding]?.loseQWatcher)
        binding.cbQualify.setOnCheckedChangeListener(null)
        binding.spRound.onItemSelectedListener = null

        binding.etWin.setText(bean.scoreWin.toString())
        binding.etLose.setText(bean.scoreLose.toString())

        if (bean.qualifyItem == null) {
            binding.cbQualify.isChecked = false
            binding.llQualify.visibility = View.GONE
            binding.etWinQ.setText("0")
            binding.etLoseQ.setText("0")
        }
        else {
            binding.cbQualify.isChecked = true
            binding.llQualify.visibility = View.VISIBLE
            binding.etWinQ.setText(bean.qualifyItem!!.scoreWin.toString())
            binding.etLoseQ.setText(bean.qualifyItem!!.scoreLose.toString())
        }

        binding.cbQualify.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                bean.qualifyItem = ScoreQualifyItem(0, 0)
                binding.llQualify.visibility = View.VISIBLE
            }
            else {
                bean.qualifyItem = null
                binding.llQualify.visibility = View.GONE
            }
        }

        var adapter = ArrayAdapter<String>(binding.spRound.context, R.layout.simple_dropdown_item_1line, roundTextList)
        binding.spRound.adapter = adapter
        binding.spRound.setSelection(getRoundIndex(bean.round))
        // spinner会自动触发onItemSelected 0
        binding.spRound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                bean.round = roundList[position].id
            }
        }

        var pack = textWatchers[binding]
        if (pack == null) {
            pack = WatcherPack()
            textWatchers[binding] = pack
        }
        pack.winWatcher = object : SimpleWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                kotlin.runCatching {
                    bean.scoreWin = s.toString().toInt()
                }
            }
        };
        binding.etWin.addTextChangedListener(pack.winWatcher)
        pack.loseWatcher = object : SimpleWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                kotlin.runCatching {
                    bean.scoreLose = s.toString().toInt()
                }
            }
        }
        binding.etLose.addTextChangedListener(pack.loseWatcher)
        pack.winQWatcher = object : SimpleWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                kotlin.runCatching {
                    bean.qualifyItem?.scoreWin = s.toString().toInt()
                }
            }
        }
        binding.etWinQ.addTextChangedListener(pack.winQWatcher)
        pack.loseQWatcher = object : SimpleWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                kotlin.runCatching {
                    bean.qualifyItem?.scoreLose = s.toString().toInt()
                }
            }
        }
        binding.etLoseQ.addTextChangedListener(pack.loseQWatcher)

        binding.ivDelete.setOnClickListener { onDeleteItemListener?.onDelete(position) }
    }

    private fun getRoundIndex(round: Int): Int {
        roundList.forEachIndexed { index, roundPack ->
            if (round == roundPack.id) {
                return index
            }
        }
        return 0
    }

    abstract class SimpleWatcher: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun afterTextChanged(s: Editable?) {

        }

    }

    interface OnDeleteItemListener {
        fun onDelete(position: Int)
    }
}