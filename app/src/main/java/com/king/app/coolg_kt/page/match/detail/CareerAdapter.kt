package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
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

    var onRecordListener: OnRecordListener? = null

    var onMatchListener: OnMatchListener? = null

    var itemList = mutableListOf<Any>()
    var list: List<CareerPeriod>? = null
        set(value) {
            field = value
            createItemList()
        }

    private fun createItemList() {
        itemList.clear()
        list?.forEach { period ->
            itemList.add(period)
            if (period.isExpand) {
                period.matches.forEach { match ->
                    itemList.add(match)
                    if (match.isExpand) {
                        match.records.forEach { record ->
                            itemList.add(record)
                        }
                    }
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
        onMatchListener?.onClickMatch(position, data)
    }

    fun onClickRecord(view: View, position: Int, data: CareerRecord) {
        onRecordListener?.onClickRecord(position, data)
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
        binding.tvPeriod.text = "P${bean.period}"
        binding.tvTitles.text = if (bean.titles > 1) "${bean.titles} titles"
        else "${bean.titles} title"
        binding.tvTitles.visibility = if (bean.titles > 0) View.VISIBLE else View.GONE
        if (bean.isExpand) {
            binding.ivExpand.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
        }
        else {
            binding.ivExpand.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
        }
        binding.ivExpand.setOnClickListener {
            bean.isExpand = !bean.isExpand
            createItemList()
            // period的收起和展开，涉及嵌套的处理，这里就不麻烦了，直接通知全部刷新，摒弃动画
            notifyDataSetChanged()
        }
    }

    private fun onBindMatch(binding: AdapterMatchCareerMatchBinding, position: Int, bean: CareerMatch) {
        binding.bean = bean
        binding.tvWeek.text = "W${bean.week}"
        binding.tvLevel.setTextColor(bean.levelColor)
        if (bean.isExpand) {
            binding.ivExpand.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
        }
        else {
            binding.ivExpand.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
        }
        binding.ivExpand.setOnClickListener {
            bean.isExpand = !bean.isExpand
            createItemList()
            if (bean.isExpand) {
                // 为使用动画，采用notifyItemRangeInserted
                notifyItemRangeInserted(position + 1, bean.records.size)
                // insert与remove一样，后面挪动的位置也需要刷新
                notifyItemRangeChanged(position + 1 + bean.records.size, bean.records.size)
                // 当前位置也要刷新，改变箭头方向
                notifyItemChanged(position)
            }
            else {
                // 为使用动画，采用notifyItemRangeRemoved
                notifyItemRangeRemoved(position + 1, bean.records.size)
                // remove后必须刷新变化后的位置，不然会引起混乱（比如删除后，有CareerMatch顶上来了，不刷新的话，它的ivExpand还保留上次的监听事件，点它再收起，positionStart就是上次的值了）
                notifyItemRangeChanged(position + 1, bean.records.size)
                // 当前位置也要刷新，改变箭头方向
                notifyItemChanged(position)
            }
        }
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

    fun expandAllPeriod(expand: Boolean) {
        list?.forEach {
            it.isExpand = expand
        }
        createItemList()
        notifyDataSetChanged()
    }

    fun expandAllMatches(expand: Boolean) {
        list?.forEach {
            it.matches.forEach { match -> match.isExpand = expand }
        }
        createItemList()
        notifyDataSetChanged()
    }

    class BindingHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnRecordListener {
        fun onClickRecord(position: Int, record: CareerRecord)
    }

    interface OnMatchListener {
        fun onClickMatch(position: Int, record: CareerMatch)
    }
}
