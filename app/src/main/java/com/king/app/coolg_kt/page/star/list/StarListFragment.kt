package com.king.app.coolg_kt.page.star.list

import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.FragmentStarRichBinding
import com.king.app.coolg_kt.page.star.OnStarRatingListener
import com.king.app.coolg_kt.page.star.StarRatingDialog
import com.king.app.coolg_kt.page.star.phone.StarActivity
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.widget.FitSideBar.OnSidebarStatusListener
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 14:52
 */
class StarListFragment : BaseFragment<FragmentStarRichBinding, StarListViewModel>(), OnStarRatingListener {

    companion object {
        private const val ARG_STAR_TYPE = "star_type"
        private const val ARG_STUDIO_ID = "studio_id"
        fun newInstance(type: String): StarListFragment {
            val fragment = StarListFragment()
            val bundle = Bundle()
            bundle.putString(ARG_STAR_TYPE, type)
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(type: String, studioId: Long): StarListFragment {
            val fragment = StarListFragment()
            val bundle = Bundle()
            bundle.putString(ARG_STAR_TYPE, type)
            bundle.putLong(ARG_STUDIO_ID, studioId)
            fragment.arguments = bundle
            return fragment
        }
    }

    var mCircleAdapter = StarCircleAdapter()
    var mRichAdapter = StarRichAdapter()
    var holder: IStarListHolder? = null

    override fun getBinding(inflater: LayoutInflater): FragmentStarRichBinding = FragmentStarRichBinding.inflate(inflater)

    override fun createViewModel(): StarListViewModel = generateViewModel(StarListViewModel::class.java)
    
    override fun initView(view: View) {
        mBinding.model = mModel
        mBinding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // 按音序排列，在滑动过程中显示当前的详细index
                if (mModel.mSortMode == AppConstants.STAR_SORT_NAME) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> updateDetailIndex()
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                        }
                        else -> holder?.hideDetailIndex()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                //在这里进行第二次滚动（最后的距离）
                if (needMove) {
                    needMove = false
                    //获取要置顶的项在当前屏幕的位置，mIndex是记录的要置顶项在RecyclerView中的位置
                    val n =
                        nSelection - (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (n >= 0 && n < recyclerView.childCount) {
                        recyclerView.scrollBy(0, recyclerView.getChildAt(n).top) //滚动到顶部
                    }
                }
                if (recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    updateDetailIndex()
                }
            }
        })
        mBinding.sidebar.setOnSidebarStatusListener(object : OnSidebarStatusListener {
            override fun onChangeFinished() {
                mBinding.tvIndexPopup.visibility = View.GONE
            }

            override fun onSideIndexChanged(index: String) {
                val selection = mModel.getLetterPosition(index)
                scrollToPosition(selection)
                mBinding.tvIndexPopup.text = index
                mBinding.tvIndexPopup.visibility = View.VISIBLE
            }
        })
    }

    private var needMove = false
    private var nSelection = 0
    private fun scrollToPosition(selection: Int) {
        nSelection = selection
        val manager = mBinding.rvList.layoutManager as LinearLayoutManager
        val fir = manager.findFirstVisibleItemPosition()
        val end = manager.findLastVisibleItemPosition()
        when {
            selection <= fir -> {
                mBinding.rvList.scrollToPosition(selection)
            }
            selection <= end -> {
                val top = mBinding.rvList.getChildAt(selection - fir).top
                mBinding.rvList.scrollBy(0, top)
            }
            else -> {
                //当要置顶的项在当前显示的最后一项的后面时
                mBinding.rvList.scrollToPosition(selection)
                //记录当前需要在RecyclerView滚动监听里面继续第二次滚动
                needMove = true
            }
        }
    }

    private fun updateDetailIndex() {
        var position = -1
        val manager = mBinding.rvList.layoutManager
        if (manager is LinearLayoutManager) {
            position = manager.findFirstVisibleItemPosition()
        } else if (manager is GridLayoutManager) {
            position = manager.findFirstVisibleItemPosition()
        }
        var name: String? = null
        if (position != -1) {
            name = mModel.getDetailIndex(position)
        }
        name?.let {
            holder?.updateDetailIndex(it)
        }
    }

    override fun initData() {
        mModel.indexObserver.observe(this) {
            mBinding.sidebar.clear()
            it.forEach { index ->
                mBinding.sidebar.addIndex(index)
            }
            mBinding.sidebar.build()
            mBinding.sidebar.visibility = View.VISIBLE
        }
        mModel.circleListObserver.observe(this,
            Observer { list: List<StarWrap> ->
                showCircleList(list)
            }
        )
        mModel.richListObserver.observe(this,
            Observer { list: List<StarWrap> ->
                showRichList(list)
            }
        )
        mModel.imageChanged.observe(this, Observer {
            mBinding.rvList.adapter?.notifyItemRangeChanged(it.start, it.count)
        })
        mModel.circleUpdateObserver.observe(this,
            Observer {
                mCircleAdapter.notifyDataSetChanged()
            }
        )
        mModel.richUpdateObserver.observe(this,
            Observer {
                mRichAdapter.notifyDataSetChanged()
            }
        )
        mBinding.sidebar.clear()
        mModel.mStarType = requireArguments().getString(ARG_STAR_TYPE)?:DataConstants.STAR_MODE_ALL
        mModel.mStudioId = requireArguments().getLong(ARG_STUDIO_ID)
        mModel.loadStarList()
    }

    private val richDecoration: ItemDecoration = object : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = ScreenUtils.dp2px(10f)
        }
    }

    private fun showRichList(list: List<StarWrap>) {
        mBinding.rvList.layoutManager = LinearLayoutManager(activity)
        mBinding.rvList.removeItemDecoration(richDecoration)
        mBinding.rvList.addItemDecoration(richDecoration)
        mRichAdapter.list = list
        mRichAdapter.mExpandMap = mModel.expandMap
        mRichAdapter.onStarRatingListener = this
        mRichAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<StarWrap> {
            override fun onClickItem(view: View, position: Int, data: StarWrap) {
                onStarClick(data)
            }
        })
        mBinding.rvList.adapter = mRichAdapter
    }

    private fun showCircleList(list: List<StarWrap>) {
        mBinding.rvList.removeItemDecoration(richDecoration)
        val column = 2
        mBinding.rvList.layoutManager = GridLayoutManager(activity, column)
        mCircleAdapter.list = list
        mCircleAdapter.onStarRatingListener = this
        mCircleAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<StarWrap> {
            override fun onClickItem(view: View, position: Int, data: StarWrap) {
                onStarClick(data )
            }
        })
        mCircleAdapter.setOnItemLongClickListener(object : BaseBindingAdapter.OnItemLongClickListener<StarWrap> {
            override fun onLongClickItem(view: View, position: Int, data: StarWrap) {
                onStarLongClick(data)
            }
        })
        mBinding.rvList.adapter = mCircleAdapter
    }

    fun onStarClick(star: StarWrap) {
        val consumed = holder?.dispatchClickStar(star.bean)
        if (consumed == true) {
            return
        }
        StarActivity.startPage(requireContext(), star.bean.id!!)
    }

    fun onStarLongClick(star: StarWrap) {
        holder?.dispatchOnLongClickStar(star.bean)
    }

    override fun onUpdateRating(position: Int, starId: Long) {
        val dialog = StarRatingDialog()
        dialog.starId = starId
        dialog.onDismissListener = DialogInterface.OnDismissListener {
            mCircleAdapter.notifyStarChanged(starId)
            mRichAdapter.notifyStarChanged(starId)
        }
        dialog.show(childFragmentManager, "StarRatingDialog")
    }

    fun onViewModeChanged() {
        if (!mModel.isLoading) {
            DebugLog.e(requireArguments().getString(ARG_STAR_TYPE))
            mBinding.sidebar.clear()
            mModel.loadStarList()
        }
    }

    fun setExpandAll(expand: Boolean) {
        mModel.setExpandAll(expand)
        mCircleAdapter.notifyDataSetChanged()
        mRichAdapter.notifyDataSetChanged()
    }

    fun updateSortType(sortMode: Int) {
        mBinding.sidebar.clear()
        mModel.mSortMode = sortMode
    }

    fun toggleSidebar() {
        mBinding.sidebar.visibility =
            if (mBinding.sidebar.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    fun filterStar(text: String) {
        mModel.onKeywordChanged(text)
    }

    fun updateStarType(type: String) {
        mModel.mStarType = type
        mModel.loadStarList()
    }

    fun updateStudioId(studioId: Long) {
        mModel.mStudioId = studioId
        mModel.loadStarList()
    }

    override fun onResume() {
        DebugLog.e(requireArguments().getString(ARG_STAR_TYPE))
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        DebugLog.e(requireArguments().getString(ARG_STAR_TYPE) + " --> hidden" + hidden)
        super.onHiddenChanged(hidden)
    }

    val isNotScrolling: Boolean
        get() = mBinding.rvList.scrollState == RecyclerView.SCROLL_STATE_IDLE

    fun goTop() {
        mBinding.rvList.scrollToPosition(0)
    }

}