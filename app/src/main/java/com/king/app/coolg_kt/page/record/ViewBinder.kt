package com.king.app.coolg_kt.page.record

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.king.app.coolg_kt.R
import com.king.app.coolg_kt.conf.PreferenceValue
import com.king.app.coolg_kt.databinding.AdapterRecordItemGridBinding
import com.king.app.coolg_kt.databinding.AdapterRecordItemListBinding
import com.king.app.coolg_kt.model.GlideApp
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.utils.FormatUtil
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/14 16:37
 */
class RecordItemBinder: BaseItemBinder() {
    fun bind(binding: AdapterRecordItemListBinding, position: Int, item: RecordWrap) {
        var bean = item.bean
        binding.tvName.text = bean.name
        if (bean.scoreBareback > 0) {
            binding.tvName.setTextColor(
                binding.tvName.resources.getColor(R.color.gdb_record_text_bareback_light)
            )
        } else {
            binding.tvName.setTextColor(
                binding.tvName.resources.getColor(R.color.gdb_record_text_normal_light)
            )
        }
        binding.tvPath.text = bean.directory
        binding.tvDate.text = FormatUtil.formatDate(bean.lastModifyTime)
        binding.tvDeprecated.visibility = if (bean.deprecated == 1) View.VISIBLE else View.GONE
        binding.tvSeq.text = (position + 1).toString()
        if (TextUtils.isEmpty(bean.specialDesc)) {
            binding.tvSpecial.visibility = View.GONE
        } else {
            binding.tvSpecial.visibility = View.VISIBLE
            binding.tvSpecial.text = bean.specialDesc
        }
        binding.tvScene.text = bean.scene

        if (PreferenceValue.GDB_SR_ORDERBY_DATE === mSortMode) {
            binding.tvDate.visibility = View.INVISIBLE
        }
        try {
            showSortScore(binding.tvSort, item, mSortMode)
        } catch (e: Exception) {
        }

        bindImage(binding.ivImage, item)

        if (selectionMode) {
            binding.cbCheck.visibility = View.VISIBLE
            var isCheck = mCheckMap[bean.id!!]
            binding.cbCheck.isChecked =  isCheck?: false
        } else {
            binding.cbCheck.visibility = View.GONE
        }
    }
}

class RecordItemGridBinder: BaseItemBinder() {
    var onPopupListener: OnPopupListener? = null

    fun bind(binding: AdapterRecordItemGridBinding, position: Int, item: RecordWrap) {
        if (selectionMode) {
            binding.tvSeq.visibility = View.GONE
            binding.cbCheck.visibility = View.VISIBLE
            binding.cbCheck.isChecked = mCheckMap[item.bean.id] != null
        } else {
            binding.tvSeq.visibility = View.VISIBLE
            binding.cbCheck.visibility = View.GONE
            binding.tvSeq.text = (position + 1).toString()
            binding.ivEdit.setOnClickListener { v ->
                onPopupListener?.onPopupRecord(v, position, item)
            }
        }
        val starList = item.starList
        val starBuffer = StringBuffer()
        starList.forEach { starBuffer.append(", ").append(it.name) }
        var starText = starBuffer.toString()
        if (starText.length > 1) {
            starText = starText.substring(1)
        }
        binding.tvStars.setText(starText)

        binding.ivPlay.visibility = View.INVISIBLE

        binding.tvPics.text = "(${ImageProvider.getRecordPicNumber(item.bean.name)} pics)"
        bindImage(binding.ivRecord, item)
        showSortScore(binding.tvSort, item, mSortMode)
    }

}

interface OnPopupListener {
    fun onPopupRecord(view: View, position: Int, record: RecordWrap)
}

open class BaseItemBinder {
    var mSortMode = 0
    var selectionMode = false
    var mCheckMap = mutableMapOf<Long, Boolean>()

    fun bindImage(view: ImageView, item: RecordWrap) {
        GlideApp.with(view.context)
            .load(item.imageUrl)
            .error(R.drawable.def_small)
            .into(view)
    }

