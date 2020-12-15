package com.king.app.coolg_kt.page.record

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.gdb.data.entity.Tag

class TagAdapter : BaseBindingAdapter<AdapterTagItemBinding, Tag>() {
    var showDelete = false
    var onDeleteListener: OnDeleteListener? = null
    var selection = -1

    fun toggleDelete() {
        showDelete = !showDelete
    }

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup): AdapterTagItemBinding = AdapterTagItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterTagItemBinding,
        position: Int,
        bean: Tag
    ) {
        binding.tvName.text = bean.name
        binding.ivRemove.visibility = if (showDelete) View.VISIBLE else View.GONE
        binding.ivRemove.setOnClickListener {
            onDeleteListener?.onDelete(position, bean)
        }
        binding.tvName.isSelected = position == selection
    }

    override fun onClickItem(v: View, position: Int, bean: Tag) {
        if (position != selection) {
            super.onClickItem(v, position, bean)
        }
        selection = position
        notifyDataSetChanged()
    }

    interface OnDeleteListener {
        fun onDelete(position: Int, bean: Tag)
    }
}