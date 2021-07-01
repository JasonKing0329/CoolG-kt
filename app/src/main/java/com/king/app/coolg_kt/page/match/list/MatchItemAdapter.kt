package com.king.app.coolg_kt.page.match.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchItemBinding
import com.king.app.coolg_kt.databinding.AdapterMatchItemGroupBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:03
 */
class MatchItemAdapter: HeadChildBindingAdapter<AdapterMatchItemGroupBinding, AdapterMatchItemBinding, String, Match>() {

    var isDeleteMode = false

    var onMatchItemListener: OnMatchItemListener? = null

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

    override fun onBindHead(binding: AdapterMatchItemGroupBinding, position: Int, head: String) {
        binding.title = head
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
    }

    interface OnMatchItemListener {
        fun onEdit(position: Int, bean: Match)
        fun onDelete(position: Int, bean: Match)
    }
}