package com.king.app.coolg_kt.page.star.phone

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityStarRelationBinding
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.coolg_kt.model.bean.HistoryRelationItem
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.page.pub.TagAdapter
import com.king.app.coolg_kt.page.star.list.StarSelectorActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.CoverView
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.coolg_kt.view.widget.relation.NetAdapter
import com.king.app.coolg_kt.view.widget.relation.RelationItem
import com.king.app.gdb.data.entity.FavorRecordOrder

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
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.rvHistory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        mBinding.cbFocusMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                (mBinding.relationGroup.adapter as RelationAdapter).isFocusMode = false
                mBinding.relationGroup.cancelFocusOn()
            }
        }
    }

    override fun initData() {
        mModel.relations.observe(this) {
            mBinding.relationGroup.adapter = RelationAdapter(it).apply {
                onItemListener = object : OnItemListener {
                    override fun onClickItem(position: Int, item: RelationItem) {
                        if (mBinding.cbFocusMode.isChecked) {
                            isFocusMode = true
                            mBinding.relationGroup.focusOn(position)
                        }
                        else {
                            StarActivity.startPage(this@StarRelationActivity, item.starId)
                        }
                    }

                    override fun onLongClickItem(position: Int, item: RelationItem) {
                        showConfirmCancelMessage(
                            "Delete item?",
                            { dialog, which -> mModel.deleteStar(item.starId) },
                            null
                        )
                    }
                }
            }
        }

        HistoryAdapter().apply {
            list = SettingProperty.getHistoryRelations().list
            listenerClick = object : BaseBindingAdapter.OnItemClickListener<HistoryRelationItem> {
                override fun onClickItem(view: View, position: Int, data: HistoryRelationItem) {
                    mModel.mCurrentHistory = data
                    mModel.clear()
                    mModel.loadRelationsByIds(data.list)
                }
            }
            mBinding.rvHistory.adapter = this
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

    override fun onBackPressed() {
        showNeutralMessage(
            "Save current relationships?",
            getString(R.string.yes),
            { dialog, which ->
                if (mModel.needInputName()) {
                    SimpleDialogs().openInputDialog(this, "History name") {
                        if (!mModel.isValidName(it)) {
                            showMessageShort("Name may be already existed, please change one")
                            return@openInputDialog
                        }
                        mModel.saveAsHistory(it)
                        doBack()
                    }
                } else {
                    mModel.saveHistory()
                    doBack()
                }
            },
            getString(R.string.no),
            { dialog, which -> doBack() },
            getString(R.string.cancel),
            null
        )
    }

    private fun doBack() {
        super.onBackPressed()
    }

    class RelationAdapter(val list: List<RelationItem>?): NetAdapter() {

        var onItemListener: OnItemListener? = null

        var isFocusMode = false

        override fun getRelatedPositions(position: Int): List<Int> {
            return if (isFocusMode) {
                list?.get(position)?.lineRelationsWhenFocus?: listOf()
            }
            else {
                list?.get(position)?.lineRelations?: listOf()
            }
        }

        override fun getCount(): Int {
            return list?.size?:0
        }

        override fun getView(parent: View, position: Int): View {
            return CoverView(parent.context).apply {
                val size = ScreenUtils.dp2px(50F)
                layoutParams = ViewGroup.MarginLayoutParams(size, size)
                setCoverRadius(0)
                setTextSize(ScreenUtils.dp2px(10F).toFloat())
                list?.get(position)?.let { item ->
                    setCoverText("${item.starName}\n${item.allRelations.size}")
                    ImageBindingAdapter.setCoverStarUrl(this, item.imageUrl)
                    setOnClickListener { onItemListener?.onClickItem(position, item) }
                    setOnLongClickListener {
                        onItemListener?.onLongClickItem(position, item)
                        true
                    }
                }
            }
        }
    }

    class HistoryAdapter : BaseBindingAdapter<AdapterTagItemBinding, HistoryRelationItem>() {
        var selection = -1

        override fun onCreateBind(
            inflater: LayoutInflater,
            parent: ViewGroup): AdapterTagItemBinding = AdapterTagItemBinding.inflate(inflater, parent, false)

        override fun onBindItem(
            binding: AdapterTagItemBinding,
            position: Int,
            bean: HistoryRelationItem
        ) {
            binding.tvName.text = bean.name
            binding.ivRemove.visibility = View.GONE
            binding.tvName.isSelected = position == selection
        }

        override fun onClickItem(v: View, position: Int, bean: HistoryRelationItem) {
            if (position != selection) {
                super.onClickItem(v, position, bean)
            }
            selection = position
            notifyDataSetChanged()
        }
    }

    interface OnItemListener {
        fun onClickItem(position: Int, item: RelationItem)
        fun onLongClickItem(position: Int, item: RelationItem)
    }
}