package com.king.app.coolg_kt.page.star.phone

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.coolg_kt.base.BaseViewModel
import com.king.app.coolg_kt.model.image.ImageProvider
import com.king.app.coolg_kt.view.widget.relation.RelationItem

class StarRelationViewModel(application: Application): BaseViewModel(application) {

    var relations = MutableLiveData<List<RelationItem>>()

    val map = mutableMapOf<String, Boolean>()

    var starList = mutableListOf<Long>()

    fun clear() {
        starList.clear()
    }

    fun loadRelations(idList: List<CharSequence>) {
        launchSingle(
            {
                map.clear()
                starList.addAll(idList.map { it.toString().toLong() })
                val list = mutableListOf<RelationItem>()
                starList.forEach { starId ->
                    getDatabase().getStarDao().getStar(starId)?.apply {
                        val allRelations = getDatabase().getStarDao().getStarRelationships(starId)
                            .filter { star -> starList.contains(star.star.id) }
                            .map { star -> star.star.id!! }
                        list.add(RelationItem(id!!, allRelations, listOf(), 0, ImageProvider.getStarRandomPath(name, null)))
                    }
                }
                // 先按关系数量排序，确定line优先级
                list.sortByDescending { it.allRelations.size }
                list.forEachIndexed { index, relationItem -> relationItem.order = index }
                // 然后确定lineRelations，关系数量最多的优先确定
                list.forEach { item ->
                    val lines = mutableListOf<Int>()
                    item.allRelations.forEach { target ->
                        if (addRelation(item.starId, target)) {
                            lines.add(findStarPosition(list, target))
                        }
                    }
                    item.lineRelations = lines
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

    private fun addRelation(starId1: Long, starId2: Long): Boolean {
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
}