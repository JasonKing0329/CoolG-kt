package com.king.app.coolg_kt.page.star.phone

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.bean.HistoryRelationItem
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.coolg_kt.view.widget.relation.RelationItem

class StarRelationViewModel(application: Application): BaseViewModel(application) {

    var relations = MutableLiveData<List<RelationItem>>()

    val map = mutableMapOf<String, Boolean>()

    var starList = mutableListOf<Long>()

    var mCurrentHistory: HistoryRelationItem? = null

    fun clear() {
        starList.clear()
        loadRelations(listOf())
    }

    fun deleteStar(starId: Long) {
        starList.remove(starId)
        val target = starList.map { it.toString() as CharSequence }
        starList.clear()
        loadRelations(target)
    }

    fun loadRelations(idList: List<CharSequence>) {
        loadRelationsByIds(idList.map { it.toString().toLong() })
    }

    fun loadRelationsByIds(idList: List<Long>) {
        launchSingle(
            {
                map.clear()
                starList.addAll(idList)
                val list = mutableListOf<RelationItem>()
                starList.forEach { starId ->
                    getDatabase().getStarDao().getStar(starId)?.apply {
                        val allRelations = getDatabase().getStarDao().getStarRelationships(starId)
                            .filter { star -> starList.contains(star.star.id) }
                            .map { star -> star.star.id!! }
                        list.add(RelationItem(id!!, name, allRelations, listOf(), listOf(), 0, ImageProvider.getStarRandomPath(name, null)))
                    }
                }
                // 先按关系数量排序，确定line优先级
                list.sortByDescending { it.allRelations.size }
                list.forEachIndexed { index, relationItem -> relationItem.order = index }
                // 然后确定lineRelations，关系数量最多的优先确定
                list.forEach { item ->
                    val lines = mutableListOf<Int>()
                    val linesWhenFocus = mutableListOf<Int>()
                    item.allRelations.forEach { target ->
                        val targetPosition = findStarPosition(list, target)
                        linesWhenFocus.add(targetPosition)
                        if (addUniqueRelation(item.starId, target)) {
                            lines.add(targetPosition)
                        }
                    }
                    item.lineRelations = lines
                    item.lineRelationsWhenFocus = linesWhenFocus
                }
                list
            },
            withLoading = true
        ) {
            relations.value = it
        }
    }

    private fun findStarPosition(list: MutableList<RelationItem>, starId: Long): Int {
        return list.indexOfFirst { it.starId == starId }
    }

    private fun addUniqueRelation(starId1: Long, starId2: Long): Boolean {
        val key = if (starId2 < starId1) {
            "${starId2}_${starId1}"
        }
        else {
            "${starId1}_${starId2}"
        }
        return if (map[key] == null) {
            map[key] = true
            true
        }
        else {
            false
        }
    }

    fun needInputName(): Boolean {
        return mCurrentHistory == null
    }

    fun saveHistory() {
        saveAsHistory(mCurrentHistory?.name)
    }

    fun saveAsHistory(name: String?) {
        val ids = relations.value?.map { it.starId }?: listOf()
        val bean = SettingProperty.getHistoryRelations()
        // new history
        if (mCurrentHistory == null) {
            val list = bean.list.toMutableList()
            list.add(
                HistoryRelationItem(name?:"Unknown", System.currentTimeMillis(),  ids)
            )
            bean.list = list
            SettingProperty.setHistoryRelations(bean)
        }
        // update history
        else {
            bean.list.firstOrNull { it.name == name }?.apply {
                list = ids
            }
            SettingProperty.setHistoryRelations(bean)
        }
    }

    fun isValidName(name: String?): Boolean {
        return SettingProperty.getHistoryRelations().list.firstOrNull { it.name == name} == null
    }
}