package com.king.app.coolg_kt.page.match.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchDetailGmBinding
import com.king.app.coolg_kt.page.match.RoundItem
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/25 11:05
 */
class Gm1000Adapter: BaseBindingAdapter<AdapterMatchDetailGmBinding, RoundItem>() {
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchDetailGmBinding = AdapterMatchDetailGmBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchDetailGmBinding, position: Int, bean: RoundItem) {
        if (bean.isTitle || bean.isPeriod) {
            binding.tvRound.background = null
        }
        else {
            binding.tvRound.setBackgroundResource(R.drawable.selector_match_gs_bg)
        }
        if (bean.text == "Win") {
            binding.tvRound.setTextColor(Color.RED)
        }
        else {
            binding.tvRound.setTextColor(binding.tvRound.resources.getColor(R.color.text_second))
        }
        binding.tvRound.text = bean.text
    }
}