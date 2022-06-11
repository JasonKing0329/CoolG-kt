package com.king.app.coolg_kt.page.record.phone

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.databinding.ActivityRecordPhoneBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.model.setting.ViewProperty
import com.king.app.coolg_kt.page.image.ImageManagerActivity
import com.king.app.coolg_kt.page.pub.BannerSettingFragment
import com.king.app.coolg_kt.page.record.RecordViewModel
import com.king.app.coolg_kt.utils.BannerHelper
import com.king.app.coolg_kt.utils.DebugLog
import com.king.app.coolg_kt.view.dialog.DraggableDialogFragment
import com.king.app.coolg_kt.view.dialog.SimpleDialogs
import com.king.lib.banner.CoolBannerAdapter

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

    private var ftDetail: RecordDetailFragment? = null
    private var ftModify: RecordModifyFragment? = null

    override fun getContentView(): Int = R.layout.activity_record_phone

    override fun createViewModel(): RecordViewModel = generateViewModel(RecordViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }
        mBinding.actionbar.setOnMenuItemListener { menuId: Int ->
            when (menuId) {
                R.id.menu_edit -> mModel.checkEdit()
                R.id.menu_banner_setting -> showSettingDialog()
            }
        }
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
        mBinding.ivTv.setOnClickListener {
            SimpleDialogs().openInputDialog(this, "Ip", SettingProperty.getSocketServerUrl(), SimpleDialogs.OnDialogActionListener {
                mModel.playInSocketServer(it)
            })
        }
        // 取消对全屏的拦截，用默认的全屏事件。系统播放器改用另外的按钮启动
//        mBinding.videoView.interceptFullScreenListener = View.OnClickListener {
//            showConfirmCancelMessage("是否在临时列表中打开，若是，视频将从上一次记录的位置开始播放？",
//                getString(R.string.yes),
//                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> mModel.playInPlayer() },
//                getString(R.string.no),
//                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int -> mBinding.videoView.executeFullScreen() }
//            )
//        }
        mBinding.ivMore.setOnClickListener {
            var intent = Intent(this@RecordActivity, ImageManagerActivity::class.java)
            intent.putExtra(ImageManagerActivity.EXTRA_TYPE, ImageManagerActivity.TYPE_RECORD)
            intent.putExtra(ImageManagerActivity.EXTRA_DATA, recordId)
            startActivity(intent)
        }

        mBinding.actionbar.setOnConfirmListener {
            if (ftModify?.executeModify() == true) {
                false
            }
            else {
                showDetailPage()
                true
            }
        }
        mBinding.actionbar.setOnCancelListener {
            if (ftModify?.isDataChanged() == true) {
                showConfirmCancelMessage(
                    "Data is changed, are you sure to drop it?",
                    { dialog, which ->
                        showDetailPage()
                        mBinding.actionbar.cancelConfirmStatus()
                    },
                    null
                )
                false
            }
            else {
                showDetailPage()
                true
            }
        }
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

    private fun showDetailPage() {
        if (ftDetail == null) {
            RecordDetailFragment().apply {
                ftDetail = this
                supportFragmentManager.beginTransaction()
                    .replace(R.id.group_ft, this, "RecordDetailFragment")
                    .commit()
            }
        }
        else {
            supportFragmentManager.beginTransaction().apply {
                ftModify?.let {
                    remove(it)
                }
                ftModify = null
                show(ftDetail!!)
                commit()
            }
        }
    }

    override fun initData() {
        mModel.recordObserver.observe(this) {
            kotlin.runCatching { mBinding.actionbar.cancelConfirmStatus() }
            showDetailPage()
        }
        mModel.canEdit.observe(this) {
            mBinding.actionbar.showConfirmStatus(0)
            ftModify = RecordModifyFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.group_ft, ftModify!!, "RecordModifyFragment")
                .hide(ftDetail!!)
                .commit()
        }
        mModel.imagesObserver.observe(this) {
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
        mModel.videoUrlObserver.observe(this, Observer { previewVideo(it) })
        mModel.bitmapObserver.observe(this, Observer { bitmap: Bitmap ->
            mBinding.banner.visibility = View.GONE
            mBinding.videoView.visibility = View.VISIBLE
            GlideApp.with(this)
                .load(bitmap)
                .centerCrop()
                .into(mBinding.videoView.posterImageView)
        })
        mModel.loadRecord(recordId)
    }

    private val recordId: Long
        private get() = intent.getLongExtra(EXTRA_RECORD_ID, -1)

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

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        if (ftModify?.isVisible == true) {
            showConfirmMessage("Please save or cancel edition first", null)
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