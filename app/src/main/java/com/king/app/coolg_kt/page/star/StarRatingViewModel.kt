package com.king.app.coolg_kt.page.star

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.http.observer.SimpleObserver
import com.king.app.coolg_kt.model.image.ImageProvider.getStarRandomPath
import com.king.app.coolg_kt.model.palette.PaletteUtil
import com.king.app.coolg_kt.model.repository.StarRepository
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.entity.StarRating
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 16:27
 */
class StarRatingViewModel(application: Application) : BaseViewModel(application) {

    lateinit var mStar: StarWrap
    var rating: StarRating? = null
    val repository: StarRepository = StarRepository()
    var starObserver = MutableLiveData<StarWrap>()
    var ratingObserver = MutableLiveData<StarRating>()

    fun loadStarRating(starId: Long) {
        repository.getStar(starId)
            .compose(applySchedulers())
            .subscribe(object : SimpleObserver<StarWrap>(getComposite()) {
                override fun onNext(star: StarWrap) {
                    mStar = star
                    starObserver.value = star
                    if (star.rating == null) {
                        rating = StarRating(null, mStar.bean.id!!)
                    }
                    else {
                        rating = star.rating
                        ratingObserver.value = rating
                    }
                }

                override fun onError(e: Throwable) {
                    e?.printStackTrace()
                    messageObserver.value = e?.message
                }
            })
    }

    fun getComplex(): String {
        rating?.let {
            val complex = calculateComplex(it)
            return "${StarRatingUtil.getRatingValue(complex)}(${FormatUtil.formatScore(complex.toDouble(), 2)})"
        }
        return "NR"
    }

    fun getStarImage(): String? {
        return getStarRandomPath(mStar.bean.name, null)
    }

    fun saveRating() {
        rating?.let {
            it.starId = mStar.bean.id!!
            it.complex = calculateComplex(it)
            if (it.id == null) {
                getDatabase().getStarDao().insertStarRating(it)
            }
            else {
                getDatabase().getStarDao().updateStarRating(it)
            }
            messageObserver.setValue("Save successfully")
        }
    }

    private fun calculateComplex(mRating: StarRating): Float {
        return mRating.body * 0.15f + mRating.dk * 0.08f + mRating.video * 0.07f + mRating.face * 0.15f + mRating.passion * 0.15f + mRating.sexuality * 0.2f + mRating.prefer * 0.2f
    }

    fun generateStarColor(resources: Resources, palette: Palette?): Int {
        val swatch = PaletteUtil.getDefaultSwatch(palette)
        return swatch?.rgb ?: resources.getColor(R.color.colorAccent)
    }

}