package com.king.app.coolg_kt.page.match.rank

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentDialogRankFilterBinding
import com.king.app.coolg_kt.model.extension.toIntOrZero
import com.king.app.coolg_kt.page.match.RankItem
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment
import com.king.app.gdb.data.entity.Record

class RankFilterDialog: DraggableContentFragment<FragmentDialogRankFilterBinding>() {

    private var adapter = RankAdapter<Record?>()

    var list: List<RankItem<Record?>>? = null
    var focusToRank: Int = 0
    var clickListener: BaseBindingAdapter.OnItemClickListener<RankItem<Record?>>? = null
    var itemListener: RankAdapter.OnItemListener<Record?>? = null

    override fun getBinding(inflater: LayoutInflater): FragmentDialogRankFilterBinding
        = FragmentDialogRankFilterBinding.inflate(inflater)

    override fun initData() {
        mBinding.rvList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter
        adapter.listenerClick = clickListener
        adapter.onItemListener = itemListener

        mBinding.btnOk.setOnClickListener { filterRanks() }
        mBinding.etMin.setText("0")
        mBinding.etMax.setText("0")
        filterRanks()
        if (focusToRank > 0) {
            mBinding.rvList.post {
                mBinding.rvList.scrollToPosition(getFocusPosition())
            }
        }
    }

    /**
     * 初始化跳转到离focusToRank最近的位置
     */
    private fun getFocusPosition(): Int {
        adapter.list?.let {
            for (i in 0..it.size) {
                if (it[i].rank >= focusToRank) {
                    return i
                }
            }
        }
        return 0
    }

    private fun filterRanks() {
        val min = mBinding.etMin.toIntOrZero()
        val max = mBinding.etMax.toIntOrZero()
        val result = list?.filter { it.levelMatchCount in min..max }
        adapter.list = result
        adapter.notifyDataSetChanged()
    }

    override fun fixHeightAsMaxHeight(): Boolean {
        return true
    }
}