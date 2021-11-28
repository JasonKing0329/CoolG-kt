package com.king.app.coolg_kt.page.record

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordItemGridBinding
import com.king.app.gdb.data.relation.RecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2020/4/5 17:36
 */
class RecordGridAdapter : BaseBindingAdapter<AdapterRecordItemGridBinding, RecordWrap>() {
    var mSortMode = 0
    var selectionMode = false
    var mCheckMap: MutableMap<Long, Boolean> = mutableMapOf()
    val binder = RecordItemGridBinder()
    var popupListener: OnPopupListener? = null

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordItemGridBinding = AdapterRecordItemGridBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordItemGridBinding,
        position: Int,
        bean: RecordWrap
    ) {
        binder.mSortMode = mSortMode
        binder.selectionMode = selectionMode
        binder.mCheckMap = mCheckMap
        binder.onPopupListener = popupListener
        binder.bind(binding, position, bean)
    }

    override fun onClickItem(v: View, position: Int, bean: RecordWrap) {
        if (selectionMode) {
            val key: Long = bean.bean.id!!
            if (mCheckMap[key] == null) {
                mCheckMap[key] = true
            } else {
                mCheckMap.remove(key)
            }
            notifyItemChanged(position)
        } else {
            super.onClickItem(v, position, bean)
        }
    }

    fun getSelectedItems(): List<RecordWrap> {
        return list?.filter { mCheckMap[it.bean.id!!]?:false }?: listOf()
    }
}