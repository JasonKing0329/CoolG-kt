package com.king.app.coolg_kt.page.studio

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStudioSimpleBinding
import com.king.app.coolg_kt.utils.ColorUtil

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/25 10:03
 */
class StudioSimpleAdapter: BaseBindingAdapter<AdapterStudioSimpleBinding, StudioSimpleItem>() {

    private var colorMap = mutableMapOf<String, Int>()

    init {
        createCharColors()
    }

    private fun createCharColors() {
        for (i in 'A' until 'Z') {
            colorMap[i.toString()] = ColorUtil.randomWhiteTextBgColor()
        }
    }

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterStudioSimpleBinding = AdapterStudioSimpleBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStudioSimpleBinding,
        position: Int,
        bean: StudioSimpleItem
    ) {
        binding.bean = bean
        binding.tvIndex.text = (position + 1).toString()

        val color = colorMap[bean.firstChar]
        color?.let {
            val drawable = binding.tvChar.background as GradientDrawable
            drawable.setColor(it)
            binding.tvChar.background = drawable
        }
    }
}