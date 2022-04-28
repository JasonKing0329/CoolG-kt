package com.king.app.coolg_kt.page.pub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.coolg_kt.databinding.AdapterTagItemStarlistBinding
import com.king.app.coolg_kt.model.bean.StudioStarWrap
import com.king.app.gdb.data.entity.FavorRecordOrder

class StudioTagAdapter : BaseBindingAdapter<AdapterTagItemStarlistBinding, StudioStarWrap>() {
    var selection = 0

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup): AdapterTagItemStarlistBinding = AdapterTagItemStarlistBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterTagItemStarlistBinding,
        position: Int,
        bean: StudioStarWrap
    ) {
        binding.tvName.text = bean.text
        binding.tvCount.text = bean.starCount.toString()
        binding.tvName.isSelected = position == selection
        binding.tvCount.isSelected = position == selection
        binding.group.isSelected = position == selection
    }

    override fun onClickItem(v: View, position: Int, bean: StudioStarWrap) {
        if (position != selection) {
            super.onClickItem(v, position, bean)
        }
        selection = position
        notifyDataSetChanged()
    }
}