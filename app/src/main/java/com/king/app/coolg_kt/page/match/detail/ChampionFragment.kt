package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentMatchDetailChampionBinding
import com.king.app.coolg_kt.page.match.ChampionItem
import com.king.app.coolg_kt.page.match.ChampionLevel
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.widget.flow_rc.FlowLayoutManager

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/24 16:36
 */
class ChampionFragment: AbsDetailChildFragment<FragmentMatchDetailChampionBinding, EmptyViewModel>() {

    private val adapter = ChampionAdapter()

    private val levelAdapter = ChampionLevelAdapter()

    private var totalList = listOf<ChampionItem>()

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun getBinding(inflater: LayoutInflater): FragmentMatchDetailChampionBinding = FragmentMatchDetailChampionBinding.inflate(inflater)

    override fun initView(view: View) {
        mBinding.tvChampion.setOnClickListener {
            if (!mBinding.tvChampion.isSelected) {
                mBinding.tvChampion.isSelected = true
                mBinding.tvRu.isSelected = false
                showItems(true)
            }
        }
        mBinding.tvRu.setOnClickListener {
            if (!mBinding.tvRu.isSelected) {
                mBinding.tvChampion.isSelected = false
                mBinding.tvRu.isSelected = true
                showItems(false)
            }
        }
        mBinding.rvList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter
        mBinding.rvLevel.layoutManager = FlowLayoutManager(requireContext(), false)
        mBinding.rvLevel.adapter = levelAdapter

        levelAdapter.onLevelListener = object : ChampionLevelAdapter.OnLevelListener {
            override fun onAll() {
                adapter.list = totalList
                adapter.notifyDataSetChanged()
            }

            override fun onLevel(id: Int) {
                val list = totalList.filter { it.levelId == id }
                adapter.list = list
                adapter.notifyDataSetChanged()
            }
        }

        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<ChampionItem>{
            override fun onClickItem(view: View, position: Int, data: ChampionItem) {
                showRoadDialog(data.recordId, data.matchPeriodId)
            }
        })
    }

    override fun initData() {

        mBinding.tvChampion.isSelected = true
        showItems(true)
    }

    private fun showRoadDialog(recordId: Long, matchPeriodId: Long) {
        var content = RoadDialog()
        content.matchPeriodId = matchPeriodId
        content.recordId = recordId
        var dialog = DraggableDialogFragment()
        dialog.setTitle("Upgrade Road")
        dialog.contentFragment = content
        dialog.show(childFragmentManager, "RoadDialog")
    }

    private fun showItems(isWin: Boolean) {
        totalList = if (isWin) mainViewModel.getChampionItems()
            else mainViewModel.getRunnerUpItems()
        toLevelTags()
        adapter.list = totalList
        adapter.notifyDataSetChanged()
        mBinding.tvPerTotal.text = mainViewModel.championPerTotalText
        mBinding.tvRate.text = mainViewModel.championRateText
    }

    private fun toLevelTags() {
        val levels = mutableListOf<ChampionLevel>()
        totalList.forEach {
            val id = it.levelId
            var levelBean = levels.firstOrNull { l -> l.levelId == id }
            if (levelBean == null) {
                levelBean = ChampionLevel(it.level, id, 0)
                levels.add(levelBean)
            }
            levelBean.count ++
        }
        levels.sortBy { it.levelId }
        levelAdapter.selection = -1
        levelAdapter.list = levels
        levelAdapter.notifyDataSetChanged()
    }
}