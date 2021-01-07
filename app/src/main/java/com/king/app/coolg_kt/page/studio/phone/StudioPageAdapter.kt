package com.king.app.coolg_kt.page.studio.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.base.adapter.BindingHolder
import com.king.app.coolg_kt.databinding.AdapterStudioPageRecordBinding
import com.king.app.coolg_kt.databinding.AdapterStudioPageRecordHeadBinding
import com.king.app.coolg_kt.databinding.AdapterStudioPageStarBinding
import com.king.app.coolg_kt.databinding.AdapterStudioPageStarHeadBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.gdb.data.bean.StarWrapWithCount
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 9:55
 */
class StudioPageAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_STAR_HEAD = 0
    private val TYPE_STAR = 1
    private val TYPE_RECORD_HEAD = 2
    private val TYPE_RECORD = 3

    var list: List<Any>? = null

    var onPageListener: OnPageListener? = null

    fun getSpanSize(position: Int): Int {
        return when(getItemViewType(position)) {
            TYPE_STAR_HEAD -> 6
            TYPE_STAR -> 2
            TYPE_RECORD_HEAD -> 6
            else -> 3
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(list!![position].javaClass) {
            PageStarHead::class.java -> TYPE_STAR_HEAD
            StarWrapWithCount::class.java -> TYPE_STAR
            PageRecordHead::class.java -> TYPE_RECORD_HEAD
            else -> TYPE_RECORD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            TYPE_STAR_HEAD -> {
                val binding = AdapterStudioPageStarHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                holder
            }
            TYPE_STAR -> {
                val binding = AdapterStudioPageStarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener { v ->
                    onClickStar(binding.root, holder.layoutPosition, list!![holder.layoutPosition] as StarWrapWithCount)
                }
                holder
            }
            TYPE_RECORD_HEAD -> {
                val binding = AdapterStudioPageRecordHeadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                holder
            }
            else -> {
                val binding = AdapterStudioPageRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val holder = BindingHolder(binding.root)
                binding.root.setOnClickListener { v ->
                    onClickRecord(binding.root, holder.layoutPosition, list!![holder.layoutPosition] as RecordWrap)
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
            TYPE_STAR_HEAD -> {
                val binding = DataBindingUtil.getBinding<AdapterStudioPageStarHeadBinding>(holder.itemView)!!
                onBindStarHead(binding, position, list!![position] as PageStarHead)
                binding.executePendingBindings()
            }
            TYPE_STAR -> {
                val binding = DataBindingUtil.getBinding<AdapterStudioPageStarBinding>(holder.itemView)!!
                onBindStar(binding, position, list!![position] as StarWrapWithCount)
                binding.executePendingBindings()
            }
            TYPE_RECORD_HEAD-> {
                val binding = DataBindingUtil.getBinding<AdapterStudioPageRecordHeadBinding>(holder.itemView)!!
                onBindRecordHead(binding, position, list!![position] as PageRecordHead)
                binding.executePendingBindings()
            }
            else -> {
                val binding = DataBindingUtil.getBinding<AdapterStudioPageRecordBinding>(holder.itemView)!!
                onBindRecord(binding, position, list!![position] as RecordWrap)
                binding.executePendingBindings()
            }
        }
    }

    private fun onBindStarHead(binding: AdapterStudioPageStarHeadBinding, position: Int, bean: PageStarHead) {
        binding.tvAll.text = "View all(${bean.total} stars)"
        binding.tvAll.setOnClickListener { onPageListener?.viewAllStars() }
    }

    private fun onBindStar(binding: AdapterStudioPageStarBinding, position: Int, bean: StarWrapWithCount) {
        ImageBindingAdapter.setStarUrl(binding.ivImage, bean.imagePath)
        binding.tvNum.text = "${bean.extraCount} videos"
        binding.tvName.text = bean.bean.name
    }

    private fun onBindRecordHead(binding: AdapterStudioPageRecordHeadBinding, position: Int, bean: PageRecordHead) {
        binding.tvVideos.text = bean.title
        binding.tvMore.setOnClickListener { onPageListener?.viewMoreRecords(bean.type) }
    }

    private fun onBindRecord(binding: AdapterStudioPageRecordBinding, position: Int, recordWrap: RecordWrap) {
        ImageBindingAdapter.setStarUrl(binding.ivRecord, recordWrap.imageUrl)
        binding.tvName.text = recordWrap.bean.name
        binding.tvScore.text = recordWrap.bean.score.toString()
        binding.tvDate.text = FormatUtil.formatDate(recordWrap.bean.lastModifyTime)
    }

    private fun onClickStar(root: View, position: Int, studioStar: StarWrapWithCount) {
        onPageListener?.onClickStar(position, studioStar)
    }

    private fun onClickRecord(root: View, position: Int, recordWrap: RecordWrap) {
        onPageListener?.onClickRecord(position, recordWrap)
    }

    interface OnPageListener {
        fun viewAllStars()
        fun onClickStar(position: Int, star: StarWrapWithCount)
        fun viewMoreRecords(type: Int)
        fun onClickRecord(position: Int, record: RecordWrap)
    }
}