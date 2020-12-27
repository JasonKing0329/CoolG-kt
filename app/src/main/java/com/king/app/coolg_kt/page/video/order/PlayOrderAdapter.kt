package com.king.app.coolg_kt.page.video.order

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.AdapterPlayOrderItemBinding
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/27 11:30
 */
class PlayOrderAdapter: BaseBindingAdapter<AdapterPlayOrderItemBinding, VideoPlayList>() {

    private var isMultiSelect = false

    var mViewType: Int = if (ScreenUtils.isTablet()) AppConstants.VIEW_TYPE_GRID_TAB
    else SettingProperty.getVideoPlayOrderViewType()

    fun setMultiSelect(multiSelect: Boolean) {
        isMultiSelect = multiSelect
        list?.forEach {
            it.visibility = if (multiSelect) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterPlayOrderItemBinding = AdapterPlayOrderItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterPlayOrderItemBinding,
        position: Int,
        bean: VideoPlayList
    ) {
        binding.bean = bean
        var coverParam = binding.cover.layoutParams
        val tvParam = binding.tvVideos.layoutParams as MarginLayoutParams
        when (mViewType) {
            AppConstants.VIEW_TYPE_LIST -> {
                coverParam.height = binding.cover.resources.getDimensionPixelSize(R.dimen.play_order_height_cell2)
                binding.cover.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24f)
                binding.tvVideos.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                tvParam.marginEnd = ScreenUtils.dp2px(16f)
                tvParam.bottomMargin = ScreenUtils.dp2px(16f)
            }
            AppConstants.VIEW_TYPE_GRID -> {
                coverParam.height = binding.cover.resources.getDimensionPixelSize(R.dimen.play_order_height_cell1)
                binding.cover.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
                binding.tvVideos.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
                tvParam.marginEnd = ScreenUtils.dp2px(8f)
                tvParam.bottomMargin = ScreenUtils.dp2px(8f)
            }
            else -> {
                coverParam.height = binding.cover.resources.getDimensionPixelSize(R.dimen.play_order_height_cell1)
                binding.cover.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24f)
                binding.tvVideos.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                tvParam.marginEnd = ScreenUtils.dp2px(16f)
                tvParam.bottomMargin = ScreenUtils.dp2px(16f)
            }
        }
        binding.cover.layoutParams = coverParam
        binding.tvVideos.layoutParams = tvParam
    }

    override fun onClickItem(v: View, position: Int, bean: VideoPlayList) {
        if (isMultiSelect) {
            bean.isChecked = !bean.isChecked
        } else {
            super.onClickItem(v, position, bean)
        }
    }
}