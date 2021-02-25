package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentMatchDetailGmBinding
import com.king.app.coolg_kt.databinding.FragmentMatchDetailGsBinding
import com.king.app.coolg_kt.page.match.RoundItem
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/25 11:06
 */
class Gm1000Fragment: AbsDetailChildFragment<FragmentMatchDetailGmBinding, LevelViewModel>() {

    val adapter = Gm1000Adapter()

    override fun createViewModel(): LevelViewModel = generateViewModel(LevelViewModel::class.java)

    override fun getBinding(inflater: LayoutInflater): FragmentMatchDetailGmBinding = FragmentMatchDetailGmBinding.inflate(inflater)

    override fun initView(view: View) {

    }

    override fun initData() {
        mModel.mRecordId = mainViewModel.mRecordId
        mModel.listObserver.observe(this, Observer {
            mBinding.rvGrid.layoutManager = GridLayoutManager(requireContext(), mModel.matchCount + 1)
            adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RoundItem>{
                override fun onClickItem(view: View, position: Int, data: RoundItem) {
                    if (data.matchPeriodId != 0L) {
                        showRoadDialog(mModel.mRecordId, data.matchPeriodId)
                    }
                }
            })
            adapter.list = it
            mBinding.rvGrid.adapter = adapter
        })
        mModel.loadGm1000Data()
    }

    private fun showRoadDialog(recordId: Long, matchPeriodId: Long) {
        var content = RoadDialog()
        content.matchPeriodId = matchPeriodId
        content.recordId = recordId
        var dialog = DraggableDialogFragment()
        dialog.setTitle("Upgrade Road")
        dialog.contentFragment = content
        dialog.fixedHeight = content.idealHeight
        dialog.show(childFragmentManager, "RoadDialog")
    }

}