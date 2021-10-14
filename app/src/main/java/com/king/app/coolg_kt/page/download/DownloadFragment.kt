package com.king.app.coolg_kt.page.download

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.ViewModelFactory
import com.king.app.coolg_kt.databinding.FragmentDownloadBinding
import com.king.app.coolg_kt.model.bean.DownloadDialogBean
import com.king.app.coolg_kt.view.dialog.DraggableContentFragment

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 15:12
 */
class DownloadFragment: DraggableContentFragment<FragmentDownloadBinding>() {

    lateinit var mModel: DownloadViewModel

    var onDownloadListener: OnDownloadListener? = null

    var downloadDialogBean: DownloadDialogBean? = null

    private var ftPreview: PreviewFragment? = null

    private var ftList: ListFragment? = null

    /**
     * 根布局是match_parent，需要覆盖为true
     */
    override fun fixHeightAsMaxHeight(): Boolean {
        return true
    }

    override fun getBinding(inflater: LayoutInflater): FragmentDownloadBinding = FragmentDownloadBinding.inflate(inflater)

    override fun initData() {
        mModel = ViewModelProvider(this, ViewModelFactory(CoolApplication.instance)).get(DownloadViewModel::class.java)

        mModel.downloadDialogBean = downloadDialogBean
        mModel.onDownloadListener = onDownloadListener
        mModel.setSavePath(downloadDialogBean!!.savePath)

        mModel.showListPage.observe(this, Observer {
            showListPage()
        })
        mModel.dismissDialog.observe(this, Observer {
            dismissAllowingStateLoss()
        })

        mModel.itemsObserver.observe(this, Observer{ list -> ftList?.showList(list) })
        mModel.progressObserver.observe(
            this,
            Observer{ position -> ftList?.onProgressChanged(position) })

        childFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, getContentViewFragment(), "Download")
            .commit()

    }

    private fun getContentViewFragment(): Fragment {
        return if (downloadDialogBean!!.isShowPreview) {
            ftPreview = PreviewFragment()
            ftPreview!!
        } else {
            ftList = ListFragment()
            ftList!!
        }
    }

    private fun showListPage() {
        ftList = ListFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, ftList!!, "ListFragment")
            .commit()
    }
}