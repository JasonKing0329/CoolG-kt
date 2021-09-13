package com.king.app.gdb.data.dao

import androidx.room.*
import com.king.app.gdb.data.bean.*
import com.king.app.gdb.data.entity.match.*
import com.king.app.gdb.data.relation.*

/**
 * @description:
 * @author：Jing
 * @date: 2021/01/09 12:05
 */
@Dao
interface MatchDao {

    @Query("select * from `match` where id=:matchId")
    fun getMatch(matchId: Long): Match

    @Query("select * from `match` where name=:name")
    fun getMatchByName(name: String): Match?

    @Query("select m.* from `match` m join match_period mp on m.id=mp.matchId where mp.id=:matchPeriodId")
    fun getMatchByPeriodId(matchPeriodId: Long): Match

    @Query("select * from `match`")
    fun getAllMatches(): List<Match>

    @Query("select * from `match` order by orderInPeriod")
    fun getAllMatchesByOrder(): List<Match>

    @Query("select * from `match` where level=:level order by orderInPeriod")
    fun getMatchByLevel(level: Int): List<Match>

    @Query("select * from `match_period` where matchId=:matchId and period=:period")
    fun getMatchPeriod(matchId: Long, period: Int): MatchPeriod

    @Query("select * from `match_period` where id=:id")
    fun getMatchPeriod(id: Long): MatchPeriodWrap

    @Query("select * from `match_period` where period=:period and orderInPeriod=:orderInPeriod")
    fun getMatchPeriods(period: Int, orderInPeriod: Int): List<MatchPeriod>

    @Query("select * from `match_period` where matchId=:matchId order by period desc")
    fun getMatchPeriods(matchId: Long): List<MatchPeriod>

    @Query("select * from match_period")
    fun getAllMatchPeriods(): List<MatchPeriod>

    @Query("select * from match_period order by period desc, orderInPeriod desc")
    fun getAllMatchPeriodsOrdered(): List<MatchPeriodWrap>

    @Query("select * from match_item")
    fun getAllMatchItems(): List<MatchItem>

    @Query("select * from match_item where matchId=:matchPeriodId and round=:round")
    fun getRoundMatchItems(matchPeriodId: Long, round: Int): List<MatchItem>

    @Query("select * from match_item where matchId=:matchPeriodId")
    fun getMatchItems(matchPeriodId: Long): List<MatchItemWrap>

    @Query("select * from match_item where matchId=:matchPeriodId order by round, `order`")
    fun getMatchItemsSorted(matchPeriodId: Long): List<MatchItemWrap>

    @Query("select mi.* from match_item mi join match_record mr on mi.id=mr.matchItemId where mi.matchId=:matchPeriodId and mr.recordId=:recordId")
    fun getMatchItems(matchPeriodId: Long, recordId: Long): List<MatchItemWrap>

    @Query("select mi.* from match_item mi join match_period mp on mi.matchId=mp.id where mi.round=:round order by mp.period desc, mp.orderInPeriod desc")
    fun getMatchItemsByRound(round: Int): List<MatchItemWrap>

    @Query("select mi.* from match_item mi join match_period mp on mi.matchId=mp.id and mp.id=:matchPeriodId where mi.round=:round")
    fun getMatchItemsByRound(matchPeriodId: Long, round: Int): List<MatchItemWrap>

    @Query("select mi.* from match_item mi join match_period mp on mi.matchId=mp.id join `match` m on mp.matchId=m.id and m.level=:level where mi.round=:round order by mp.period desc, mp.orderInPeriod desc")
    fun getMatchItemsByRoundLevel(round: Int, level: Int): List<MatchItemWrap>

    @Query("select mr.recordId, mi.winnerId, mp.period from match_record mr join match_item mi on mr.matchItemId=mi.id join match_period mp on mi.matchId=mp.id join 'match' m on mp.matchId=m.id and m.id=:matchId where mi.round=:round")
    fun getMatchRoundItems(matchId: Long, round: Int): List<DataForRoundCount>

    @Query("select mi.* from match_item mi join match_record mr on mi.id=mr.matchItemId and mr.recordId=:recordId join match_period mp on mi.matchId=mp.id where mi.round=:round order by mp.period desc, mp.orderInPeriod desc")
    fun getRecordMatchItemsByRound(recordId: Long, round: Int): List<MatchItemWrap>

