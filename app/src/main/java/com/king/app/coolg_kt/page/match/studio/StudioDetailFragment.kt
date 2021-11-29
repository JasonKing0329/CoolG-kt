package com.king.app.coolg_kt.page.match.studio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentMatchStudioPageBinding
import com.king.app.coolg_kt.page.match.StudioItem
import com.king.app.coolg_kt.page.match.StudioTitle
import com.king.app.coolg_kt.page.match.detail.DetailActivity
import com.king.app.coolg_kt.page.match.rank.RankActivity

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/11/24 10:32
 */
class StudioDetailFragment: BaseFragment<FragmentMatchStudioPageBinding, StudioDetailViewModel>() {

    companion object {
        val STUDIO_ID = "studio_id"
        fun newInstance(studioId: Long): StudioDetailFragment {
            var ft = StudioDetailFragment()
            ft.arguments = Bundle()
            ft.requireArguments().putLong(STUDIO_ID, studioId)
            return ft
        }
    }

    val adapter = StudioDetailAdapter()

    override fun createViewModel(): StudioDetailViewModel = generateViewModel(StudioDetailViewModel::class.java)

    override fun getBinding(inflater: LayoutInflater): FragmentMatchStudioPageBinding = FragmentMatchStudioPageBinding.inflate(inflater)

    override fun initView(view: View) {
        var manager = GridLayoutManager(context, 3)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position)
            }
        }
        mBinding.rvList.layoutManager = manager

        adapter.onHeadClickListener = object : HeadChildBindingAdapter.OnHeadClickListener<StudioTitle> {
            override fun onClickHead(view: View, position: Int, data: StudioTitle) {
                when(data.flag) {
                    StudioDetailViewModel.FLAG_TITLE_CUR_HIGH -> {
                        RankActivity.startPageStudioItems(requireActivity(), requireArguments().getLong(STUDIO_ID))
                    }
                }
            }
        }
        adapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<StudioItem> {
            override fun onClickItem(view: View, position: Int, data: StudioItem) {
                DetailActivity.startRecordPage(requireContext(), data.record!!.id!!)
            }
        }
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        mModel.data.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.rangeObserver.observe(this, Observer {
            adapter.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.loadStudioData(requireArguments().getLong(STUDIO_ID))
    }
}