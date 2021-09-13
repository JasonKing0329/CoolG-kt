package com.king.app.coolg_kt.page.match.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchRecordCountTitleBinding
import com.king.app.coolg_kt.databinding.AdapterMatchRecordRoundItemBinding
import com.king.app.coolg_kt.page.match.MatchCountTitle
import com.king.app.coolg_kt.page.match.MatchRoundRecord

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/9/13 10:36
 */
class RoundAdapter() : HeadChildBindingAdapter<AdapterMatchRecordCountTitleBinding,
        AdapterMatchRecordRoundItemBinding, MatchCountTitle, MatchRoundRecord>() {

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordCountTitleBinding = AdapterMatchRecordCountTitleBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordRoundItemBinding = AdapterMatchRecordRoundItemBinding.inflate(from, parent, false)

    override fun onBindHead(
        binding: AdapterMatchRecordCountTitleBinding,
        position: Int,
        head: MatchCountTitle
    ) {
        binding.tvCount.text = head.times
    }

    override fun onBindItem(
        binding: AdapterMatchRecordRoundItemBinding,
        position: Int,
        item: MatchRoundRecord
    ) {
        binding.bean = item
    }

    override val itemClass: Class<*>
        get() = MatchRoundRecord::class.java

    fun getSpanSize(position: Int): Int {
        return if (isHead(position)) 2
        else 1
    }
}