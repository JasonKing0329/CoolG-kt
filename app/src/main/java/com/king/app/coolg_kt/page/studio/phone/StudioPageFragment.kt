package com.king.app.coolg_kt.page.studio.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.databinding.FragmentStudioPageBinding
import com.king.app.coolg_kt.page.record.phone.PhoneRecordListActivity
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.star.phone.StarActivity
import com.king.app.coolg_kt.page.star.phone.StarsPhoneActivity
import com.king.app.gdb.data.bean.StarWrapWithCount
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/7 10:50
 */
class StudioPageFragment: BaseFragment<FragmentStudioPageBinding, StudioPageViewModel>() {

    companion object {
        val ARG_STUDIO_ID = "studio_id"
        fun newInstance(studioId: Long): StudioPageFragment {
            var fragment = StudioPageFragment()
            var bundle = Bundle()
            bundle.putLong(ARG_STUDIO_ID, studioId)
            fragment.arguments = bundle
            return fragment
        }
    }

    val adapter = StudioPageAdapter()

    override fun createViewModel(): StudioPageViewModel = generateViewModel(StudioPageViewModel::class.java)

    override fun getBinding(inflater: LayoutInflater): FragmentStudioPageBinding = FragmentStudioPageBinding.inflate(inflater)

    override fun initView(view: View) {
        var manager = GridLayoutManager(requireContext(), 6)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position)
            }
        }
        mBinding.rvPage.layoutManager = manager

        adapter.onPageListener = object : StudioPageAdapter.OnPageListener {
            override fun viewAllStars() {
                StarsPhoneActivity.startStudioPage(requireContext(), requireArguments().getLong(ARG_STUDIO_ID))
            }

            override fun onClickStar(position: Int, star: StarWrapWithCount) {
                StarActivity.startPage(requireContext(), star.bean.id!!)
            }

            override fun viewMoreRecords(type: Int) {
                PhoneRecordListActivity.startStudioPage(requireContext(), requireArguments().getLong(ARG_STUDIO_ID))
            }

            override fun onClickRecord(position: Int, record: RecordWrap) {
                RecordActivity.startPage(requireContext(), record.bean.id!!)
            }
        }
        mBinding.rvPage.adapter = adapter
    }

    override fun initData() {
        mModel.pageDataObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.loadPageData(requireArguments().getLong(ARG_STUDIO_ID))
    }
}