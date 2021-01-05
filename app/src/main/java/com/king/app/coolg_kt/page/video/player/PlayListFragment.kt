package com.king.app.coolg_kt.page.video.player

import android.content.DialogInterface
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.FragmentVideoPlayListBinding
import com.king.app.coolg_kt.model.bean.PlayList
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.coolg_kt.page.record.popup.RecommendFragment
import com.king.app.coolg_kt.page.record.popup.RecommendFragment.OnRecommendListener
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/11/16 11:34
 */
class PlayListFragment : BaseFragment<FragmentVideoPlayListBinding, EmptyViewModel>() {
    
    private lateinit var playerViewModel: PlayerViewModel
    private var adapter = PlayListAdapter()
    
    override fun getBinding(inflater: LayoutInflater): FragmentVideoPlayListBinding = FragmentVideoPlayListBinding.inflate(inflater)
    
    override fun createViewModel(): EmptyViewModel = emptyViewModel()
    
    override fun initView(view: View) {
        
        playerViewModel = getActivityViewModel(PlayerViewModel::class.java)
        
        mBinding.model = playerViewModel
        mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mBinding.ivClose.setOnClickListener { playerViewModel.closeListObserver.value = true }
        mBinding.ivClear.setOnClickListener { v: View? ->
            showConfirmCancelMessage(
                "This action will clear all play items, continue?",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> playerViewModel.clearAll() },
                null
            )
        }
        mBinding.tvPlayMode.setOnClickListener { playerViewModel.switchPlayMode() }
        mBinding.tvTitle.isSelected = true
        mBinding.tvTitle.setOnClickListener {
            playerViewModel.setIsCustomRandomPlay(false)
            mBinding.tvTitle.isSelected = true
            mBinding.tvRandom.isSelected = false
            mBinding.ivRandomSetting.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_IN)
        }
        mBinding.tvRandom.setOnClickListener {
            playerViewModel.setIsCustomRandomPlay(true)
            mBinding.tvRandom.isSelected = true
            mBinding.tvTitle.isSelected = false
            mBinding.ivRandomSetting.setColorFilter(resources.getColor(R.color.yellowF7D23E), PorterDuff.Mode.SRC_IN)
        }
        mBinding.ivRandomSetting.setOnClickListener { v: View? -> setRecommend() }
    }

    private fun setRecommend() {
        val content = RecommendFragment()
        content.isHideOnline = true
        content.mBean = SettingProperty.getVideoRecBean()
        content.onRecommendListener = object : OnRecommendListener {
            override fun onSetSql(bean: RecommendBean) {
                playerViewModel.updateRecommend(bean)
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.setTitle("Recommend Setting")
        dialogFragment.contentFragment = content
        dialogFragment.maxHeight = ScreenUtils.getScreenHeight()
        dialogFragment.show(childFragmentManager, "RecommendFragment")
    }

    override fun initData() {

        adapter.enableDelete = true
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<PlayList.PlayItem> {
            override fun onClickItem(view: View, position: Int, data: PlayList.PlayItem) {
                playerViewModel.playItem(data, position)
            }
        })
        adapter.onDeleteListener = object : PlayListAdapter.OnDeleteListener {
            override fun onDelete(position: Int, bean: PlayList.PlayItem) {
                playerViewModel.deletePlayItem(position, bean)
                adapter.notifyItemRemoved(position)
            }
        }
        mBinding.rvList.adapter = adapter

        playerViewModel.loadPlayItems()
    }

    fun focusToIndex(position: Int) {
        mBinding.rvList.scrollToPosition(position)
        adapter.mPlayIndex = position
        adapter.notifyDataSetChanged()
    }

    fun showList(list: List<PlayList.PlayItem>) {
        adapter.list = list
        adapter.notifyDataSetChanged()
    }
}