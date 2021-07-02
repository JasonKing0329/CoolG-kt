package com.king.app.coolg_kt.model.db

import com.king.app.gdb.data.entity.*
import com.king.app.gdb.data.entity.match.*

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2021/7/2 11:31
 */
/**
 * 从服务端更新下来的原始数据库包含的内容（主体数据，以此为主）
 */
data class ServerData (
    var recordList: List<Record>,
    var recordType1v1List: List<RecordType1v1>,
    var recordType3wList: List<RecordType3w>,
    var recordStarList: List<RecordStar>,
    var starList: List<Star>,
    var properties: List<GProperties>
)

/**
 * 本地数据库在原始数据库的基础上扩展的内容
 */
data class LocalData (
    var favorRecordList: List<FavorRecord>,
    var favorStarList: List<FavorStar>,
    var favorRecordOrderList: List<FavorRecordOrder>,
    var favorStarOrderList: List<FavorStarOrder>,
    var starRatingList: List<StarRating>,
    var playOrderList: List<PlayOrder>,
    var playItemList: List<PlayItem>,
    var playDurationList: List<PlayDuration>,
    var videoCoverPlayOrders: List<VideoCoverPlayOrder>,
    var videoCoverStars: List<VideoCoverStar>,
    var categoryList: List<TopStarCategory>,
    var categoryStarList: List<TopStar>,
    var tagList: List<Tag>,
    var tagRecordList: List<TagRecord>,
    var tagStarList: List<TagStar>,
    var matchList: List<Match>,
    var matchPeriodList: List<MatchPeriod>,
    var matchItemList: List<MatchItem>,
    var matchRecordList: List<MatchRecord>,
    var matchScoreStarList: List<MatchScoreStar>,
    var matchScoreRecordList: List<MatchScoreRecord>,
    var matchRankStarList: List<MatchRankStar>,
    var matchRankRecordList: List<MatchRankRecord>,
    var matchRankDetailList: List<MatchRankDetail>
)