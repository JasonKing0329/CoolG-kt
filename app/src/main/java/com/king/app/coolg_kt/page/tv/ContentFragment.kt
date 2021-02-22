package com.king.app.coolg_kt.page.tv

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentTvContentBinding
import com.king.app.coolg_kt.model.http.bean.data.FileBean
import com.king.app.coolg_kt.page.tv.player.SystemPlayerActivity
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.utils.UrlUtil
import com.king.app.coolg_kt.view.widget.PageIndicator

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/13 22:43
 */
class ContentFragment: BaseFragment<FragmentTvContentBinding, ContentViewModel>() {

    private var adapter = TvItemAdapter()

    override fun createViewModel(): ContentViewModel = generateViewModel(ContentViewModel::class.java)

    override fun getBinding(inflater: LayoutInflater): FragmentTvContentBinding = FragmentTvContentBinding.inflate(inflater)

    override fun initView(view: View) {
        mBinding.model = mModel
        mBinding.tvUpper.setOnClickListener { mModel.goUpper() }
        mBinding.rvList.layoutManager = GridLayoutManager(requireContext(), 4)
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildLayoutPosition(view)
                outRect.top = if (position / 4 == 0) {
                    0
                }
                else {
                    ScreenUtils.dp2px(10f)
                }
                outRect.left = if (position % 4 == 0) {
                    0
                }
                else {
                    ScreenUtils.dp2px(10f)
                }
            }
        })
        mBinding.pageIndicator.setOnPageListener {
            if (mModel.currentPage != it) {
                mModel.currentPage = it
                mModel.onPageChanged()
            }
        }
    }

    override fun initData() {
        mModel.totalPageObserver.observe(this, Observer {
            if (it == 1) {
                mBinding.pageIndicator.visibility = View.INVISIBLE
            }
            else {
                mBinding.pageIndicator.visibility = View.VISIBLE
                mBinding.pageIndicator.setPage(it)
            }
        })
        mModel.listObserver.observe(this,
            Observer { list ->
                if (mBinding.rvList.adapter == null) {
                    adapter.list = list
                    adapter.setOnItemClickListener(object :
                        BaseBindingAdapter.OnItemClickListener<FileBean> {
                        override fun onClickItem(view: View, position: Int, data: FileBean) {
                            if (data.isFolder) {
                                // clear filter when first tap it
                                mModel.clearFilter()
                                mModel.loadNewFolder(data)
                            } else {
                                playItem(data)
                            }
                        }
                    })
                    mBinding.rvList.adapter = adapter
                } else {
                    adapter.list = list
                    adapter.notifyDataSetChanged()
                }
            }
        )

        onUserChanged()
    }

    /**
     * 经实测，饺子播放器的默认JZMediaSystem引擎在小米电视上只出声音，不出画面（一直停止于转圈画面）
     * 切换为JZMediaIjk的引擎才能正常播放，所以可以用IjkPlayerActivity播放url，但是ijkplayer引擎又有个问题：不支持mkv和rmvb模式
     * 而且用饺子播放器其layout的controller部分基本上没法处理焦点问题，所以还是使用原生VideoView来做播放器
     */
    private fun playItem(data: FileBean) {
        try {
            val url = UrlUtil.toVideoUrl(data.sourceUrl)
            DebugLog.e("playUrl $url")
            url?.let {
                SystemPlayerActivity.startPage(requireContext(), it, data.path)
            }
        } catch (e: Exception) {
            showMessageShort("Unavailable url")
        }
    }

    fun onUserChanged() {
        mModel.isSuperUser = getActivityViewModel(TvViewModel::class.java).isSuperUser
        mModel.loadRoot()
    }

}