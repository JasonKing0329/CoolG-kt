package com.king.app.coolg_kt.page.match.item

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchWallBinding
import com.king.app.coolg_kt.page.match.WallItem
import com.king.app.coolg_kt.page.match.detail.RoadDialog
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/9 16:02
 */
class WallActivity: BaseActivity<ActivityMatchWallBinding, ChampionWallViewModel>() {

    companion object {
        val TYPE = "type"
        fun startPageGs(context: Context) {
            var intent = Intent(context, WallActivity::class.java)
            intent.putExtra(TYPE, 0)
            context.startActivity(intent)
        }
        fun startPageGM1000(context: Context) {
            var intent = Intent(context, WallActivity::class.java)
            intent.putExtra(TYPE, 1)
            context.startActivity(intent)
        }
    }

    val titleAdapter = WallAdapter()
    val adapter = WallAdapter()

    fun getType(): Int {
        return intent.getIntExtra(TYPE, 0)
    }

    override fun getContentView(): Int = R.layout.activity_match_wall

    override fun createViewModel(): ChampionWallViewModel = generateViewModel(ChampionWallViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        // GM1000
        if (getType() == 1) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        if (getType() == 1) {
            mBinding.actionbar.setTitle("GS Wall")
        }
        else {
            mBinding.actionbar.setTitle("GM1000 Wall")
        }

        // titles与items复用同样属性的layoutManager，在视觉效果上达到统一
        mBinding.rvTitles.layoutManager = createLayoutManager(true, titleAdapter)
        mBinding.rvTitles.adapter = titleAdapter
        mBinding.rvList.layoutManager = createLayoutManager(false, adapter)
        mBinding.rvList.adapter = adapter

        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<WallItem> {
            override fun onClickItem(view: View, position: Int, data: WallItem) {
                data.recordId?.let {
                    showRoadDialog(it, data.matchPeriodId!!)
                }
            }
        })
    }

    private fun showRoadDialog(recordId: Long, matchPeriodId: Long) {
        var content = RoadDialog()
        content.matchPeriodId = matchPeriodId
        content.recordId = recordId
        var dialog = DraggableDialogFragment()
        dialog.setTitle("Upgrade Road")
        dialog.contentFragment = content
        dialog.fixedHeight = content.idealHeight
        dialog.show(supportFragmentManager, "RoadDialog")
    }

    private fun createLayoutManager(isTitle: Boolean, adapter: WallAdapter): RecyclerView.LayoutManager {
        val manager = GridLayoutManager(this, adapter.getColumn(getType()))
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (isTitle) {
                    return adapter.getTitleSpanSize(position)
                }
                else {
                    return adapter.getSpanSize(position)
                }
            }
        }
        return manager
    }

    override fun initData() {
        mModel.titlesObserver.observe(this, Observer {
            titleAdapter.list = it
            titleAdapter.notifyDataSetChanged()
        })
        mModel.itemsObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
        mModel.rangeChangedObserver.observe(this, Observer {
            adapter.notifyItemRangeChanged(it.start, it.count)
        })
        if (getType() == 1) {
            mModel.loadGm1000Items()
        }
        else {
            mModel.loadGsItems()
        }
    }
}