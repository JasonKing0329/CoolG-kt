package com.king.app.coolg_kt.page.match.titles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchTitlesGroupBinding
import com.king.app.coolg_kt.databinding.AdapterMatchTitlesItemBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.TitleCountItem

/**
 * @description:
 * @author：Jing
 * @date: 2021/5/16 11:26
 */
class TitlesCountAdapter:
    HeadChildBindingAdapter<AdapterMatchTitlesGroupBinding, AdapterMatchTitlesItemBinding, String, TitleCountItem>() {

    override val itemClass: Class<*>
        get() = TitleCountItem::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchTitlesGroupBinding = AdapterMatchTitlesGroupBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchTitlesItemBinding = AdapterMatchTitlesItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterMatchTitlesGroupBinding, position: Int, head: String) {
        binding.tvName.text = head
    }

    override fun onBindItem(
        binding: AdapterMatchTitlesItemBinding,
        position: Int,
        item: TitleCountItem
    ) {
        binding.tvRank.text = " R ${item.rank} "
        binding.tvName.text = item.record.name
        if (item.details == null || item.details!!.isEmpty()) {
            binding.tvDetails.visibility = View.GONE
        }
        else {
            binding.tvDetails.text = item.details
            binding.tvDetails.visibility = View.VISIBLE
        }
        ImageBindingAdapter.setRecordUrl(binding.ivRecord, item.imageUrl)
    }

    fun getSpanSize(position: Int): Int {
        return if (isHead(position)) {
            2
        } else {
            var item = list!![position] as TitleCountItem
            if (item.isOnlyOne) {
                2
            }
            else {
                1
            }
        }
    }
}