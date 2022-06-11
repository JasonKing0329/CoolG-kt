package com.king.app.coolg_kt.page.record.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterModifyRecordStarBinding
import com.king.app.coolg_kt.model.http.bean.request.RecordUpdateStarItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/6/10 16:18
 */
class ModifyStarAdapter: BaseBindingAdapter<AdapterModifyRecordStarBinding, RecordUpdateStarItem>() {

    var isDeleteMode = false
    var onItemListener: OnItemListener? = null
    var onDataChangedListener: OnDataChangedListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterModifyRecordStarBinding = AdapterModifyRecordStarBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterModifyRecordStarBinding,
        position: Int,
        bean: RecordUpdateStarItem
    ) {
        binding.ivStar.setOnClickListener { onItemListener?.onClickImage(position, bean) }
        binding.ivDelete.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        binding.ivDelete.setOnClickListener { onItemListener?.onDelete(position, bean) }
        binding.etName.onlyFullInput = true
        binding.etName.listenInput {
            bean.starName = it
            onDataChangedListener?.onDataChanged()
        }
        binding.etName.setValue(bean.starName)
        binding.etScore.listenInput {
            bean.score = it.toIntOrNull()?:0
            onDataChangedListener?.onDataChanged()
        }
        binding.etScore.setValue(bean.score.toString())
        binding.etScoreC.listenInput {
            bean.scoreC = it.toIntOrNull()?:0
            onDataChangedListener?.onDataChanged()
        }
        binding.etScoreC.setValue(bean.scoreC.toString())
    }

    fun notifyItemChanged(item: RecordUpdateStarItem) {
        list?.indexOfFirst { it.starId == item.starId }?.let {
            if (it != -1) {
                notifyItemChanged(it)
            }
        }
    }

    fun toggleDelete() {
        isDeleteMode = !isDeleteMode
        notifyDataSetChanged()
    }

    interface OnItemListener {
        fun onDelete(position: Int, bean: RecordUpdateStarItem)
        fun onClickImage(position: Int, bean: RecordUpdateStarItem)
    }

    /**
     * star list只有有输入，不管是否与原值相同，都视作data发生变化
     */
    interface OnDataChangedListener {
        fun onDataChanged()
    }
}