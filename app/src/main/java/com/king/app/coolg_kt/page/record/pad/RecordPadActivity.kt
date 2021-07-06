package com.king.app.coolg_kt.page.record.pad

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityRecordPadBinding
import com.king.app.coolg_kt.model.bean.PassionPoint
import com.king.app.coolg_kt.model.bean.TitleValueBean
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.palette.PaletteUtil
import com.king.app.coolg_kt.model.palette.ViewColorBound
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.model.setting.ViewProperty
import com.king.app.coolg_kt.page.pub.BannerSettingFragment
import com.king.app.coolg_kt.page.pub.BannerSettingFragment.OnAnimSettingListener
import com.king.app.coolg_kt.page.pub.TagAdapter
import com.king.app.coolg_kt.page.pub.TagManagerFragment
import com.king.app.coolg_kt.page.record.RecordOrdersAdapter
import com.king.app.coolg_kt.page.record.RecordPlayOrdersAdapter
import com.king.app.coolg_kt.page.record.pad.RecordPadActivity
import com.king.app.coolg_kt.page.record.pad.RecordPagerAdapter.OnHolderListener
import com.king.app.coolg_kt.page.star.phone.StarActivity
import com.king.app.coolg_kt.page.studio.phone.StudioActivity
import com.king.app.coolg_kt.page.video.order.PlayOrderActivity
import com.king.app.coolg_kt.page.video.player.PlayerActivity
import com.king.app.coolg_kt.utils.BannerHelper
import com.king.app.coolg_kt.utils.ColorUtil
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.AlertDialogFragment
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.RecordStarWrap
import com.king.app.gdb.data.relation.RecordWrap
import java.util.*

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/22 13:47
 */
class RecordPadActivity : BaseActivity<ActivityRecordPadBinding, RecordPadViewModel>() {

    companion object {
        const val EXTRA_RECORD_ID = "key_record_id"

        fun startPage(context: Context, recordId: Long) {
            var intent = Intent(context, RecordPadActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            context.startActivity(intent)
        }
    }

    private val REQUEST_ADD_ORDER = 1602
    private val REQUEST_SELECT_STUDIO = 1603
    private val REQUEST_VIDEO_ORDER = 1604
    private val REQUEST_SET_VIDEO_COVER = 1605
    private var starAdapter = RecordStarAdapter()
    private var starDetailAdapter = RecordStarDetailAdapter()
    private var scoreDetailAdapter = RecordScoreAdapter()
    private var passionAdapter = PassionPointAdapter()
    private var pagerAdapter = RecordPagerAdapter(lifecycle)
    private var recordGallery = RecordGallery()
    private var ordersAdapter = RecordOrdersAdapter()
    private var playOrdersAdapter = RecordPlayOrdersAdapter()
    private var tagAdapter = TagAdapter()
    
    override fun getContentView(): Int {
        return R.layout.activity_record_pad
    }

    override fun isFullScreen(): Boolean = true

    override fun initView() {
        ColorUtil.updateIconColor(mBinding.ivBack, resources.getColor(R.color.colorPrimary))
        ColorUtil.updateIconColor(mBinding.ivOrder, resources.getColor(R.color.colorPrimary))
        ColorUtil.updateIconColor(
            mBinding.ivSetCover,
            resources.getColor(R.color.colorPrimary)
        )
        ColorUtil.updateIconColor(mBinding.ivDelete, resources.getColor(R.color.colorPrimary))
        ColorUtil.updateIconColor(mBinding.ivSetting, resources.getColor(R.color.colorPrimary))
        ColorUtil.updateIconColor(mBinding.ivDesktop, resources.getColor(R.color.colorPrimary))
        ColorUtil.updateIconColor(mBinding.ivTv, resources.getColor(R.color.colorPrimary))
        initRecyclerViews()
        initBanner()
        mBinding.ivBack.setOnClickListener { finish() }
        mBinding.ivOrder.setOnClickListener { toggleOrders() }
        mBinding.ivSetCover.setOnClickListener {
            onApplyImage(mModel.getCurrentImage(mBinding.banner.currentItem))
        }
        mBinding.ivDelete.setOnClickListener {
            mModel.deleteImage(mModel.getCurrentImage(mBinding.banner.currentItem))
        }
        mBinding.ivSetting.setOnClickListener { showBannerSetting() }
        mBinding.tvStudio.setOnClickListener { selectStudio() }
        mBinding.ivAddOrder.setOnClickListener { selectOrderToAddRecord() }
        mBinding.ivAddPlayOrder.setOnClickListener { onAddToPlayOrder() }
        //        mBinding.tvScene.setOnClickListener(v -> );
//        mBinding.ivPlay.setOnClickListener(v -> );
        mBinding.tvScore.setOnClickListener {
            if (mBinding.groupDetail.visibility == View.VISIBLE) {
                mBinding.groupDetail.startAnimation(detailDisappear)
            } else {
                mBinding.groupDetail.startAnimation(detailAppear)
            }
        }
        mBinding.groupBottom.setOnClickListener {
            initGallery()
            recordGallery.show(supportFragmentManager, "GalleryDialog")
        }
        mBinding.ivDesktop.setOnClickListener {
            showConfirmCancelMessage(
                "即将在电脑上打开视频，是否继续？",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> mModel.openOnServer() },
                null
            )
        }
        mBinding.ivTv.setOnClickListener {
            SimpleDialogs().openInputDialog(this, "Ip", SettingProperty.getSocketServerUrl(), SimpleDialogs.OnDialogActionListener {
                mModel.playInSocketServer(it)
            })
        }
    }

