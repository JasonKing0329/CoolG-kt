package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchMajorRankBinding
import com.king.app.coolg_kt.databinding.AdapterMatchMajorRoundBinding
import com.king.app.coolg_kt.page.match.MajorRank
import com.king.app.coolg_kt.page.match.MajorRound

class MajorAdapter: HeadChildBindingAdapter<AdapterMatchMajorRankBinding, AdapterMatchMajorRoundBinding, MajorRank, MajorRound>() {
    override val itemClass: Class<*>
        get() = MajorRound::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchMajorRankBinding = AdapterMatchMajorRankBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchMajorRoundBinding = AdapterMatchMajorRoundBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterMatchMajorRankBinding, position: Int, head: MajorRank) {
        binding.bean = head
    }

    override fun onBindItem(
        binding: AdapterMatchMajorRoundBinding,
        position: Int,
        item: MajorRound
    ) {
        binding.bean = item
    }
}