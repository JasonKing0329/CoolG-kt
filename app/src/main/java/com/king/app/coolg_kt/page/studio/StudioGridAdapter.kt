package com.king.app.coolg_kt.page.studio

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStudioGridBinding
import com.king.app.coolg_kt.utils.ColorUtil

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/25 10:03
 */
class StudioGridAdapter: BaseBindingAdapter<AdapterStudioGridBinding, StudioSimpleItem>() {

    private var colorMap = mutableMapOf<String, Int>()

    var onEditListener: OnEditListener? = null

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
    ): AdapterStudioGridBinding = AdapterStudioGridBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStudioGridBinding,
        position: Int,
        bean: StudioSimpleItem
    ) {
        binding.bean = bean

        binding.ivEdit.setOnClickListener { onEditListener?.onEditItem(position, bean) }
    }

    interface OnEditListener {
        fun onEditItem(position: Int, bean: StudioSimpleItem)
    }
}