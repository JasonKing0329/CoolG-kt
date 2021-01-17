package com.king.app.coolg_kt.page.match.h2h

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityMatchH2hBinding
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity

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

        mBinding.ivRecord1.setOnClickListener { selectPlayer(1) }

        mBinding.ivRecord2.setOnClickListener { selectPlayer(2) }
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
        PhoneRecordListActivity.startPageToSelect(this, REQUEST_PLAYER)
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