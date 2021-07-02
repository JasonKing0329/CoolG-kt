package com.king.app.coolg_kt.model.db

import com.king.app.coolg_kt.CoolApplication
import com.king.app.coolg_kt.conf.AppConfig
import io.reactivex.rxjava3.core.Observable
import java.io.File

/**
 * Desc: 提前将本地数据库的扩展内容保存到内存中，server端更新的数据库直接替换本地数据库，再插入内存中保存的扩展内容
 * @author：Jing Yang
 * @date: 2021/7/2 11:29
 */
@Deprecated("v8.8.0之前的老方法，随着database越来越大，此方法占内存较大，且更新较慢，替换为ServerToLocalEngine")
class LocalToServerEngine: DbUpgradeEngine() {

    private var mLocalData: LocalData? = null

    fun saveLocalData(): Observable<LocalData> {
        return Observable.create{

            // 将数据库备份至History文件夹
            backupDatabase()

            // 额外的数据表
            var data = LocalData(
                getDatabase().getFavorDao().getAllFavorRecords(),
                getDatabase().getFavorDao().getAllFavorStars(),
                getDatabase().getFavorDao().getAllFavorRecordOrders(),
                getDatabase().getFavorDao().getAllFavorStarOrders(),
                getDatabase().getStarDao().getAllStarRatings(),
                getDatabase().getPlayOrderDao().getAllPlayOrders(),
                getDatabase().getPlayOrderDao().getAllPlayItems(),
                getDatabase().getPlayOrderDao().getAllPlayDurations(),
                getDatabase().getPlayOrderDao().getVideoCoverOrders(),
                getDatabase().getPlayOrderDao().getVideoCoverStars(),
                getDatabase().getStarDao().getAllTopStarCategory(),
                getDatabase().getStarDao().getAllTopStar(),
                getDatabase().getTagDao().getAllTags(),
                getDatabase().getTagDao().getAllTagRecords(),
                getDatabase().getTagDao().getAllTagStars(),
                getDatabase().getMatchDao().getAllMatches(),
                getDatabase().getMatchDao().getAllMatchPeriods(),
                getDatabase().getMatchDao().getAllMatchItems(),
                getDatabase().getMatchDao().getAllMatchRecords(),
                getDatabase().getMatchDao().getAllMatchScoreStars(),
                getDatabase().getMatchDao().getAllMatchScoreRecords(),
                getDatabase().getMatchDao().getAllMatchRankStars(),
                getDatabase().getMatchDao().getAllMatchRankRecords(),
                getDatabase().getMatchDao().getAllMatchRankDetails()
            )
            mLocalData = data
            it.onNext(data)
            it.onComplete()
        }
    }

    /**
     * 如果是更新的upload数据库，直接替换后就完成了；下载的是默认database，保存本地的其他表单
     * @param isUploadedDb
     * @return
     */
    fun updateLocalData(isUploadedDb: Boolean): Observable<Boolean> {
        return Observable.create {
            File(AppConfig.GDB_DB_JOURNAL).delete()
            // 重新加载数据库
            CoolApplication.instance.reCreateDatabase()
            if (!isUploadedDb) {
                updateFavorTables()
                updateStarRelated()
                updatePlayList()
                updateTags()
                updateMatches()
                createCountData()
                // 数据插入完毕后务必先关闭数据库
                // 根据调试发现，数据库onOpen后，部分数据可能会写入到gdata.db-wal这个文件中，如果没有执行close，-wal文件会一直存在
                // 而主数据库文件gdata.db中就会缺少部分数据
                closeDatabase()
            }
            it.onNext(true)
            it.onComplete()
        }
    }

