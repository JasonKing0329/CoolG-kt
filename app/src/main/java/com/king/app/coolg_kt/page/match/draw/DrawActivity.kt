package com.king.app.coolg_kt.page.match.draw

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.conf.MatchConstants
import com.king.app.coolg_kt.conf.RoundPack
import com.king.app.coolg_kt.databinding.ActivityMatchDrawBinding
import com.king.app.coolg_kt.utils.DebugLog

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/10 15:11
 */
class DrawActivity: BaseActivity<ActivityMatchDrawBinding, DrawViewModel>() {

    companion object {
        val EXTRA_MATCH_PERIOD_ID = "match_period_id"
        fun startPage(context: Context, id: Long) {
            var intent = Intent(context, DrawActivity::class.java)
            intent.putExtra(EXTRA_MATCH_PERIOD_ID, id)
            context.startActivity(intent)
        }
    }

    val adapter = DrawAdapter()

    override fun getContentView(): Int = R.layout.activity_match_draw

    override fun createViewModel(): DrawViewModel = generateViewModel(DrawViewModel::class.java)

    override fun initView() {

        mBinding.model = mModel
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_edit -> {
                    mBinding.actionbar.showConfirmStatus(it)
                }
                R.id.menu_create_draw -> {
                    mModel.createDraw()
                }
                R.id.menu_create_score -> {

                }
            }
        }
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = adapter

        mBinding.spRound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                DebugLog.e()
                mModel.onRoundPositionChanged(position)
            }

        }

        mBinding.tvMain.isSelected = true
        mBinding.tvMain.setOnClickListener {
            mModel.drawType = MatchConstants.DRAW_MAIN
            mModel.onDrawTypeChanged()
            mBinding.tvMain.isSelected = true
            mBinding.tvQualify.isSelected = false
        }
        mBinding.tvQualify.setOnClickListener {
            mModel.drawType = MatchConstants.DRAW_QUALIFY
            mModel.onDrawTypeChanged()
            mBinding.tvMain.isSelected = false
            mBinding.tvQualify.isSelected = true
        }
    }

    override fun initData() {

        mModel.setRoundPosition.observe(this, Observer { mBinding.spRound.setSelection(it) })
        mModel.roundList.observe(this, Observer {
            mBinding.spRound.adapter = RoundAdapter(it)
        })
        mModel.itemsObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadMatch(intent.getLongExtra(EXTRA_MATCH_PERIOD_ID, -1))
    }

    class RoundAdapter constructor(var list: List<RoundPack>): BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var contentView = convertView
                ?: LayoutInflater.from(parent.context).inflate(android.R.layout.simple_dropdown_item_1line, null)
            val textView = contentView.findViewById<TextView>(android.R.id.text1)
            textView.text = list[position].shortName
            return contentView
        }

        override fun getItem(position: Int): Any {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return list[position].id.toLong()
        }

        override fun getCount(): Int {
            return list.size
        }

    }
}