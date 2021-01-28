package com.king.app.coolg_kt.page.video.order

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.Jzvd
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityPlayerOrderItemsBinding
import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.page.record.phone.RecordActivity
import com.king.app.coolg_kt.page.video.PlayItemAdapter
import com.king.app.coolg_kt.page.video.player.PlayerActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.video.OnPlayEmptyUrlListener
import com.king.app.gdb.data.entity.Record

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/4 11:38
 */
class PlayOrderItemsActivity:
    BaseActivity<ActivityPlayerOrderItemsBinding, PlayOrderItemsViewModel>() {

    var adapter = PlayItemAdapter()

    companion object {

        const val EXTRA_STAR_ID = "key_star_id"
        const val EXTRA_ORDER_ID = "order_id"

        fun playStar(context: Context, starId: Long) {
            var intent = Intent(context, PlayOrderItemsActivity::class.java)
            intent.putExtra(EXTRA_STAR_ID, starId)
            context.startActivity(intent)
        }
        fun playOrder(context: Context, orderId: Long) {
            var intent = Intent(context, PlayOrderItemsActivity::class.java)
            intent.putExtra(EXTRA_ORDER_ID, orderId)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_player_order_items

    override fun createViewModel(): PlayOrderItemsViewModel = generateViewModel(PlayOrderItemsViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.rvVideos.layoutManager = GridLayoutManager(this, 2)
        mBinding.rvVideos.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = ScreenUtils.dp2px(8f)
            }
        })
        registerVideoList(mBinding.rvVideos)

        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener { menuId ->
            when (menuId) {
                R.id.menu_play_sequence -> playList(false)
                R.id.menu_play_random -> playList(true)
                R.id.menu_clear -> showConfirmCancelMessage("Clear all play items?"
                    , DialogInterface.OnClickListener { dialog, which -> mModel.clearOrder() }
                    , null)
            }
        }
        if (isStarPage()) {
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_clear, false)
        }
    }

    private fun isStarPage(): Boolean = getStarId() != -1L

    private fun playItem() {
        PlayerActivity.startPage(this@PlayOrderItemsActivity, false)
    }

    private fun playList(isRandom: Boolean) {
        showNeutralMessage("是否清空当前播放列表，如果不清空，将会执行当前播放列表的播放模式"
            , "清空",
            DialogInterface.OnClickListener { dialog, which ->
                mModel.createPlayList(true, isRandom)
            }
            , "保留",
            DialogInterface.OnClickListener { dialog, which ->
                mModel.createPlayList(false, isRandom)
            }
            , "取消", null)
    }

    override fun initData() {

        mModel.itemsObserver.observe(this, Observer{ showItems(it) })
        mModel.playListCreated.observe(this, Observer{ created ->
            if (created) {
                playItem()
            }
        })
        mModel.videoPlayOnReadyObserver.observe(this, Observer{ playItem() })
        mModel.definePage(getOrderId(), getStarId())
    }

    private fun getOrderId(): Long {
        return intent.getLongExtra(EXTRA_ORDER_ID, -1)
    }

    private fun getStarId(): Long {
        return intent.getLongExtra(EXTRA_STAR_ID, -1)
    }

    private fun showItems(list: List<PlayItemViewBean>) {
        if (mBinding.rvVideos.adapter == null) {
            adapter.enableDelete = !isStarPage()
            adapter.onPlayItemListener = object : PlayItemAdapter.OnPlayItemListener {
                override fun onPlayItem(position: Int, bean: PlayItemViewBean) {
                    mModel.playItem(bean)
                }

                override fun onDeleteItem(position: Int, bean: PlayItemViewBean) {
                    mModel.deleteItem(position)
                    setResultChanged()
                }
            }
            adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<PlayItemViewBean> {
                override fun onClickItem(view: View, position: Int, data: PlayItemViewBean) {
                    goToRecordPage(data.record.bean)
                }
            })
            adapter.onPlayEmptyUrlListener = OnPlayEmptyUrlListener { position, callback ->
                mModel.getPlayUrl(position, callback)
            }
            adapter.list = list
            mBinding.rvVideos.adapter = adapter
        } else {
            adapter.list = list
            adapter.notifyDataSetChanged()
        }
    }

    private fun goToRecordPage(record: Record) {
        if (ScreenUtils.isTablet()) {

        }
        else {
            RecordActivity.startPage(this, record.id!!)
        }
    }

    fun setResultChanged() {
        setResult(RESULT_OK)
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }

}