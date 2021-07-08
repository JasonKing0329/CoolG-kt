package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.AdapterMatchCareerMatchItemBinding
import com.king.app.coolg_kt.databinding.AdapterMatchItemGroupBinding
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.match.CareerCategoryMatch
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:03
 */
class CareerMatchItemAdapter: HeadChildBindingAdapter<AdapterMatchItemGroupBinding, AdapterMatchCareerMatchItemBinding, String, CareerCategoryMatch>() {

    var isDeleteMode = false

    var onMatchItemListener: OnMatchItemListener? = null
    var onMatchGroupListener: OnMatchGroupListener? = null

    var isDemoImage = SettingProperty.isDemoImageMode()

    override val itemClass: Class<*>
        get() = CareerCategoryMatch::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchItemGroupBinding = AdapterMatchItemGroupBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterMatchCareerMatchItemBinding = AdapterMatchCareerMatchItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterMatchItemGroupBinding, position: Int, head: String) {
        binding.title = head
        binding.ivAdd.visibility = View.GONE
    }

    override fun onBindItem(binding: AdapterMatchCareerMatchItemBinding, position: Int, bean: CareerCategoryMatch) {

        binding.tvTimesNum.text = bean.times.toString()

        binding.tvWeek.text = "W${bean.match.orderInPeriod}"
        binding.tvWinLose.text = " ${bean.winLose} "
        binding.tvBest.text = bean.best
        binding.tvName.text = " ${bean.match.name} "
        binding.tvLevel.text = " ${MatchConstants.MATCH_LEVEL[bean.match.level]} "
        val color = when(bean.match.level) {
            MatchConstants.MATCH_LEVEL_GS -> binding.tvLevel.resources.getColor(R.color.match_level_gs)
            MatchConstants.MATCH_LEVEL_FINAL -> binding.tvLevel.resources.getColor(R.color.match_level_final)
            MatchConstants.MATCH_LEVEL_GM1000 -> binding.tvLevel.resources.getColor(R.color.match_level_gm1000)
            MatchConstants.MATCH_LEVEL_GM500 -> binding.tvLevel.resources.getColor(R.color.match_level_gm500)
            MatchConstants.MATCH_LEVEL_GM250 -> binding.tvLevel.resources.getColor(R.color.match_level_gm250)
            else -> binding.tvLevel.resources.getColor(R.color.match_level_low)
        }
        binding.tvLevel.setTextColor(color)
    }

    interface OnMatchGroupListener {
        fun onAddGroupItem(level: Int)
    }

    interface OnMatchItemListener {
        fun onEdit(position: Int, bean: Match)
        fun onDelete(position: Int, bean: Match)
    }
}