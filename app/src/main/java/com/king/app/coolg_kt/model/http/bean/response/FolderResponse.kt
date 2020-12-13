package com.king.app.coolg_kt.model.http.bean.response

import com.king.app.coolg_kt.model.http.bean.data.FileBean

class FolderResponse {
    /**
     * see HttpConstants.FOLDER_TYPE_XX
     */
    var type: String? = null
    var fileList: List<FileBean>? = null

}