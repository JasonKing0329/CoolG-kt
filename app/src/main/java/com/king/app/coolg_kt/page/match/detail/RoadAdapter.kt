package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterMatchRoundRoadBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.RoadBean

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/16 18:38
 */
class RoadAdapter: BaseBindingAdapter<AdapterMatchRoundRoadBinding, RoadBean>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchRoundRoadBinding = AdapterMatchRoundRoadBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterMatchRoundRoadBinding, position: Int, bean: RoadBean) {
        ImageBindingAdapter.setRecordUrl(binding.ivRecord, bean.imageUrl)
        binding.tvRound.text = bean.round
        binding.tvSeed.text = if (bean.seed?.isNotEmpty() == true) {
            "${bean.seed}/${bean.rank}"
        }
        else {
            "${bean.rank}"
        }
        if (bean.isLose) {
            binding.tvWin.text = "L"
            binding.tvWin.setTextColor(binding.tvWin.resources.getColor(R.color.text_second))
        }
        else {
            binding.tvWin.text = "W"
            binding.tvWin.setTextColor(binding.tvWin.resources.getColor(R.color.redC93437))
        }
    }
}