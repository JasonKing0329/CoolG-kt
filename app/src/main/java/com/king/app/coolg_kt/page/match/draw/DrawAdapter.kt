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
        var seed1 = 0
        bean.matchRecord1?.bean?.recordSeed?.let {
            seed1 = it
        }
        var seed2 = 0
        bean.matchRecord2?.bean?.recordSeed?.let {
            seed2 = it
        }
        if (seed1 > 0) {
            binding.tvIndex1.text = "${bean.matchItem.order}\n[$seed1]"
        }
        else {
            binding.tvIndex1.text = bean.matchItem.order.toString()
        }
        if (seed2 > 0) {
            binding.tvIndex2.text = "${bean.matchItem.order + 1}\n[$seed1]"
        }
        else {
            binding.tvIndex2.text = (bean.matchItem.order + 1).toString()
        }

        ImageBindingAdapter.setRecordUrl(binding.ivPlayer1, bean.matchRecord1?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivPlayer2, bean.matchRecord2?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivWinner, bean.winner?.imageUrl)
    }
}