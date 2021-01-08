package com.king.app.coolg_kt.page.star.random

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityStarRandomBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.page.star.list.StarSelectorActivity
import com.king.app.coolg_kt.page.star.phone.StarActivity
import com.king.app.coolg_kt.utils.ColorUtil
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/1/8 15:51
 */
class StarRandomActivity: BaseActivity<ActivityStarRandomBinding, StarRandomViewModel>() {

    companion object {
        val REQUEST_SELECT_STAR = 421
        fun startPage(context: Context) {
            var intent = Intent(context, StarRandomActivity::class.java)
            context.startActivity(intent)
        }
    }

    var candidateAdapter = CandidateAdapter()
    var selectedAdapter = CandidateAdapter()

    override fun getContentView(): Int = R.layout.activity_star_random

    override fun createViewModel(): StarRandomViewModel = generateViewModel(StarRandomViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvSelected.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.btnRule.setOnClickListener {
            mModel.stopRandom()
            showRuleSetting()
        }
        mBinding.btnMark.setOnClickListener {
            mModel.stopRandom()
            mModel.markCurrentStar()
        }
        mBinding.btnReset.setOnClickListener {
            mModel.stopRandom()
            showConfirmCancelMessage("This action will clear all data in current page, continue?"
                , DialogInterface.OnClickListener { dialog, which -> mModel.clearAll() }
                , null)
        }
        mBinding.ivStar.setOnClickListener { v ->
            if (mModel.getCurrentStar() != null) {
                StarActivity.startPage(this@StarRandomActivity, mModel.getCurrentStar()!!.bean.id!!)
            }
        }

        val color: Int = mModel.iconColor
        ColorUtil.updateIconColor(mBinding.btnReset, color)
        ColorUtil.updateIconColor(mBinding.btnMark, color)
        ColorUtil.updateIconColor(mBinding.btnRule, color)
        ColorUtil.updateIconColor(mBinding.btnStart, color)

        onDeleteModeChanged()
    }

    override fun initData() {
        mModel.candidatesObserver.observe(this, Observer{ list -> showCandidates(list) })
        mModel.selectedObserver.observe(this, Observer{ list -> showSelectedList(list) })
        mModel.starObserver.observe(this, Observer{ star -> showStar(star) })
        mModel.loadDefaultData()
    }

    private fun showStar(star: StarWrap) {
        var request = GlideApp.with(this)
            .load(star.imagePath)
        if (star.width != 0 && star.width != 0) {
            request.override(star.width!!, star.height!!)
        }
        request.error(R.drawable.def_person_square)
            .into(mBinding.ivStar)
    }

    private fun showRuleSetting() {
        val content = RandomSettingFragment()
        content.randomRule = mModel.randomRule
        content.onSettingListener = object : RandomSettingFragment.OnSettingListener{
            override fun onSetRule(randomRule: RandomRule) {
                mModel.randomRule = randomRule
                mModel.resetRandomList()
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Random rules")
        dialogFragment.show(supportFragmentManager, "RandomSettingFragment")
    }

    private fun onDeleteModeChanged() {
        if (candidateAdapter.isDeleteMode) {
            mBinding.ivRange.setImageResource(R.drawable.ic_delete_grey_600_24dp)
            mBinding.ivRange.setOnClickListener {
                mModel.clearCandidates()
                candidateAdapter.isDeleteMode = false
                onDeleteModeChanged()
            }
        } else {
            mBinding.ivRange.setImageResource(R.drawable.ic_add_grey_600_36dp)
            mBinding.ivRange.setOnClickListener {
                StarSelectorActivity.startPage(this@StarRandomActivity, false, 0, REQUEST_SELECT_STAR)
            }
        }
    }

    private fun showCandidates(list: List<StarWrap>) {
        candidateAdapter.list = list
        if (mBinding.rvList.adapter == null) {
            candidateAdapter.onDeleteListener = object : CandidateAdapter.OnDeleteListener{
                override fun onDeleteCandidate(position: Int, star: StarWrap) {
                    mModel.deleteCandidate(star)
                }
            }
            candidateAdapter.setOnItemLongClickListener(object : BaseBindingAdapter.OnItemLongClickListener<StarWrap> {
                override fun onLongClickItem(view: View, position: Int, data: StarWrap) {
                    candidateAdapter.isDeleteMode = !candidateAdapter.isDeleteMode
                    onDeleteModeChanged()
                    candidateAdapter.notifyDataSetChanged()
                }
            })
            mBinding.rvList.adapter = candidateAdapter
        } else {
            candidateAdapter.notifyDataSetChanged()
        }
        if (list.isNotEmpty()) {
            mBinding.tvList.visibility = View.GONE
        } else {
            mBinding.tvList.visibility = View.VISIBLE
            candidateAdapter.isDeleteMode = false
            onDeleteModeChanged()
        }
    }

    private fun showSelectedList(list: List<StarWrap>) {
        selectedAdapter.list = list
        if (mBinding.rvSelected.adapter == null) {
            selectedAdapter.onDeleteListener = object : CandidateAdapter.OnDeleteListener{
                override fun onDeleteCandidate(position: Int, star: StarWrap) {
                    mModel.deleteSelected(star)
                }
            }
            selectedAdapter.setOnItemLongClickListener(object : BaseBindingAdapter.OnItemLongClickListener<StarWrap>{
                override fun onLongClickItem(view: View, position: Int, data: StarWrap) {
                    selectedAdapter.isDeleteMode = !selectedAdapter.isDeleteMode
                    selectedAdapter.notifyDataSetChanged()
                }
            })
            mBinding.rvSelected.adapter = selectedAdapter
        } else {
            selectedAdapter.notifyDataSetChanged()
        }
        if (list.isEmpty()) {
            selectedAdapter.isDeleteMode = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_STAR) {
            if (resultCode == RESULT_OK) {
                val list = data?.getCharSequenceArrayListExtra(StarSelectorActivity.RESP_SELECT_RESULT)
                mModel.setCandidates(list)
            }
        }
    }
}