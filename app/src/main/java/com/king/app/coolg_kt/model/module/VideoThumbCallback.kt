package com.king.app.coolg_kt.model.module

import android.graphics.Bitmap

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2017/7/19 16:15
 */
interface VideoThumbCallback {
    fun onThumbnailCreated(list: List<Bitmap?>?)
}