package com.king.app.coolg_kt.page.video.player

import com.king.app.coolg_kt.model.bean.PlayItemViewBean
import com.king.app.coolg_kt.model.bean.PlayList
import com.king.app.coolg_kt.model.setting.SettingProperty
import com.king.app.gdb.data.entity.Record

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2020/9/27 10:49
 */
class PlayListInstance private constructor() {

    companion object {
        private var instance: PlayListInstance? = null
        fun getInstance(): PlayListInstance {
            synchronized(PlayListInstance::class.java) {
                if (instance == null) {
                    instance =
                        PlayListInstance()
                }
            }
            return instance!!
        }
    }
    
    fun destroy() {
        instance = null
    }

    val playList: PlayList
        get() = SettingProperty.getPlayList()

    fun updatePlayMode(mode: Int) {
        val playList = playList
        playList.playMode = mode
        saveList(playList)
    }

    fun updatePlayIndex(index: Int) {
        val playList = playList
        playList.playIndex = index
        saveList(playList)
    }

    fun setPlayIndexAsLast() {
        val playList = playList
        playList.playIndex = if (playList.list.isEmpty()) 0
        else playList.list.size - 1
        saveList(playList)
    }

    private fun findExistedItem(playList: PlayList, url: String?, recordId: Long): Int {
        var existIndex = -1
        for (i in playList.list.indices) {
            val item = playList.list[i]
            // 先判断recordId
            if (recordId > 0 && item.recordId == recordId) {
                existIndex = i
                break
            }
            // 再判断url
            if (item.url != null && item.url == url) {
                existIndex = i
                break
            }
        }
        return existIndex
    }

    fun addUrl(url: String?) {
        val playList = playList
        val existIndex = findExistedItem(playList, url, 0)
        val item = PlayList.PlayItem()
        item.url = url
        // 已有则删除并重新加在末尾，但保留播放时长
        if (existIndex != -1) {
            item.playTime = playList.list[existIndex].playTime
            playList.list.removeAt(existIndex)
        }
        item.index = playList.list.size
        playList.list.add(item)
        saveList(playList)
    }

    fun addPlayItemViewBean(bean: PlayItemViewBean) {
        bean.record?.let {
            val playList = playList
            val existIndex = findExistedItem(playList, bean.playUrl, it.bean.id!!)
            val item = PlayList.PlayItem()
            item.url = bean.playUrl
            item.recordId = it.bean.id!!
            item.name = it.bean.name
            //        item.setDuration();
            // 已有则删除并重新加在末尾，但保留播放时长
            if (existIndex != -1) {
                item.playTime = playList.list[existIndex].playTime
                playList.list.removeAt(existIndex)
            }
            item.index = playList.list.size
            playList.list.add(item)
            saveList(playList)
        }
    }

    fun addRecord(record: Record, url: String?): PlayList.PlayItem {
        val playList = playList
        val existIndex = findExistedItem(playList, url, record.id!!)
        val item = PlayList.PlayItem()
        item.url = url
        item.recordId = record.id!!
        item.name = record.name
        // 已有则删除并重新加在末尾，但保留播放时长
        if (existIndex != -1) {
            item.playTime = playList.list[existIndex].playTime
            playList.list.removeAt(existIndex)
        }
        item.index = playList.list.size
        playList.list.add(item)
        saveList(playList)
        return item
    }

    fun addPlayItem(item: PlayList.PlayItem) {
        val playList = playList
        val existIndex = findExistedItem(playList, item.url, item.recordId)
        // 已有则删除并重新加在末尾，但保留播放时长
        if (existIndex != -1) {
            item.playTime = playList.list[existIndex].playTime
            playList.list.removeAt(existIndex)
        }
        playList.list.add(item)
        saveList(playList)
    }

    fun addPlayItems(list: List<PlayItemViewBean>) {
        for (bean in list) {
            addPlayItemViewBean(bean)
        }
    }

    fun deleteItem(item: PlayList.PlayItem) {
        val playList = playList
        for (i in playList.list.indices) {
            val playItem = playList.list[i]
            if (item.recordId == playItem.recordId) {
                playList.list.removeAt(i)
                break
            }
            if (playItem.url != null && playItem.url == item.url) {
                playList.list.removeAt(i)
                break
            }
        }
        saveList(playList)
    }

    private fun saveList(playList: PlayList) {
        SettingProperty.setPlayList(playList)
    }

    fun clearPlayList() {
        val playList = playList
        playList.list.clear()
        playList.playIndex = 0
        saveList(playList)
    }

    fun updatePlayItem(item: PlayList.PlayItem) {
        val playList = playList
        val index = findExistedItem(playList, item.url, item.recordId)
        try {
            playList.list[index] = item
            saveList(playList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}