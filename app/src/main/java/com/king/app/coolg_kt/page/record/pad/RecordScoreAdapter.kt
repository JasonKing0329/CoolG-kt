package com.king.app.coolg_kt.page.record.pad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordScorePadBinding
import com.king.app.coolg_kt.model.bean.TitleValueBean

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 13:35
 */
class RecordScoreAdapter :
    BaseBindingAdapter<AdapterRecordScorePadBinding, TitleValueBean>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordScorePadBinding = AdapterRecordScorePadBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordScorePadBinding,
        position: Int,
        bean: TitleValueBean
    ) {
        if (bean.isOnlyValue) {
            binding.tvTitle.visibility = View.GONE
        } else {
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvTitle.text = bean.title
        }
        binding.tvValue.text = bean.value
    }
}