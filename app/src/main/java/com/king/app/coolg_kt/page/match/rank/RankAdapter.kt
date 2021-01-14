package com.king.app.coolg_kt.page.match.rank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchRankBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.gdb.data.entity.Star

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/14 16:31
 */
class RankAdapter<T>: BaseBindingAdapter<AdapterMatchRankBinding, RankItem<T>>() {

    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterMatchRankBinding = AdapterMatchRankBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchRankBinding, position: Int, bean: RankItem<T>) {
        binding.bean = bean
        if (bean.bean is Star) {
            binding.ivHead.visibility = View.GONE
            binding.tvName.visibility = View.VISIBLE
//            ImageBindingAdapter.setStarUrl(binding.ivHead, bean.imageUrl)
        }
        else {
            binding.ivHead.visibility = View.VISIBLE
            binding.tvName.visibility = View.GONE
            ImageBindingAdapter.setRecordUrl(binding.ivHead, bean.imageUrl)
        }
        binding.tvChange.visibility = if (bean.change.isEmpty()) View.GONE else View.VISIBLE
    }
}