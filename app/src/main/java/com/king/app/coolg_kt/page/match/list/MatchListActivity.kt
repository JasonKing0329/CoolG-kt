package com.king.app.coolg_kt.page.match.list

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.HeadChildBindingAdapter
import com.king.app.coolg_kt.databinding.ActivityMatchListBinding
import com.king.app.coolg_kt.page.match.item.MatchActivity
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.entity.match.Match

/**
 * @description:
 * @author：Jing
 * @date: 2021/1/9 22:07
 */
class MatchListActivity: BaseActivity<ActivityMatchListBinding, MatchListViewModel>() {

    companion object {
        val EXTRA_SELECT_MODE = "select_mode"
        val RESP_MATCH_ID = "resp_match_id"
        fun startPage(context: Context) {
            var intent = Intent(context, MatchListActivity::class.java)
            context.startActivity(intent)
        }
        fun startPageToSelect(activity: Activity, requestCode: Int) {
            var intent = Intent(activity, MatchListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            activity.startActivityForResult(intent, requestCode)
        }
        fun startPageToSelect(fragment: Fragment, requestCode: Int) {
            var intent = Intent(fragment.context, MatchListActivity::class.java)
            intent.putExtra(EXTRA_SELECT_MODE, true)
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    val adapter = MatchItemAdapter()

    override fun getContentView(): Int = R.layout.activity_match_list

    override fun createViewModel(): MatchListViewModel = generateViewModel(MatchListViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_add -> editMatch(null, -1)
                R.id.menu_delete -> {
                    adapter.isDeleteMode = true
                    adapter.notifyDataSetChanged()
                    mBinding.actionbar.showConfirmStatus(it)
                }
            }
        }
        mBinding.actionbar.registerPopupMenuOn(
            R.id.menu_sort,
            R.menu.match_groupby
        ) {
            when(it.itemId) {
                R.id.menu_group_by_level -> mModel.groupByLevel()
                R.id.menu_group_by_week -> mModel.groupByWeek()
            }
            true
        }
        mBinding.actionbar.setOnConfirmListener {
            adapter.isDeleteMode = false
            adapter.notifyDataSetChanged()
            true
        }
        mBinding.actionbar.setOnCancelListener {
            adapter.isDeleteMode = false
            adapter.notifyDataSetChanged()
            true
        }
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = ScreenUtils.dp2px(8f)
            }
        })

        adapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<Match> {
            override fun onClickItem(view: View, position: Int, data: Match) {
                if (isSelectMode()) {
                    val intent = Intent()
                    intent.putExtra(RESP_MATCH_ID, data.id)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                else {
                    MatchActivity.startPage(this@MatchListActivity, data.id)
                }
            }
        }
        adapter.onMatchItemListener = object : MatchItemAdapter.OnMatchItemListener {
            override fun onEdit(position: Int, bean: Match) {
                editMatch(bean, position)
            }

            override fun onDelete(position: Int, bean: Match) {
                showConfirmCancelMessage("It will delete all records related to current match, continue?",
                    DialogInterface.OnClickListener { dialog, which ->
                        mModel.deleteMatch(bean)
                        mModel.loadMatches()
                    },
                    null)
            }
        }
        mBinding.rvList.adapter = adapter
    }

    private fun isSelectMode(): Boolean {
        return intent.getBooleanExtra(EXTRA_SELECT_MODE, false)
    }

    override fun initData() {
        mModel.matchesObserver.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()

            if (isSelectMode()) {
                mBinding.rvList.scrollToPosition(mModel.jumpTo())
            }
        })

        mModel.loadMatches()
    }

    private fun editMatch(editMatch: Match?, position: Int) {
        val content = MatchEditor()
        content.match = editMatch
        content.onMatchListener = object : MatchEditor.OnMatchListener {
            override fun onUpdated(match: Match) {
                if (editMatch == null) {
                    mModel.loadMatches()
                }
                else {
                    adapter.notifyItemChanged(position)
                }
            }
        }
        val dialogFragment = DraggableDialogFragment()
        val title = if (editMatch == null) "New match" else "Edit match"
        dialogFragment.setTitle(title)
        dialogFragment.contentFragment = content
        dialogFragment.maxHeight = ScreenUtils.getScreenHeight() * 2 / 3
        dialogFragment.show(supportFragmentManager, "MatchEditor")
    }

}