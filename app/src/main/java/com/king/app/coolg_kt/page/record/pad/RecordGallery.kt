package com.king.app.coolg_kt.page.record.pad

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BindingDialogFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.DialogRecordGalleryBinding
import com.king.app.coolg_kt.utils.ScreenUtils

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 13:38
 */
class RecordGallery : BindingDialogFragment<DialogRecordGalleryBinding>() {

    var list: List<String>? = null
    private var currentPage = 0
    private val adapter = RecordGalleryAdapter()
    var onItemClickListener: BaseBindingAdapter.OnItemClickListener<String>? = null

    override fun getBinding(inflater: LayoutInflater): DialogRecordGalleryBinding = DialogRecordGalleryBinding.inflate(inflater)

    override fun initView(view: View) {

    }

    override fun onResume() {
        super.onResume()

        // 在initView里不起作用
        setWidth(ScreenUtils.getScreenWidth())
        mBinding.rvGallery.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvGallery.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position > 0) {
                    outRect.left = ScreenUtils.dp2px(15f)
                }
            }
        })
        adapter.list = list
        mBinding.rvGallery.adapter = adapter

        onItemClickListener?.let {
            adapter.setOnItemClickListener(it)
        }
        adapter.updateSelection(currentPage)
        mBinding.rvGallery.scrollToPosition(currentPage)
    }

    fun setCurrentPage(currentPage: Int) {
        this.currentPage = currentPage
        if (isVisible) {
            adapter.updateSelection(currentPage)
            mBinding.rvGallery.scrollToPosition(currentPage)
        }
    }
}