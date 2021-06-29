package com.king.app.gdb.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.king.app.gdb.data.dao.*
import com.king.app.gdb.data.entity.*
import com.king.app.gdb.data.entity.match.*

/**
 * @description:
 * @author：Jing
 * @date: 2020/12/9 22:26
 */
@Database(
    entities = [CountStar::class, StarRating::class, Star::class
        , Record::class, RecordType1v1::class, RecordType3w::class, CountRecord::class, RecordStar::class
        , FavorRecordOrder::class, FavorRecord::class, FavorStarOrder::class, FavorStar::class
        , GProperties::class, TopStar::class, TopStarCategory::class
        , PlayDuration::class, PlayItem::class, PlayOrder::class, VideoCoverPlayOrder::class, VideoCoverStar::class
        , Tag::class, TagRecord::class, TagStar::class
        , Match::class, MatchPeriod::class, MatchItem::class, MatchRecord::class
        , MatchRankRecord::class, MatchRankStar::class, MatchScoreRecord::class, MatchScoreStar::class, MatchRankDetail::class],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    companion object{
        var instance: AppDatabase? = null
        fun getInstance(context: Context, dbPath: String): AppDatabase {
            if (instance == null) {
                synchronized(AppDatabase::class.java) {
                    if (instance == null) {
                        instance = buildDatabase(context, dbPath)
                    }
                }
            }
            return instance!!
        }

        fun newInstance(context: Context, dbPath: String): AppDatabase {
            instance = buildDatabase(context, dbPath)
            return instance!!
        }

        private fun buildDatabase(appContext: Context, dbPath: String): AppDatabase {
            Log.e(AppDatabase::class.simpleName, dbPath)
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                dbPath
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        Log.e(AppDatabase::class.simpleName, "onCreate")
                        //生成数据库时使用,可以初始化一些信息
                        super.onCreate(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        Log.e(AppDatabase::class.simpleName, "onOpen")
                        super.onOpen(db)
                    }
                })
                .allowMainThreadQueries() //允许主线程查询
                .addMigrations(DataMigration.MIGRATION_1_11)
                .addMigrations(DataMigration.MIGRATION_11_12)
                .addMigrations(DataMigration.MIGRATION_12_13)
                .addMigrations(DataMigration.MIGRATION_13_14)
                .addMigrations(DataMigration.MIGRATION_14_15)
                .addMigrations(DataMigration.MIGRATION_15_16)
                .build()
        }
    }

    abstract fun getRecordDao(): RecordDao

    abstract fun getStarDao(): StarDao

    abstract fun getPlayOrderDao(): PlayOrderDao

    abstract fun getPropertyDao(): PropertyDao

    abstract fun getTagDao(): TagDao

    abstract fun getFavorDao(): FavorDao

    abstract fun getMatchDao(): MatchDao

    fun destroy() {
        Log.e(AppDatabase::class.simpleName, "destroy")
        instance?.close()
        instance = null
    }

}