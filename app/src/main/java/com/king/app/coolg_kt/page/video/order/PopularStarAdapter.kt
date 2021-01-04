package com.king.app.coolg_kt.page.video.order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterPopularStarItemBinding
import com.king.app.coolg_kt.model.bean.VideoGuy
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/4 16:01
 */
class PopularStarAdapter: BaseBindingAdapter<AdapterPopularStarItemBinding, VideoGuy>() {

    private var isMultiSelect = false

    fun setMultiSelect(multiSelect: Boolean) {
        isMultiSelect = multiSelect
        list?.forEach {
            it.visibility = if (isMultiSelect) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterPopularStarItemBinding = AdapterPopularStarItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterPopularStarItemBinding, position: Int, bean: VideoGuy) {

        binding.bean = bean
        // 瀑布流必须给item设置具体的宽高，否则会严重错位
        var params = binding.group.layoutParams
        params.height = bean.height
        params.width = bean.width
        binding.group.layoutParams = params
        params = binding.ivCover.layoutParams
        params.height = bean.height
        params.width = bean.width
        binding.ivCover.layoutParams = params

        ImageBindingAdapter.setStarUrl(binding.ivCover, bean.imageUrl)
    }

    override fun onClickItem(v: View, position: Int, bean: VideoGuy) {
        if (isMultiSelect) {
            bean.isChecked = !bean.isChecked
        }
        else {
            super.onClickItem(v, position, bean)
        }
    }
}