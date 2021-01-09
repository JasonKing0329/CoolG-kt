package com.king.app.gdb.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.king.app.gdb.data.entity.match.*

/**
 * @description:
 * @author：Jing
 * @date: 2021/01/09 12:05
 */
@Dao
interface MatchDao {
    @Query("select * from `match`")
    fun getAllMatches(): List<Match>

    @Query("select * from match_period")
    fun getAllMatchPeriods(): List<MatchPeriod>

    @Query("select * from match_item")
    fun getAllMatchItems(): List<MatchItem>

    @Query("select * from match_record")
    fun getAllMatchRecords(): List<MatchRecord>

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
    fun insertMatchItems(list: List<MatchItem>)

    @Insert
    fun insertMatchRecords(list: List<MatchRecord>)

    @Insert
    fun insertMatchRankRecords(list: List<MatchRankRecord>)

    @Insert
    fun insertMatchRankStars(list: List<MatchRankStar>)

    @Insert
    fun insertMatchScoreStars(list: List<MatchScoreStar>)

    @Insert
    fun insertMatchScoreRecords(list: List<MatchScoreRecord>)

    @Query("delete from `match`")
    fun deleteMatches()

    @Query("delete from match_period")
    fun deleteMatchPeriods()

    @Query("delete from match_item")
    fun deleteMatchItems()

    @Query("delete from match_record")
    fun deleteMatchRecords()

    @Query("delete from match_rank_star")
    fun deleteMatchRankStars()

    @Query("delete from match_rank_record")
    fun deleteMatchRankRecords()

    @Query("delete from match_score_star")
    fun deleteMatchScoreStars()

    @Query("delete from match_score_record")
    fun deleteMatchScoreRecords()

}