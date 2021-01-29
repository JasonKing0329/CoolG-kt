package com.king.app.coolg_kt.page.record.pad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordGalleryBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 13:28
 */
class RecordGalleryAdapter :
    BaseBindingAdapter<AdapterRecordGalleryBinding, String>() {
    private var selection = -1

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordGalleryBinding = AdapterRecordGalleryBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordGalleryBinding,
        position: Int,
        bean: String
    ) {
        ImageBindingAdapter.setRecordUrl(binding.ivItem, bean)
        binding.groupItem.isSelected = position == selection
    }

    fun updateSelection(selection: Int) {
        if (selection != this.selection) {
            val lastSelection = this.selection
            this.selection = selection
            if (lastSelection != -1) {
                notifyItemChanged(lastSelection)
            }
            notifyItemChanged(selection)
        }
    }

    override fun onClickItem(v: View, position: Int, bean: String) {
        if (selection != position) {
            updateSelection(position)
            super.onClickItem(v, position, bean)
        }
    }
}