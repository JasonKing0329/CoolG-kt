package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchMilestoneBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.match.MilestoneBean

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/8 16:03
 */
class MilestoneAdapter: BaseBindingAdapter<AdapterMatchMilestoneBinding, MilestoneBean>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchMilestoneBinding = AdapterMatchMilestoneBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterMatchMilestoneBinding,
        position: Int,
        bean: MilestoneBean
    ) {
        binding.tvIndex.text = bean.winIndex
        binding.tvLevel.text = MatchConstants.MATCH_LEVEL[bean.match.level]
        binding.tvName.text = bean.match.name
        binding.tvRank.text = bean.rankSeed
        binding.tvNameCpt.text = bean.cptName
        binding.tvDefeat.text = "defeat ${bean.cptRankSeed}"
        binding.tvWeek.text = bean.period
        binding.tvRound.text = MatchConstants.roundResultShort(bean.matchItem.round, false)
        ImageBindingAdapter.setRecordUrl(binding.ivRecord, bean.cptImageUrl)
    }
}