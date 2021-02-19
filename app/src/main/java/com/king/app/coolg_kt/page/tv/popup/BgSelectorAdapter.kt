package com.king.app.coolg_kt.page.tv.popup

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTvBgSelectorItemBinding
import com.king.app.coolg_kt.model.GlideApp

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/17 12:23
 */
class BgSelectorAdapter:BaseBindingAdapter<AdapterTvBgSelectorItemBinding, String>() {
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterTvBgSelectorItemBinding = AdapterTvBgSelectorItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterTvBgSelectorItemBinding, position: Int, bean: String) {
        GlideApp.with(binding.ivItem.context)
            .load(bean)
            .placeholder(R.drawable.def_small)
            .error(R.drawable.def_small)
            .into(binding.ivItem)
    }
}