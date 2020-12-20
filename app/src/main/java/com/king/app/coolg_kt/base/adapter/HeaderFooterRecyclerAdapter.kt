package com.king.app.coolg_kt.base.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Administrator on 2017/5/20 0020.
 * 封装带header和footer的adapter
 * 处理position
 */
abstract class HeaderFooterRecyclerAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_HEAD = 0
    private val TYPE_ITEM = 1
    private val TYPE_MORE = 2

    var list: List<T>? = null

    override fun getItemViewType(position: Int): Int {
        var type = TYPE_ITEM
        if (position == 0) {
            type = TYPE_HEAD
        }
        if (position == itemCount - 1) {
            type = TYPE_MORE
        }
        return type
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            onCreateItem(parent)
        } else if (viewType == TYPE_HEAD) {
            onCreateHeader(parent)
        } else {
            onCreateFooter(parent)
        }
    }

    protected abstract fun onCreateHeader(parent: ViewGroup): RecyclerView.ViewHolder
    protected abstract fun onCreateFooter(parent: ViewGroup): RecyclerView.ViewHolder
    protected abstract fun onCreateItem(parent: ViewGroup): RecyclerView.ViewHolder
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (position) {
            0 -> {
                onBindHeaderView(holder)
            }
            itemCount - 1 -> {
                onBindFooterView(holder)
            }
            else -> {
                // position 按照正常的以0开始
                onBindItemView(holder, position - 1)
            }
        }
    }

    protected abstract fun onBindHeaderView(holder: RecyclerView.ViewHolder)
    protected abstract fun onBindFooterView(holder: RecyclerView.ViewHolder)
    protected abstract fun onBindItemView(holder: RecyclerView.ViewHolder, position: Int)

    override fun getItemCount(): Int {
        return if (list == null) 2 else list!!.size + 2 // 首尾分别为header和footer
    }
}