package com.king.app.coolg_kt.page.match.rank

import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.base.ViewModelFactory
import com.king.app.coolg_kt.databinding.FragmentRankScoresBinding
import com.king.app.coolg_kt.page.match.ScoreHead
import com.king.app.coolg_kt.page.match.detail.ScoreAdapter
import com.king.app.coolg_kt.page.match.detail.ScoreViewModel
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/10/13 17:06
 */
class RankScoresFragment: DraggableContentFragment<FragmentRankScoresBinding>() {

    lateinit var mModel: ScoreViewModel

    var period = 0
    var orderInPeriod = 0
    var recordId: Long = 0

    var adapter = ScoreAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentRankScoresBinding = FragmentRankScoresBinding.inflate(inflater)

    override fun initData() {
        mModel = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(ScoreViewModel::class.java)

        mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        mModel.scoresObserver.observe(this, Observer {
            // 去掉头部
            it.filterNot { item -> item is ScoreHead }.apply {
                adapter.list = this
                adapter.notifyDataSetChanged()
            }
        })
        mModel.recordId = recordId
        mModel.loadRankPeriod(period, orderInPeriod)
    }
}