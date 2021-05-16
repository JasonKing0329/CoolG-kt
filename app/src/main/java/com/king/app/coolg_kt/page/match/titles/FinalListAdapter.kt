package com.king.app.coolg_kt.page.match.titles

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchFinalItemBinding
import com.king.app.coolg_kt.page.match.FinalListItem
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/22 13:08
 */
class FinalListAdapter: BaseBindingAdapter<AdapterMatchFinalItemBinding, FinalListItem>() {

    var onClickRecordListener: OnClickRecordListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchFinalItemBinding = AdapterMatchFinalItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterMatchFinalItemBinding,
        position: Int,
        bean: FinalListItem
    ) {
        binding.bean = bean
        binding.tvMatch.text = bean.match.match.name
        binding.tvWeek.text = "P${bean.match.bean.period} W${bean.match.bean.orderInPeriod}"
        binding.tvLevel.text = "${MatchConstants.MATCH_LEVEL[bean.match.match.level]} "
        val color = when(bean.match.match.level) {
            MatchConstants.MATCH_LEVEL_GS -> binding.tvLevel.resources.getColor(R.color.match_level_gs)
            MatchConstants.MATCH_LEVEL_FINAL -> binding.tvLevel.resources.getColor(R.color.match_level_final)
            MatchConstants.MATCH_LEVEL_GM1000 -> binding.tvLevel.resources.getColor(R.color.match_level_gm1000)
            MatchConstants.MATCH_LEVEL_GM500 -> binding.tvLevel.resources.getColor(R.color.match_level_gm500)
            MatchConstants.MATCH_LEVEL_GM250 -> binding.tvLevel.resources.getColor(R.color.match_level_gm250)
            else -> binding.tvLevel.resources.getColor(R.color.match_level_low)
        }
        binding.tvLevel.setTextColor(color)
        bindRecord(binding.tvNameWin, binding.tvSeedWin, bean.recordWin)
        bindRecord(binding.tvNameLose, binding.tvSeedLose, bean.recordLose)
        binding.ivPlayerWin.setOnClickListener { onClickRecordListener?.onClickRecord(bean.recordWin) }
        binding.ivPlayerLose.setOnClickListener { onClickRecordListener?.onClickRecord(bean.recordLose) }
    }

    private fun bindRecord(tvName: TextView, tvSeed: TextView, record: MatchRecordWrap) {
        tvSeed.text = if (record.bean.recordSeed?:0 > 0) {
            "[${record.bean.recordSeed}]/${record.bean.recordRank}"
        }
        else {
            "${record.bean.recordRank}"
        }
        tvName.text = record.record?.name
    }
    
    interface OnClickRecordListener {
        fun onClickRecord(matchRecordWrap: MatchRecordWrap)
    }
}