    fun showSortScore(sortView: TextView, item: RecordWrap, mSortMode: Int) {
        when (mSortMode) {
            PreferenceValue.GDB_SR_ORDERBY_DATE -> {
                sortView.visibility = View.VISIBLE
                sortView.text = FormatUtil.formatDate(item.bean.lastModifyTime)
            }
            PreferenceValue.GDB_SR_ORDERBY_BAREBACK -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreBareback}"
            }
            PreferenceValue.GDB_SR_ORDERBY_BJOB -> {
                sortView.visibility = View.VISIBLE
                when(item.bean.type) {
                    DataConstants.VALUE_RECORD_TYPE_1V1 -> sortView.text = "${item.recordType1v1?.scoreBjob}"
                    DataConstants.VALUE_RECORD_TYPE_3W
                        , DataConstants.VALUE_RECORD_TYPE_MULTI
                        , DataConstants.VALUE_RECORD_TYPE_LONG -> sortView.text = "${item.recordType3w?.scoreBjob}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_CSHOW -> {
                sortView.visibility = View.VISIBLE
                when(item.bean.type) {
                    DataConstants.VALUE_RECORD_TYPE_1V1 -> sortView.text = "${item.recordType1v1?.scoreCshow}"
                    DataConstants.VALUE_RECORD_TYPE_3W
                        , DataConstants.VALUE_RECORD_TYPE_MULTI
                        , DataConstants.VALUE_RECORD_TYPE_LONG -> sortView.text = "${item.recordType3w?.scoreCshow}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_CUM -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreCum}"
            }
            PreferenceValue.GDB_SR_ORDERBY_PASSION -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scorePassion}"
            }
            PreferenceValue.GDB_SR_ORDERBY_FK1 -> {
                sortView.visibility = View.VISIBLE
                if (item.bean.type == DataConstants.VALUE_RECORD_TYPE_1V1) {
                    sortView.text = "${item.recordType1v1?.scoreFkType1}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_FK2 -> {
                sortView.visibility = View.VISIBLE
                if (item.bean.type == DataConstants.VALUE_RECORD_TYPE_1V1) {
                    sortView.text = "${item.recordType1v1?.scoreFkType2}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_FK3 -> {
                sortView.visibility = View.VISIBLE
                if (item.bean.type == DataConstants.VALUE_RECORD_TYPE_1V1) {
                    sortView.text = "${item.recordType1v1?.scoreFkType3}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_FK4 -> {
                sortView.visibility = View.VISIBLE
                if (item.bean.type == DataConstants.VALUE_RECORD_TYPE_1V1) {
                    sortView.text = "${item.recordType1v1?.scoreFkType4}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_FK5 -> {
                sortView.visibility = View.VISIBLE
                if (item.bean.type == DataConstants.VALUE_RECORD_TYPE_1V1) {
                    sortView.text = "${item.recordType1v1?.scoreFkType5}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_FK6 -> {
                sortView.visibility = View.VISIBLE
                if (item.bean.type == DataConstants.VALUE_RECORD_TYPE_1V1) {
                    sortView.text = "${item.recordType1v1?.scoreFkType6}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_FOREPLAY -> {
                sortView.visibility = View.VISIBLE
                when(item.bean.type) {
                    DataConstants.VALUE_RECORD_TYPE_1V1 -> sortView.text = "${item.recordType1v1?.scoreForePlay}"
                    DataConstants.VALUE_RECORD_TYPE_3W
                        , DataConstants.VALUE_RECORD_TYPE_MULTI
                        , DataConstants.VALUE_RECORD_TYPE_LONG -> sortView.text = "${item.recordType3w?.scoreForePlay}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_HD -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.hdLevel}"
            }
            PreferenceValue.GDB_SR_ORDERBY_RHYTHM -> {
                sortView.visibility = View.VISIBLE
                when(item.bean.type) {
                    DataConstants.VALUE_RECORD_TYPE_1V1 -> sortView.text = "${item.recordType1v1?.scoreRhythm}"
                    DataConstants.VALUE_RECORD_TYPE_3W
                        , DataConstants.VALUE_RECORD_TYPE_MULTI
                        , DataConstants.VALUE_RECORD_TYPE_LONG -> sortView.text = "${item.recordType3w?.scoreRhythm}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_RIM -> {
                sortView.visibility = View.VISIBLE
                when(item.bean.type) {
                    DataConstants.VALUE_RECORD_TYPE_1V1 -> sortView.text = "${item.recordType1v1?.scoreRim}"
                    DataConstants.VALUE_RECORD_TYPE_3W
                        , DataConstants.VALUE_RECORD_TYPE_MULTI
                        , DataConstants.VALUE_RECORD_TYPE_LONG -> sortView.text = "${item.recordType3w?.scoreRim}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_SCENE -> {
                sortView.visibility = View.VISIBLE
                when(item.bean.type) {
                    DataConstants.VALUE_RECORD_TYPE_1V1 -> sortView.text = "${item.recordType1v1?.scoreScene}"
                    DataConstants.VALUE_RECORD_TYPE_3W
                        , DataConstants.VALUE_RECORD_TYPE_MULTI
                        , DataConstants.VALUE_RECORD_TYPE_LONG -> sortView.text = "${item.recordType3w?.scoreScene}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_SCOREFEEL -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreFeel}"
            }
            PreferenceValue.GDB_SR_ORDERBY_SPECIAL -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreSpecial}"
            }
            PreferenceValue.GDB_SR_ORDERBY_STAR1 -> {
            }
            PreferenceValue.GDB_SR_ORDERBY_STAR2 -> {
            }
            PreferenceValue.GDB_SR_ORDERBY_STARCC1 -> {
            }
            PreferenceValue.GDB_SR_ORDERBY_STARCC2 -> {
            }
            PreferenceValue.GDB_SR_ORDERBY_STORY -> {
                sortView.visibility = View.VISIBLE
                when(item.bean.type) {
                    DataConstants.VALUE_RECORD_TYPE_1V1 -> sortView.text = "${item.recordType1v1?.scoreStory}"
                    DataConstants.VALUE_RECORD_TYPE_3W
                        , DataConstants.VALUE_RECORD_TYPE_MULTI
                        , DataConstants.VALUE_RECORD_TYPE_LONG -> sortView.text = "${item.recordType3w?.scoreStory}"
                }
            }
            PreferenceValue.GDB_SR_ORDERBY_SCORE_BASIC -> {
            }
            PreferenceValue.GDB_SR_ORDERBY_SCORE_EXTRA -> {
            }
            PreferenceValue.GDB_SR_ORDERBY_STAR -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreStar}"
            }
            PreferenceValue.GDB_SR_ORDERBY_STARC -> {
            }
            PreferenceValue.GDB_SR_ORDERBY_BODY -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreBody}"
            }
            PreferenceValue.GDB_SR_ORDERBY_COCK -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreCock}"
            }
            PreferenceValue.GDB_SR_ORDERBY_ASS -> {
                sortView.visibility = View.VISIBLE
                sortView.text = "${item.bean.scoreAss}"
            }
            else -> sortView.text = "${item.bean.score}"
        }
    }
}

data class RecordTag(
    var name: String,
    var id: Long,
    var number: Int = 0
)