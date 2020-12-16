package com.king.app.coolg_kt.page.record

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarOrdersBinding
import com.king.app.gdb.data.entity.FavorRecordOrder

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 16:17
 */
class RecordOrdersAdapter: BaseBindingAdapter<AdapterStarOrdersBinding, FavorRecordOrder>() {

    var deleteMode = false

    var onDeleteListener: OnDeleteListener? = null

    var mTextColor: Int? = null

    init {
        setOnItemLongClickListener(object : OnItemLongClickListener<FavorRecordOrder> {
            override fun onLongClickItem(view: View, position: Int, data: FavorRecordOrder) {
                toggleDeleteMode()
                notifyDataSetChanged()
            }
        })
    }

    fun toggleDeleteMode() {
        deleteMode = !deleteMode
    }

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterStarOrdersBinding = AdapterStarOrdersBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStarOrdersBinding,
        position: Int,
        bean: FavorRecordOrder
    ) {
        binding.bean = bean
        mTextColor?.let {
            binding.tvName.setTextColor(it)
        }
        if (deleteMode) {
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivDelete.setOnClickListener { v ->
                onDeleteListener?.onDeleteOrder(bean)
            }
        } else {
            binding.ivDelete.visibility = View.GONE
            binding.ivDelete.setOnClickListener(null)
        }
    }

    interface OnDeleteListener {
        fun onDeleteOrder(order: FavorRecordOrder)
    }
}