package com.king.app.coolg_kt.page.star

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.BindingDialogFragment
import com.king.app.coolg_kt.databinding.FragmentDialogStarRatingBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.palette.BitmapPaletteListener
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.coolg_kt.view.widget.StarRatingView
import com.king.app.coolg_kt.view.widget.StarRatingView.OnStarChangeListener
import com.king.app.gdb.data.entity.StarRating
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 16:25
 */
class StarRatingDialog : BindingDialogFragment<FragmentDialogStarRatingBinding>(), OnStarChangeListener {
    var onDismissListener: DialogInterface.OnDismissListener? = null
    private lateinit var mModel: StarRatingViewModel
    var starId: Long = 0

    override fun getBinding(inflater: LayoutInflater): FragmentDialogStarRatingBinding = FragmentDialogStarRatingBinding.inflate(inflater)

    override fun onStart() {
        setWidth(resources.getDimensionPixelSize(R.dimen.dlg_rating_width))
        super.onStart()
    }

    override fun initView(view: View) {
        mModel = generateViewModel(StarRatingViewModel::class.java)
        mBinding.starFace.setOnStarChangeListener(this)
        mBinding.starBody.setOnStarChangeListener(this)
        mBinding.starDk.setOnStarChangeListener(this)
        mBinding.starPassion.setOnStarChangeListener(this)
        mBinding.starVideo.setOnStarChangeListener(this)
        mBinding.starSex.setOnStarChangeListener(this)
        mBinding.starPrefer.setOnStarChangeListener(this)
        mBinding.ivClose.setOnClickListener { dismissAllowingStateLoss() }
        mModel.starObserver.observe(this, Observer { star: StarWrap -> showStar(star) })
        mModel.ratingObserver.observe(this, Observer { rating: StarRating -> showRatings(rating) })
        mBinding.tvSave.setOnClickListener {
            mModel.saveRating()
            dismissAllowingStateLoss()
        }
        mModel.loadStarRating(starId)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }

    fun showStar(star: StarWrap) {
        mBinding.tvStar.text = star.bean.name
        GlideApp.with(requireContext())
            .asBitmap()
            .load(mModel.getStarImage())
            .listener(object : BitmapPaletteListener(lifecycle) {
                override fun onPaletteCreated(palette: Palette) {
                    setStarColor(mModel.generateStarColor(resources, palette))
                }
            })
            .error(R.drawable.ic_def_person_wide)
            .into(mBinding.ivStar)
    }

    fun showRatings(rating: StarRating) {
        mBinding.tvRating.text = mModel.getComplex()
        mBinding.starFace.setCheckNumber(rating.face)
        mBinding.tvFace.text = StarRatingUtil.getSubRatingValue(rating.face)
        mBinding.starBody.setCheckNumber(rating.body)
        mBinding.tvBody.text = StarRatingUtil.getSubRatingValue(rating.body)
        mBinding.starDk.setCheckNumber(rating.dk)
        mBinding.tvDk.text = StarRatingUtil.getSubRatingValue(rating.dk)
        mBinding.starSex.setCheckNumber(rating.sexuality)
        mBinding.tvSex.text = StarRatingUtil.getSubRatingValue(rating.sexuality)
        mBinding.starPassion.setCheckNumber(rating.passion)
        mBinding.tvPassion.text = StarRatingUtil.getSubRatingValue(rating.passion)
        mBinding.starVideo.setCheckNumber(rating.video)
        mBinding.tvVideo.text = StarRatingUtil.getSubRatingValue(rating.video)
        mBinding.starPrefer.setCheckNumber(rating.prefer)
        mBinding.tvPrefer.text = StarRatingUtil.getSubRatingValue(rating.prefer)
    }

    fun setStarColor(color: Int) {
        mBinding.starVideo.setStarColor(color)
        mBinding.starSex.setStarColor(color)
        mBinding.starPassion.setStarColor(color)
        mBinding.starDk.setStarColor(color)
        mBinding.starFace.setStarColor(color)
        mBinding.starBody.setStarColor(color)
        mBinding.starPrefer.setStarColor(color)
    }

    override fun onStarChanged(view: StarRatingView, checkedStar: Float) {
        mModel.rating?.let {
            val rateValue = StarRatingUtil.getSubRatingValue(checkedStar)
            when (view.id) {
                R.id.star_face -> {
                    it.face = checkedStar
                    mBinding.tvFace.text = rateValue
                }
                R.id.star_body -> {
                    it.body = checkedStar
                    mBinding.tvBody.text = rateValue
                }
                R.id.star_dk -> {
                    it.dk = checkedStar
                    mBinding.tvDk.text = rateValue
                }
                R.id.star_passion -> {
                    it.passion = checkedStar
                    mBinding.tvPassion.text = rateValue
                }
                R.id.star_video -> {
                    it.video = checkedStar
                    mBinding.tvVideo.text = rateValue
                }
                R.id.star_sex -> {
                    it.sexuality = checkedStar
                    mBinding.tvSex.text = rateValue
                }
                R.id.star_prefer -> {
                    it.prefer = checkedStar
                    mBinding.tvPrefer.text = rateValue
                }
            }
        }
        mBinding.tvRating.text = mModel.getComplex()
    }
}