    @Query("select mi.* from match_item mi join match_record mr on mi.id=mr.matchItemId and mr.recordId=:recordId join match_period mp on mi.matchId=mp.id join 'match' m on mp.matchId=m.id and m.level=:level where mi.round=:round order by mp.period desc, mp.orderInPeriod desc")
    fun getRecordMatchItemsByRoundLevel(recordId: Long, round: Int, level: Int): List<MatchItemWrap>

    @Query("select r.* from match_record rp join match_item r on rp.matchItemId=r.id where rp.recordId=:recordId1 and r.winnerId!=0 and rp.matchItemId in (select matchItemId from match_record where recordId=:recordId2)")
    fun getH2hItems(recordId1: Long, recordId2: Long): List<MatchItemWrap>

    @Query("select mi.*, mp.period, mp.orderInPeriod from match_item mi join match_record mr on mi.id=mr.matchItemId join match_period mp on mi.matchId=mp.id and mp.period*:circleTotal + mp.orderInPeriod>=:rangeStart and mp.period*:circleTotal + mp.orderInPeriod<=:rangeEnd where mr.recordId=:recordId and mi.winnerId>0 and mi.isBye=0")
    fun getRecordMatchItemsRange(recordId: Long, rangeStart: Int, rangeEnd: Int, circleTotal: Int): List<MatchItem>

    @Query("select mi.*, mp.period, mp.orderInPeriod from match_item mi join match_period mp on mi.matchId=mp.id join match_record mr on mi.id=mr.matchItemId where mr.recordId=:recordId and mi.isBye=0 and mi.winnerId=:recordId")
    fun getRecordWinMatchItems(recordId: Long): List<MatchItemPeriodWrap>

    @Query("select mr.*, mi.round, mi.winnerId, mp.period, mp.orderInPeriod from match_record mr join match_item mi on mr.matchItemId=mi.id join match_period mp on mr.matchId=mp.id where mr.recordId!=:recordId and mr.recordId!=0 and mr.matchItemId in (select mr.matchItemId from match_record mr  join match_period mp on mr.matchId=mp.id where mr.recordId=:recordId)")
    fun getAllTimeMatchRecordsCompetitor(recordId: Long): List<MatchRecordDetailWrap>

    /**
     * range范围内的matchPeriodId
     */
    @Query("select mi.matchId from match_item mi join match_record mr on mi.id=mr.matchItemId join match_period mp on mi.matchId=mp.id and mp.period*:circleTotal + mp.orderInPeriod>=:rangeStart and mp.period*:circleTotal + mp.orderInPeriod<=:rangeEnd where mr.recordId=:recordId group by mi.matchId")
    fun getRecordMatchesRange(recordId: Long, rangeStart: Int, rangeEnd: Int, circleTotal: Int): List<Long>

    /**
     * 指定时期内的指定轮次胜利的次数
     */
    @Query("select count(*) from match_item mi join match_period mp on mi.matchId=mp.id and mp.period*:circleTotal + mp.orderInPeriod>=:rangeStart and mp.period*:circleTotal + mp.orderInPeriod<=:rangeEnd where mi.winnerId=:recordId and mi.round=:round")
    fun countRecordWinIn(recordId: Long, round: Int, rangeStart: Int, rangeEnd: Int, circleTotal: Int): Int

    @Query("select msr.* from match_score_record msr join match_period mp on msr.matchId=mp.id and mp.period*:circleTotal + mp.orderInPeriod>=:rangeStart and mp.period*:circleTotal + mp.orderInPeriod<=:rangeEnd where msr.recordId=:recordId order by msr.score desc")
    fun getRecordScoresInPeriodRange(recordId: Long, rangeStart: Int, rangeEnd: Int, circleTotal: Int): List<MatchScoreRecord>

    @Query("select msr.score from match_score_record msr join match_period mp on msr.matchId=mp.id and mp.period=:period and mp.orderInPeriod=:orderInPeriod where msr.recordId=:recordId")
    fun getRecordScoreBy(recordId: Long, period: Int, orderInPeriod: Int): Int

