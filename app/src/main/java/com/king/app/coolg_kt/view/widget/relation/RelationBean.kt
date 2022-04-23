package com.king.app.coolg_kt.view.widget.relation

data class RelationItem(
    var starId: Long,
    var allRelations: List<Long>,
    var lineRelations: List<Int>,
    var order: Int = 0,
    var imageUrl: String? = null
)

data class StarIdList(
    var list: List<Long>
)