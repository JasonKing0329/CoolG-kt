package com.king.app.coolg_kt.page.match.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchItemBinding
import com.king.app.coolg_kt.databinding.AdapterMatchItemGroupBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.MatchItemGroup
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:03
 */
class MatchItemAdapter: HeadChildBindingAdapter<AdapterMatchItemGroupBinding, AdapterMatchItemBinding, MatchItemGroup, Match>() {

    var isDeleteMode = false

    var onMatchItemListener: OnMatchItemListener? = null
    var onMatchGroupListener: OnMatchGroupListener? = null

    var isDemoImage = SettingProperty.isDemoImageMode()

    override val itemClass: Class<*>
        get() = Match::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchItemGroupBinding = AdapterMatchItemGroupBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchItemBinding = AdapterMatchItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterMatchItemGroupBinding, position: Int, head: MatchItemGroup) {
        binding.title = head.text
        binding.ivAdd.setOnClickListener { onMatchGroupListener?.onAddGroupItem(head.level) }
    }

    override fun onBindItem(binding: AdapterMatchItemBinding, position: Int, bean: Match) {
        binding.bean = bean
        binding.ivDelete.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        binding.ivDelete.setOnClickListener { onMatchItemListener?.onDelete(position, bean) }
        binding.ivEdit.setOnClickListener { onMatchItemListener?.onEdit(position, bean) }
        binding.tvOrder.text = "W${bean.orderInPeriod}"
        binding.tvLevel.text = MatchConstants.MATCH_LEVEL[bean.level]
        if (bean.byeDraws > 0) {
            binding.tvDraws.text = "${bean.draws - bean.byeDraws} Draws(${bean.byeDraws} bye, ${bean.qualifyDraws} qualify)"
        }
        else {
            binding.tvDraws.text = "${bean.draws} Draws(${bean.qualifyDraws} qualify)"
        }
        val color = when(bean.level) {
            MatchConstants.MATCH_LEVEL_GS -> binding.tvLevel.resources.getColor(R.color.match_level_gs)
            MatchConstants.MATCH_LEVEL_FINAL -> binding.tvLevel.resources.getColor(R.color.match_level_final)
            MatchConstants.MATCH_LEVEL_GM1000 -> binding.tvLevel.resources.getColor(R.color.match_level_gm1000)
            MatchConstants.MATCH_LEVEL_GM500 -> binding.tvLevel.resources.getColor(R.color.match_level_gm500)
            MatchConstants.MATCH_LEVEL_GM250 -> binding.tvLevel.resources.getColor(R.color.match_level_gm250)
            else -> binding.tvLevel.resources.getColor(R.color.match_level_low)
        }
        binding.tvLevel.setTextColor(color)
    }

    interface OnMatchGroupListener {
        fun onAddGroupItem(level: Int)
    }

    interface OnMatchItemListener {
        fun onEdit(position: Int, bean: Match)
        fun onDelete(position: Int, bean: Match)
    }
}