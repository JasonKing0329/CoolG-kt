package com.king.app.coolg_kt.page.star.phone

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityStarListPhoneBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.image.ImageProvider.getStarRandomPath
import com.king.app.coolg_kt.model.setting.ViewProperty
import com.king.app.coolg_kt.page.pub.BannerSettingFragment
import com.king.app.coolg_kt.page.pub.BannerSettingFragment.OnAnimSettingListener
import com.king.app.coolg_kt.page.star.list.IStarListHolder
import com.king.app.coolg_kt.page.star.list.StarListFragment
import com.king.app.coolg_kt.page.star.list.StarListPagerAdapter
import com.king.app.coolg_kt.utils.BannerHelper
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.DataConstants
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
class StarListPhoneActivity : BaseActivity<ActivityStarListPhoneBinding, StarListTitleViewModel>(), IStarListHolder {

    companion object {
        const val EXTRA_STUDIO_ID = "studio_id"
        fun startPage(context: Context) {
            var intent = Intent(context, StarListPhoneActivity::class.java)
            context.startActivity(intent)
        }
        fun startStudioPage(context: Context, studioId: Long) {
            var intent = Intent(context, StarListPhoneActivity::class.java)
            intent.putExtra(EXTRA_STUDIO_ID, studioId)
            context.startActivity(intent)
        }
    }

    private lateinit var pagerAdapter: StarListPagerAdapter

    /**
     * 控制detail index显示的timer
     */
    private var indexDisposable: Disposable? = null
    private var curDetailIndex: String? = null

    override fun getContentView(): Int = R.layout.activity_star_list_phone

    override fun initView() {
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

        // 默认只缓存另外2个，在切换时处理view mode及sort type有很多弊端，改成缓存全部另外3个可以规避问题
        mBinding.viewpager.offscreenPageLimit = 3
        mBinding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pagerAdapter.list[position].onRefresh(mModel.sortMode)
            }
        })
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
        dialogFragment.show(supportFragmentManager, "BannerSettingFragment")
    }

    private fun initActionbar() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnSearchListener { currentPage.filterStar(it) }
        mBinding.actionbar.registerPopupMenu(R.id.menu_sort)
        mBinding.actionbar.setPopupMenuProvider { iconMenuId: Int, anchorView: View ->
            if (iconMenuId == R.id.menu_sort) {
                return@setPopupMenuProvider createSortPopup(anchorView)
            }
            null
        }
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_index -> changeSideBarVisible()
                R.id.menu_gdb_view_mode -> {
                    mModel.toggleViewMode(resources)
                    pagerAdapter.onViewModeChanged()
                }
                R.id.menu_gdb_category -> goToCategory()
                R.id.menu_gdb_expand_all -> currentPage.setExpandAll(true)
                R.id.menu_gdb_collapse_all -> currentPage.setExpandAll(false)
            }
        }
    }

    private fun goToCategory() {
//        Router.build("Category")
//            .go(this)
        TODO()
    }

    private fun createSortPopup(anchorView: View): PopupMenu {
        val menu = PopupMenu(this, anchorView)
        menu.menuInflater.inflate(R.menu.player_sort, menu.menu)
        menu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
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
        return menu
    }

    private fun changeSideBarVisible() {
        currentPage.toggleSidebar()
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
        mModel.menuViewModeObserver.observe(this,
            Observer { title ->
                mBinding.actionbar.updateMenuText(R.id.menu_gdb_view_mode, title)
            }
        )
        mModel.sortTypeObserver.observe(this, Observer { type -> currentPage.updateSortType(type) })
        showTitles()
        if (studioId != 0L) {
            val studio = mModel.getStudioName(studioId)
            mBinding.actionbar.setTitle(studio)
        }
    }

    private val studioId: Long
        private get() = intent.getLongExtra(EXTRA_STUDIO_ID, 0)

    private val isStudioStarPage: Boolean
        private get() = studioId != 0L

    override fun updateTabTitle(starType: String, title: String) {
        when (starType) {
            DataConstants.STAR_MODE_ALL -> {
                mBinding.tabLayout.getTabAt(0)?.text = title
            }
            DataConstants.STAR_MODE_TOP -> {
                mBinding.tabLayout.getTabAt(1)?.text = title
            }
            DataConstants.STAR_MODE_BOTTOM -> {
                mBinding.tabLayout.getTabAt(2)?.text = title
            }
            DataConstants.STAR_MODE_HALF -> {
                mBinding.tabLayout.getTabAt(3)?.text = title
            }
        }
    }

    private fun showTitles() {
        var list = mutableListOf<StarListFragment>()
        val fragmentAll = StarListFragment.newInstance(DataConstants.STAR_MODE_ALL, studioId)
        list.add(fragmentAll)
        val fragment1 = StarListFragment.newInstance(DataConstants.STAR_MODE_TOP, studioId)
        list.add(fragment1)
        val fragment0 = StarListFragment.newInstance(DataConstants.STAR_MODE_BOTTOM, studioId)
        list.add(fragment0)
        val fragment05 = StarListFragment.newInstance(DataConstants.STAR_MODE_HALF, studioId)
        list.add(fragment05)
        pagerAdapter = StarListPagerAdapter(this, list)
        mBinding.viewpager.adapter = pagerAdapter

        var mediator = TabLayoutMediator(mBinding.tabLayout, mBinding.viewpager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position -> tab.text = AppConstants.STAR_LIST_TITLES[position] })
        mediator.attach()
    }

    private val currentPage: StarListFragment
        private get() = pagerAdapter.list[mBinding.viewpager.currentItem]

    override fun dispatchClickStar(star: Star): Boolean {
        return false
    }

    override fun dispatchOnLongClickStar(star: Star): Boolean {
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

    public override fun onResume() {
        super.onResume()
        mBinding.banner?.startAutoPlay()

        // 控制tvIndex在切换显示列表后的隐藏状况
        indexDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (mBinding.tvIndex.visibility == View.VISIBLE) {
                    if (currentPage.isNotScrolling) {
                        mBinding.tvIndex.visibility = View.GONE
                    }
                }
            }
    }

    public override fun onDestroy() {
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
        StarActivity.startPage(this, bean.id!!)
    }
}