    private fun updateFavorTables() {
        getDatabase().getFavorDao().deleteFavorRecordOrders()
        getDatabase().getFavorDao().deleteFavorRecords()
        getDatabase().getFavorDao().deleteFavorStarOrders()
        getDatabase().getFavorDao().deleteFavorStars()
        getDatabase().getFavorDao().insertFavorRecordOrders(mLocalData!!.favorRecordOrderList)
        getDatabase().getFavorDao().insertFavorRecords(mLocalData!!.favorRecordList)
        getDatabase().getFavorDao().insertFavorStarOrders(mLocalData!!.favorStarOrderList)
        getDatabase().getFavorDao().insertFavorStars(mLocalData!!.favorStarList)
    }

    private fun updateStarRelated() {
        getDatabase().getStarDao().deleteStarRatings()
        getDatabase().getStarDao().deleteTopStarCategories()
        getDatabase().getStarDao().deleteTopStars()
        getDatabase().getStarDao().insertStarRatings(mLocalData!!.starRatingList)
        getDatabase().getStarDao().insertTopStarCategories(mLocalData!!.categoryList)
        getDatabase().getStarDao().insertTopStars(mLocalData!!.categoryStarList)
    }

    private fun updatePlayList() {
        getDatabase().getPlayOrderDao().deletePlayDurations()
        getDatabase().getPlayOrderDao().deletePlayItems()
        getDatabase().getPlayOrderDao().deletePlayOrders()
        getDatabase().getPlayOrderDao().deleteVideoCoverPlayOrders()
        getDatabase().getPlayOrderDao().deleteVideoCoverStars()
        getDatabase().getPlayOrderDao().insertPlayDurations(mLocalData!!.playDurationList)
        getDatabase().getPlayOrderDao().insertPlayItems(mLocalData!!.playItemList)
        getDatabase().getPlayOrderDao().insertPlayOrders(mLocalData!!.playOrderList)
        getDatabase().getPlayOrderDao().insertVideoCoverPlayOrders(mLocalData!!.videoCoverPlayOrders)
        getDatabase().getPlayOrderDao().insertVideoCoverStars(mLocalData!!.videoCoverStars)
    }

    private fun updateTags() {
        getDatabase().getTagDao().deleteTags()
        getDatabase().getTagDao().deleteTagStars()
        getDatabase().getTagDao().deleteTagRecords()
        getDatabase().getTagDao().insertTags(mLocalData!!.tagList)
        getDatabase().getTagDao().insertTagStars(mLocalData!!.tagStarList)
        getDatabase().getTagDao().insertTagRecords(mLocalData!!.tagRecordList)
    }

    private fun updateMatches() {
        getDatabase().getMatchDao().deleteMatches()
        getDatabase().getMatchDao().deleteMatchItems()
        getDatabase().getMatchDao().deleteMatchPeriods()
        getDatabase().getMatchDao().deleteMatchRecords()
        getDatabase().getMatchDao().deleteMatchRankRecords()
        getDatabase().getMatchDao().deleteMatchRankStars()
        getDatabase().getMatchDao().deleteMatchScoreRecords()
        getDatabase().getMatchDao().deleteMatchScoreStars()
        getDatabase().getMatchDao().insertMatches(mLocalData!!.matchList)
        getDatabase().getMatchDao().insertMatchPeriods(mLocalData!!.matchPeriodList)
        getDatabase().getMatchDao().insertMatchItems(mLocalData!!.matchItemList)
        getDatabase().getMatchDao().insertMatchRecords(mLocalData!!.matchRecordList)
        getDatabase().getMatchDao().insertMatchScoreStars(mLocalData!!.matchScoreStarList)
        getDatabase().getMatchDao().insertMatchScoreRecords(mLocalData!!.matchScoreRecordList)
        getDatabase().getMatchDao().insertMatchRankStars(mLocalData!!.matchRankStarList)
        getDatabase().getMatchDao().insertMatchRankRecords(mLocalData!!.matchRankRecordList)
        getDatabase().getMatchDao().insertOrReplaceMatchRankDetails(mLocalData!!.matchRankDetailList)
    }

}