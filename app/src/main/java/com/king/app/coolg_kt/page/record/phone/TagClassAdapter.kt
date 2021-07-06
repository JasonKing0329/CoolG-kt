package com.king.app.coolg_kt.page.record.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.gdb.data.entity.TagClass

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/6 20:59
 */
class TagClassAdapter: BaseBindingAdapter<AdapterTagItemBinding, TagClass>() {

    var selection = 0

    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterTagItemBinding
            = AdapterTagItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterTagItemBinding, position: Int, bean: TagClass) {
        binding.tvName.text = bean.name
        binding.tvName.isSelected = position == selection
        binding.ivRemove.visibility = View.GONE
    }

    override fun onClickItem(v: View, position: Int, bean: TagClass) {
        if (position != selection) {
            super.onClickItem(v, position, bean)
        }
        selection = position
        notifyDataSetChanged()
    }

}