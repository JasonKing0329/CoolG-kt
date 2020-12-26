package com.king.app.coolg_kt.page.home.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BindingHolder
import com.king.app.coolg_kt.databinding.AdapterHomeRecordPhoneBinding
import com.king.app.coolg_kt.databinding.AdapterHomeStarPhoneBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.home.HomeRecord
import com.king.app.coolg_kt.page.home.HomeStar

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/24 10:44
 */
class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TYPE_RECORD = 0
    val TYPE_STAR = 1
    var list: List<Any> = listOf()
    var onListListener: OnListListener? = null

    override fun getItemViewType(position: Int): Int {
        return when {
            list[position] is HomeStar ->  TYPE_STAR
            else -> TYPE_RECORD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_STAR -> {
                val binding = AdapterHomeStarPhoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener { v ->
                    onClickStar(
                        binding.root,
                        holder.layoutPosition,
                        list[holder.layoutPosition] as HomeStar
                    )
                }
                holder
            }
            else -> {
                val binding = AdapterHomeRecordPhoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener { v ->
                    onClickRecord(
                        binding.root,
                        holder.layoutPosition,
                        list[holder.layoutPosition] as HomeRecord
                    )
                }
                holder
            }
        }
    }

    /**
     * 永远+1，最后一个为foot
     */
    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            TYPE_STAR -> {
                val binding = DataBindingUtil.getBinding<AdapterHomeStarPhoneBinding>(holder.itemView)
                onBindStar(binding!!, position, list[position] as HomeStar)
                binding.executePendingBindings()
            }
            else -> {
                val binding = DataBindingUtil.getBinding<AdapterHomeRecordPhoneBinding>(holder.itemView)
                onBindRecord(binding!!, position, list[position] as HomeRecord)
                binding.executePendingBindings()
            }
        }
    }

    private fun onBindStar(binding: AdapterHomeStarPhoneBinding, position: Int, bean: HomeStar) {
        binding.bean = bean
        var param = binding.cover.layoutParams
        param.height = if (bean.imageHeight == 0) {
            if (bean.cell == 1) {
                binding.cover.resources.getDimensionPixelSize(R.dimen.home_star_height_cell2)
            }
            else {
                binding.cover.resources.getDimensionPixelSize(R.dimen.home_star_height_cell1)
            }
        }
        else {
            bean.imageHeight
        }
        binding.cover.layoutParams = param
    }

    private fun onBindRecord(binding: AdapterHomeRecordPhoneBinding, position: Int, bean: HomeRecord) {
        ImageBindingAdapter.setRecordUrl(binding.ivRecordImage, bean.bean.imageUrl)
        binding.tvName.text = bean.bean.bean.name
        binding.tvRank.text = "R-${bean.bean.countRecord?.rank}"
        binding.tvRecordDate.visibility = if (bean.showDate) View.VISIBLE else View.GONE
        binding.tvRecordDate.text = bean.date
        binding.tvDeprecated.visibility = if (bean.bean.bean.deprecated == 1) View.VISIBLE else View.GONE
        binding.ivPlay.visibility = if (bean.bean.bean.deprecated == 1) View.GONE else View.VISIBLE
        binding.ivPlay.setOnClickListener { onListListener?.onAddPlay(bean) }
    }

    private fun onClickStar(view: View, position: Int, bean: HomeStar) {
        onListListener?.onClickStar(view, position, bean)
    }

    private fun onClickRecord(view: View, position: Int, bean: HomeRecord) {
        onListListener?.onClickRecord(view, position, bean)
    }

    fun getSpanSize(position: Int): Int {
        return when(getItemViewType(position)) {
            TYPE_STAR -> (list[position] as HomeStar).cell
            else -> 2
        }
    }

    interface OnListListener {
        fun onLoadMore()
        fun onClickRecord(view: View, position: Int, record: HomeRecord)
        fun onClickStar(view: View, position: Int, star: HomeStar)
        fun onAddPlay(record: HomeRecord)
    }

}