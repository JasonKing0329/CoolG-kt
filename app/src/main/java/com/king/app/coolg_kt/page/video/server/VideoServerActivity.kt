package com.king.app.coolg_kt.page.video.server

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.databinding.ActivityVideoServerBinding
import com.king.app.coolg_kt.model.http.bean.data.FileBean
import com.king.app.coolg_kt.page.video.player.PlayerActivity
import com.king.app.coolg_kt.page.video.server.FileAdapter.OnActionListener
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.UrlUtil

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/11/11 9:11
 */
class VideoServerActivity : BaseActivity<ActivityVideoServerBinding, VideoServerViewModel>() {

    companion object {
        fun startPage(context: Context) {
            var intent = Intent(context, VideoServerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var adapter = FileAdapter()
    
    override fun getContentView(): Int = R.layout.activity_video_server
    
    override fun createViewModel(): VideoServerViewModel = generateViewModel(VideoServerViewModel::class.java)
    
    override fun initView() {
        mBinding.model = mModel
        mBinding.tvUpper.setOnClickListener { mModel.goUpper() }
        mBinding.actionbar.setOnBackListener { super.onBackPressed() }
        mBinding.actionbar.setOnSearchListener { text -> mModel.onFilterChanged(text) }
        mBinding.actionbar.setOnMenuItemListener { menuId ->
            when (menuId) {
                R.id.menu_refresh -> mModel.refresh()
            }
        }
        mBinding.actionbar.registerPopupMenuOn(
            R.id.menu_sort,
            R.menu.sort_video_server
        ) { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_sort_by_name -> mModel.onSortTypeChanged(PreferenceValue.VIDEO_SERVER_SORT_NAME)
                R.id.menu_sort_by_date -> mModel.onSortTypeChanged(PreferenceValue.VIDEO_SERVER_SORT_DATE)
                R.id.menu_sort_by_size -> mModel.onSortTypeChanged(PreferenceValue.VIDEO_SERVER_SORT_SIZE)
            }
            true
        }
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onBackPressed() {
        if (mModel.backFolder()) {
            return
        }
        super.onBackPressed()
    }

    override fun initData() {
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
                                try {
                                    val url = UrlUtil.toVideoUrl(data.sourceUrl)
                                    DebugLog.e("playUrl $url")
                                    mModel.createPlayList(url)
                                    playUrl()
                                } catch (e: Exception) {
                                    showMessageShort("Unavailable url")
                                }
                            }
                        }
                    })
                    adapter.onActionListener = object : OnActionListener {
                        override fun onOpenServer(bean: FileBean) {
                            mModel.openFile(bean)
                        }
                    }
                    mBinding.rvList.adapter = adapter
                } else {
                    adapter.list = list
                    adapter.notifyDataSetChanged()
                }
            }
        )
        mModel.loadNewFolder(null)
    }

    private fun playUrl() {
        PlayerActivity.startPage(this, false)
    }
}