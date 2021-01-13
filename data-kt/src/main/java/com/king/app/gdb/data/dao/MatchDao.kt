package com.king.app.gdb.data.dao

import androidx.room.*
import com.king.app.gdb.data.entity.match.*
import com.king.app.gdb.data.relation.MatchItemWrap
import com.king.app.gdb.data.relation.MatchPeriodWrap
import com.king.app.gdb.data.relation.MatchRecordWrap

/**
 * @description:
 * @author：Jing
 * @date: 2021/01/09 12:05
 */
@Dao
interface MatchDao {

    @Query("select * from `match` where id=:matchId")
    fun getMatch(matchId: Long): Match

    @Query("select * from `match`")
    fun getAllMatches(): List<Match>

    @Query("select * from `match` order by orderInPeriod")
    fun getAllMatchesByOrder(): List<Match>

    @Query("select * from `match_period` where id=:id")
    fun getMatchPeriod(id: Long): MatchPeriodWrap

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

    @Query("select * from match_record")
    fun getAllMatchRecords(): List<MatchRecord>

    @Query("select * from match_record where matchItemId=:matchItemId and recordId=:recordId")
    fun getMatchRecord(matchItemId: Long, recordId:Long): MatchRecordWrap?

    @Query("select * from match_record where matchItemId=:matchItemId and `order`=:order")
    fun getMatchRecord(matchItemId: Long, order: Int): MatchRecordWrap?

    @Query("select * from match_rank_record")
    fun getAllMatchRankRecords(): List<MatchRankRecord>

    @Query("select * from match_rank_star")
    fun getAllMatchRankStars(): List<MatchRankStar>

    @Query("select * from match_score_star")
    fun getAllMatchScoreStars(): List<MatchScoreStar>

    @Query("select * from match_score_record")
    fun getAllMatchScoreRecords(): List<MatchScoreRecord>

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

    @Query("delete from match_record")
    fun deleteMatchRecords()

    @Query("delete from match_record where matchId =:matchPeriodId")
    fun deleteMatchRecordsByMatchPeriod(matchPeriodId: Long)

    @Query("delete from match_rank_star")
    fun deleteMatchRankStars()

    @Query("delete from match_rank_record")
    fun deleteMatchRankRecords()

    @Query("delete from match_score_star")
    fun deleteMatchScoreStars()

    @Query("delete from match_score_record")
    fun deleteMatchScoreRecords()

    @Update
    fun updateMatch(match: Match)

    @Update
    fun updateMatchPeriod(match: MatchPeriod)

    @Query("select count(*) from match_item where matchId=:matchPeriodId")
    fun countMatchItemsByMatchPeriod(matchPeriodId: Long): Int

    @Query("select * from match_item where matchId=:matchId and round=:round and `order`=:order")
    fun getMatchItem(matchId: Long, round: Int, order: Int): MatchItem?

    @Query("select * from match_record where matchId=:matchPeriodId and type=3 and recordId=0")
    fun getUndefinedQualifies(matchPeriodId: Long): List<MatchRecord>

    @Query("select r._id from record r join match_record mr on r._id=mr.recordId join match_period mp on mr.matchId=mp.id where mp.period=:period and mp.orderInPeriod=:matchOrderInPeriod group by r._id")
    fun getSamePeriodRecordIds(period: Int, matchOrderInPeriod: Int): List<Long>

}