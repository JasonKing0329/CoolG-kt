package com.king.app.coolg_kt.page.match.h2h

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchH2hBinding
import com.king.app.coolg_kt.databinding.ActivityMatchH2hRoadBinding
import com.king.app.coolg_kt.page.match.H2HRoadGroup
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.match.rank.RankActivity
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/17 11:35
 */
class H2hActivity: BaseActivity<ActivityMatchH2hBinding, H2hViewModel>() {

    companion object {
        val EXTRA_RECORD_ID1= "record_id_1"
        val EXTRA_RECORD_ID2= "record_id_2"
        val EXTRA_MATCH_PERIOD_ID= "match_period_id"
        val EXTRA_FACE_ROUND_ID= "face_round_id"
        fun startPage(context: Context) {
            var intent = Intent(context, H2hActivity::class.java)
            context.startActivity(intent)
        }
        fun startH2hPage(context: Context, recordId1: Long, recordId2: Long, matchPeriodId: Long? = 0, faceRoundId: Int? = 0) {
            var intent = Intent(context, H2hActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID1, recordId1)
            intent.putExtra(EXTRA_RECORD_ID2, recordId2)
            intent.putExtra(EXTRA_MATCH_PERIOD_ID, matchPeriodId)
            intent.putExtra(EXTRA_FACE_ROUND_ID, faceRoundId)
            context.startActivity(intent)
        }
    }

    private val REQUEST_PLAYER = 1

    val adapter = H2hRoadAdapter()

    override fun getContentView(): Int = R.layout.activity_match_h2h

    override fun createViewModel(): H2hViewModel = generateViewModel(H2hViewModel::class.java)

    override fun initView() {
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = ScreenUtils.dp2px(1f)
            }
        })

        adapter.onH2hListener = object : H2hRoadAdapter.OnH2hListener {
            override fun onClickPlayer1() {
                selectPlayer(1)
            }

            override fun onClickPlayer2() {
                selectPlayer(2)
            }

            override fun onClickRoadPlayer(playerId: Long) {
                DetailActivity.startRecordPage(this@H2hActivity, playerId)
            }

            override fun onSelectLevel(level: Int) {
                mModel.filterByLevel(level)
            }

            override fun onClickGroup(position: Int, group: H2HRoadGroup) {
                mModel.toggleGroup(group)
            }
        }
    }

    override fun initData() {

        mModel.h2hObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.matchPeriodId = intent.getLongExtra(EXTRA_MATCH_PERIOD_ID, 0)
        mModel.faceRoundId = intent.getIntExtra(EXTRA_FACE_ROUND_ID, 0)
        val id1 = intent.getLongExtra(EXTRA_RECORD_ID1, 0)
        val id2 = intent.getLongExtra(EXTRA_RECORD_ID2, 0)
        mModel.loadH2h(id1, id2)
    }

    private fun selectPlayer(i: Int) {
        mModel.indexToReceivePlayer = i
        AlertDialogFragment()
            .setItems(
                arrayOf("Record List", "Rank List")
            ) { dialog, which ->
                when(which) {
                    0 -> PhoneRecordListActivity.startPageToSelect(this@H2hActivity, REQUEST_PLAYER, displayRank = true)
                    1 -> RankActivity.startPageToSelect(this@H2hActivity, REQUEST_PLAYER)
                }
            }
            .show(supportFragmentManager, "AlertDialogFragment")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PLAYER) {
            if (resultCode == Activity.RESULT_OK) {
                var playerId = data?.getLongExtra(PhoneRecordListActivity.RESP_RECORD_ID, 0)
                mModel.loadReceivePlayer(playerId!!)
            }
        }
    }
}