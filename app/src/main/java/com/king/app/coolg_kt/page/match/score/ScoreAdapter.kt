package com.king.app.coolg_kt.page.match.score

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.base.adapter.BindingHolder
import com.king.app.coolg_kt.databinding.*
import com.king.app.coolg_kt.page.match.ScoreBean
import com.king.app.coolg_kt.page.match.ScoreHead
import com.king.app.coolg_kt.page.match.ScoreTitle

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/31 11:58
 */
class ScoreAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEAD = 0
    private val TYPE_LEVEL = 1
    private val TYPE_ITEM = 2

    var list: List<Any>? = null
    var onPageListener: OnPageListener? = null

    override fun getItemViewType(position: Int): Int {
        return when(list!![position].javaClass) {
            ScoreHead::class.java -> TYPE_HEAD
            ScoreTitle::class.java -> TYPE_LEVEL
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_HEAD -> {
                val binding = AdapterScoreHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                holder
            }
            TYPE_LEVEL -> {
                val binding = AdapterScoreTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                holder
            }
            else -> {
                val binding = AdapterScoreItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener {
                    onClickItem(binding.root, holder.layoutPosition, list!![holder.layoutPosition] as ScoreBean)
                }
                holder
            }
        }
    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEAD -> {
                val binding = DataBindingUtil.getBinding<AdapterScoreHeadBinding>(holder.itemView)!!
                onBindHead(binding, position, list!![position] as ScoreHead)
                binding.executePendingBindings()
            }
            TYPE_LEVEL -> {
                val binding = DataBindingUtil.getBinding<AdapterScoreTitleBinding>(holder.itemView)!!
                onBindLevel(binding, position, list!![position] as ScoreTitle)
                binding.executePendingBindings()
            }
            else -> {
                val binding = DataBindingUtil.getBinding<AdapterScoreItemBinding>(holder.itemView)!!
                onBindItem(binding, position, list!![position] as ScoreBean)
                binding.executePendingBindings()
            }
        }
    }

    private fun onBindHead(binding: AdapterScoreHeadBinding, position: Int, scoreHead: ScoreHead) {
        binding.bean = scoreHead
        binding.ivHead.setOnClickListener { onPageListener?.onClickRecord(scoreHead.recordId) }
        binding.groupRankHigh.setOnClickListener { onPageListener?.onClickRank(scoreHead.recordId) }
        binding.groupRankLow.setOnClickListener { onPageListener?.onClickRank(scoreHead.recordId) }
    }

    private fun onBindLevel(binding: AdapterScoreTitleBinding, position: Int, head: ScoreTitle) {
        binding.tvTitle.text = "${head.name} "
        binding.tvTitle.setTextColor(head.color)
        binding.underline.setBackgroundColor(head.color)
    }

    private fun onBindItem(binding: AdapterScoreItemBinding, position: Int, item: ScoreBean) {
        binding.tvName.text = item.name
        binding.tvRound.text = item.round
        binding.tvScore.text = item.score.toString()
        binding.ivWinner.visibility = if (item.isChampion) View.VISIBLE else View.GONE
        binding.tvComplete.visibility = if (item.isCompleted) View.VISIBLE else View.GONE
        binding.tvNo.visibility = if (item.isNotCount) View.VISIBLE else View.GONE
        binding.tvWeek.text = "W${item.matchPeriod.orderInPeriod}"
    }

    private fun onClickItem(root: View, position: Int, scoreBean: ScoreBean) {
        onPageListener?.onClickScore(position, scoreBean)
    }

    interface OnPageListener {
        fun onClickRank(recordId: Long)
        fun onClickRecord(recordId: Long)
        fun onClickScore(position: Int, scoreBean: ScoreBean)
    }
}