package com.king.app.coolg_kt.page.video

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterPlaylistItemBinding
import com.king.app.coolg_kt.model.bean.PlayList

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/17 22:08
 */
class PlayListAdapter: BaseBindingAdapter<AdapterPlaylistItemBinding, PlayList.PlayItem>() {

    var mPlayIndex = 0

    var onDeleteListener: OnDeleteListener? = null
    var enableDelete = false

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterPlaylistItemBinding = AdapterPlaylistItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterPlaylistItemBinding,
        position: Int,
        bean: PlayList.PlayItem
    ) {
        binding.bean = bean
        if (position == mPlayIndex) {
            binding.root.setBackgroundColor(
                binding.root.context.resources.getColor(R.color.playlist_bg_focus)
            )
        } else {
            binding.root.setBackgroundColor(Color.TRANSPARENT)
        }
        binding.ivDelete.visibility = if (enableDelete) View.VISIBLE else View.GONE
        binding.ivDelete.setOnClickListener { v -> onDeleteListener?.onDelete(position, bean) }

    }

    interface OnDeleteListener {
        fun onDelete(position: Int, bean: PlayList.PlayItem)
    }
}