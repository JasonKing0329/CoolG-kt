package com.king.app.coolg_kt.model.bean

import com.king.app.gdb.data.entity.PlayItem
import com.king.app.gdb.data.relation.RecordWrap

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/12/16 13:18
 */
class PlayList {
    var list: MutableList<PlayItem> = mutableListOf()
    var playIndex = 0
    /**
     * 0 顺序；1 随机
     */
    var playMode = 0

    class PlayItem {
        var url: String? = null
        var name: String? = null
        var playTime = 0
        var index = 0
        var recordId: Long = 0
        var duration = 0

        // 扩展字段，不进行持久化存储
        @Transient
        var imageUrl: String? = null

    }
}

data class PlayItemViewBean (
    var record: RecordWrap,
    var playItem: PlayItem? = null,
    var cover: String? = null,
    var playUrl: String? = null,
    var name: String? = null
)