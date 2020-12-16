package com.king.app.coolg_kt.page.record.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterRecordScoreItemBinding
import com.king.app.coolg_kt.model.bean.TitleValueBean

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/15 16:30
 */
class ScoreItemAdapter: BaseBindingAdapter<AdapterRecordScoreItemBinding, TitleValueBean>() {
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterRecordScoreItemBinding = AdapterRecordScoreItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterRecordScoreItemBinding,
        position: Int,
        bean: TitleValueBean
    ) {
        binding.bean = bean
        binding.divider.visibility = if (position == 0) View.GONE else View.VISIBLE
    }
}