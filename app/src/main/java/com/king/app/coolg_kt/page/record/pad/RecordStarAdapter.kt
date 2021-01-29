package com.king.app.coolg_kt.page.record.pad

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordStarPadBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.gdb.data.relation.RecordStarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 13:32
 */
class RecordStarAdapter :
    BaseBindingAdapter<AdapterRecordStarPadBinding, RecordStarWrap>() {
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordStarPadBinding = AdapterRecordStarPadBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordStarPadBinding,
        position: Int,
        star: RecordStarWrap
    ) {
        ImageBindingAdapter.setStarUrl(binding.ivStar, star.imageUrl)
    }
}