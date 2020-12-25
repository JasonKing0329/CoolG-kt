package com.king.app.coolg_kt.page.record.phone

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.databinding.ActivityRecordPhoneBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.bean.PassionPoint
import com.king.app.coolg_kt.model.bean.TitleValueBean
import com.king.app.coolg_kt.model.bean.VideoPlayList
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.image.ImageProvider.getRecordCuPath
import com.king.app.coolg_kt.model.setting.ViewProperty
import com.king.app.coolg_kt.page.image.ImageManagerActivity
import com.king.app.coolg_kt.page.pub.BannerSettingFragment
import com.king.app.coolg_kt.page.pub.TagFragment
import com.king.app.coolg_kt.page.record.*
import com.king.app.coolg_kt.page.star.phone.StarActivity
import com.king.app.coolg_kt.page.studio.phone.StudioActivity
import com.king.app.coolg_kt.page.video.PlayerActivity
import com.king.app.coolg_kt.utils.BannerHelper
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.coolg_kt.utils.ScreenUtils
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.FavorRecordOrder
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.relation.RecordStarWrap
import com.king.app.gdb.data.relation.RecordWrap
import com.king.lib.banner.CoolBannerAdapter
import tcking.github.com.giraffeplayer2.PlayerManager

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/8 13:25
 */
class RecordActivity : BaseActivity<ActivityRecordPhoneBinding, RecordViewModel>() {

    companion object {
        const val EXTRA_RECORD_ID = "key_record_id"

        fun startPage(context: Context, recordId: Long) {
            var intent = Intent(context, RecordActivity::class.java)
            intent.putExtra(EXTRA_RECORD_ID, recordId)
            context.startActivity(intent)
        }
    }

    private val REQUEST_ADD_ORDER = 1602
    private val REQUEST_SELECT_STUDIO = 1603
    private val REQUEST_VIDEO_ORDER = 1604

    private var starAdapter = RecordStarAdapter()
    private var orderAdapter = RecordOrdersAdapter()
    private var playOrdersAdapter = RecordPlayOrdersAdapter()
    private var scoreAdapter = ScoreItemAdapter()
    private var tagAdapter = TagAdapter()

    override fun getContentView(): Int = R.layout.activity_record_phone

    override fun createViewModel(): RecordViewModel = generateViewModel(RecordViewModel::class.java)

    override fun initView() {
        mBinding.rvStars.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvScores.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_banner_setting -> showSettingDialog()
            }
        }
        mBinding.groupScene.setOnClickListener {  }
        mBinding.ivOrderAdd.setOnClickListener { selectOrderToAddStar() }
        mBinding.ivOrderDelete.setOnClickListener {
            orderAdapter.toggleDeleteMode()
            orderAdapter.notifyDataSetChanged()
        }
        mBinding.groupOrder.setOnClickListener {
            // collapse
            if (mBinding.ivOrderArrow.isSelected) {
                mBinding.ivOrderArrow.isSelected = false
                mBinding.ivOrderArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
                mBinding.rvOrders.visibility = View.GONE
            } else {
                mBinding.ivOrderArrow.isSelected = true
                mBinding.ivOrderArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
                mBinding.rvOrders.visibility = View.VISIBLE
            }
        }
        mBinding.rvOrders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.ivPlayOrderAdd.setOnClickListener {
//            Router.build("PlayOrder")
//                .with(PlayOrderActivity.EXTRA_MULTI_SELECT, true)
//                .requestCode(REQUEST_VIDEO_ORDER)
//                .go(this@RecordActivity)
            TODO()
        }
        mBinding.ivPlayOrderDelete.setOnClickListener {
            playOrdersAdapter.toggleDeleteMode()
            playOrdersAdapter.notifyDataSetChanged()
        }
        mBinding.groupPlayOrder.setOnClickListener {
            // collapse
            if (mBinding.ivPlayOrderArrow.isSelected) {
                mBinding.ivPlayOrderArrow.isSelected = false
                mBinding.ivPlayOrderArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_grey_700_24dp)
                mBinding.rvPlayOrders.visibility = View.GONE
            } else {
                mBinding.ivPlayOrderArrow.isSelected = true
                mBinding.ivPlayOrderArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_grey_700_24dp)
                mBinding.rvPlayOrders.visibility = View.VISIBLE
            }
        }
        mBinding.rvPlayOrders.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.groupStudio.setOnClickListener { view: View? -> selectStudio() }
        // Jzvd小窗快速滑动有BUG，慎用
