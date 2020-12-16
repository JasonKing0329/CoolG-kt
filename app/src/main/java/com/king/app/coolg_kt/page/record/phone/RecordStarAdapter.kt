package com.king.app.coolg_kt.page.record.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordStarPhoneBinding
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.relation.RecordStarWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 15:59
 */
class RecordStarAdapter: BaseBindingAdapter<AdapterRecordStarPhoneBinding, RecordStarWrap>() {
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordStarPhoneBinding = AdapterRecordStarPhoneBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordStarPhoneBinding,
        position: Int,
        bean: RecordStarWrap
    ) {
        binding.bean = bean
        val buffer = StringBuffer()
        buffer.append(DataConstants.getTextForType(bean.bean.type))
        if (bean.bean.score != 0 || bean.bean.scoreC != 0) {
            buffer.append("(").append(bean.bean.score).append(")")
        }
        binding.tvFlag.text = buffer.toString()
        if (bean.countStar != null) {
            binding.tvRank.visibility = View.VISIBLE
            binding.tvRank.text = "R-" + bean.countStar!!.rank
        } else {
            binding.tvRank.visibility = View.GONE
        }
    }
}