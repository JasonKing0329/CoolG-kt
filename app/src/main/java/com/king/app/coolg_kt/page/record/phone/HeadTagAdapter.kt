package com.king.app.coolg_kt.page.record.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.coolg_kt.page.record.RecordTag
import com.king.app.gdb.data.entity.Tag

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/6 20:59
 */
class HeadTagAdapter: BaseBindingAdapter<AdapterTagItemBinding, RecordTag>() {

    var selection = -1

    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterTagItemBinding
            = AdapterTagItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterTagItemBinding, position: Int, bean: RecordTag) {
        binding.tvName.text = if (bean.number > 0) "${bean.name}(${bean.number})"
            else bean.name
        binding.tvName.isSelected = position == selection
        binding.ivRemove.visibility = View.GONE
    }

    override fun onClickItem(v: View, position: Int, bean: RecordTag) {
        if (position != selection) {
            super.onClickItem(v, position, bean)
        }
        selection = position
        notifyDataSetChanged()
    }

}