    private fun showBannerSetting() {
        val bannerSettingDialog = BannerSettingFragment()
        bannerSettingDialog.params = ViewProperty.getRecordBannerParams()
        bannerSettingDialog.onAnimSettingListener = object : OnAnimSettingListener {
            override fun onParamsUpdated(params: BannerHelper.BannerParams) {
            }

            override fun onParamsSaved(params: BannerHelper.BannerParams) {
                ViewProperty.setRecordBannerParams(params)
                BannerHelper.setBannerParams(mBinding.banner, params)
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = bannerSettingDialog
        dialogFragment.setTitle("Banner Setting")
        dialogFragment.show(supportFragmentManager, "BannerSettingFragment")
    }

    private fun initGallery() {
        recordGallery.setCurrentPage(mBinding.banner.currentItem)
        recordGallery.list = mModel.imageList
        recordGallery.onItemClickListener = object : BaseBindingAdapter.OnItemClickListener<String> {
            override fun onClickItem(view: View, position: Int, data: String) {
                mBinding.banner.controller.setPage(position)
            }
        }
    }

    private fun initBanner() {
        BannerHelper.setBannerParams(mBinding.banner, ViewProperty.getRecordBannerParams())
    }

    private fun initRecyclerViews() {
        var manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        mBinding.rvStars.layoutManager = manager
        mBinding.rvStars.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position > 0) {
                    outRect.left = ScreenUtils.dp2px(10f)
                }
            }
        })
        manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        mBinding.rvStarsDetail.layoutManager = manager
        mBinding.rvStarsDetail.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position > 0) {
                    outRect.top = ScreenUtils.dp2px(10f)
                }
            }
        })
        manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        mBinding.rvScoreDetail.layoutManager = manager
        mBinding.rvScoreDetail.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position > 0) {
                    outRect.top = ScreenUtils.dp2px(10f)
                }
            }
        })
        mBinding.ivPlayVideo.setOnClickListener { v: View? -> mModel.playVideo() }
        mModel.videoPlayOnReadyObserver.observe(
            this,
            Observer {
                PlayerActivity.startPage(this, true)
            }
        )
        mBinding.rvOrders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvPlayOrders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvTags.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvTags.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildLayoutPosition(view)
                if (position > 0) {
                    outRect.left = ScreenUtils.dp2px(16f)
                }
            }
        })
        mBinding.ivAddTag.setOnClickListener { addTag() }
    }

    private fun addTag() {
        val fragment = TagManagerFragment()
        fragment.tagType = DataConstants.TAG_TYPE_RECORD
        fragment.onTagSelectListener = object : TagManagerFragment.OnTagSelectListener{
            override fun onSelectTag(tag: Tag) {
                mModel.addTag(tag)
            }
        }
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = fragment
        dialogFragment.setTitle("Select tag")
        dialogFragment.fixedHeight = fragment.idealHeight
        dialogFragment.setBackgroundColor(resources.getColor(R.color.dlg_tag_bg))
        dialogFragment.dismissListener = DialogInterface.OnDismissListener { mModel.refreshTags() }
        dialogFragment.show(supportFragmentManager, "TagManagerFragment")
    }

    override fun createViewModel(): RecordPadViewModel = generateViewModel(RecordPadViewModel::class.java)

    override fun onResume() {
        super.onResume()
        mBinding.banner.startAutoPlay()
    }

    override fun onPause() {
        super.onPause()
        mBinding.banner.stopAutoPlay()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        initData()
    }

    override fun initData() {
        mModel.recordObserver.observe(this, Observer { showRecord(it) })
        mModel.starsObserver.observe(this, Observer { showStars(it) })
        mModel.passionsObserver.observe(this, Observer { showPassions(it) })
        mModel.scoreObserver.observe(this, Observer<List<TitleValueBean>> { showScores(it) })
        mModel.imagesObserver.observe(this, Observer { showImages(it) })
        mModel.ordersObserver.observe(this, Observer { showOrders(it) })
        mModel.playOrdersObserver.observe(this, Observer { showPlayOrders(it) })
        mModel.studioObserver.observe(
            this,
            Observer {
                if (TextUtils.isEmpty(it)) {
                    mBinding.tvStudio.text = "Select Studio"
                } else {
                    mBinding.tvStudio.text = it
                }
            }
        )
        mModel.videoPathObserver.observe(
            this,
            Observer {
                if (it == null) {
                    mBinding.ivPlay.visibility = View.GONE
                } else {
                    mBinding.ivPlay.visibility = View.VISIBLE
                }
            }
        )
        mModel.paletteObserver.observe(this, Observer { updatePalette(it) })
        mModel.viewBoundsObserver.observe(this, Observer { updateViewBounds(it) })
        mModel.videoUrlObserver.observe(this, Observer { mBinding.ivPlayVideo.visibility = View.VISIBLE })
        mModel.tagsObserver.observe(this, Observer { showTags(it) })
        mModel.loadRecord(intent.getLongExtra(EXTRA_RECORD_ID, -1))
    }

    private fun showScores(list: List<TitleValueBean>) {
        scoreDetailAdapter.list = list
        mBinding.rvScoreDetail.adapter = scoreDetailAdapter

        // it will be a little stuck as soon as activity started, it's better delay executing animation
        Handler().postDelayed(
            { mBinding.groupDetail.startAnimation(detailAppear) },
            1000
        )
    }

    private fun showPassions(list: List<PassionPoint>) {
        passionAdapter = PassionPointAdapter()
        passionAdapter!!.list = list
        mBinding.groupFk.setAdapter(passionAdapter)
        mBinding.groupFk.startAnimation(pointAnim)
    }

    private fun showStars(list: List<RecordStarWrap>) {
        // 延迟一些效果更好
        Handler().postDelayed({

            // 如果是在xml里就注册了layoutAnimation，那么延时就不起作用，所以在这里才注册anim
            val controller = AnimationUtils.loadLayoutAnimation(this@RecordPadActivity, R.anim.layout_pad_simple_stars)
            mBinding.rvStars.layoutAnimation = controller
            starAdapter.list = list
            starAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordStarWrap> {
                override fun onClickItem(view: View, position: Int, data: RecordStarWrap) {
                    goToStarPage(data.bean.starId)
                }
            })
            mBinding.rvStars.adapter = starAdapter

            starDetailAdapter.list = list
            starDetailAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordStarWrap> {
                override fun onClickItem(view: View, position: Int, data: RecordStarWrap) {
                    goToStarPage(data.bean.starId)
                }
            })
            mBinding.rvStarsDetail.adapter = starDetailAdapter
        }, 1000)
    }

    private fun showTags(tags: List<Tag>) {
        tagAdapter.list = tags
        tagAdapter.setOnItemLongClickListener(object : BaseBindingAdapter.OnItemLongClickListener<Tag> {
            override fun onLongClickItem(view: View, position: Int, data: Tag) {
                tagAdapter.toggleDelete()
                tagAdapter.notifyDataSetChanged()
            }
        })
        tagAdapter.onDeleteListener = object : TagAdapter.OnDeleteListener {
            override fun onDelete(position: Int, bean: Tag) {
                mModel.deleteTag(bean)
            }
        }
        mBinding.rvTags.adapter = tagAdapter
    }

    private fun goToStarPage(starId: Long) {
        StarActivity.startPage(this, starId)
    }

    private fun selectStudio() {
        StudioActivity.startPageToSelect(this, REQUEST_SELECT_STUDIO)
    }

    private fun showRecord(record: RecordWrap) {
        mBinding.tvName.text = record.bean.name
        if (record.bean.deprecated == DataConstants.DEPRECATED) {
            mBinding.tvParent.text = "(Deprecated)  " + record.bean.directory
        } else {
            mBinding.tvParent.text = record.bean.directory
        }
        mBinding.tvScore.text = record.bean.score.toString()
        mBinding.tvBareback.visibility =
            if (record.bean.deprecated == DataConstants.DEPRECATED) View.VISIBLE else View.GONE
        mBinding.tvScene.text = record.bean.scene
        mModel.loadRecordOrders()
        mModel.loadRecordPlayOrders()
    }

    /**
     * appear animation of groupDetail
     * @return
     */
    private val detailAppear: Animation
        private get() {
            mBinding.groupDetail.visibility = View.VISIBLE
            val set = AnimationSet(true)
            set.interpolator = AccelerateDecelerateInterpolator()
            set.duration = 500
            val translation: Animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                , Animation.RELATIVE_TO_SELF, -1.5f, Animation.RELATIVE_TO_SELF, 0f
            )
            set.addAnimation(translation)
            val scale: Animation = ScaleAnimation(
                0f, 1f, 0f, 1f
                , Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 1f
            )
            set.addAnimation(scale)
            return set
        }

    /**
     * disappear animation of groupDetail
     * @return
     */
    private val detailDisappear: Animation
        private get() {
            val set = AnimationSet(true)
            set.interpolator = AccelerateDecelerateInterpolator()
            set.duration = 500
            val translation: Animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                , Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1.5f
            )
            set.addAnimation(translation)
            val scale: Animation = ScaleAnimation(
                1f, 0f, 1f, 0f
                , Animation.RELATIVE_TO_SELF, 0.1f, Animation.RELATIVE_TO_SELF, 1f
            )
            set.addAnimation(scale)
            set.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    mBinding.groupDetail.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            return set
        }

    /**
     * appear animation for item of groupFk
     * @return
     */
    private val pointAnim: Animation
        private get() {
            val set = AnimationSet(true)
            set.interpolator = AccelerateDecelerateInterpolator()
            set.duration = 1500
            val scale: Animation = ScaleAnimation(
                0f, 1f, 0f, 1f
                , Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f
            )
            set.addAnimation(scale)
            return set
        }

    private fun showImages(list: List<String>) {
        showBanner(list)
    }

    private fun showBanner(list: List<String>) {
        mBinding.banner.stopAutoPlay()
        val viewList = mutableListOf<View>()
        viewList.add(mBinding.ivBack)
        viewList.add(mBinding.ivOrder)
        viewList.add(mBinding.ivSetCover)
        viewList.add(mBinding.ivDelete)
        viewList.add(mBinding.ivSetting)
        viewList.add(mBinding.ivDesktop)
        viewList.add(mBinding.ivTv)
        pagerAdapter.viewList = viewList
        pagerAdapter.list = list
        mBinding.banner.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(position: Int) {
                onPagePresented(position)
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
        pagerAdapter.onHolderListener = object : OnHolderListener {
            override fun onPaletteCreated(position: Int, palette: Palette?) {
                mModel.cachePalette(position, palette!!)
                if (position == mBinding.banner.currentItem) {
                    onPagePresented(position)
                }
            }

            override fun onBoundsCreated(
                position: Int,
                bounds: List<ViewColorBound>?
            ) {
                mModel.cacheViewBounds(position, bounds)
                if (position == mBinding.banner.currentItem) {
                    onPagePresented(position)
                }
            }
        }
        mBinding.banner.adapter = pagerAdapter
        mBinding.banner.startAutoPlay()
    }

    private fun onPagePresented(position: Int) {
        recordGallery.setCurrentPage(position)
        mModel.refreshBackground(position)
    }

    private fun updateViewBounds(bounds: List<ViewColorBound>) {
        for (bound in bounds) {
            ColorUtil.updateIconColor(bound.view as ImageView, bound.color)
        }
    }

    private fun updatePalette(palette: Palette) {
        val swatch = PaletteUtil.getDefaultSwatch(palette)
        if (swatch != null) {
            mBinding.tvName.setTextColor(swatch.titleTextColor)
            mBinding.tvParent.setTextColor(swatch.bodyTextColor)
            mBinding.tvBareback.setTextColor(swatch.bodyTextColor)
            mBinding.tvScene.setTextColor(swatch.bodyTextColor)
            mBinding.tvScore.setTextColor(swatch.titleTextColor)
            mBinding.groupBottom.setBackgroundColor(swatch.rgb)
        }
        passionAdapter.swatches = palette.swatches
        mBinding.groupFk.invalidate()
    }

    private fun toggleOrders() {
        if (mBinding.llOrders.visibility == View.VISIBLE) {
            mBinding.llOrders.startAnimation(ordersDisappear)
        } else {
            mBinding.llOrders.visibility = View.VISIBLE
            mBinding.llOrders.startAnimation(ordersAppear)
        }
    }

    private fun showOrders(list: List<FavorRecordOrder>) {
        ordersAdapter.mTextColor = resources.getColor(R.color.white)
        ordersAdapter.list = list
        ordersAdapter.onDeleteListener = object :
            RecordOrdersAdapter.OnDeleteListener {
            override fun onDeleteOrder(order: FavorRecordOrder) {
                mModel.deleteOrderOfRecord(order.id!!)
                mModel.loadRecordOrders()
            }
        }
        mBinding.rvOrders.adapter = ordersAdapter
    }

    private fun showPlayOrders(list: List<VideoPlayList>) {
        playOrdersAdapter.list = list
        playOrdersAdapter.onDeleteListener = object : RecordPlayOrdersAdapter.OnDeleteListener {
            override fun onDeleteOrder(order: VideoPlayList) {
                mModel.deletePlayOrderOfRecord(order.playOrder!!.id!!)
                mModel.loadRecordPlayOrders()
            }
        }
        mBinding.rvPlayOrders.adapter = playOrdersAdapter
    }

    /**
     * appear animation of orders
     * @return
     */
    private val ordersAppear: Animation
        private get() {
            mBinding.llOrders.visibility = View.VISIBLE
            val set = AnimationSet(true)
            set.interpolator = AccelerateDecelerateInterpolator()
            set.duration = 500
            val translation: Animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                , Animation.RELATIVE_TO_SELF, -1.5f, Animation.RELATIVE_TO_SELF, 0f
            )
            set.addAnimation(translation)
            val scale: Animation = ScaleAnimation(
                0f, 1f, 0f, 1f
                , Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            set.addAnimation(scale)
            return set
        }

    /**
     * disappear animation of orders
     * @return
     */
    private val ordersDisappear: Animation
        private get() {
            val set = AnimationSet(true)
            set.interpolator = AccelerateDecelerateInterpolator()
            set.duration = 500
            val translation: Animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
                , Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1.5f
            )
            set.addAnimation(translation)
            val scale: Animation = ScaleAnimation(
                1f, 0f, 1f, 0f
                , Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            set.addAnimation(scale)
            set.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    mBinding.llOrders.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            return set
        }

    protected fun selectOrderToAddRecord() {
//        Router.build("OrderPhone")
//            .with(OrderPhoneActivity.EXTRA_SELECT_MODE, true)
//            .with(OrderPhoneActivity.EXTRA_SELECT_RECORD, true)
//            .requestCode(REQUEST_ADD_ORDER)
//            .go(this)
    }

    private fun onAddToPlayOrder() {
        PlayOrderActivity.startPageToSelect(this, REQUEST_VIDEO_ORDER)
    }

    private fun onApplyImage(path: String?) {
        DebugLog.e(path)
        val options = arrayOf("Order", "Play Order")
        AlertDialogFragment()
            .setItems(
                options
            ) { dialogInterface: DialogInterface?, i: Int ->
                if (i == 0) {
                    onSetCoverForOrder(path)
                } else if (i == 1) {
                    onSetCoverForPlayOrder(path)
                }
            }
            .show(supportFragmentManager, "AlertDialogFragment")
    }

    private fun onSetCoverForOrder(path: String?) {
//        if (!TextUtils.isEmpty(path)) {
//            Router.build("OrderPhone")
//                .with(OrderPhoneActivity.EXTRA_SET_COVER, path)
//                .go(this)
//        }
    }

    private fun onSetCoverForPlayOrder(path: String?) {
        path?.let {
            mModel.mUrlToSetCover = it
            PlayOrderActivity.startPageToSelect(this, REQUEST_SET_VIDEO_COVER)
        }
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // 如果收不到回调，检查所在Activity是否实现了onActivityResult并且没有执行super.onActivityResult
        if (requestCode == REQUEST_ADD_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                val orderId = data!!.getLongExtra(AppConstants.RESP_ORDER_ID, -1)
                mModel.addToOrder(orderId)
            }
        } else if (requestCode == REQUEST_SELECT_STUDIO) {
            if (resultCode == Activity.RESULT_OK) {
                val orderId = data!!.getLongExtra(AppConstants.RESP_ORDER_ID, -1)
                mModel.addToOrder(orderId)
            }
        } else if (requestCode == REQUEST_VIDEO_ORDER) {
            if (resultCode == Activity.RESULT_OK) {
                val list =
                    data!!.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
                mModel.addToPlay(list)
            }
        } else if (requestCode == REQUEST_SET_VIDEO_COVER) {
            if (resultCode == Activity.RESULT_OK) {
                val list =
                    data!!.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
//                mModel.setPlayOrderCover(list)
            }
        }
    }
}