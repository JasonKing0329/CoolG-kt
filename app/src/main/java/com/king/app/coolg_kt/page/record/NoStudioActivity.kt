package com.king.app.coolg_kt.page.record

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityRecordNoStudioBinding
import com.king.app.coolg_kt.page.match.h2h.H2hActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.gdb.data.relation.RecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/23 21:05
 */
class NoStudioActivity: BaseActivity<ActivityRecordNoStudioBinding, NoStudioViewModel>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, NoStudioActivity::class.java)
            context.startActivity(intent)
        }
    }

    val adapter = RecordListAdapter()

    override fun getContentView(): Int = R.layout.activity_record_no_studio

    override fun createViewModel(): NoStudioViewModel = generateViewModel(NoStudioViewModel::class.java)

    override fun initView() {
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordWrap> {
            override fun onClickItem(view: View, position: Int, data: RecordWrap) {
                RecordActivity.startPage(this@NoStudioActivity, data.bean.id!!)
            }
        })
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        mModel.listObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadData()
    }
}