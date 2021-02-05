package com.king.app.coolg_kt.page.star.phone

import android.content.Context
import android.content.Intent
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.ActivityStarsPhoneBinding

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/5 18:35
 */
class StarsPhoneActivity: BaseActivity<ActivityStarsPhoneBinding, EmptyViewModel>() {

    companion object {
        const val EXTRA_STUDIO_ID = "studio_id"
        fun startPage(context: Context) {
            var intent = Intent(context, StarsPhoneActivity::class.java)
            context.startActivity(intent)
        }
        fun startStudioPage(context: Context, studioId: Long) {
            var intent = Intent(context, StarsPhoneActivity::class.java)
            intent.putExtra(EXTRA_STUDIO_ID, studioId)
            context.startActivity(intent)
        }
    }

    override fun getContentView(): Int = R.layout.activity_stars_phone

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView() {
        val fragment = StarListClassicFragment()
        fragment.studioId = studioId

        supportFragmentManager.beginTransaction()
            .replace(R.id.group_ft, fragment, "StarListFragment")
            .commit()
    }

    override fun initData() {

    }

    private val studioId: Long
        private get() = intent.getLongExtra(EXTRA_STUDIO_ID, 0)

}