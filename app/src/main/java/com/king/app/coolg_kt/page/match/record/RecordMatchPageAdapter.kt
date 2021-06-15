package com.king.app.coolg_kt.page.match.record

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchRecordMatchItemBinding
import com.king.app.coolg_kt.databinding.AdapterMatchRecordMatchTitleBinding
import com.king.app.coolg_kt.page.match.RecordMatchPageItem
import com.king.app.coolg_kt.page.match.RecordMatchPageTitle

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/15 14:22
 */
class RecordMatchPageAdapter: HeadChildBindingAdapter<AdapterMatchRecordMatchTitleBinding, AdapterMatchRecordMatchItemBinding, RecordMatchPageTitle, RecordMatchPageItem>() {

    override val itemClass: Class<*> get() = RecordMatchPageItem::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordMatchTitleBinding = AdapterMatchRecordMatchTitleBinding.inflate(
        from,
        parent,
        false
    )

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordMatchItemBinding = AdapterMatchRecordMatchItemBinding.inflate(
        from,
        parent,
        false
    )

    override fun onBindHead(
        binding: AdapterMatchRecordMatchTitleBinding,
        position: Int,
        head: RecordMatchPageTitle
    ) {
        binding.tvTitle.text = head.period
        binding.ivCup.visibility = if (head.isChampion) View.VISIBLE else View.GONE
        binding.tvRank.text = head.rankSeed
    }

    override fun onBindItem(
        binding: AdapterMatchRecordMatchItemBinding,
        position: Int,
        item: RecordMatchPageItem
    ) {
        binding.bean = item
        val drawable = binding.tvRound.background as GradientDrawable
        if (item.isChampion) {
            drawable.setColor(binding.tvRound.resources.getColor(R.color.match_timeline_champion))
        } else {
            drawable.setColor(binding.tvRound.resources.getColor(R.color.match_timeline))
        }
        binding.tvRound.background = drawable
        binding.groupCard.setOnClickListener { onItemClickListener?.onClickItem(it, position, item) }
    }
}