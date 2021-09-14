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
        fun startPage(context: Context) {
            var intent = Intent(context, H2hActivity::class.java)
            context.startActivity(intent)
        }
        fun startH2hPage(context: Context, recordId1: Long, recordId2: Long) {
            var intent = Intent(context, H2hActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID1, recordId1)
            intent.putExtra(EXTRA_RECORD_ID2, recordId2)
            context.startActivity(intent)
        }
    }

    private val REQUEST_PLAYER = 1

    val adapter = H2hAdapter()

    override fun getContentView(): Int = R.layout.activity_match_h2h

    override fun createViewModel(): H2hViewModel = generateViewModel(H2hViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

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

        mBinding.ivRecord1.setOnClickListener { selectPlayer(1) }

        mBinding.ivRecord2.setOnClickListener { selectPlayer(2) }

        mBinding.spLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mModel.filterByLevel(position - 1)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    override fun initData() {

        mModel.h2hObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        val id1 = intent.getLongExtra(EXTRA_RECORD_ID1, -1L)
        val id2 = intent.getLongExtra(EXTRA_RECORD_ID2, -1L)
        if (id1 != -1L && id2 != -1L) {
            mModel.loadH2h(id1, id2)
        }
    }

    private fun selectPlayer(i: Int) {
        mModel.indexToReceivePlayer = i
        AlertDialogFragment()
            .setItems(
                arrayOf("Record List", "Rank List", "Studio Records", "Out of rank")
            ) { dialog, which ->
                when(which) {
                    0 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@H2hActivity, REQUEST_PLAYER)
                    1 -> RankActivity.startPageToSelect(this@H2hActivity, REQUEST_PLAYER)
                    2 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@H2hActivity, REQUEST_PLAYER)
                    3 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@H2hActivity, REQUEST_PLAYER, true)
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