package com.king.app.coolg_kt.page.star.random

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterCandidateStarBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/8 15:48
 */
class CandidateAdapter: BaseBindingAdapter<AdapterCandidateStarBinding, StarWrap>() {

    var onDeleteListener: OnDeleteListener? = null

    var isDeleteMode = false

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterCandidateStarBinding = AdapterCandidateStarBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterCandidateStarBinding, position: Int, bean: StarWrap) {
        ImageBindingAdapter.setStarUrl(binding.ivHead, bean.imagePath)
        binding.tvName.text = bean.bean.name
        binding.ivDelete.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        binding.ivDelete.setOnClickListener {
            onDeleteListener?.onDeleteCandidate(position, bean)
        }
    }

    interface OnDeleteListener {
        fun onDeleteCandidate(position: Int, star: StarWrap)
    }
}