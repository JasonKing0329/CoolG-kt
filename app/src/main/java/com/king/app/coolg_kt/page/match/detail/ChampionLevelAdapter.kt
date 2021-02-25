package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchChampionLevelBinding
import com.king.app.coolg_kt.page.match.ChampionLevel

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/25 10:08
 */
class ChampionLevelAdapter: BaseBindingAdapter<AdapterMatchChampionLevelBinding, ChampionLevel>() {

    var selection = -1

    var onLevelListener: OnLevelListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchChampionLevelBinding = AdapterMatchChampionLevelBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterMatchChampionLevelBinding,
        position: Int,
        bean: ChampionLevel
    ) {
        binding.tvName.text = "${bean.level}(${bean.count})"
        binding.tvName.isSelected = position == selection
    }

    override fun onClickItem(v: View, position: Int, bean: ChampionLevel) {
        if (position == selection) {
            selection = -1
            onLevelListener?.onAll()
        }
        else {
            selection = position
            onLevelListener?.onLevel(bean.levelId)
        }
        notifyDataSetChanged()
    }

    interface OnLevelListener {
        fun onAll()
        fun onLevel(id: Int)
    }
}