package com.king.app.coolg_kt.model.bean

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