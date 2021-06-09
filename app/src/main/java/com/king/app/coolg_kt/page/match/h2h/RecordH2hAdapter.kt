package com.king.app.coolg_kt.page.match.h2h

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchRecordH2hBinding
import com.king.app.coolg_kt.page.match.RecordH2hItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/6/8 15:22
 */
class RecordH2hAdapter: BaseBindingAdapter<AdapterMatchRecordH2hBinding, RecordH2hItem>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRecordH2hBinding = AdapterMatchRecordH2hBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterMatchRecordH2hBinding,
        position: Int,
        bean: RecordH2hItem
    ) {
        binding.bean = bean
        binding.tvWin.text = bean.win.toString()
        binding.tvLose.text = bean.lose.toString()
        when {
            bean.win > bean.lose -> {
                binding.tvWin.setTextColor(binding.tvWin.resources.getColor(R.color.redC93437))
                binding.tvLose.setTextColor(binding.tvWin.resources.getColor(R.color.text_second))
            }
            bean.win < bean.lose -> {
                binding.tvWin.setTextColor(binding.tvWin.resources.getColor(R.color.text_second))
                binding.tvLose.setTextColor(binding.tvWin.resources.getColor(R.color.redC93437))
            }
            else -> {
                binding.tvWin.setTextColor(binding.tvWin.resources.getColor(R.color.text_second))
                binding.tvLose.setTextColor(binding.tvWin.resources.getColor(R.color.text_second))
            }
        }
    }
}