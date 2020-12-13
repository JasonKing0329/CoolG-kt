package com.king.app.coolg_kt.page.download

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.databinding.FragmentDownloadListBinding
import com.king.app.coolg_kt.model.bean.DownloadItemProxy

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/7 13:26
 */
class ListFragment : AbsDownloadChildFragment<FragmentDownloadListBinding>() {

    private var adapter: ListAdapter = ListAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentDownloadListBinding {
        return FragmentDownloadListBinding.inflate(inflater)
    }

    override fun initData() {
        mBinding.rvList.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        mainViewModel.initDownloadItems()
    }

    fun showList(list: List<DownloadItemProxy>) {
        if (list.isEmpty()) {
            mBinding.tvEmpty.visibility = View.VISIBLE
            mBinding.rvList.visibility = View.GONE
            return
        }
        adapter.list = list
        mBinding.rvList.adapter = adapter
        mainViewModel.startDownload()
    }

    fun onProgressChanged(position: Int) {
        adapter.notifyItemChanged(position)
    }
}