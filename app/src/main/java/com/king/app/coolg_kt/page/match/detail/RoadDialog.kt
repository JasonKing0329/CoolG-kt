package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.databinding.FragmentDialogMatchRoadBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.page.match.RoadBean
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/16 18:36
 */
class RoadDialog: DraggableContentFragment<FragmentDialogMatchRoadBinding>() {

    val adapter = RoadAdapter()

    var matchPeriodId: Long = 0
    var recordId: Long = 0

    var idealHeight = ScreenUtils.getScreenHeight() * 3 / 4

    override fun getBinding(inflater: LayoutInflater): FragmentDialogMatchRoadBinding = FragmentDialogMatchRoadBinding.inflate(inflater)

    override fun initData() {
        mBinding.rvList.layoutManager = GridLayoutManager(requireContext(), 2)
        mBinding.rvList.adapter = adapter

        val match = CoolApplication.instance.database!!.getMatchDao().getMatchPeriod(matchPeriodId)
        val record = CoolApplication.instance.database!!.getRecordDao().getRecordBasic(recordId)
        ImageBindingAdapter.setRecordUrl(mBinding.ivRecord, ImageProvider.getRecordRandomPath(record?.name, null))
        mBinding.tvDate.text = "P${match.bean.period}-W${match.bean.orderInPeriod}"
        mBinding.tvLevel.text = MatchConstants.MATCH_LEVEL[match.match.level]
        mBinding.tvMatch.text = match.match.name

        loadRecordRoad();
    }

    private fun loadRecordRoad() {
        var dao = CoolApplication.instance.database!!.getMatchDao()
        var list = dao.getMatchItems(matchPeriodId, recordId).sortedByDescending { MatchConstants.getRoundSortValue(it.bean.round) }
        var data = mutableListOf<RoadBean>()
        list.forEachIndexed { index, it ->
            var matchRecord = it.recordList.first { item -> item.recordId != recordId }
            val record = CoolApplication.instance.database!!.getRecordDao().getRecordBasic(matchRecord.recordId)
            var seed = ""
            when(matchRecord.type) {
                MatchConstants.MATCH_RECORD_QUALIFY -> seed = "[Q]"
                MatchConstants.MATCH_RECORD_WILDCARD -> seed = "[WC]"
                else -> if (matchRecord.recordSeed?:0 > 0) seed = "[${matchRecord.recordSeed}]"
            }
            val rank = if (it.bean.isBye) "Bye" else matchRecord.recordRank.toString()
            var bean = RoadBean(MatchConstants.roundResultShort(it.bean.round, false), rank
                , ImageProvider.getRecordRandomPath(record?.name, null), seed)
            data.add(bean)

            if (index == 0) {
                val roadRecord = it.recordList.first { item -> item.recordId == recordId }
                val result = MatchConstants.roundResultShort(it.bean.round, it.bean.winnerId == recordId)
                var seed = if (roadRecord.recordSeed?:0 > 0) "[${roadRecord.recordSeed}]/" else ""
                mBinding.tvRank.text = "$seed${roadRecord.recordRank}"
                mBinding.tvResult.text = result
            }
        }
        adapter.list = data
        adapter.notifyDataSetChanged()
    }
}