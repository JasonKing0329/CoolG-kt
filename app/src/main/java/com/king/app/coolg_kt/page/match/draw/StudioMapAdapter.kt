package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterNameValueBinding
import com.king.app.coolg_kt.page.match.StudioMapItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/10 14:32
 */
class StudioMapAdapter: BaseBindingAdapter<AdapterNameValueBinding, StudioMapItem>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterNameValueBinding = AdapterNameValueBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterNameValueBinding, position: Int, bean: StudioMapItem) {
        binding.tvName.text = bean.studio
        binding.tvValue.text = bean.count.toString()
    }
}