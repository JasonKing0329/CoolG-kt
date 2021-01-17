package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchRecordBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 14:38
 */
class DrawAdapter: BaseBindingAdapter<AdapterMatchRecordBinding, DrawItem>() {

    var onDrawListener: OnDrawListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordBinding = AdapterMatchRecordBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchRecordBinding, position: Int, bean: DrawItem) {
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
        if (type1 == MatchConstants.MATCH_RECORD_BYE) {
            binding.tvBye1.visibility = View.VISIBLE
            binding.tvSeed1.text = ""
            binding.tvRank1.text = ""
            binding.tvQ1.text = ""
        }
        else {
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
        }

        var type2 = bean.matchRecord2?.bean?.type
        if (type2 == MatchConstants.MATCH_RECORD_BYE) {
            binding.tvBye2.visibility = View.VISIBLE
            binding.tvSeed2.text = ""
            binding.tvRank2.text = ""
            binding.tvQ2.text = ""
        }
        else {
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

        binding.ivPlayer1.setOnClickListener { onDrawListener?.onClickPlayer(position, bean, bean.matchRecord1) }
        binding.ivPlayer2.setOnClickListener { onDrawListener?.onClickPlayer(position, bean, bean.matchRecord2) }
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
        fun onClickPlayer(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?)
        fun onPlayerWin(position: Int, drawItem: DrawItem, bean: MatchRecordWrap?)
        fun onClickH2H(position: Int, drawItem: DrawItem)
    }
}