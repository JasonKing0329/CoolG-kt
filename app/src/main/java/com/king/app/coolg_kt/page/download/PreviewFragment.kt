package com.king.app.coolg_kt.page.download

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.databinding.FragmentDownloadPreviewBinding
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import java.lang.String

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 15:28
 */
class PreviewFragment: AbsDownloadChildFragment<FragmentDownloadPreviewBinding>() {

    private var adapter: PreviewAdapter = PreviewAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentDownloadPreviewBinding = FragmentDownloadPreviewBinding.inflate(inflater)

    override fun initData() {
        var col = 2
        if (ScreenUtils.isTablet()) {
            col = 3
        }

        mBinding.rvExisted.layoutManager = GridLayoutManager(activity, col)

        mBinding.tvContinue.setOnClickListener { v -> showReadyDialog() }

        if (mainViewModel.getExistedList().isNotEmpty()) {
            mBinding.rvExisted.visibility = View.VISIBLE
            adapter = PreviewAdapter()
            adapter.list = mainViewModel.getExistedList()
            mBinding.rvExisted.adapter = adapter
            mBinding.cbSelectAll.visibility = View.VISIBLE
            mBinding.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    adapter.selectAll()
                } else {
                    adapter.unSelectAll()
                }
                adapter.notifyDataSetChanged()
            }
        } else {
            showReadyDialog()
            mBinding.cbSelectAll.visibility = View.GONE
        }
    }

    private fun showReadyDialog() {
        var items: Int = mainViewModel.getDownloadList().size
        if (adapter != null) {
            items += adapter.getCheckedItems().size
        }
        val message = String.format(getString(R.string.gdb_option_download), items)
        val dialog = AlertDialogFragment()
        dialog.setMessage(message)
        dialog.setPositiveText(getString(R.string.ok))
        dialog.setPositiveListener { dialog1, which ->
            // 有初始化过才pick
            if (adapter.list != null) {
                mainViewModel.addDownloadItems(adapter.getCheckedItems())
            }
            mainViewModel.showListPage()
        }
        dialog.setNegativeText(getString(R.string.cancel))
        dialog.setNegativeListener { dialog12, which ->
            if (mainViewModel.getExistedList().isEmpty()) {
                mainViewModel.dismiss()
            }
        }
        dialog.show(childFragmentManager, "AlertDialogFragmentV4")
    }

}