package com.king.app.coolg_kt.page.match.h2h

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchH2hBinding
import com.king.app.coolg_kt.page.match.H2hItem

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/17 11:31
 */
class H2hAdapter: BaseBindingAdapter<AdapterMatchH2hBinding, H2hItem>() {
    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterMatchH2hBinding = AdapterMatchH2hBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchH2hBinding, position: Int, bean: H2hItem) {
        binding.bean = bean
        binding.group.setBackgroundColor(bean.bgColor)
        binding.tvSeq.text = "${position + 1}"
    }
}