    @Query("select * from match_record")
    fun getAllMatchRecords(): List<MatchRecord>

    @Query("select * from match_record where matchItemId=:matchItemId and recordId=:recordId")
    fun getMatchRecord(matchItemId: Long, recordId:Long): MatchRecordWrap?

    /**
     * 获取recordId对应的competitor的recordId
     */
    @Query("select * from match_record mr join match_item mi on mr.matchItemId=mi.id where mi.id=:matchItemId and mr.recordId!=:recordId")
    fun getMatchRecordCpt(matchItemId: Long, recordId:Long): MatchRecordWrap?

    @Query("select * from match_record where matchItemId=:matchItemId and `order`=:order")
    fun getMatchRecord(matchItemId: Long, order: Int): MatchRecordWrap?

    @Query("select * from match_record where matchId=:matchPeriodId and recordId=:recordId")
    fun getMatchRecords(matchPeriodId: Long, recordId:Long): List<MatchRecord>

    @Query("select count(*) from match_record mr join match_period mp on mr.matchId=mp.id where mp.period=:period and mp.orderInPeriod=:orderInPeriod and recordId=:recordId")
    fun countMatchRecord(period: Int, orderInPeriod: Int, recordId:Long): Int

    @Query("select * from match_rank_record")
    fun getAllMatchRankRecords(): List<MatchRankRecord>

    @Query("select * from match_rank_record where period=:period and orderInPeriod=:orderInPeriod order by score desc, matchCount asc")
    fun getMatchRankRecordsBy(period: Int, orderInPeriod: Int): List<MatchRankRecordWrap>

    @Query("select * from match_rank_detail where recordId=:recordId")
    fun getMatchRankDetail(recordId: Long): MatchRankDetail?

    @Query("select * from match_rank_detail")
    fun getAllMatchRankDetails(): List<MatchRankDetail>

    @Query("select min(rank) from match_rank_record where recordId=:recordId")
    fun getRecordHighestRank(recordId: Long): Int

    @Query("select max(rank) from match_rank_record where recordId=:recordId")
    fun getRecordLowestRank(recordId: Long): Int

    @Query("select * from match_rank_record where recordId=:recordId and rank=:rank order by period, orderInPeriod limit 1")
    fun getRecordRankFirstTime(recordId: Long, rank: Int): MatchRankRecord?

    @Query("select count(*) from match_rank_record where recordId=:recordId and rank=:rank")
    fun getRecordRankWeeks(recordId: Long, rank: Int): Int

    @Query("select * from match_rank_star")
    fun getAllMatchRankStars(): List<MatchRankStar>

    @Query("select * from match_rank_star where period=:period and orderInPeriod=:orderInPeriod order by score desc, matchCount asc")
    fun getMatchRankStarsBy(period: Int, orderInPeriod: Int): List<MatchRankStarWrap>

    @Query("select * from match_score_star")
    fun getAllMatchScoreStars(): List<MatchScoreStar>

    @Query("select mss.* from match_score_star mss join match_period mp on mss.matchId=mp.id where mp.period=:period and mp.orderInPeriod=:orderInPeriod and mss.starId=:starId")
    fun getMatchScoreStarBy(period: Int, orderInPeriod: Int, starId: Long): MatchScoreStar?

    @Query("select * from match_score_record")
    fun getAllMatchScoreRecords(): List<MatchScoreRecord>

    @Query("select max(msr.score) from record_star rs join match_score_record msr on rs.RECORD_ID=msr.recordId join match_period mp on msr.matchId=mp.id and mp.period=:period and mp.orderInPeriod<=:maxOrderInPeriod where rs.STAR_ID=:starId group by msr.recordId order by max(msr.score) desc limit 3")
    fun getStarTop3Records(starId: Long, period: Int, maxOrderInPeriod: Int): List<Int>

    @Query("select * from match_rank_record where period=:period and orderInPeriod=:orderInPeriod order by rank limit 30")
    fun getTop30RankRecord(period: Int, orderInPeriod: Int): List<MatchRankRecord>

    @Insert
    fun insertMatches(list: List<Match>)

    @Insert
    fun insertMatchPeriods(list: List<MatchPeriod>)

    @Insert
    fun insertMatchItems(list: List<MatchItem>): List<Long>

