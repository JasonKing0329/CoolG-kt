package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.base.adapter.BindingHolder
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchFinalHeadBinding
import com.king.app.coolg_kt.databinding.AdapterMatchFinalRoundBinding
import com.king.app.coolg_kt.databinding.AdapterMatchFinalScoreBinding
import com.king.app.coolg_kt.databinding.AdapterMatchRecordBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.FinalHead
import com.king.app.coolg_kt.page.match.FinalRound
import com.king.app.coolg_kt.page.match.FinalScore
import com.king.app.gdb.data.entity.Record
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/19 16:02
 */
class FinalDrawAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TYPE_HEAD = 0
    val TYPE_SCORE = 1
    val TYPE_ROUND = 2
    val TYPE_ITEM = 3
    var list: List<Any> = listOf()

    var onDrawListener: OnDrawListener? = null

    fun getSpanSize(position: Int): Int {
        return when(getItemViewType(position)) {
            TYPE_SCORE -> 1
            else -> 2
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(list!![position].javaClass) {
            FinalHead::class.java -> TYPE_HEAD
            FinalRound::class.java -> TYPE_ROUND
            FinalScore::class.java -> TYPE_SCORE
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_HEAD -> {
                val binding = AdapterMatchFinalHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                holder
            }
            TYPE_ROUND -> {
                val binding = AdapterMatchFinalRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                holder
            }
            TYPE_SCORE -> {
                val binding = AdapterMatchFinalScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                holder
            }
            else -> {
                val binding = AdapterMatchRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
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
                val binding = DataBindingUtil.getBinding<AdapterMatchFinalHeadBinding>(holder.itemView)!!
                onBindHead(binding, position, list!![position] as FinalHead)
                binding.executePendingBindings()
            }
            TYPE_ROUND -> {
                val binding = DataBindingUtil.getBinding<AdapterMatchFinalRoundBinding>(holder.itemView)!!
                onBindRound(binding, position, list!![position] as FinalRound)
                binding.executePendingBindings()
            }
            TYPE_SCORE-> {
                val binding = DataBindingUtil.getBinding<AdapterMatchFinalScoreBinding>(holder.itemView)!!
                onBindScore(binding, position, list!![position] as FinalScore)
                binding.executePendingBindings()
            }
            else -> {
                val binding = DataBindingUtil.getBinding<AdapterMatchRecordBinding>(holder.itemView)!!
                onBindItem(binding, position, list!![position] as DrawItem)
                binding.executePendingBindings()
            }
        }
    }

    private fun onBindHead(binding: AdapterMatchFinalHeadBinding, position: Int, bean: FinalHead) {
        if (bean.groupAList.size == 5) {
            binding.tvSeed1A.text = bean.groupAList[0].rank.toString()
            binding.tvSeed2A.text = bean.groupAList[1].rank.toString()
            binding.tvSeed3A.text = bean.groupAList[2].rank.toString()
            binding.tvSeed4A.text = bean.groupAList[3].rank.toString()
            binding.tvSeed5A.text = bean.groupAList[4].rank.toString()
            ImageBindingAdapter.setRecordUrl(binding.ivTop1A, bean.groupAList[0].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop2A, bean.groupAList[1].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop3A, bean.groupAList[2].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop4A, bean.groupAList[3].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop5A, bean.groupAList[4].record.imageUrl)
            binding.ivTop1A.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupAList[0].record.bean) }
            binding.ivTop2A.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupAList[1].record.bean) }
            binding.ivTop3A.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupAList[2].record.bean) }
            binding.ivTop4A.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupAList[3].record.bean) }
            binding.ivTop5A.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupAList[4].record.bean) }
        }
        if (bean.groupBList.size == 5) {
            binding.tvSeed1B.text = bean.groupBList[0].rank.toString()
            binding.tvSeed2B.text = bean.groupBList[1].rank.toString()
            binding.tvSeed3B.text = bean.groupBList[2].rank.toString()
            binding.tvSeed4B.text = bean.groupBList[3].rank.toString()
            binding.tvSeed5B.text = bean.groupBList[4].rank.toString()
            ImageBindingAdapter.setRecordUrl(binding.ivTop1B, bean.groupBList[0].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop2B, bean.groupBList[1].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop3B, bean.groupBList[2].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop4B, bean.groupBList[3].record.imageUrl)
            ImageBindingAdapter.setRecordUrl(binding.ivTop5B, bean.groupBList[4].record.imageUrl)
            binding.ivTop1B.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupBList[0].record.bean) }
            binding.ivTop2B.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupBList[1].record.bean) }
            binding.ivTop3B.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupBList[2].record.bean) }
            binding.ivTop4B.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupBList[3].record.bean) }
            binding.ivTop5B.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.groupBList[4].record.bean) }
        }
    }

    private fun onBindRound(binding: AdapterMatchFinalRoundBinding, position: Int, bean: FinalRound) {
        binding.tvRound.text = bean.round
    }

    private fun onBindScore(binding: AdapterMatchFinalScoreBinding, position: Int, bean: FinalScore) {
        binding.tvRank.text = bean.rank
        binding.tvResult.text = "${bean.win}胜${bean.lose}负"
        binding.ivRecord.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.record.bean) }
        ImageBindingAdapter.setRecordUrl(binding.ivRecord, bean.record.imageUrl)
    }

    private fun onBindItem(binding: AdapterMatchRecordBinding, position: Int, bean: DrawItem) {
        var firstIndex = bean.matchItem.order * 2
        var seed1 = 0
        bean.matchRecord1?.bean?.recordSeed?.let { seed1 = it }
        var seed2 = 0
        bean.matchRecord2?.bean?.recordSeed?.let { seed2 = it }
        var seedWinner = 0
        bean.winner?.bean?.recordSeed?.let { seedWinner = it }
        binding.tvIndex1.text = (firstIndex + 1).toString()
        binding.tvIndex2.text = (firstIndex + 2).toString()

        var type1 = bean.matchRecord1?.bean?.type
        binding.tvBye1.visibility = View.GONE
        binding.tvSeed1.text = if (seed1 > 0) { "[$seed1]" } else { "" }
        binding.tvRank1.text = "R ${bean.matchRecord1?.bean?.recordRank}"
        binding.tvQ1.text = when(type1) {
            MatchConstants.MATCH_RECORD_QUALIFY -> "[Q]"
            MatchConstants.MATCH_RECORD_WILDCARD -> "[WC]"
            else -> ""
        }
        binding.tvQ1.visibility = when(type1) {
            MatchConstants.MATCH_RECORD_QUALIFY, MatchConstants.MATCH_RECORD_WILDCARD -> View.VISIBLE
            else -> View.GONE
        }

        var type2 = bean.matchRecord2?.bean?.type
        binding.tvBye2.visibility = View.GONE
        binding.tvSeed2.text = if (seed2 > 0) { "[$seed2]" } else { "" }
        binding.tvRank2.text = "R ${bean.matchRecord2?.bean?.recordRank}"
        binding.tvQ2.text = when(type2) {
            MatchConstants.MATCH_RECORD_QUALIFY -> "[Q]"
            MatchConstants.MATCH_RECORD_WILDCARD -> "[WC]"
            else -> ""
        }
        binding.tvQ2.visibility = when(type2) {
            MatchConstants.MATCH_RECORD_QUALIFY, MatchConstants.MATCH_RECORD_WILDCARD -> View.VISIBLE
            else -> View.GONE
        }
        var typeWinner = bean.winner?.bean?.type
        binding.tvSeedWinner.text = if (seedWinner > 0) { "[$seedWinner]" } else { "" }
        binding.tvRankWinner.text = "R ${bean.winner?.bean?.recordRank}"
        binding.tvQWinner.text = when(typeWinner) {
            MatchConstants.MATCH_RECORD_QUALIFY -> "[Q]"
            MatchConstants.MATCH_RECORD_WILDCARD -> "[WC]"
            else -> ""
        }
        binding.tvQWinner.visibility = when(typeWinner) {
            MatchConstants.MATCH_RECORD_QUALIFY, MatchConstants.MATCH_RECORD_WILDCARD -> View.VISIBLE
            else -> View.GONE
        }

        ImageBindingAdapter.setRecordUrl(binding.ivPlayer1, bean.matchRecord1?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivPlayer2, bean.matchRecord2?.imageUrl)
        ImageBindingAdapter.setRecordUrl(binding.ivWinner, bean.winner?.imageUrl)

        binding.ivPlayer1.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.matchRecord1?.record) }
        binding.ivPlayer2.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.matchRecord2?.record) }
        binding.ivWinner.setOnClickListener { onDrawListener?.onClickPlayer(position, bean.winner?.record) }
        binding.ivPlayer1.setOnLongClickListener {
            onDrawListener?.onPlayerWin(position, bean, bean.matchRecord1)
            true
        }
        binding.ivPlayer2.setOnLongClickListener {
            onDrawListener?.onPlayerWin(position, bean, bean.matchRecord2)
            true
        }
        binding.tvH2h.setOnClickListener { onDrawListener?.onClickH2H(position, bean) }
    }

    interface OnDrawListener {
        fun onClickPlayer(position: Int, bean: Record?)
        fun onPlayerWin(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?)
        fun onClickH2H(position: Int, drawItem: DrawItem)
    }
}