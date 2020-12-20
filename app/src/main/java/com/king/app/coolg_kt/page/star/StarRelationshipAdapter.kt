package com.king.app.coolg_kt.page.star

import android.view.LayoutInflater
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
    }
}