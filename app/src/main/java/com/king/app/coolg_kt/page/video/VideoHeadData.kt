package com.king.app.coolg_kt.page.video

import android.view.View
import com.king.app.coolg_kt.model.bean.VideoGuy
import com.king.app.coolg_kt.model.bean.VideoPlayList

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/22 16:37
 */
class VideoHeadData {
    private var guyList: List<VideoGuy>? = null
    private var playLists: List<VideoPlayList>? = null
    var padPlayListCover: String? = null
    var padGuyCover: String? = null
    fun setGuyList(guyList: List<VideoGuy>) {
        this.guyList = guyList
    }

    fun setPlayLists(playLists: List<VideoPlayList>) {
        this.playLists = playLists
    }

    fun getPlayListVisibility(position: Int): Int {
        return if (playLists != null && position < playLists!!.size) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun getPlayListUrl(position: Int): String? {
        return if (playLists != null && position < playLists!!.size) {
            playLists!![position].imageUrl
        } else {
            null
        }
    }

    fun getPlayListName(position: Int): String? {
        return if (playLists != null && position < playLists!!.size) {
            playLists!![position].name
        } else {
            null
        }
    }

    fun getPlayList(position: Int): VideoPlayList? {
        return if (playLists != null && position < playLists!!.size) {
            playLists!![position]
        } else {
            null
        }
    }

    fun getGuyVisibility(position: Int): Int {
        return if (guyList != null && position < guyList!!.size) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun getGuyUrl(position: Int): String? {
        return if (guyList != null && position < guyList!!.size) {
            guyList!![position].imageUrl
        } else {
            null
        }
    }

    fun getGuyName(position: Int): String? {
        return if (guyList != null && position < guyList!!.size) {
            guyList!![position].star!!.name
        } else {
            null
        }
    }

    fun getGuy(position: Int): VideoGuy? {
        return if (guyList != null && position < guyList!!.size) {
            guyList!![position]
        } else {
            null
        }
    }

}