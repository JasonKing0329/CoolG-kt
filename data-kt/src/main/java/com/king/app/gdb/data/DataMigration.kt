package com.king.app.gdb.data

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/13 11:51
 */
object DataMigration {
    val MIGRATION_1_11: Migration = object : Migration(1, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_1_11")
            // version 1_2
            database.execSQL(
                "CREATE TABLE \"favor_order_record\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"NAME\" TEXT,\"COVER_URL\" TEXT,\"NUMBER\" INTEGER NOT NULL ,\"SORT_SEQ\" INTEGER NOT NULL ,\"CREATE_TIME\" INTEGER,\"UPDATE_TIME\" INTEGER,\"PARENT_ID\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"favor_order_star\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"NAME\" TEXT,\"COVER_URL\" TEXT,\"NUMBER\" INTEGER NOT NULL ,\"SORT_SEQ\" INTEGER NOT NULL ,\"PARENT_ID\" INTEGER NOT NULL ,\"CREATE_TIME\" INTEGER,\"UPDATE_TIME\" INTEGER)"
            )
            database.execSQL(
                "CREATE TABLE \"favor_record\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"ORDER_ID\" INTEGER NOT NULL ,\"RECORD_ID\" INTEGER NOT NULL ,\"CREATE_TIME\" INTEGER,\"UPDATE_TIME\" INTEGER)"
            )
            database.execSQL(
                "CREATE TABLE \"favor_star\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"ORDER_ID\" INTEGER NOT NULL ,\"STAR_ID\" INTEGER NOT NULL ,\"CREATE_TIME\" INTEGER,\"UPDATE_TIME\" INTEGER)"
            )
            // version 2_3
            database.execSQL(
                "CREATE TABLE \"star_rating\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"STAR_ID\" INTEGER NOT NULL ,\"FACE\" REAL NOT NULL ,\"BODY\" REAL NOT NULL ,\"SEXUALITY\" REAL NOT NULL ,\"DK\" REAL NOT NULL ,\"PASSION\" REAL NOT NULL ,\"VIDEO\" REAL NOT NULL ,\"COMPLEX\" REAL NOT NULL ,\"PREFER\" REAL NOT NULL )"
            )
            // version 3_4
            // version 4_5
            database.execSQL(
                "CREATE TABLE \"play_duration\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"RECORD_ID\" INTEGER NOT NULL ,\"DURATION\" INTEGER NOT NULL ,\"TOTAL\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"play_item\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"ORDER_ID\" INTEGER NOT NULL ,\"RECORD_ID\" INTEGER NOT NULL ,\"URL\" TEXT,\"INDEX\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"play_order\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"NAME\" TEXT,\"COVER_URL\" TEXT)"
            )
            // version 5_6
            database.execSQL(
                "CREATE TABLE \"star_category\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"NAME\" TEXT,\"INDEX\" INTEGER NOT NULL ,\"TYPE\" INTEGER NOT NULL ,\"NUMBER\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"star_category_details\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"CATEGORY_ID\" INTEGER NOT NULL ,\"STAR_ID\" INTEGER NOT NULL ,\"LEVEL\" INTEGER NOT NULL ,\"LEVEL_INDEX\" INTEGER NOT NULL )"
            )
            // version 6_7
            database.execSQL(
                "CREATE TABLE \"video_cover_order\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"ORDER_ID\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"video_cover_star\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"STAR_ID\" INTEGER NOT NULL )"
            )
            // version 7_8
            // version 8_9
            database.execSQL(
                "CREATE TABLE \"tag\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"NAME\" TEXT,\"TYPE\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"tag_record\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"TAG_ID\" INTEGER NOT NULL ,\"RECORD_ID\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"tag_star\" (\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ,\"TAG_ID\" INTEGER NOT NULL ,\"STAR_ID\" INTEGER NOT NULL )"
            )
            // version 9_10
            // version 10_11
            database.execSQL(
                "CREATE TABLE \"count_record\" (\"_id\" INTEGER PRIMARY KEY ,\"RANK\" INTEGER NOT NULL )"
            )
            database.execSQL(
                "CREATE TABLE \"count_star\" (\"_id\" INTEGER PRIMARY KEY ,\"RANK\" INTEGER NOT NULL )"
            )
        }
    }
    val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_4_5")
            database.execSQL(
                ""
            )
        }
    }

    fun logMessage(msg: String) {
        Log.e(DataMigration::class.simpleName, msg)
    }
}