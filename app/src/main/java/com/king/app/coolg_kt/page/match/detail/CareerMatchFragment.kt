package com.king.app.coolg_kt.page.match.detail

import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentCareerMatchBinding
import com.king.app.coolg_kt.page.match.CareerCategoryMatch
import com.king.app.coolg_kt.page.match.record.RecordMatchActivity

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/8 9:22
 */
class CareerMatchFragment:AbsDetailChildFragment<FragmentCareerMatchBinding, CareerMatchViewModel>() {

    val adapter = CareerMatchItemAdapter()

    override fun createViewModel(): CareerMatchViewModel = generateViewModel(CareerMatchViewModel::class.java)

    override fun initView(view: View) {
        mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        adapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<CareerCategoryMatch> {
            override fun onClickItem(view: View, position: Int, data: CareerCategoryMatch) {
                RecordMatchActivity.startPage(requireContext(), mainViewModel.mRecordId, data.match.id)
            }
        }
        mBinding.rvList.adapter = adapter

        mBinding.spSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    mModel.SORT_BY_WEEK -> mModel.sortByWeek()
                    mModel.SORT_BY_TIMES -> mModel.sortByTimes()
                    mModel.SORT_BY_LEVEL -> mModel.sortByLevel()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        mBinding.cbJoin.setOnCheckedChangeListener { buttonView, isChecked -> mModel.isJoinedMatchChanged(isChecked) }
    }

    override fun getBinding(inflater: LayoutInflater): FragmentCareerMatchBinding = FragmentCareerMatchBinding.inflate(inflater)

    override fun initData() {
        mModel.mRecordId = mainViewModel.mRecordId
        mModel.matchesObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.rangeChangedObserver.observe(this, Observer {
            adapter.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.loadMatches()
    }
}