//        mBinding.scrollParent.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
//            if (mBinding.videoView.visibility == View.VISIBLE && mBinding.videoView.isPlaying) {
//                floatOrEmbedVideo(oldScrollY, scrollY, mBinding.videoView.height)
//            }
//        }
        mBinding.ivDesktop.setOnClickListener {
            showConfirmCancelMessage(
                "即将在电脑上打开视频，是否继续？",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> mModel.openOnServer() },
                null
            )
        }
        mBinding.videoView.interceptFullScreenListener = View.OnClickListener {
            showConfirmCancelMessage("是否在临时列表中打开，若是，视频将从上一次记录的位置开始播放？",
                getString(R.string.yes),
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> mModel.playInPlayer() },
                getString(R.string.no),
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> mBinding.videoView.executeFullScreen() }
            )
        }
        mBinding.rvTags.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mBinding.rvTags.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildLayoutPosition(view)
                if (position > 0) {
                    outRect.left = ScreenUtils.dp2px(10f)
                }
            }
        })
        mBinding.ivTagAdd.setOnClickListener { addTag() }
        mBinding.ivTagDelete.setOnClickListener {
            tagAdapter!!.toggleDelete()
            tagAdapter!!.notifyDataSetChanged()
        }
        mBinding.ivMore.setOnClickListener {
            var intent = Intent(this@RecordActivity, ImageManagerActivity::class.java)
            intent.putExtra(ImageManagerActivity.EXTRA_TYPE, ImageManagerActivity.TYPE_RECORD)
            intent.putExtra(ImageManagerActivity.EXTRA_DATA, recordId)
            startActivity(intent)
        }
    }

    private fun addTag() {
        val fragment = TagFragment()
        fragment.onTagSelectListener = object : TagFragment.OnTagSelectListener{
            override fun onSelectTag(tag: Tag) {
                mModel.addTag(tag)
            }
        }
        fragment.tagType = DataConstants.TAG_TYPE_RECORD
        val dialogFragment = DraggableDialogFragment()
        dialogFragment.contentFragment = fragment
        dialogFragment.setTitle("Select tag")
        dialogFragment.maxHeight = ScreenUtils.dp2px(450f)
        dialogFragment.dismissListener = DialogInterface.OnDismissListener { mModel.refreshTags() }
        dialogFragment.show(supportFragmentManager, "TagFragment")
    }

    private var tinySwitchControl: Long = 0

    private fun floatOrEmbedVideo(oldScrollY: Int, scrollY: Int, edge: Int) {
        var current = System.currentTimeMillis()
        if (current - tinySwitchControl < 3000) {
            tinySwitchControl = current
            return
        }
        // 上滑且超出边界
        if (scrollY > edge && scrollY > oldScrollY && !mBinding.videoView.isTinyScreen) {
            DebugLog.e("gotoTinyScreen")
            mBinding.videoView.gotoTinyScreen()
        }
        else if (scrollY <= edge && scrollY < oldScrollY && mBinding.videoView.isTinyScreen) {
            DebugLog.e("cancelTinyScreen")
            // 快速滑动有BUG，慎用
            mBinding.videoView.cancelTinyScreen()
        }
    }

    private fun selectStudio() {
        StudioActivity.startPageToSelect(this, REQUEST_SELECT_STUDIO)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mModel.loadRecord(intent.getLongExtra(EXTRA_RECORD_ID, -1))
    }

    private fun setNoImage() {
        mBinding.banner.visibility = View.GONE
        mBinding.guideView.visibility = View.GONE
        mBinding.ivRecord.visibility = View.VISIBLE
        Glide.with(this@RecordActivity)
            .load(R.drawable.def_small)
            .into(mBinding.ivRecord)
    }

    private fun setSingleImage(path: String?) {
        mBinding.banner.visibility = View.GONE
        mBinding.guideView.visibility = View.GONE
        mBinding.ivRecord.visibility = View.VISIBLE
        GlideApp.with(this)
            .load(path)
            .error(R.drawable.def_large)
            .into(mBinding.ivRecord)
    }

    override fun initData() {
        initAdapters()

        mModel.imagesObserver.observe(this,
            Observer {
                when {
                    it.isEmpty() -> setNoImage()
                    it.size == 1 -> setSingleImage(it[0])
                    else -> {
                        mBinding.ivRecord.visibility = View.GONE
                        mBinding.banner.visibility = View.VISIBLE
                        mBinding.guideView.visibility = View.VISIBLE
                        showBanner(it)
                    }
                }
            }
        )
        mModel.starsObserver.observe(this, Observer { showStars(it) })
        mModel.recordObserver.observe(this, Observer {
            showRecord(it)
            mModel.loadRecordOrders()
            mModel.loadRecordPlayOrders()
        })
        mModel.scoresObserver.observe(this, Observer { showScores(it) })

        mModel.ordersObserver.observe(this,
            Observer {
                mBinding.tvOrder.text = it.size.toString()
                orderAdapter.list = it
                orderAdapter.notifyDataSetChanged()
            }
        )
        mModel.playOrdersObserver.observe(this,
            Observer {
                mBinding.tvPlayOrder.text = it.size.toString()
                playOrdersAdapter.list = it
                playOrdersAdapter.notifyDataSetChanged()
            }
        )
        mModel.passionsObserver.observe(this,
            Observer { list: List<PassionPoint> ->
                showPassionPoints(list)
            }
        )
        mModel.studioObserver.observe(this,
            Observer { studio: String? ->
                mBinding.tvStudio.text = studio
            }
        )
        mModel.videoUrlObserver.observe(this, Observer { previewVideo(it) })
        mModel.playVideoInPlayer.observe(this, Observer { playList() })
        mModel.bitmapObserver.observe(this, Observer { bitmap: Bitmap ->
            mBinding.banner.visibility = View.GONE
            mBinding.videoView.visibility = View.VISIBLE
            GlideApp.with(this)
                .load(bitmap)
                .centerCrop()
                .into(mBinding.videoView.posterImageView)
        })
        mModel.tagsObserver.observe(this,
            Observer { tags: List<Tag> ->
                showTags(tags)
            }
        )
        mModel.loadRecord(recordId)
    }

    private fun initAdapters() {
        orderAdapter.onDeleteListener =
            object : RecordOrdersAdapter.OnDeleteListener {
                override fun onDeleteOrder(order: FavorRecordOrder) {
                    mModel.deleteOrderOfRecord(order.id!!)
                    mModel.loadRecordOrders()
                }
            }
        mBinding.rvOrders.adapter = orderAdapter

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

        mBinding.rvScores.adapter = scoreAdapter

        starAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<RecordStarWrap> {
            override fun onClickItem(view: View, position: Int, data: RecordStarWrap) {
                goToStarPage(data)
            }
        })
        mBinding.rvStars.adapter = starAdapter

        playOrdersAdapter.onDeleteListener = object : RecordPlayOrdersAdapter.OnDeleteListener {
            override fun onDeleteOrder(order: VideoPlayList) {
                mModel.deletePlayOrderOfRecord(order.playOrder!!.id!!)
                mModel.loadRecordPlayOrders()
            }
        }
        mBinding.rvPlayOrders.adapter = playOrdersAdapter
    }

    private val recordId: Long
        private get() = intent.getLongExtra(EXTRA_RECORD_ID, -1)

    private fun showTags(tags: List<Tag>) {
        if (tags.isEmpty()) {
            mBinding.ivTagDelete.visibility = View.GONE
            mBinding.tvTagsTitle.visibility = View.VISIBLE
            tagAdapter.showDelete = false
        } else {
            mBinding.ivTagDelete.visibility = View.VISIBLE
            mBinding.tvTagsTitle.visibility = View.GONE
        }
        tagAdapter.list = tags
        tagAdapter.notifyDataSetChanged()
    }

    private fun playList() {
        startActivity(Intent(this, PlayerActivity::class.java))
    }

    private fun showScores(list: List<TitleValueBean>) {
        scoreAdapter.list = list
        scoreAdapter.notifyDataSetChanged()
    }

    private fun showPassionPoints(list: List<PassionPoint>) {
        val adapter = PassionPointAdapter()
        adapter.setList(list)
        mBinding.groupFk.setAdapter(adapter)
    }

    private fun showBanner(list: List<String?>) {
        BannerHelper.setBannerParams(mBinding.banner, ViewProperty.getRecordBannerParams())
        val adapter = HeadBannerAdapter()
        adapter.list = list
        mBinding.guideView.setPointNumber(list.size)
        mBinding.guideView.setGuideTextGravity(Gravity.CENTER)
        mBinding.banner.setOnBannerPageListener { page: Int, adapterIndex: Int ->
            mBinding.guideView.setFocusIndex(
                page
            )
        }
        mBinding.banner.adapter = adapter
        mBinding.banner.startAutoPlay()
    }

    private fun showStars(list: List<RecordStarWrap>) {
        starAdapter.list = list
        starAdapter.notifyDataSetChanged()
    }

    private fun goToStarPage(data: RecordStarWrap) {
        StarActivity.startPage(this, data.bean.starId)
    }

    private fun showRecord(record: RecordWrap) {
        // Record公共部分
        mBinding.tvDate.text = FormatUtil.formatDate(record.bean.lastModifyTime)
        mBinding.tvScene.text = record.bean.scene
        mBinding.tvPath.text = record.bean.directory + "/" + record.bean.name
        mBinding.tvHd.text = "" + record.bean.hdLevel
        mBinding.tvScoreTotal.text = "" + record.bean.score
        mBinding.tvFeel.text = "" + record.bean.scoreFeel
        if (record.bean.scoreBareback > 0) {
            mBinding.groupBareback.visibility = View.VISIBLE
        } else {
            mBinding.groupBareback.visibility = View.GONE
        }
        mBinding.tvCum.text = "" + record.bean.scoreCum
        mBinding.tvSpecial.text = "" + record.bean.scoreSpecial
        if (TextUtils.isEmpty(record.bean.specialDesc)) {
            mBinding.groupSpecial.visibility = View.GONE
        } else {
            mBinding.groupSpecial.visibility = View.VISIBLE
            mBinding.tvSpecialContent.text = record.bean.specialDesc
        }
        mBinding.tvFk.text = "Passion(" + record.bean.scorePassion + ")"
        mBinding.tvStar.text = "" + record.bean.scoreStar
        mBinding.tvBody.text = "" + record.bean.scoreBody
        mBinding.tvCock.text = "" + record.bean.scoreCock
        mBinding.tvAss.text = "" + record.bean.scoreAss
        mBinding.tvDeprecated.visibility = if (record.bean.deprecated == 1) View.VISIBLE else View.GONE
        val cuPath = getRecordCuPath(record.bean.name!!)
        if (!TextUtils.isEmpty(cuPath)) {
            mBinding.ivCum.visibility = View.VISIBLE
            GlideApp.with(this)
                .asGif()
                .load(cuPath)
                .into(mBinding.ivCum)
        }
        record.countRecord?.let {
            mBinding.tvRank.text = "R-${it.rank}"
        }
    }

    private fun showSettingDialog() {
        val bannerSettingDialog = BannerSettingFragment()
        bannerSettingDialog.params = ViewProperty.getRecordBannerParams()
        bannerSettingDialog.onAnimSettingListener = object : BannerSettingFragment.OnAnimSettingListener {
            override fun onParamsUpdated(params: BannerHelper.BannerParams) {}
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

    /**
     * init video player
     * 本页面嵌入式播放视频不做播放时间记录，全屏播放器才记录
     * @param url
     */
    private fun previewVideo(url: String) {
        mBinding.banner.visibility = View.GONE
        mBinding.videoView.visibility = View.VISIBLE
        // 本地没有图片，从网络视频获取帧图片
        if (mModel.mVideoCover == null) {
            mModel.loadVideoBitmap()
        }
        mBinding.videoView.setUp(url, "")
        ImageBindingAdapter.setRecordUrl(mBinding.videoView.posterImageView, mModel.mVideoCover)

//        mBinding.videoView.setVideoPath(url)
//        mBinding.videoView.prepare()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // videoView必须在manifest中所属activity指定
        // android:configChanges="orientation|screenSize",且其中两个参数缺一不可
        // 同时在onConfigurationChanged中加入相关代码。
        // 这样在点击全屏时才能顺畅地切换为全屏
        PlayerManager.getInstance().onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    public override fun onPause() {
        super.onPause()
        kotlin.runCatching {
            mBinding.banner.stopAutoPlay()
        }
        Jzvd.releaseAllVideos()
    }

    public override fun onResume() {
        super.onResume()
        if (mBinding != null && mBinding.banner != null) {
            mBinding.banner.startAutoPlay()
        }
    }

    private fun selectOrderToAddStar() {
//        Router.build("OrderPhone")
//            .with(OrderPhoneActivity.EXTRA_SELECT_MODE, true)
//            .with(OrderPhoneActivity.EXTRA_SELECT_RECORD, true)
//            .requestCode(REQUEST_ADD_ORDER)
//            .go(this)
        TODO()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
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
                TODO()
//                val list = data?.getCharSequenceArrayListExtra(PlayOrderActivity.RESP_SELECT_RESULT)
//                mModel.addToPlay(list)
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mBinding != null && mBinding.banner != null) {
            mBinding.banner.stopAutoPlay()
        }
    }

    private inner class HeadBannerAdapter : CoolBannerAdapter<String?>() {
        override fun getLayoutRes(): Int {
            return R.layout.adapter_banner_image
        }

        override fun onBindView(
            view: View,
            position: Int,
            path: String?
        ) {
            val imageView = view.findViewById<ImageView>(R.id.iv_image)
            ImageBindingAdapter.setRecordUrl(imageView, path)
        }
    }
}