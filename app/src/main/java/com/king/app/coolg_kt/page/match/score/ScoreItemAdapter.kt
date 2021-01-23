package com.king.app.coolg_kt.page.match.score

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterScoreItemBinding
import com.king.app.coolg_kt.databinding.AdapterScoreTitleBinding
import com.king.app.coolg_kt.page.match.ScoreBean
import com.king.app.coolg_kt.page.match.ScoreTitle

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/16 15:32
 */
class ScoreItemAdapter: HeadChildBindingAdapter<AdapterScoreTitleBinding, AdapterScoreItemBinding, ScoreTitle, ScoreBean>() {

    override val itemClass: Class<*>
        get() = ScoreBean::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterScoreTitleBinding = AdapterScoreTitleBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterScoreItemBinding = AdapterScoreItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterScoreTitleBinding, position: Int, head: ScoreTitle) {
        binding.tvTitle.text = "${head.name} "
        binding.tvTitle.setTextColor(head.color)
        binding.underline.setBackgroundColor(head.color)
    }

    override fun onBindItem(binding: AdapterScoreItemBinding, position: Int, item: ScoreBean) {
        binding.tvName.text = item.name
        binding.tvRound.text = item.round
        binding.tvScore.text = item.score.toString()
        binding.ivWinner.visibility = if (item.isChampion) View.VISIBLE else View.GONE
        binding.tvComplete.visibility = if (item.isCompleted) View.VISIBLE else View.GONE
        binding.tvWeek.text = "W${item.matchPeriod.orderInPeriod}"
    }
}