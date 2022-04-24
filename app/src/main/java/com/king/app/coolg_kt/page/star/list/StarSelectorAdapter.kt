package com.king.app.coolg_kt.page.star.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarSelectorBinding
import com.king.app.coolg_kt.model.bean.SelectStar
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.entity.StarRating

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 16:28
 */
class StarSelectorAdapter: BaseBindingAdapter<AdapterStarSelectorBinding, SelectStar>() {

    var checkMap = mutableMapOf<Long, Boolean>()

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterStarSelectorBinding = AdapterStarSelectorBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterStarSelectorBinding, position: Int, bean: SelectStar) {
        binding.bean = bean
        binding.tvIndex.text = (position + 1).toString()
        if (bean.star!!.rating != null) {
            val rating: StarRating = bean.star!!.rating!!
            binding.tvRating.text = StarRatingUtil.getRatingValue(rating.complex)
            StarRatingUtil.updateRatingColor(binding.tvRating, rating)
        } else {
            binding.tvRating.text = StarRatingUtil.NON_RATING
            StarRatingUtil.updateRatingColor(binding.tvRating, null)
        }
        binding.cbCheck.isChecked = isChecked(bean.star?.bean?.id)
    }

    private fun isChecked(starId: Long?): Boolean {
        return if (starId == null) {
            false
        }
        else {
            checkMap[starId] == true
        }
    }

    override fun onClickItem(v: View, position: Int, bean: SelectStar) {
        bean.observer?.onSelect(bean)
    }
}