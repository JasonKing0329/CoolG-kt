package com.king.app.gdb.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2018/3/13 16:59
 */
@Entity(tableName = "favor_order_star")
data class FavorStarOrder (
   @PrimaryKey(autoGenerate = true)
   @ColumnInfo(name = "_id")
   var id: Long?,
   @ColumnInfo(name = "NAME")
   var name: String?,

   @ColumnInfo(name = "COVER_URL")
   var coverUrl: String?,
   @ColumnInfo(name = "NUMBER")
   var number:Int = 0,

   @ColumnInfo(name = "SORT_SEQ")
   var sortSeq:Int = 0,

   @ColumnInfo(name = "CREATE_TIME")
   var createTime: Long?,

   @ColumnInfo(name = "UPDATE_TIME")
   var updateTime: Long?,

   @ColumnInfo(name = "PARENT_ID")
   var parentId: Long = 0
)