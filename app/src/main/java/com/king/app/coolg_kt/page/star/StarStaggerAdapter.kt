package com.king.app.coolg_kt.page.star

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarStaggerBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/7/30 10:01
 */
class StarStaggerAdapter : BaseBindingAdapter<AdapterStarStaggerBinding, StarWrap>() {
    var onStarRatingListener: OnStarRatingListener? = null
    private var selectionMode = false
    var mCheckMap: MutableMap<Long, Boolean> = mutableMapOf()

    fun setSelectionMode(selectionMode: Boolean) {
        this.selectionMode = selectionMode
        if (selectionMode) {
            mCheckMap.clear()
        }
    }

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterStarStaggerBinding = AdapterStarStaggerBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterStarStaggerBinding, position: Int, item: StarWrap) {

        // 瀑布流必须给item设置具体的宽高，否则会严重错位
        var params = binding.group.layoutParams
        params.height = item.height!!
        params.width = item.width!!
        binding.group.layoutParams = params
        params = binding.ivStar.layoutParams
        params.height = item.height!!
        params.width = item.width!!
        binding.ivStar.layoutParams = params
        binding.tvName.text = item.bean.name
        binding.tvVideos.text = "${item.bean.records} Videos"
        binding.tvSeq.text = (position + 1).toString()
        ImageBindingAdapter.setStarUrl(binding.ivStar, item.imagePath)
        if (item.rating == null) {
            binding.tvRating.text = StarRatingUtil.NON_RATING
            StarRatingUtil.updateRatingColor(binding.tvRating, null)
        } else {
            binding.tvRating.text = StarRatingUtil.getRatingValue(item.rating!!.complex)
            StarRatingUtil.updateRatingColor(binding.tvRating, item.rating!!)
        }
        binding.tvRating.tag = item
        binding.tvRating.setOnClickListener {
            onStarRatingListener?.onUpdateRating(position, item.bean.id!!)
        }
        if (selectionMode) {
            binding.cbCheck.visibility = View.VISIBLE
            binding.cbCheck.isChecked = mCheckMap[item.bean.id!!] != null
        } else {
            binding.cbCheck.visibility = View.GONE
        }
    }

    fun notifyStarChanged(starId: Long) {
        list?.forEachIndexed { index, starWrap ->
            if (starWrap.bean.id == starId) {
                notifyItemChanged(index)
                return
            }
        }
    }

    override fun onClickItem(v: View, position: Int, bean: StarWrap) {
        if (selectionMode) {
            val key: Long = bean.bean.id!!
            if (mCheckMap[key] == null) {
                mCheckMap[key] = true
            } else {
                mCheckMap.remove(key)
            }
            notifyItemChanged(position)
        } else {
            super.onClickItem(v, position, bean)
        }
    }
}