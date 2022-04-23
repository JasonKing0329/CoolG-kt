package com.king.app.coolg_kt.page.star.phone

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityStarRelationBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.star.list.StarSelectorActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.relation.NetAdapter
import com.king.app.coolg_kt.view.widget.relation.RelationItem

class StarRelationActivity: BaseActivity<ActivityStarRelationBinding, StarRelationViewModel>() {

    companion object {

        fun startPage(context: Context) {
            var intent = Intent(context, StarRelationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_star_relation

    override fun createViewModel(): StarRelationViewModel = generateViewModel(StarRelationViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_add -> StarSelectorActivity.startPage(this, false, 0, 1)
                R.id.menu_clear -> mModel.clear()
            }
        }
    }

    override fun initData() {
        mModel.relations.observe(this) {
            mBinding.relationGroup.adapter = RelationAdapter(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.getCharSequenceArrayListExtra(StarSelectorActivity.RESP_SELECT_RESULT)?.apply {
                mModel.loadRelations(this)
            }
        }
    }

    class RelationAdapter(val list: List<RelationItem>?): NetAdapter() {

        override fun getRelatedPositions(position: Int): List<Int> {
            return list?.get(position)?.lineRelations?: listOf()
        }

        override fun getCount(): Int {
            return list?.size?:0
        }

        override fun getView(parent: View, position: Int): View {
            return ImageView(parent.context).apply {
                val size = ScreenUtils.dp2px(50F)
                layoutParams = ViewGroup.MarginLayoutParams(size, size)
                scaleType = ImageView.ScaleType.CENTER_CROP
                ImageBindingAdapter.setStarUrl(this, list?.get(position)?.imageUrl)
            }
        }
    }
}