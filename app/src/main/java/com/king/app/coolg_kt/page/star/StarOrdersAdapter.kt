package com.king.app.coolg_kt.page.star

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarOrdersBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.gdb.data.entity.FavorStarOrder

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/20 11:26
 */
class StarOrdersAdapter: BaseBindingAdapter<AdapterStarOrdersBinding, FavorStarOrder>() {

    var deleteMode = false

    var onDeleteListener: OnDeleteListener? = null

    init {
        setOnItemLongClickListener(object : OnItemLongClickListener<FavorStarOrder> {
            override fun onLongClickItem(view: View, position: Int, data: FavorStarOrder) {
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
        bean: FavorStarOrder
    ) {
        binding.tvName.text = bean.name
        ImageBindingAdapter.setStarUrl(binding.ivHead, bean.coverUrl)
        if (deleteMode) {
            binding.ivDelete.visibility = View.VISIBLE
            binding.ivDelete.setOnClickListener {
                onDeleteListener?.onDeleteOrder(bean)
            }
        } else {
            binding.ivDelete.visibility = View.GONE
            binding.ivDelete.setOnClickListener(null)
        }
    }

    interface OnDeleteListener {
        fun onDeleteOrder(order: FavorStarOrder)
    }
}