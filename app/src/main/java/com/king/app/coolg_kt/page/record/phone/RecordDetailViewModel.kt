package com.king.app.coolg_kt.page.record.phone

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.PassionPoint
import com.king.app.coolg_kt.model.bean.TitleValueBean
import com.king.app.coolg_kt.model.repository.RecordRepository
import com.king.app.gdb.data.entity.Tag
import com.king.app.gdb.data.entity.TagRecord
import com.king.app.gdb.data.relation.RecordStarWrap
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2022/6/9 9:39
 */
class RecordDetailViewModel(application: Application): BaseViewModel(application) {

    var starsObserver: MutableLiveData<List<RecordStarWrap>> = MutableLiveData()

    var passionsObserver: MutableLiveData<List<PassionPoint>> = MutableLiveData()

    var scoresObserver: MutableLiveData<List<TitleValueBean>> = MutableLiveData()

    var tagsObserver: MutableLiveData<List<Tag>> =
        MutableLiveData()

    private val repository = RecordRepository()

    lateinit var record: RecordWrap

    fun loadDetails(record: RecordWrap) {
        this.record = record
        launchMain {
            // scores
            scoresObserver.value = repository.createScoreItems(record)

            // stars
            starsObserver.value = repository.getRecordStars(record.bean.id!!)

            // passion point
            passionsObserver.value = repository.getPassions(record)

            // tags
            tagsObserver.value = getTags(record)
        }

    }

    private fun refreshTags() {
        tagsObserver.value = getTags(record)
    }

    private fun getTags(record: RecordWrap): List<Tag> {
        return getDatabase().getTagDao().getRecordTags(record.bean.id!!)
    }

    fun deleteTag(bean: Tag) {
        getDatabase().getTagDao().deleteTagRecordsBy(bean.id!!, record.bean.id!!)
        refreshTags()
    }

    fun addTag(tag: Tag) {
        addTag(tag.id!!)
    }

    fun addTag(tagId: Long) {
        var count = getDatabase().getTagDao().countRecordTag(record.bean.id!!, tagId)
        if (count == 0) {
            var list = mutableListOf<TagRecord>()
            list.add(TagRecord(null, tagId, record.bean.id!!))
            getDatabase().getTagDao().insertTagRecords(list)
            refreshTags()
        }
    }

}