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
            logMessage("MIGRATION_11_12")
            database.execSQL(
                "CREATE TABLE `match` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `level` INTEGER NOT NULL, `draws` INTEGER NOT NULL, `byeDraws` INTEGER NOT NULL, `qualifyDraws` INTEGER NOT NULL, `wildcardDraws` INTEGER NOT NULL, `orderInPeriod` INTEGER NOT NULL, `name` TEXT NOT NULL, `imgUrl` TEXT NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE `match_period` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `matchId` INTEGER NOT NULL, `date` INTEGER NOT NULL, `period` INTEGER NOT NULL, `orderInPeriod` INTEGER NOT NULL, `isRankCreated` INTEGER NOT NULL, `isScoreCreated` INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE `match_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `matchId` INTEGER NOT NULL, `round` INTEGER NOT NULL, `winnerId` INTEGER, `isQualify` INTEGER NOT NULL, `isBye` INTEGER NOT NULL, `order` INTEGER NOT NULL, `groupFlag` INTEGER)"
            )
            database.execSQL(
                "CREATE TABLE `match_rank_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `matchId` INTEGER NOT NULL, `recordId` INTEGER NOT NULL, `rank` INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE `match_rank_star` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `matchId` INTEGER NOT NULL, `starId` INTEGER NOT NULL, `rank` INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE `match_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` INTEGER NOT NULL, `matchId` INTEGER NOT NULL, `matchItemId` INTEGER NOT NULL, `recordId` INTEGER NOT NULL, `recordRank` INTEGER, `recordSeed` INTEGER, `order` INTEGER)"
            )
            database.execSQL(
                "CREATE TABLE `match_score_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `matchId` INTEGER NOT NULL, `matchItemId` INTEGER NOT NULL, `recordId` INTEGER NOT NULL, `score` INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE `match_score_star` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `matchId` INTEGER NOT NULL, `matchItemId` INTEGER NOT NULL, `recordId` INTEGER NOT NULL, `starId` INTEGER NOT NULL, `score` INTEGER NOT NULL)"
            )
        }
    }

    val MIGRATION_12_13: Migration = object : Migration(12, 13) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_12_13")
            database.execSQL(
                "ALTER TABLE 'match_rank_star' ADD COLUMN `score` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE 'match_rank_star' ADD COLUMN `matchCount` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE 'match_rank_record' ADD COLUMN `score` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE 'match_rank_record' ADD COLUMN `matchCount` INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_13_14: Migration = object : Migration(13, 14) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_13_14")
            database.execSQL(
                "DROP TABLE `match_rank_record`"
            )
            database.execSQL(
                "DROP TABLE `match_rank_star`"
            )
            database.execSQL(
                "CREATE TABLE `match_rank_record` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `period` INTEGER NOT NULL, `orderInPeriod` INTEGER NOT NULL, `recordId` INTEGER NOT NULL, `rank` INTEGER NOT NULL, `score` INTEGER NOT NULL, `matchCount` INTEGER NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE `match_rank_star` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `period` INTEGER NOT NULL, `orderInPeriod` INTEGER NOT NULL, `starId` INTEGER NOT NULL, `rank` INTEGER NOT NULL, `score` INTEGER NOT NULL, `matchCount` INTEGER NOT NULL)"
            )
        }
    }

    val MIGRATION_14_15: Migration = object : Migration(14, 15) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_14_15")
            database.execSQL(
                "ALTER TABLE 'match_period' ADD COLUMN `mainWildcard` INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE 'match_period' ADD COLUMN `qualifyWildcard` INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_15_16: Migration = object : Migration(15, 16) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_15_16")
            database.execSQL(
                "CREATE TABLE `match_rank_detail` (`recordId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `studioId` INTEGER NOT NULL, 'studioName' TEXT, `gsCount` INTEGER NOT NULL, `gm1000Count` INTEGER NOT NULL, `gm500Count` INTEGER NOT NULL, `gm250Count` INTEGER NOT NULL, `lowCount` INTEGER NOT NULL)"
            )
        }
    }

    val MIGRATION_16_17: Migration = object : Migration(16, 17) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_16_17")
            database.execSQL(
                "CREATE TABLE `tag_class` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'type' INTEGER NOT NULL, `name` TEXT NOT NULL, 'nameForSort' TEXT NOT NULL)"
            )
            database.execSQL(
                "CREATE TABLE `tag_class_item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `classId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL)"
            )
            database.execSQL(
                "ALTER TABLE 'tag' ADD COLUMN `nameForSort` TEXT NOT NULL DEFAULT \"\""
            )
        }
    }

    val MIGRATION_17_18: Migration = object : Migration(17, 18) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_17_18")
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `score_plan` (`matchId` INTEGER NOT NULL, `period` INTEGER NOT NULL, `plan` TEXT NOT NULL, PRIMARY KEY(`matchId`, `period`))"
            )
        }
    }

    val MIGRATION_18_19: Migration = object : Migration(18, 19) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_18_19")
            database.execSQL("CREATE TABLE IF NOT EXISTS `match_black_list` (`recordId` INTEGER NOT NULL, PRIMARY KEY(`recordId`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS `temp_high_rank` (`recordId` INTEGER NOT NULL, `high` INTEGER NOT NULL, PRIMARY KEY(`recordId`))")
        }
    }

    val MIGRATION_19_20: Migration = object : Migration(19, 20) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_19_20")
            database.execSQL("ALTER TABLE 'record' ADD COLUMN `studioId` INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_20_21: Migration = object : Migration(20, 21) {
        override fun migrate(database: SupportSQLiteDatabase) {
            logMessage("MIGRATION_20_21")
            database.execSQL("ALTER TABLE 'match_rank_detail' ADD COLUMN `microCount` INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun logMessage(msg: String) {
        Log.e(DataMigration::class.simpleName, msg)
    }
}