    @Insert
    fun insertMatchItem(bean: MatchItem): Long

    @Insert
    fun insertMatchRecords(list: List<MatchRecord>)

    @Insert
    fun insertMatchRankRecords(list: List<MatchRankRecord>)

    @Insert
    fun insertMatchRankStars(list: List<MatchRankStar>)

    @Insert
    fun insertMatchScoreStars(list: List<MatchScoreStar>)

    @Update
    fun updateMatchScoreStars(list: List<MatchScoreStar>)

    @Insert
    fun insertMatchScoreRecords(list: List<MatchScoreRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceMatchRankDetails(list: List<MatchRankDetail>)

    @Update
    fun updateMatchItems(list: List<MatchItem>)

    @Update
    fun updateMatchRecords(list: List<MatchRecord>)

    @Delete
    fun deleteMatch(match: Match)

    @Query("delete from `match`")
    fun deleteMatches()

    @Delete
    fun deleteMatchPeriod(match: MatchPeriod)

    @Query("delete from match_period")
    fun deleteMatchPeriods()

    @Query("delete from match_item")
    fun deleteMatchItems()

    @Query("delete from match_item where matchId =:matchPeriodId")
    fun deleteMatchItemsByMatchPeriod(matchPeriodId: Long)

    @Query("delete from match_item where matchId =:matchPeriodId and round=:round")
    fun deleteMatchItemsBy(matchPeriodId: Long, round: Int)

    @Query("delete from match_record")
    fun deleteMatchRecords()

    @Query("delete from match_record where matchId =:matchPeriodId")
    fun deleteMatchRecordsByMatchPeriod(matchPeriodId: Long)

    @Query("delete from match_rank_star")
    fun deleteMatchRankStars()

    @Query("delete from match_rank_star where period=:period and orderInPeriod=:orderInPeriod")
    fun deleteMatchRankStars(period: Int, orderInPeriod: Int)

    @Query("delete from match_rank_record")
    fun deleteMatchRankRecords()

    @Query("delete from match_rank_record where period=:period and orderInPeriod=:orderInPeriod")
    fun deleteMatchRankRecords(period: Int, orderInPeriod: Int)

    @Query("delete from match_rank_detail")
    fun clearMatchRankDetail()

    @Query("delete from match_score_star")
    fun deleteMatchScoreStars()

    @Query("delete from match_score_star where matchId=:matchPeriodId")
    fun deleteMatchScoreStarsByMatch(matchPeriodId: Long)

    @Query("delete from match_score_record")
    fun deleteMatchScoreRecords()

    @Query("delete from match_score_record where matchId=:matchPeriodId")
    fun deleteMatchScoreRecordsByMatch(matchPeriodId: Long)

    @Update
    fun updateMatch(match: Match)

    @Update
    fun updateMatchPeriod(match: MatchPeriod)

    @Query("select count(*) from match_item where matchId=:matchPeriodId")
    fun countMatchItemsByMatchPeriod(matchPeriodId: Long): Int

    @Query("select * from match_item where matchId=:matchId and round=:round and `order`=:order")
    fun getMatchItem(matchId: Long, round: Int, order: Int): MatchItemWrap?

    @Query("select * from match_item where id=:id")
    fun getMatchItem(id: Long): MatchItem?

    @Query("select mi.* from match_item mi join match_period mp on mi.matchId=mp.id join `match` m on mp.matchId=m.id and m.id=:matchId join match_record mr on mi.id=mr.matchItemId and mr.recordId=:recordId where mi.isBye=0")
    fun getRecordMatchItems(recordId: Long, matchId: Long): List<MatchItem>

    @Query("select * from match_record where matchId=:matchPeriodId and type=3 and recordId=0")
    fun getUndefinedQualifies(matchPeriodId: Long): List<MatchRecord>

    @Query("select r._id from record r join match_record mr on r._id=mr.recordId join match_period mp on mr.matchId=mp.id where mp.period=:period and mp.orderInPeriod=:matchOrderInPeriod group by r._id")
    fun getSamePeriodRecordIds(period: Int, matchOrderInPeriod: Int): List<Long>

    @Query("select * from match_period order by period desc, orderInPeriod desc limit 1")
    fun getLastMatchPeriod(): MatchPeriod?

    @Query("select * from match_period where isScoreCreated=1 order by period desc, orderInPeriod desc limit 1")
    fun getLastCompletedMatchPeriod(): MatchPeriod?

    @Query("select msr.* from match_score_record msr join match_period mp on msr.matchId=mp.id where msr.recordId=:recordId and mp.period=:period and mp.orderInPeriod>=:start and mp.orderInPeriod<=:end order by score desc limit :limitNum")
    fun getRecordScoreLimit(recordId: Long, limitNum: Int, period: Int, start: Int, end: Int): List<MatchScoreRecord>

    @Query("select msr.* from match_score_record msr join match_period mp on msr.matchId=mp.id where msr.recordId=:recordId and (mp.period=:startPeriod and mp.orderInPeriod>=:startPIO or mp.period=:endPeriod and mp.orderInPeriod<=:endPIO) order by score desc limit :limitNum")
    fun getRecordScoreLimit(recordId: Long, limitNum: Int, startPeriod: Int, startPIO: Int, endPeriod: Int, endPIO: Int): List<MatchScoreRecord>

    @Query("select msr.recordId as id, sum(msr.score) as score, count(msr.recordId) as matchCount from match_score_record msr join match_period mp on msr.matchId=mp.id where mp.period=:period and mp.orderInPeriod>=:start and mp.orderInPeriod<=:end and msr.recordId!=0 group by msr.recordId order by score desc, matchCount asc")
    fun countRecordScoreInPeriod(period: Int, start: Int, end: Int): List<ScoreCount>

    @Query("select msr.recordId as id, sum(msr.score) as score, count(msr.recordId) as matchCount from match_score_record msr join match_period mp on msr.matchId=mp.id where (mp.period=:startPeriod and mp.orderInPeriod>=:startPIO or mp.period=:endPeriod and mp.orderInPeriod<=:endPIO) and msr.recordId!=0 group by msr.recordId order by score desc, matchCount asc")
    fun countRecordScoreInPeriod(startPeriod: Int, startPIO: Int, endPeriod: Int, endPIO: Int): List<ScoreCount>

    @Query("select msr.starId as id, sum(msr.score) as score, count(msr.starId) as matchCount from match_score_star msr join match_period mp on msr.matchId=mp.id where mp.period=:period and mp.orderInPeriod>=:start and mp.orderInPeriod<=:end group by msr.starId order by score desc, matchCount asc")
    fun countStarScoreInPeriod(period: Int, start: Int, end: Int): List<ScoreCount>

    @Query("select msr.starId as id, sum(msr.score) as score, count(msr.starId) as matchCount from match_score_star msr join match_period mp on msr.matchId=mp.id where (mp.period=:startPeriod and mp.orderInPeriod>=:startPIO or mp.period=:endPeriod and mp.orderInPeriod<=:endPIO) group by msr.starId order by score desc, matchCount asc")
    fun countStarScoreInPeriod(startPeriod: Int, startPIO: Int, endPeriod: Int, endPIO: Int): List<ScoreCount>

    @Query("select count(*) from match_rank_record where period=:period and orderInPeriod=:orderInPeriod")
    fun countRecordRankItems(period: Int, orderInPeriod: Int): Int

    @Query("select count(*) from match_rank_star where period=:period and orderInPeriod=:orderInPeriod")
    fun countStarRankItems(period: Int, orderInPeriod: Int): Int

    /**
     * 获取周期内积分，降序排列（不支持跨多周期）
     */
    @Query("select msr.*, m.id as matchRealId from match_score_record msr join match_period mp on msr.matchId=mp.id join 'match' m on mp.matchId=m.id where ((mp.period=:startPeriod and mp.orderInPeriod>=:startPIO) or (mp.period=:endPeriod and mp.orderInPeriod<=:endPIO)) and msr.recordId=:recordId order by msr.score desc")
    fun getRecordScoresInPeriod(recordId: Long, startPeriod: Int, startPIO: Int, endPeriod: Int, endPIO: Int): List<MatchScoreRecordWrap>

    @Query("select * from match_rank_record where recordId=:recordId and period=:period and orderInPeriod=:orderInPeriod")
    fun getRecordRank(recordId: Long, period: Int, orderInPeriod: Int): MatchRankRecord?

    @Query("select * from match_rank_record where recordId=:recordId and rank=:rank order by period, orderInPeriod")
    fun getRecordRanks(recordId: Long, rank: Int): List<MatchRankRecord>

    @Query("select * from match_rank_record where recordId=:recordId order by period, orderInPeriod")
    fun getRecordRanks(recordId: Long): List<MatchRankRecord>

    /**
     * 从match_rank_record里按排名加载所有入围record
     * 不在排名体系里的赋值为9999(MatchConstants.RANK_OUT_OF_SYSTEM)，但不在排名体系的record需要满足其在count_record中的排名在rankLimit之内
     */
    @Query("select r._id as recordId, (case when mrr.rank>0 then mrr.rank else 9999 end) as rank, 0 as seed from record r left join match_rank_record mrr on r._id=mrr.recordId and period=:period and orderInPeriod=:orderInPeriod join count_record cr on r._id=cr._id where cr.RANK<=:rankLimit or mrr.rank>0 order by rank")
    fun getRankRecords(rankLimit: Int, period: Int, orderInPeriod: Int): List<RankRecord>

    /**
     * record在match指定时期的结果轮次
     */
    @Query("select mi.* from match_item mi join match_period mp on mi.matchId=mp.id and mp.matchId=:matchId and mp.period=:period join match_score_record msr on mi.id=msr.matchItemId and msr.recordId=:recordId")
    fun getResultMatchItem(recordId: Long, matchId: Long, period: Int): MatchItem?

    @Query("select recordId from match_rank_record where period=:period and orderInPeriod=:orderInPeriod order by rank limit :top")
    fun getTopRecordRanks(period: Int, orderInPeriod: Int, top: Int): List<Long>

    @Query("select winnerId, count(winnerId) as num from match_item where round=:roundId group by winnerId order by num desc")
    fun countRecordRound(roundId: Int): List<TitlesCount>

    @Query("select mi.winnerId, count(winnerId) as num from match_item mi join match_period mp on mi.matchId=mp.id join 'match' m on mp.matchId=m.id where m.level=:level and mi.round=:roundId group by mi.winnerId order by num desc")
    fun countRecordRoundByLevel(roundId: Int, level: Int): List<TitlesCount>

    @Query("select record.*, count(*) as num from match_record join record on match_record.recordId=record._id where matchItemId in (select matchItemId from match_record where recordId=:recordId) and recordId!=:recordId and recordId != 0 group by recordId")// 为0是轮空的
    fun getRecordCompetitors(recordId: Long): List<RecordCpt>

    @Query("select * from match_record where recordId!=:recordId and recordId!=0 and matchItemId in (select mr.matchItemId from match_record mr  join match_period mp on mr.matchId=mp.id where mr.recordId=:recordId and mp.matchId=:matchId)")
    fun getRecordCompetitorsInMatch(recordId: Long, matchId: Long): List<MatchRecord>

    @Query("select mp.* from match_period mp join match_record mr on mp.id=mr.matchId where mr.recordId=:recordId limit 1")
    fun getDebutMatch(recordId: Long): MatchPeriodWrap?

    @Query("select period from match_rank_record where recordId=:recordId order by period limit 1")
    fun getRecordStartPeriod(recordId: Long): Int?

    @Query("select recordId, min(rank) as high from match_rank_record where rank<=:lessEqThan group by recordId order by high")
    fun groupRecordsRank(lessEqThan: Int): List<RecordHighestRank>

    @Query("select mi.winnerId, mp.period, m.id as levelMatchId, mi.matchId as matchPeriodId from match_item mi join match_period mp on mi.matchId=mp.id join 'match' m on mp.matchId=m.id and m.level=:level where mi.round=:finalRoundId order by mp.period, mp.orderInPeriod")
    fun getMatchChampionsByLevel(level: Int, finalRoundId: Int): List<LevelChampion>

    @Query("select mr.* from match_record mr join match_period mp on mr.matchId=mp.id where mp.matchId=:matchId and mr.recordId!=0 group by mr.matchId, mr.recordId")
    fun getMatchParticipates(matchId: Long): List<MatchRecord>

}