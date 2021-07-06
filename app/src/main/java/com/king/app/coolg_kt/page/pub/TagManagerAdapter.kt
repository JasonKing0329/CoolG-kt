package com.king.app.coolg_kt.page.pub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTagClassBinding
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.coolg_kt.model.bean.TagGroupItem
import com.king.app.gdb.data.entity.TagClass

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/5 16:00
 */
class TagManagerAdapter() : HeadChildBindingAdapter<AdapterTagClassBinding, AdapterTagItemBinding, TagClass, TagGroupItem>() {

    var onTagClassListener: OnTagClassListener? = null

    var onTagItemListener: OnTagItemListener? = null

    override val itemClass: Class<*>
        get() = TagGroupItem::class.java

    override fun onCreateHeadBind(from: LayoutInflater, parent: ViewGroup): AdapterTagClassBinding = AdapterTagClassBinding.inflate(from, parent, false)

    override fun onCreateItemBind(from: LayoutInflater, parent: ViewGroup): AdapterTagItemBinding = AdapterTagItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterTagClassBinding, position: Int, head: TagClass) {
        binding.tvName.text = head.name
        binding.ivAdd.setOnClickListener { onTagClassListener?.onAddItem(position, head) }
        binding.ivDelete.setOnClickListener { onTagClassListener?.onDeleteItem(position, head) }
        binding.ivEdit.setOnClickListener {
            list?.filterIsInstance<TagGroupItem>()
                ?.filter { it.parent.id == head.id }
                ?.forEach {
                    it.isEditing = !it.isEditing
                    notifyDataSetChanged()
                }
        }
    }

    override fun onBindItem(binding: AdapterTagItemBinding, position: Int, item: TagGroupItem) {
        binding.tvName.text = item.item.name
        binding.ivRemove.visibility = if (item.isEditing) View.VISIBLE else View.GONE
        binding.ivRemove.setOnClickListener { onTagItemListener?.onDeleteItem(position, item) }
    }

    interface OnTagClassListener {
        fun onAddItem(position: Int, bean: TagClass)
        fun onDeleteItem(position: Int, bean: TagClass)
    }

    interface OnTagItemListener {
        fun onDeleteItem(position: Int, item: TagGroupItem)
    }
}