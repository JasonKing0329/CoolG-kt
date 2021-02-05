package com.king.app.coolg_kt.page.star.pad

import android.content.Context
import android.content.Intent
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseActivity
import com.king.app.coolg_kt.base.EmptyViewModel
import com.king.app.coolg_kt.databinding.ActivityStarsPadBinding
import com.king.app.coolg_kt.page.record.RecordsFragment
import com.king.app.coolg_kt.page.star.phone.StarListClassicFragment

/**
 * @description:
 * @author：Jing
 * @date: 2021/2/5 19:26
 */
class StarsPadActivity: BaseActivity<ActivityStarsPadBinding, EmptyViewModel>() {

    companion object {
        const val EXTRA_STUDIO_ID = "studio_id"
        fun startPage(context: Context) {
            var intent = Intent(context, StarsPadActivity::class.java)
            context.startActivity(intent)
        }
        fun startStudioPage(context: Context, studioId: Long) {
            var intent = Intent(context, StarsPadActivity::class.java)
            intent.putExtra(EXTRA_STUDIO_ID, studioId)
            context.startActivity(intent)
        }
    }

    private var ftRecord: RecordsFragment? = null

    override fun isFullScreen(): Boolean = true

    override fun getContentView(): Int = R.layout.activity_stars_pad

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView() {
        val star = StarListClassicFragment()
        star.onClickStarListener = object : StarListClassicFragment.OnClickStarListener {
            override fun onClickStar(starId: Long) {
                if (ftRecord == null) {
                    ftRecord = RecordsFragment()
                    ftRecord!!.factor.starId = starId
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fl_record, ftRecord!!, "RecordsFragment")
                        .commit()
                }
                else {
                    ftRecord!!.factor.starId = starId
                    ftRecord!!.onDataChanged()
                }
            }

            override fun onLongClickStar(starId: Long) {

            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_star, star, "StarListFragment")
            .commit()
    }

    override fun initData() {

    }

}