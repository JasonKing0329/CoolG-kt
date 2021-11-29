package com.king.app.coolg_kt.page.match.draw

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchBlacklistBinding
import com.king.app.coolg_kt.page.match.rank.RankActivity
import com.king.app.coolg_kt.page.record.RecordGridAdapter
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/28 16:54
 */
class BlackListActivity: BaseActivity<ActivityMatchBlacklistBinding, BlackListViewModel>() {

    val adapter = RecordGridAdapter()
    val REQUEST_SELECT = 501

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, BlackListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_match_blacklist

    override fun createViewModel(): BlackListViewModel = generateViewModel(BlackListViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_add -> {
                    selectRecord(REQUEST_SELECT)
                }
                R.id.menu_delete -> {
                    mBinding.actionbar.showConfirmStatus(it)
                    adapter.selectionMode = true
                    adapter.notifyDataSetChanged()
                }
            }
        }
        mBinding.actionbar.setOnCancelListener {
            adapter.selectionMode = false
            adapter.notifyDataSetChanged()
            true
        }
        mBinding.actionbar.setOnConfirmListener {
            when(it) {
                R.id.menu_delete -> {
                    adapter.selectionMode = false
                    mModel.deleteFromBlackList(adapter.getSelectedItems())
                }
            }
            true
        }

        mBinding.rvList.layoutManager = GridLayoutManager(this, 3)
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordWrap> {
            override fun onClickItem(view: View, position: Int, data: RecordWrap) {
                RecordActivity.startPage(this@BlackListActivity, data.bean.id!!)
            }
        })
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        mModel.blackList.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadBlackList()
    }

    private fun selectRecord(requestCode: Int) {
        AlertDialogFragment()
            .setItems(
                arrayOf("Record List", "Rank List", "Out of rank")
            ) { dialog, which ->
                when(which) {
                    0 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@BlackListActivity, requestCode)
                    1 -> RankActivity.startPageToSelect(this@BlackListActivity, requestCode, 1000, 0)
                    2 -> PhoneRecordListActivity.startPageToSelectAsMatchItem(this@BlackListActivity, requestCode, true)
                }
            }
            .show(supportFragmentManager, "AlertDialogFragment")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_SELECT -> {
                if (resultCode == RESULT_OK) {
                    data?.getLongExtra(PhoneRecordListActivity.RESP_RECORD_ID, -1)?.apply {
                        mModel.addToBlackList(this)
                    }
                }
            }
        }
    }
}