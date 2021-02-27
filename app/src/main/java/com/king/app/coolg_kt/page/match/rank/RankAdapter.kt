package com.king.app.coolg_kt.page.match.rank

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
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

    var onItemListener: OnItemListener<T>? = null

    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterMatchRankBinding = AdapterMatchRankBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchRankBinding, position: Int, bean: RankItem<T>) {
        binding.bean = bean
        if (bean.canSelect) {
            binding.root.setBackgroundColor(binding.root.context.resources.getColor(R.color.white))
        }
        else  {
            binding.root.setBackgroundColor(binding.root.context.resources.getColor(R.color.darkgrey))
        }
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

        if (bean.unavailableScore == null || bean.unavailableScore == 0) {
            binding.tvScoreNo.visibility = View.GONE
        }
        else {
            binding.tvScoreNo.text = bean.unavailableScore.toString()
            binding.tvScoreNo.visibility = View.VISIBLE
        }
        binding.tvRank.setOnClickListener { onItemListener?.onClickRank(bean) }
        binding.tvChange.setOnClickListener { onItemListener?.onClickRank(bean) }
        binding.ivHead.setOnClickListener { onItemListener?.onClickId(bean) }
        binding.tvScore.setOnClickListener { onItemListener?.onClickScore(bean) }
        binding.tvMatchCount.setOnClickListener { onItemListener?.onClickScore(bean) }

        when {
            bean.change.startsWith("+") -> {
                binding.tvChange.setTextColor(binding.tvChange.resources.getColor(R.color.redC93437))
            }
            bean.change.startsWith("-") -> {
                binding.tvChange.setTextColor(binding.tvChange.resources.getColor(R.color.green34A350))
            }
            else -> {
                binding.tvChange.setTextColor(binding.tvChange.resources.getColor(R.color.text_normal))
            }
        }
    }

    interface OnItemListener<T> {
        fun onClickRank(bean: RankItem<T>)
        fun onClickScore(bean: RankItem<T>)
        fun onClickId(bean: RankItem<T>)
    }
}