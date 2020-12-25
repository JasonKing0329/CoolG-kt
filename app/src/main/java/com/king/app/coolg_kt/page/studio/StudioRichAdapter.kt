package com.king.app.coolg_kt.page.studio

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStudioRichBinding

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/25 10:07
 */
class StudioRichAdapter: BaseBindingAdapter<AdapterStudioRichBinding, StudioRichItem>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterStudioRichBinding = AdapterStudioRichBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStudioRichBinding,
        position: Int,
        bean: StudioRichItem
    ) {
        binding.bean = bean
        binding.tvIndex.text = (position + 1).toString()
    }
}