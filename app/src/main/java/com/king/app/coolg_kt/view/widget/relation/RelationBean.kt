package com.king.app.coolg_kt.view.widget.relation

data class RelationItem(
    var starId: Long,
    var starName: String?,
    var allRelations: List<Long>,
    var lineRelations: List<Int>,// 按数量排序后，唯一的对应关系（先对应的视为已有对应关系，不再连线）
    var lineRelationsWhenFocus: List<Int>,// focus在当前starId下所有的连线，实际就是allRelations对应的位置关系
    var order: Int = 0,
    var imageUrl: String? = null
)

data class StarIdList(
    var list: List<Long>
)