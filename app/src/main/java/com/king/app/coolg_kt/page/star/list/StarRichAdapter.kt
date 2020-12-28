package com.king.app.coolg_kt.page.star.list

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.base.adapter.BaseBindingAdapter
import com.king.app.coolg_kt.databinding.AdapterStarRichBinding
import com.king.app.coolg_kt.model.extension.ImageBindingAdapter
import com.king.app.coolg_kt.page.star.OnStarRatingListener
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.coolg_kt.utils.StarRatingUtil
import com.king.app.gdb.data.entity.Star
import com.king.app.gdb.data.relation.StarWrap

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/9 15:20
 */
class StarRichAdapter : BaseBindingAdapter<AdapterStarRichBinding, StarWrap>() {

    var mExpandMap = mutableMapOf<Long, Boolean>()
    var onStarRatingListener: OnStarRatingListener? = null
    override fun onCreateBind(inflater: LayoutInflater, parent: ViewGroup): AdapterStarRichBinding = AdapterStarRichBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterStarRichBinding,
        position: Int,
        bean: StarWrap
    ) {
        val star = bean.bean
        binding.tvName.text = star.name
        binding.tvVideos.text = "${star.records} Videos"
        binding.tvType.text = getTypeText(star)
        updateScore(binding.tvScore, star)
        updateScoreC(binding.tvScoreC, star)
        binding.tvIndex.text = (position + 1).toString()
        var rating = bean.rating
        if (rating != null) {
            binding.tvRating.text = StarRatingUtil.getRatingValue(rating.complex)
            StarRatingUtil.updateRatingColor(binding.tvRating, rating)
            binding.tvFace.text = "Face " + StarRatingUtil.getSubRatingValue(rating.face)
            binding.tvFace.setTextColor(
                StarRatingUtil.getSubRatingColor(rating.face, binding.tvFace.resources)
            )
            binding.tvBody.text = "Body " + StarRatingUtil.getSubRatingValue(rating.body)
            binding.tvBody.setTextColor(
                StarRatingUtil.getSubRatingColor(rating.body, binding.tvFace.resources)
            )
            binding.tvSex.text = "Sexuality " + StarRatingUtil.getSubRatingValue(rating.sexuality)
            binding.tvSex.setTextColor(
                StarRatingUtil.getSubRatingColor(rating.sexuality, binding.tvFace.resources)
            )
            binding.tvDk.text = "Dk/Butt " + StarRatingUtil.getSubRatingValue(rating.dk)
            binding.tvDk.setTextColor(
                StarRatingUtil.getSubRatingColor(rating.dk, binding.tvFace.resources)
            )
            binding.tvPassion.text = "Passion " + StarRatingUtil.getSubRatingValue(rating.passion)
            binding.tvPassion.setTextColor(
                StarRatingUtil.getSubRatingColor(rating.passion, binding.tvFace.resources)
            )
            binding.tvVideo.text = "Video " + StarRatingUtil.getSubRatingValue(rating.video)
            binding.tvVideo.setTextColor(
                StarRatingUtil.getSubRatingColor(rating.video, binding.tvFace.resources)
            )
            binding.tvPrefer.text = "Prefer " + StarRatingUtil.getSubRatingValue(rating.prefer)
            binding.tvPrefer.setTextColor(
                StarRatingUtil.getSubRatingColor(rating.prefer, binding.tvPrefer.resources)
            )
            binding.groupRating.visibility = View.VISIBLE
        }
        else {
            binding.tvRating.text = StarRatingUtil.NON_RATING
            StarRatingUtil.updateRatingColor(binding.tvRating, null)
            binding.groupRating.visibility = View.GONE
        }
        binding.tvRating.tag = position
        binding.tvRating.setOnClickListener {
            onStarRatingListener?.onUpdateRating(position, bean.bean.id!!)
        }

        ImageBindingAdapter.setStarUrl(binding.ivPlayer, bean.imagePath)
        binding.groupExpand.visibility = if (mExpandMap[star.id] == true) View.VISIBLE else View.GONE
        binding.ivMore.setImageResource(if (mExpandMap[star.id] == true) R.drawable.ic_keyboard_arrow_up_666_24dp else R.drawable.ic_keyboard_arrow_down_666_24dp)
        binding.ivMore.tag = position
        binding.ivMore.setOnClickListener {
            val starId: Long = bean.bean.id!!
            val targetExpand = !(mExpandMap[starId]?:false)
            mExpandMap[starId] = targetExpand
            notifyItemChanged(position)
        }
    }

    private fun updateScore(view: TextView, star: Star) {
        val buffer = StringBuffer()
        if (star.max > 0) {
            if (star.min == star.max) {
                buffer.append("score(").append(star.max).append(")")
            } else {
                buffer.append("max(").append(star.max).append(")  ")
                    .append("min(").append(star.min).append(")  ")
                    .append("avg(").append(FormatUtil.formatScore(star.average.toDouble(), 1))
                    .append(")")
            }
            view.text = buffer.toString()
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun updateScoreC(view: TextView, star: Star) {
        val buffer = StringBuffer()
        if (star.cmax > 0) {
            if (star.cmax == star.cmin) {
                buffer.append("C score(").append(star.cmax).append(")")
            } else {
                buffer.append("C max(").append(star.cmax).append(")  ")
                    .append("min(").append(star.cmin).append(")  ")
                    .append("avg(").append(FormatUtil.formatScore(star.caverage.toDouble(), 1))
                    .append(")")
            }
            view.text = buffer.toString()
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun getTypeText(star: Star): String {
        var text = ""
        if (star.betop > 0) {
            text = "Top ${star.betop}"
        }
        if (star.bebottom > 0) {
            if (!TextUtils.isEmpty(text)) {
                text = "$text, "
            }
            text = "${text}Bottom ${star.bebottom}"
        }
        return text
    }

    fun notifyStarChanged(starId: Long) {
        list?.let {
            for (i in it.indices) {
                if (it[i].bean.id === starId) {
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }
}