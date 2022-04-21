package com.king.app.coolg_kt.page.star

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarRelationshipsBinding
import com.king.app.gdb.data.relation.StarRelationship

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/20 11:22
 */
class StarRelationshipAdapter:
    BaseBindingAdapter<AdapterStarRelationshipsBinding, StarRelationship>() {

    var selection = -1

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterStarRelationshipsBinding = AdapterStarRelationshipsBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStarRelationshipsBinding,
        position: Int,
        bean: StarRelationship
    ) {
        binding.bean = bean
        binding.tvCount.text = "${bean.count}次"
        if (selection == position) {
            binding.groupItem.setBackgroundColor(Color.parseColor("#F4DBDB"))
        }
        else {
            binding.groupItem.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onClickItem(v: View, position: Int, bean: StarRelationship) {
        selection = if (position == selection) {
            -1
        } else {
            position
        }
        notifyDataSetChanged()
        super.onClickItem(v, position, bean)
    }
}