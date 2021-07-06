package com.king.app.coolg_kt.model.repository

import com.king.app.coolg_kt.conf.AppConstants
import com.king.app.coolg_kt.utils.PinyinUtil
import com.king.app.gdb.data.DataConstants
import com.king.app.gdb.data.entity.Tag
import io.reactivex.rxjava3.core.Observable

/**
 * @description:
 * @author：Jing
 * @date: 2020/4/6 20:47
 */
class TagRepository : BaseRepository() {

    fun loadTags(type: Int): List<Tag> {
        return getDatabase().getTagDao().getTagsByType(type)
    }

    fun loadUnClassifiedTags(type: Int): List<Tag> {
        return getDatabase().getTagDao().getUnClassifiedTags(type)
    }

    fun loadTagClassItems(classId: Long): List<Tag> {
        return getDatabase().getTagDao().getTagClassItems(classId)
    }

    fun sortTags(sortType: Int, list: List<Tag>): Observable<List<Tag>> {
        return Observable.create {
            var result = when(sortType) {
                AppConstants.TAG_SORT_NAME -> list.sortedBy { tag -> tag.nameForSort.toLowerCase() }
                AppConstants.TAG_SORT_RANDOM -> list.shuffled()
                else -> list
            }
            it.onNext(result)
            it.onComplete()
        }
    }

    fun deleteTagAndRelations(data: Tag) {
        getDatabase().runInTransaction {
            when(data.type) {
                DataConstants.TAG_TYPE_RECORD -> getDatabase().getTagDao().deleteTagRecordsByTag(data.id!!)
                DataConstants.TAG_TYPE_STAR -> getDatabase().getTagDao().deleteTagStarsByTag(data.id!!)
            }
            // delete tag
            deleteTag(data)
        }
    }

    fun deleteTag(data: Tag) {
        getDatabase().getTagDao().deleteTagById(data.id!!)
        // 从分类中删除
        getDatabase().getTagDao().deleteTagClassItemByTag(data.id!!)
    }

    fun addTag(name: String, tagType: Int): Boolean {
        val count = getDatabase().getTagDao().getTagCountBy(name, tagType)
        if (count == 0) {
            val tag = Tag(null, name, tagType, PinyinUtil.toPinyinConcat(name))
            var list = mutableListOf<Tag>()
            list.add(tag)
            getDatabase().getTagDao().insertTags(list)
            return true
        }
        return false
    }

    fun editTag(tag: Tag, newName: String): Boolean {
        val count = getDatabase().getTagDao().getTagCountBy(newName, tag.type)
        if (count == 0) {
            tag.name = newName
            tag.nameForSort = PinyinUtil.toPinyinConcat(newName)
            getDatabase().getTagDao().updateTag(tag)
            return true
        }
        return false
    }

    fun getTagItemCount(type: Int, id: Long?): Int {
        id?.let {
            return when(type) {
                DataConstants.TAG_TYPE_STAR -> getDatabase().getTagDao().countStarTagItems(it)
                DataConstants.TAG_TYPE_RECORD -> getDatabase().getTagDao().countRecordTagItems(it)
                else -> 0
            }
        }
        return 0
    }
}