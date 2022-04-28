package com.king.app.coolg_kt.page.star.phone

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseFragment
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityStarListPhoneBinding
import com.king.app.coolg_kt.databinding.AdapterTagItemBinding
import com.king.app.coolg_kt.databinding.AdapterTagItemStarlistBinding
import com.king.app.coolg_kt.model.bean.StarTypeWrap
import com.king.app.coolg_kt.model.bean.StudioStarWrap
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.image.ImageProvider.getStarRandomPath
import com.king.app.coolg_kt.model.setting.ViewProperty
import com.king.app.coolg_kt.page.pub.BannerSettingFragment
import com.king.app.coolg_kt.page.pub.BannerSettingFragment.OnAnimSettingListener
import com.king.app.coolg_kt.page.pub.StudioTagAdapter
import com.king.app.coolg_kt.page.star.list.IStarListHolder
import com.king.app.coolg_kt.page.star.list.StarListFragment
import com.king.app.coolg_kt.page.star.list.StarListPagerAdapter
import com.king.app.coolg_kt.utils.BannerHelper
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.Star
import com.king.lib.banner.CoolBannerAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 17:04
 */
class StarListClassicFragment : BaseFragment<ActivityStarListPhoneBinding, StarListTitleViewModel>(), IStarListHolder {

    private lateinit var ftStar: StarListFragment

    var studioId: Long = 0

    var onClickStarListener: OnClickStarListener? = null

    /**
     * 控制detail index显示的timer
     */
    private var indexDisposable: Disposable? = null
    private var curDetailIndex: String? = null

    override fun getBinding(inflater: LayoutInflater): ActivityStarListPhoneBinding = ActivityStarListPhoneBinding.inflate(inflater)

