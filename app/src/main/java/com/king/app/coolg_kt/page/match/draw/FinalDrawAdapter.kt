package com.king.app.coolg_kt.page.match.draw

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.base.adapter.BindingHolder
import com.king.app.coolg_kt.databinding.AdapterMatchFinalHeadBinding
import com.king.app.coolg_kt.databinding.AdapterMatchFinalRoundBinding
import com.king.app.coolg_kt.databinding.AdapterMatchFinalScoreBinding
import com.king.app.coolg_kt.databinding.AdapterMatchItemBinding
import com.king.app.coolg_kt.page.match.DrawItem
import com.king.app.coolg_kt.page.match.FinalHead
import com.king.app.coolg_kt.page.match.FinalRound
import com.king.app.coolg_kt.page.match.FinalScore

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
                val binding = AdapterMatchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                val binding = DataBindingUtil.getBinding<AdapterMatchItemBinding>(holder.itemView)!!
                onBindItem(binding, position, list!![position] as DrawItem)
                binding.executePendingBindings()
            }
        }
    }

    private fun onBindHead(binding: AdapterMatchFinalHeadBinding, position: Int, bean: FinalHead) {

    }

    private fun onBindRound(binding: AdapterMatchFinalRoundBinding, position: Int, bean: FinalRound) {

    }

    private fun onBindScore(binding: AdapterMatchFinalScoreBinding, position: Int, bean: FinalScore) {

    }

    private fun onBindItem(binding: AdapterMatchItemBinding, position: Int, bean: DrawItem) {

    }

}