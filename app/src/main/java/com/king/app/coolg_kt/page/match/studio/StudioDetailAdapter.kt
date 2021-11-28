package com.king.app.coolg_kt.page.match.studio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchStudioRecordBinding
import com.king.app.coolg_kt.databinding.AdapterMatchStudioTitleBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.StudioItem
import com.king.app.coolg_kt.page.match.StudioTitle

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/24 10:36
 */
class StudioDetailAdapter: HeadChildBindingAdapter<AdapterMatchStudioTitleBinding, AdapterMatchStudioRecordBinding, StudioTitle, StudioItem>() {

    override val itemClass: Class<*>
        get() = StudioItem::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchStudioTitleBinding = AdapterMatchStudioTitleBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchStudioRecordBinding = AdapterMatchStudioRecordBinding.inflate(from, parent, false)

    override fun onBindHead(
        binding: AdapterMatchStudioTitleBinding,
        position: Int,
        head: StudioTitle
    ) {
        binding.tvTitle.text = head.title
        if (head.hasMore) {
            binding.tvMore.visibility = View.VISIBLE
            binding.ivMore.visibility = View.VISIBLE
        }
        else {
            binding.tvMore.visibility = View.GONE
            binding.ivMore.visibility = View.GONE
        }
    }

    override fun onBindItem(
        binding: AdapterMatchStudioRecordBinding,
        position: Int,
        item: StudioItem
    ) {
        val params = binding.ivRecord.layoutParams
        params.height = if (item.column == 1) {
            binding.ivRecord.resources.getDimensionPixelSize(R.dimen.match_studio_record_height)
        }
        else {
            binding.ivRecord.resources.getDimensionPixelSize(R.dimen.img_height_in_full_screen_std)
        }
        binding.ivRecord.layoutParams = params
        binding.tvRank.text = item.currentRank
        binding.tvDetail.text = item.detail
        ImageBindingAdapter.setRecordUrl(binding.ivRecord, item.imageUrl)
    }

    fun getSpanSize(position: Int): Int {
        return if (isHead(position)) {
            3
        }
        else {
            getItem(position)?.column?:1
        }
    }
}