package com.king.app.coolg_kt.page.record.phone

import android.view.LayoutInflater
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
    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterModifyRecordStarBinding = AdapterModifyRecordStarBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterModifyRecordStarBinding,
        position: Int,
        bean: RecordUpdateStarItem
    ) {
        binding.etName.onlyFullInput = true
        binding.etName.listenInput { bean.starName = it }
        binding.etName.setValue(bean.starName)
        binding.etScore.listenInput { bean.score = it.toIntOrNull()?:0 }
        binding.etScore.setValue(bean.score.toString())
        binding.etScoreC.listenInput { bean.scoreC = it.toIntOrNull()?:0 }
        binding.etScoreC.setValue(bean.scoreC.toString())
    }
}