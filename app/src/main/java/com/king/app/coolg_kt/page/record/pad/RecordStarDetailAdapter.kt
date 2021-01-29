package com.king.app.coolg_kt.page.record.pad

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordStarDetailPadBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.relation.RecordStarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 13:34
 */
class RecordStarDetailAdapter :
    BaseBindingAdapter<AdapterRecordStarDetailPadBinding, RecordStarWrap>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordStarDetailPadBinding = AdapterRecordStarDetailPadBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordStarDetailPadBinding,
        position: Int,
        star: RecordStarWrap
    ) {
        binding.tvName.text = star.star.name
        val buffer = StringBuffer()
        buffer.append(DataConstants.getTextForType(star.bean.type))
        if (star.bean.score != 0 || star.bean.scoreC != 0) {
            buffer.append("(").append(star.bean.score).append("/").append(star.bean.scoreC)
                .append(")")
        }
        binding.tvFlag.text = buffer.toString()
        ImageBindingAdapter.setStarUrl(binding.ivStar, star.imageUrl)
    }
}