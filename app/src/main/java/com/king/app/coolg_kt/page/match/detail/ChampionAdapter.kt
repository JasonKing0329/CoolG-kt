package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchDetailChampionBinding
import com.king.app.coolg_kt.page.match.ChampionItem

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/25 8:24
 */
class ChampionAdapter: BaseBindingAdapter<AdapterMatchDetailChampionBinding, ChampionItem>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchDetailChampionBinding = AdapterMatchDetailChampionBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterMatchDetailChampionBinding,
        position: Int,
        bean: ChampionItem
    ) {
        binding.bean = bean
        val color = when(bean.levelId) {
            MatchConstants.MATCH_LEVEL_GS -> binding.tvLevel.resources.getColor(R.color.match_level_gs)
            MatchConstants.MATCH_LEVEL_FINAL -> binding.tvLevel.resources.getColor(R.color.match_level_final)
            MatchConstants.MATCH_LEVEL_GM1000 -> binding.tvLevel.resources.getColor(R.color.match_level_gm1000)
            MatchConstants.MATCH_LEVEL_GM500 -> binding.tvLevel.resources.getColor(R.color.match_level_gm500)
            MatchConstants.MATCH_LEVEL_GM250 -> binding.tvLevel.resources.getColor(R.color.match_level_gm250)
            else -> binding.tvLevel.resources.getColor(R.color.match_level_low)
        }
        binding.tvLevel.setTextColor(color)
    }
}