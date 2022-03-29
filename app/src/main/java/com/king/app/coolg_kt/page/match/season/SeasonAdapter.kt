package com.king.app.coolg_kt.page.match.season

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.daimajia.swipe.SwipeLayout
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterSeasonItemBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.utils.RippleUtil
import com.king.app.gdb.data.relation.MatchPeriodWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/5/13 15:44
 */
class SeasonAdapter: BaseBindingAdapter<AdapterSeasonItemBinding, MatchPeriodWrap>() {

    var onActionListener: OnActionListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterSeasonItemBinding = AdapterSeasonItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterSeasonItemBinding, position: Int, bean: MatchPeriodWrap) {
        binding.clGroup.background = RippleUtil.getRippleBackground(
            Color.WHITE
            , binding.clGroup.resources.getColor(R.color.ripple_color))
        binding.bean = bean
        ImageBindingAdapter.setRecordUrl(binding.ivImg, bean.imageUrl)
        binding.llDelete.setOnClickListener { onActionListener?.onDeleteItem(position, bean) }
        binding.llEdit.setOnClickListener { onActionListener?.onEditItem(position, bean) }
        binding.tvLevel.text = "${MatchConstants.MATCH_LEVEL[bean.match.level]} "
        val color = when(bean.match.level) {
            MatchConstants.MATCH_LEVEL_GS -> binding.tvLevel.resources.getColor(R.color.match_level_gs)
            MatchConstants.MATCH_LEVEL_FINAL -> binding.tvLevel.resources.getColor(R.color.match_level_final)
            MatchConstants.MATCH_LEVEL_GM1000 -> binding.tvLevel.resources.getColor(R.color.match_level_gm1000)
            MatchConstants.MATCH_LEVEL_GM500 -> binding.tvLevel.resources.getColor(R.color.match_level_gm500)
            MatchConstants.MATCH_LEVEL_GM250 -> binding.tvLevel.resources.getColor(R.color.match_level_gm250)
            MatchConstants.MATCH_LEVEL_LOW -> binding.tvLevel.resources.getColor(R.color.match_level_low)
            else -> binding.tvLevel.resources.getColor(R.color.match_level_micro)
        }
        val drawable = binding.tvIndex.background as GradientDrawable
        drawable.setColor(color)
        binding.tvIndex.background = drawable
        binding.tvIndex.text = "W${bean.bean.orderInPeriod}"

        binding.tvLevel.setTextColor(color)
        val bye = if (bean.match.byeDraws > 0) ", Bye(${bean.match.byeDraws})" else ""
        val wc = if (bean.bean.mainWildcard > 0) {
            if (bean.bean.qualifyWildcard > 0) ", WC(M-${bean.bean.mainWildcard}, Q-${bean.bean.qualifyWildcard})" else ", WC(M-${bean.bean.mainWildcard})"
        }
        else {
            if (bean.bean.qualifyWildcard > 0) ", WC(Q-${bean.bean.qualifyWildcard})" else ""
        }
        binding.tvInfo.text = "Draws(${bean.match.draws}), Q(${bean.match.qualifyDraws})$bye$wc"

        binding.group.showMode = SwipeLayout.ShowMode.LayDown// PullOut是在滑动控件右侧/左侧，随surface一起拉出；LayDown是平铺在底层，随surface滑动一点点揭开
        binding.group.addDrag(SwipeLayout.DragEdge.Left, binding.llMenu)

        // 消费掉long click事件，不然滑动完手势离开后会触发onclick
        binding.root.setOnLongClickListener { true }
//        binding.clGroup.setOnClickListener { super.onClickItem(binding.clGroup, position, bean) }
    }

    interface OnActionListener {
        fun onDeleteItem(position: Int, bean: MatchPeriodWrap)
        fun onEditItem(position: Int, bean: MatchPeriodWrap)
    }
}