    override fun initView(view: View) {
        initActionbar()
        BannerHelper.setBannerParams(mBinding.banner, ViewProperty.getStarBannerParams())
        // hide head banner when it's studio star page
        if (isStudioStarPage) {
            mBinding.rlBanner.visibility = View.GONE
            // disable scroll
            val params =
                mBinding.ctlToolbar.layoutParams as AppBarLayout.LayoutParams
            params.scrollFlags = 0
            mBinding.ctlToolbar.layoutParams = params
        } else {
            initRecommend()
        }

        mBinding.rvTypes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvStudios.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val decoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildLayoutPosition(view)
                if (position > 0) {
                    outRect.left = ScreenUtils.dp2px(8F)
                }
            }
        }
        mBinding.rvTypes.addItemDecoration(decoration)
        mBinding.rvStudios.addItemDecoration(decoration)
        mBinding.groupSetting.setOnClickListener { showSettings() }
    }

    private fun showSettings() {
        val content = BannerSettingFragment()
        content.params = ViewProperty.getRecordBannerParams()
        content.onAnimSettingListener = object : OnAnimSettingListener {
            override fun onParamsUpdated(params: BannerHelper.BannerParams) {}
            override fun onParamsSaved(params: BannerHelper.BannerParams) {
                ViewProperty.setRecordBannerParams(params)
                BannerHelper.setBannerParams(mBinding.banner, params)
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = content
        dialogFragment.setTitle("Banner Setting")
        dialogFragment.show(childFragmentManager, "BannerSettingFragment")
    }

    private fun initActionbar() {
        mBinding.actionbar.setOnBackListener { requireActivity().onBackPressed() }
        mBinding.actionbar.setOnSearchListener { ftStar.filterStar(it) }
        mBinding.actionbar.registerPopupMenuOn(
            R.id.menu_sort,
            R.menu.player_sort
        ) {
            when (it.itemId) {
                R.id.menu_sort_name -> mModel.sortMode = AppConstants.STAR_SORT_NAME
                R.id.menu_sort_records -> mModel.sortMode = AppConstants.STAR_SORT_RECORDS
                R.id.menu_sort_rating -> mModel.sortMode = AppConstants.STAR_SORT_RATING
                R.id.menu_sort_rating_face -> mModel.sortMode = AppConstants.STAR_SORT_RATING_FACE
                R.id.menu_sort_rating_body -> mModel.sortMode = AppConstants.STAR_SORT_RATING_BODY
                R.id.menu_sort_rating_dk -> mModel.sortMode = AppConstants.STAR_SORT_RATING_DK
                R.id.menu_sort_rating_sexuality -> mModel.sortMode = AppConstants.STAR_SORT_RATING_SEXUALITY
                R.id.menu_sort_rating_passion -> mModel.sortMode = AppConstants.STAR_SORT_RATING_PASSION
                R.id.menu_sort_rating_video -> mModel.sortMode = AppConstants.STAR_SORT_RATING_VIDEO
                R.id.menu_sort_random -> mModel.sortMode = AppConstants.STAR_SORT_RANDOM
            }
            false
        }
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_index -> changeSideBarVisible()
                R.id.menu_gdb_view_mode -> {
                    mModel.toggleViewMode(resources)
                    ftStar.onViewModeChanged()
                }
                R.id.menu_gdb_category -> goToCategory()
                R.id.menu_gdb_expand_all -> ftStar.setExpandAll(true)
                R.id.menu_gdb_collapse_all -> ftStar.setExpandAll(false)
            }
        }
    }

    private fun goToCategory() {
//        Router.build("Category")
//            .go(this)
    }

    private fun changeSideBarVisible() {
        ftStar.toggleSidebar()
    }

    private fun initRecommend() {

        // 采用getView时生成随机推荐，这里初始化5个（引发pageradapter的重新创建view操作）
        val list: MutableList<Star> = ArrayList()
        list.add(Star(null))
        list.add(Star(null))
        list.add(Star(null))
        list.add(Star(null))
        list.add(Star(null))
        val adapter =
            HeadBannerAdapter()
        adapter.list = list
        mBinding.banner.adapter = adapter
        mBinding.banner.startAutoPlay()
    }

    override fun createViewModel(): StarListTitleViewModel = generateViewModel(StarListTitleViewModel::class.java)

    override fun initData() {
        mModel.menuViewModeObserver.observe(this) { title ->
            mBinding.actionbar.updateMenuText(R.id.menu_gdb_view_mode, title)
        }
        mModel.sortTypeObserver.observe(this){ type -> ftStar.updateSortType(type) }
        showTags()
    }

    private fun showTags() {
        mModel.typesObserver.observe(this) {
            TypeAdapter().apply {
                list = it
                listenerClick = object : BaseBindingAdapter.OnItemClickListener<StarTypeWrap> {
                    override fun onClickItem(view: View, position: Int, data: StarTypeWrap) {
                        ftStar.updateStarType(data.type)
                    }
                }
                mBinding.rvTypes.adapter = this
            }
        }
        mModel.studiosObserver.observe(this) {
            StudioTagAdapter().apply {
                list = it
                selection = mModel.findStudioPosition(studioId)
                listenerClick = object : BaseBindingAdapter.OnItemClickListener<StudioStarWrap> {
                    override fun onClickItem(view: View, position: Int, data: StudioStarWrap) {
                        ftStar.updateStudioId(data.studio.id!!)
                    }
                }
                mBinding.rvStudios.adapter = this
            }
            showStarList()
        }
        mModel.loadTags()
    }

    private fun showStarList() {
        ftStar = StarListFragment.newInstance(DataConstants.STAR_MODE_ALL, studioId)
        childFragmentManager.beginTransaction()
            .replace(R.id.ft_list, ftStar, "StarListFragment")
            .commit()
    }

    private val isStudioStarPage: Boolean
        private get() = studioId != 0L

    override fun dispatchClickStar(star: Star): Boolean {
        onClickStarListener?.let {
            it.onClickStar(star.id!!)
            return true
        }
        return false
    }

    override fun dispatchOnLongClickStar(star: Star): Boolean {
        onClickStarListener?.let {
            it.onLongClickStar(star.id!!)
            return true
        }
        return false
    }

    override fun hideDetailIndex() {
        mBinding.tvIndex.visibility = View.GONE
    }

    override fun updateDetailIndex(name: String) {
        if (mBinding.tvIndex.visibility != View.VISIBLE) {
            mBinding.tvIndex.visibility = View.VISIBLE
        }
        val newIndex = getAvailableIndex(name)
        if (newIndex != curDetailIndex) {
            curDetailIndex = newIndex
            mBinding.tvIndex.text = newIndex
        }
    }

    /**
     * 最多支持3个字母
     * @param name
     * @return
     */
    private fun getAvailableIndex(name: String): String {
        return when {
            name.length > 2 -> name.substring(0, 3)
            name.length > 1 -> name.substring(0, 2)
            name.isNotEmpty() -> name.substring(0, 1)
            else -> ""
        }
    }

    public override fun onPause() {
        super.onPause()
        mBinding.banner?.stopAutoPlay()
    }

    override fun onStop() {
        super.onStop()
        indexDisposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
        mBinding.banner?.startAutoPlay()

        // 控制tvIndex在切换显示列表后的隐藏状况
        indexDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (mBinding.tvIndex.visibility == View.VISIBLE) {
                    if (ftStar.isNotScrolling) {
                        mBinding.tvIndex.visibility = View.GONE
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.banner?.stopAutoPlay()
    }

    private inner class HeadBannerAdapter : CoolBannerAdapter<Star>(),
        View.OnClickListener {
        override fun getLayoutRes(): Int {
            return R.layout.adapter_banner_image
        }

        override fun onBindView(view: View, position: Int, star: Star) {
            var newStar = mModel.nextFavorStar()
            newStar?.let { bean ->
                val imageView = view.findViewById<ImageView>(R.id.iv_image)
                val path = getStarRandomPath(bean.name, null)
                ImageBindingAdapter.setStarUrl(imageView, path)
                val groupContainer = view.findViewById<ViewGroup>(R.id.group_container)
                groupContainer.tag = bean
                groupContainer.setOnClickListener(this)
            }
        }

        override fun onClick(v: View) {
            val bean = v.tag as Star
            onClickBannerItem(bean)
        }
    }

    private fun onClickBannerItem(bean: Star) {
        StarActivity.startPage(requireContext(), bean.id!!)
    }

    interface OnClickStarListener {
        fun onClickStar(starId: Long)
        fun onLongClickStar(starId: Long)
    }

    class TypeAdapter: BaseBindingAdapter<AdapterTagItemStarlistBinding, StarTypeWrap>() {
        var selection = 0
        override fun onCreateBind(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): AdapterTagItemStarlistBinding = AdapterTagItemStarlistBinding.inflate(inflater, parent, false)

        override fun onBindItem(binding: AdapterTagItemStarlistBinding, position: Int, bean: StarTypeWrap) {
            binding.tvName.text = bean.text
            binding.tvCount.text = bean.starCount.toString()
            binding.tvName.isSelected = position == selection
            binding.tvCount.isSelected = position == selection
            binding.group.isSelected = position == selection
        }

        override fun onClickItem(v: View, position: Int, bean: StarTypeWrap) {
            if (position != selection) {
                super.onClickItem(v, position, bean)
            }
            selection = position
            notifyDataSetChanged()
        }
    }
}