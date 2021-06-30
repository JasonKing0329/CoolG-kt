package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.databinding.AdapterMatchCareerMatchBinding
import com.king.app.coolg_kt.databinding.AdapterMatchCareerPeriodBinding
import com.king.app.coolg_kt.databinding.AdapterMatchCareerRecordBinding
import com.king.app.coolg_kt.page.match.CareerMatch
import com.king.app.coolg_kt.page.match.CareerPeriod
import com.king.app.coolg_kt.page.match.CareerRecord

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/17 9:33
 */
class CareerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_PERIOD = 0
    private val TYPE_MATCH = 1
    private val TYPE_RECORD = 2

    var itemList = mutableListOf<Any>()
    var list: List<CareerPeriod>? = null
        set(value) {
            field = value
            value?.forEach { period ->
                itemList.add(period)
                period.matches.forEach { match ->
                    itemList.add(match)
                    match.records.forEach { record ->
                        itemList.add(record)
                    }
                }
            }
        }

    override fun getItemViewType(position: Int): Int {
        return when(itemList[position].javaClass) {
            CareerPeriod::class.java -> TYPE_PERIOD
            CareerMatch::class.java -> TYPE_MATCH
            else -> TYPE_RECORD
        }
    }

    fun isPeriod(position: Int): Boolean {
        return getItemViewType(position) == TYPE_PERIOD
    }

    fun isMatch(position: Int): Boolean {
        return getItemViewType(position) == TYPE_MATCH
    }

    fun isRecord(position: Int): Boolean {
        return getItemViewType(position) == TYPE_RECORD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when(viewType) {
            TYPE_PERIOD -> {
                val binding = AdapterMatchCareerPeriodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener { v ->
                    onClickPeriod(
                        binding.root,
                        holder.layoutPosition,
                        itemList!![holder.layoutPosition] as CareerPeriod
                    )
                }
                return holder
            }
            TYPE_MATCH -> {
                val binding = AdapterMatchCareerMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener { v ->
                    onClickMatch(
                        binding.root,
                        holder.layoutPosition,
                        itemList!![holder.layoutPosition] as CareerMatch
                    )
                }
                return holder
            }
            else -> {
                val binding = AdapterMatchCareerRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener { v ->
                    onClickRecord(
                        binding.root,
                        holder.layoutPosition,
                        itemList!![holder.layoutPosition] as CareerRecord
                    )
                }
                return holder
            }
        }
    }

    fun onClickPeriod(view: View, position: Int, data: CareerPeriod) {
//        onHeadClickListener?.onClickHead(view, position, data)
    }

    fun onClickMatch(view: View, position: Int, data: CareerMatch) {
//        onItemClickListener?.onClickItem(view, position, data)
    }

    fun onClickRecord(view: View, position: Int, data: CareerRecord) {
//        onItemClickListener?.onClickItem(view, position, data)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            TYPE_PERIOD -> {
                val binding = DataBindingUtil.getBinding<AdapterMatchCareerPeriodBinding>(holder.itemView)
                onBindPeriod(binding!!, position, itemList!![position] as CareerPeriod)
                binding!!.executePendingBindings()
            }
            TYPE_MATCH -> {
                val binding = DataBindingUtil.getBinding<AdapterMatchCareerMatchBinding>(holder.itemView)
                onBindMatch(binding!!, position, itemList!![position] as CareerMatch)
                binding!!.executePendingBindings()
            }
            else -> {
                val binding = DataBindingUtil.getBinding<AdapterMatchCareerRecordBinding>(holder.itemView)
                onBindRecord(binding!!, position, itemList!![position] as CareerRecord)
                binding!!.executePendingBindings()
            }
        }
    }

    private fun onBindPeriod(binding: AdapterMatchCareerPeriodBinding, position: Int, bean: CareerPeriod) {
        binding.bean = bean
    }

    private fun onBindMatch(binding: AdapterMatchCareerMatchBinding, position: Int, bean: CareerMatch) {
        binding.bean = bean
    }

    private fun onBindRecord(binding: AdapterMatchCareerRecordBinding, position: Int, bean: CareerRecord) {
        binding.bean = bean
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun notifyPeriodChanged(position: Int) {
        // TODO 暂时更新全部
        notifyDataSetChanged()
    }

    class BindingHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
