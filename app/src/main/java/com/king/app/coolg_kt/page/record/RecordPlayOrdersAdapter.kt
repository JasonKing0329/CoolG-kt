package com.king.app.coolg_kt.page.record

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordPlayOrderBinding
import com.king.app.coolg_kt.model.bean.VideoPlayList

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 16:25
 */
class RecordPlayOrdersAdapter: BaseBindingAdapter<AdapterRecordPlayOrderBinding, VideoPlayList>() {

    var deleteMode = false

    var onDeleteListener: OnDeleteListener? = null

    init {
        setOnItemLongClickListener(object : OnItemLongClickListener<VideoPlayList> {
            override fun onLongClickItem(view: View, position: Int, data: VideoPlayList) {
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
    ): AdapterRecordPlayOrderBinding = AdapterRecordPlayOrderBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordPlayOrderBinding,
        position: Int,
        bean: VideoPlayList
    ) {
        binding.bean = bean
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
        fun onDeleteOrder(order: VideoPlayList)
    }
}