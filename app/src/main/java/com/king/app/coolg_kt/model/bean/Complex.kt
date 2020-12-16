package com.king.app.coolg_kt.model.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.king.app.coolg_kt.BR
import com.king.app.coolg_kt.model.http.bean.data.DownloadItem
import com.king.app.coolg_kt.page.record.popup.RecommendBean
import com.king.app.gdb.data.RecordCursor

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/12 12:16
 */
data class HsvColorBean (
    var hStart:Int = -1,
    var hArg:Int = -1,
    var s:Float = -1f,
    var v:Float = -1f,

    /**
     * 0: 随机
     * 1: 配合白色文字的背景颜色
     * 2: 配合深色文字的背景颜色
     */
    var type:Int = 0
)
class DownloadDialogBean {
    /**
     * 本地不存在的待下载内容
     */
    var downloadList: MutableList<DownloadItem>? = null

    /**
     * 本地已存在的待下载任务
     */
    var existedList: List<DownloadItem>? = null

    /**
     * 下载目录
     */
    var savePath: String = ""

    /**
     * 直接下载，不提示
     */
    var isShowPreview = false

}
data class DownloadItemProxy (
    var item: DownloadItem,
    var progress:Int = 0
)

class CheckDownloadBean {
    var hasNew = false
    var downloadList = mutableListOf<DownloadItem>()
    var repeatList = mutableListOf<DownloadItem>()
    var targetPath: String = ""
}
class RecordComplexFilter {
    var sortType = 0
    // 固定recordType
    var recordType: Int? = null
    var desc = false
    var nameLike: String? = null
    var scene: String? = null
    var tagId: Long = 0
    var cursor: RecordCursor? = null
    var filter: RecommendBean? = null
    var starId: Long = 0
    var studioId: Long = 0
}

class TitleValueBean {
    var title: String? = null
    var value: String? = null

    constructor() {}
    constructor(title: String?, value: String?) {
        this.title = title
        this.value = value
    }

}
class PassionPoint {
    var key: String? = null
    var content: String? = null
}

class ImageBean : BaseObservable() {
    var url: String? = null
    var width = 0
    var height = 0

    @get:Bindable
    var isSelected = false
        set(selected) {
            field = selected
            notifyPropertyChanged(BR.selected)
        }

}