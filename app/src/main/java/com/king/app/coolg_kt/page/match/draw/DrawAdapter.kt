package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchRecordBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.DrawItem

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 14:38
 */
class DrawAdapter: BaseBindingAdapter<AdapterMatchRecordBinding, DrawItem>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordBinding = AdapterMatchRecordBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchRecordBinding, position: Int, bean: DrawItem) {
        var firstIndex = bean.matchItem.order * 2
        var seed1 = 0
        bean.matchRecord1?.bean?.recordSeed?.let { seed1 = it }
        var seed2 = 0
        bean.matchRecord2?.bean?.recordSeed?.let { seed2 = it }
        var seedWinner = 0
        bean.winner?.bean?.recordSeed?.let { seedWinner = it }
        binding.tvIndex1.text = (firstIndex + 1).toString()
        binding.tvIndex2.text = (firstIndex + 2).toString()
        binding.tvSeed1.text = if (seed1 > 0) { "[$seed1]" } else { "" }
        binding.tvSeed2.text = if (seed2 > 0) { "[$seed2]" } else { "" }
        binding.tvSeedWinner.text = if (seedWinner > 0) { "[$seedWinner]" } else { "" }
        binding.tvRank1.text = "R ${bean.matchRecord1?.bean?.recordRank}"
        binding.tvRank2.text = "R ${bean.matchRecord2?.bean?.recordRank}"
        binding.tvRankWinner.text = "R ${bean.winner?.bean?.recordRank}"

        ImageBindingAdapter.setRecordUrl(binding.ivPlayer1, bean.matchRecord1?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivPlayer2, bean.matchRecord2?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivWinner, bean.winner?.imageUrl)
    }
}