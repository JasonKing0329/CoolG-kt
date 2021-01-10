package com.king.app.coolg_kt.page.match.season

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterSeasonItemBinding
import com.king.app.coolg_kt.databinding.AdapterSeasonPeriodBinding
import com.king.app.coolg_kt.model.bean.MatchPeriodTitle
import com.king.app.coolg_kt.utils.FormatUtil
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
        binding.tvDate.text = FormatUtil.formatDate(bean.bean.date)
    }

    interface OnActionListener {
        fun onDeleteItem(position: Int, bean: MatchPeriodWrap)
        fun onEditItem(position: Int, bean: MatchPeriodWrap)
    }
}