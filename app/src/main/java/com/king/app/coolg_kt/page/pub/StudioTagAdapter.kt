package com.king.app.coolg_kt.page.pub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.gdb.data.entity.FavorRecordOrder

class StudioTagAdapter : BaseBindingAdapter<AdapterTagItemBinding, FavorRecordOrder>() {
    var selection = 0

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup): AdapterTagItemBinding = AdapterTagItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterTagItemBinding,
        position: Int,
        bean: FavorRecordOrder
    ) {
        binding.tvName.text = bean.name
        binding.ivRemove.visibility = View.GONE
        binding.tvName.isSelected = position == selection
    }

    override fun onClickItem(v: View, position: Int, bean: FavorRecordOrder) {
        if (position != selection) {
            super.onClickItem(v, position, bean)
        }
        selection = position
        notifyDataSetChanged()
    }
}