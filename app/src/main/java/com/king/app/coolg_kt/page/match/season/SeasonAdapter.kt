package com.king.app.coolg_kt.page.match.season

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterSeasonItemBinding
import com.king.app.coolg_kt.databinding.AdapterSeasonPeriodBinding
import com.king.app.coolg_kt.model.bean.MatchPeriodTitle
import com.king.app.coolg_kt.utils.RippleUtil
import com.king.app.gdb.data.relation.MatchPeriodWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/5/13 15:44
 */
class SeasonAdapter: HeadChildBindingAdapter<AdapterSeasonPeriodBinding, AdapterSeasonItemBinding, MatchPeriodTitle, MatchPeriodWrap>() {

    override val itemClass: Class<*> get() = MatchPeriodWrap::class.java

    var onActionListener: OnActionListener? = null

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterSeasonPeriodBinding = AdapterSeasonPeriodBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterSeasonItemBinding = AdapterSeasonItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterSeasonPeriodBinding, position: Int, head: MatchPeriodTitle) {
        binding.bean = head
    }

    override fun onBindItem(binding: AdapterSeasonItemBinding, position: Int, bean: MatchPeriodWrap) {
        binding.clGroup.background = RippleUtil.getRippleBackground(
            Color.WHITE
            , binding.clGroup.resources.getColor(R.color.ripple_color))
        binding.bean = bean
        binding.ivDelete.setOnClickListener { onActionListener?.onDeleteItem(position, bean) }
        binding.ivEdit.setOnClickListener { onActionListener?.onEditItem(position, bean) }
        binding.tvIndex.text = "W${bean.bean.orderInPeriod}"
        binding.tvLevel.text = "${MatchConstants.MATCH_LEVEL[bean.match.level]} "
        val color = when(bean.match.level) {
            MatchConstants.MATCH_LEVEL_GS -> binding.tvLevel.resources.getColor(R.color.match_level_gs)
            MatchConstants.MATCH_LEVEL_FINAL -> binding.tvLevel.resources.getColor(R.color.match_level_final)
            MatchConstants.MATCH_LEVEL_GM1000 -> binding.tvLevel.resources.getColor(R.color.match_level_gm1000)
            MatchConstants.MATCH_LEVEL_GM500 -> binding.tvLevel.resources.getColor(R.color.match_level_gm500)
            MatchConstants.MATCH_LEVEL_GM250 -> binding.tvLevel.resources.getColor(R.color.match_level_gm250)
            else -> binding.tvLevel.resources.getColor(R.color.match_level_low)
        }
        binding.tvLevel.setTextColor(color)
        val bye = if (bean.match.byeDraws > 0) ", Bye(${bean.match.byeDraws})" else ""
        val wc = if (bean.bean.mainWildcard > 0) {
            if (bean.bean.qualifyWildcard > 0) ", WC(M-${bean.bean.mainWildcard}, Q-${bean.bean.qualifyWildcard})" else ", WC(M-${bean.bean.mainWildcard})"
        }
        else {
            if (bean.bean.qualifyWildcard > 0) ", WC(Q-${bean.bean.qualifyWildcard})" else ""
        }
        binding.tvInfo.text = "Draws(${bean.match.draws}), Q(${bean.match.qualifyDraws})$bye$wc"
    }

    interface OnActionListener {
        fun onDeleteItem(position: Int, bean: MatchPeriodWrap)
        fun onEditItem(position: Int, bean: MatchPeriodWrap)
    }
}