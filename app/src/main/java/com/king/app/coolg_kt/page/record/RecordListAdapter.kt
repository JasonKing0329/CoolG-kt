package com.king.app.coolg_kt.page.record

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordItemListBinding
import com.king.app.gdb.data.relation.RecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/23 21:07
 */
class RecordListAdapter:BaseBindingAdapter<AdapterRecordItemListBinding, RecordWrap>() {

    val binder = RecordItemBinder()

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordItemListBinding = AdapterRecordItemListBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordItemListBinding,
        position: Int,
        bean: RecordWrap
    ) {
        binder.bind(binding, position, bean)
    }
}