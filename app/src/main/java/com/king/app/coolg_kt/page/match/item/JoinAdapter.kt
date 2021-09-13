package com.king.app.coolg_kt.page.match.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchRecordCountItemBinding
import com.king.app.coolg_kt.databinding.AdapterMatchRecordCountTitleBinding
import com.king.app.coolg_kt.page.match.MatchCountRecord
import com.king.app.coolg_kt.page.match.MatchCountTitle

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/9/13 10:36
 */
class JoinAdapter() : HeadChildBindingAdapter<AdapterMatchRecordCountTitleBinding,
        AdapterMatchRecordCountItemBinding, MatchCountTitle, MatchCountRecord>() {

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordCountTitleBinding = AdapterMatchRecordCountTitleBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordCountItemBinding = AdapterMatchRecordCountItemBinding.inflate(from, parent, false)

    override fun onBindHead(
        binding: AdapterMatchRecordCountTitleBinding,
        position: Int,
        head: MatchCountTitle
    ) {
        binding.tvCount.text = head.times
    }

    override fun onBindItem(
        binding: AdapterMatchRecordCountItemBinding,
        position: Int,
        item: MatchCountRecord
    ) {
        binding.bean = item
    }

    override val itemClass: Class<*>
        get() = MatchCountRecord::class.java
}