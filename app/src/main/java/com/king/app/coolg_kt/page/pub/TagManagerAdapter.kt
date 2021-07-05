package com.king.app.coolg_kt.page.pub

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTagClassBinding
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagClass

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/5 16:00
 */
class TagManagerAdapter() : HeadChildBindingAdapter<AdapterTagClassBinding, AdapterTagItemBinding, TagClass, Tag>() {
    override val itemClass: Class<*>
        get() = Tag::class.java

    override fun onCreateHeadBind(from: LayoutInflater, parent: ViewGroup): AdapterTagClassBinding = AdapterTagClassBinding.inflate(from, parent, false)

    override fun onCreateItemBind(from: LayoutInflater, parent: ViewGroup): AdapterTagItemBinding = AdapterTagItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterTagClassBinding, position: Int, head: TagClass) {
        binding.tvName.text = head.name
    }

    override fun onBindItem(binding: AdapterTagItemBinding, position: Int, item: Tag) {
        binding.tvName.text = item.name
    }

}