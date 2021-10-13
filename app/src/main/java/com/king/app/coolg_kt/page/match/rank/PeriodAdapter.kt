package com.king.app.coolg_kt.page.match.rank

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterPeriodItemBinding

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/10/13 16:00
 */
class PeriodAdapter: BaseBindingAdapter<AdapterPeriodItemBinding, Int>() {
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterPeriodItemBinding = AdapterPeriodItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterPeriodItemBinding, position: Int, bean: Int) {
        binding.tvName.text = "W$bean"
    }
}