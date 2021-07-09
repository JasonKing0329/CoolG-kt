package com.king.app.coolg_kt.page.match.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchChampionWallItemBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.WallItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/9 15:58
 */
class WallAdapter: BaseBindingAdapter<AdapterMatchChampionWallItemBinding, WallItem>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchChampionWallItemBinding = AdapterMatchChampionWallItemBinding.inflate(inflater)

    override fun onBindItem(
        binding: AdapterMatchChampionWallItemBinding,
        position: Int,
        bean: WallItem
    ) {
        if (bean.isTitle) {
            binding.tvTitle.visibility = View.VISIBLE
            binding.ivImage.visibility = View.GONE
            binding.tvTitle.text = bean.text
        }
        else {
            binding.tvTitle.visibility = View.GONE
            binding.ivImage.visibility = View.VISIBLE
            ImageBindingAdapter.setRecordUrl(binding.ivImage, bean.imageUrl)
        }
    }

    fun getColumn(type: Int): Int {
        // gm1000 10*2 + 1
        if (type == 1) {
            return 21;
        }
        // gs 4*2 + 1
        return 9
    }

    fun getSpanSize(position: Int): Int {
        return if (getItem(position).isTitle) {
            1
        } else {
            2
        }
    }

    fun getTitleSpanSize(position: Int): Int {
        return if (position == 0) {
            1
        } else {
            2
        }
    }
}