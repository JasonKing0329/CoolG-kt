package com.king.app.coolg_kt.page.star.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarCircleBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.star.OnStarRatingListener
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 15:01
 */
class StarCircleAdapter : BaseBindingAdapter<AdapterStarCircleBinding, StarWrap>() {
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
    ): AdapterStarCircleBinding = AdapterStarCircleBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStarCircleBinding,
        position: Int,
        item: StarWrap
    ) {
        binding.tvName.text = "${item.bean.name} (${item.bean.records})"
        binding.ivHead.visibility = View.VISIBLE
        ImageBindingAdapter.setStarUrl(binding.ivHead, item.imagePath)
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
        list?.let {
            for (i in it.indices) {
                if (it[i].bean.id === starId) {
                    notifyItemChanged(i)
                    break
                }
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