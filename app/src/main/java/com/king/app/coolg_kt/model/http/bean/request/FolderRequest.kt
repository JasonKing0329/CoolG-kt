package com.king.app.coolg_kt.model.http.bean.request

class FolderRequest {
    var folder: String? = null

    /**
     * see HttpConstants.FOLDER_TYPE_XX
     */
    var type: String? = null

    var isCountSize = true

    var isGuest = true

    /**
     * see HttpConstants.FILE_FILTER_XX
     */
    var filterType = 0

}