package com.king.app.coolg_kt.page.match.h2h

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BindingHolder
import com.king.app.coolg_kt.databinding.*
import com.king.app.coolg_kt.page.match.*
import com.king.app.coolg_kt.utils.RippleUtil

class H2hRoadAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TYPE_HEAD = 0
    val TYPE_INFO = 1
    val TYPE_GROUP = 2
    val TYPE_ROUND = 3
    val TYPE_H2H = 4

    var list: List<Any>? = null

    var onH2hListener: OnH2hListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_HEAD -> {
                val binding = AdapterMatchRoadHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.ivRecord1.setOnClickListener { onH2hListener?.onClickPlayer1() }
                binding.ivRecord2.setOnClickListener { onH2hListener?.onClickPlayer2() }
                BindingHolder(binding.root)
            }
            TYPE_INFO -> {
                val binding = AdapterMatchH2hInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BindingHolder(binding.root)
            }
            TYPE_GROUP -> {
                val binding = AdapterMatchRoadGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BindingHolder(binding.root)
            }
            TYPE_ROUND -> {
                val binding = AdapterMatchRoadRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BindingHolder(binding.root)
            }
            else -> {
                val binding = AdapterMatchH2hBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BindingHolder(binding.root)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            TYPE_HEAD -> {
                DataBindingUtil.getBinding<AdapterMatchRoadHeadBinding>(holder.itemView)?.apply {
                    onBindHead(this, position, list!![position] as H2hRoadWrap)
                }
            }
            TYPE_INFO -> {
                DataBindingUtil.getBinding<AdapterMatchH2hInfoBinding>(holder.itemView)?.apply {
                    onBindInfo(this, position, list!![position] as H2hInfo)
                }
            }
            TYPE_GROUP -> {
                DataBindingUtil.getBinding<AdapterMatchRoadGroupBinding>(holder.itemView)?.apply {
                    onBindGroup(this, position, list!![position] as H2HRoadGroup)
                }
            }
            TYPE_ROUND -> {
                DataBindingUtil.getBinding<AdapterMatchRoadRoundBinding>(holder.itemView)?.apply {
                    onBindRound(this, position, list!![position] as H2HRoadRound)
                }
            }
            TYPE_H2H -> {
                DataBindingUtil.getBinding<AdapterMatchH2hBinding>(holder.itemView)?.apply {
                    onBindH2h(this, position, list!![position] as H2hItem)
                }
            }
        }
    }

    private fun onBindInfo(binding: AdapterMatchH2hInfoBinding, position: Int, bean: H2hInfo) {
        binding.bean = bean
        binding.root.resources.apply {
            if (bean.bgColor == null) {
                binding.root.background = null
            }
            else {
                binding.root.setBackgroundColor(bean.bgColor!!)
            }
        }
    }

    private fun onBindHead(binding: AdapterMatchRoadHeadBinding, position: Int, bean: H2hRoadWrap) {
        binding.model = bean
    }

    private fun onBindGroup(binding: AdapterMatchRoadGroupBinding, position: Int, bean: H2HRoadGroup) {
        binding.bg.resources.apply {
            binding.bg.background = RippleUtil.getRippleBackground(getColor(R.color.white), getColor(R.color.ripple_color))
        }
        binding.bg.setOnClickListener { onH2hListener?.onClickGroup(position, bean) }

        binding.tvName.text = bean.name
        if (bean.showH2hFilter) {
            binding.groupH2hFilter.visibility = View.VISIBLE
            binding.spLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onH2hListener?.onSelectLevel(position - 1)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
        else {
            binding.groupH2hFilter.visibility = View.GONE
        }
        bean.infoWrap?.apply {
            binding.model = this
        }
        if (bean.isExpand) {
            binding.ivArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
        }
        else {
            binding.ivArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
        }
    }

    private fun onBindRound(binding: AdapterMatchRoadRoundBinding, position: Int, bean: H2HRoadRound) {
        binding.bean = bean
        if (bean.recordId1 == null) {
            binding.ivRecord1.visibility = View.INVISIBLE
            binding.tvBye1.visibility = View.INVISIBLE
            binding.tvSeed1.visibility = View.INVISIBLE
        }
        else {
            binding.ivRecord1.visibility = View.VISIBLE
            // bye
            if (bean.recordId1 == 0L) {
                binding.tvBye1.visibility = View.VISIBLE
                binding.tvSeed1.visibility = View.INVISIBLE
            }
            else {
                binding.tvBye1.visibility = View.INVISIBLE
                binding.tvSeed1.visibility = View.VISIBLE
                binding.ivRecord1.setOnClickListener { onH2hListener?.onClickRoadPlayer(bean.recordId1!!) }
            }
        }
        if (bean.recordId2 == null) {
            binding.ivRecord2.visibility = View.INVISIBLE
            binding.tvBye2.visibility = View.INVISIBLE
            binding.tvSeed2.visibility = View.INVISIBLE
        }
        else {
            binding.ivRecord2.visibility = View.VISIBLE
            // bye
            if (bean.recordId2 == 0L) {
                binding.tvBye2.visibility = View.VISIBLE
                binding.tvSeed2.visibility = View.INVISIBLE
            }
            else {
                binding.tvBye2.visibility = View.INVISIBLE
                binding.tvSeed2.visibility = View.VISIBLE
                binding.ivRecord2.setOnClickListener { onH2hListener?.onClickRoadPlayer(bean.recordId2!!) }
            }
        }
    }

    private fun onBindH2h(binding: AdapterMatchH2hBinding, position: Int, bean: H2hItem) {
        binding.bean = bean
        binding.group.setBackgroundColor(bean.bgColor)
        binding.tvSeq.text = bean.indexInList
    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            list?.get(position) is H2hRoadWrap -> TYPE_HEAD
            list?.get(position) is H2hInfo -> TYPE_INFO
            list?.get(position) is H2HRoadRound -> TYPE_ROUND
            list?.get(position) is H2HRoadGroup -> TYPE_GROUP
            else -> TYPE_H2H
        }
    }

    interface OnH2hListener {
        fun onClickPlayer1()
        fun onClickPlayer2()
        fun onClickRoadPlayer(playerId: Long)
        fun onSelectLevel(level: Int)
        fun onClickGroup(position: Int, group: H2HRoadGroup) {

        }
    }
}