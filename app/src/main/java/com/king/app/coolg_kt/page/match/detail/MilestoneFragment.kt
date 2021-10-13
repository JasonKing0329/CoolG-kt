package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentMatchMilestoneBinding
import com.king.app.coolg_kt.page.match.MilestoneBean
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/8 16:01
 */
class MilestoneFragment: AbsDetailChildFragment<FragmentMatchMilestoneBinding, MilestoneViewModel>() {

    val adapter = MilestoneAdapter()

    override fun createViewModel(): MilestoneViewModel = generateViewModel(MilestoneViewModel::class.java)

    override fun initView(view: View) {
        mBinding.rvList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<MilestoneBean> {
            override fun onClickItem(view: View, position: Int, data: MilestoneBean) {
                showRoadDialog(mModel.mRecordId, data.matchItem.matchId)
            }
        })
    }

    override fun getBinding(inflater: LayoutInflater): FragmentMatchMilestoneBinding = FragmentMatchMilestoneBinding.inflate(inflater)

    override fun initData() {
        mModel.mRecordId = mainViewModel.mRecordId
        mModel.dataObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadData()
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

}