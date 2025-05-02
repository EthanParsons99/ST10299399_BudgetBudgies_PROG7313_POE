package com.example.budgetbudgies_prog7313_poe

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals WHERE userid = :userId ORDER BY goalname ASC")
    fun getUserGoals(userId: Int): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE goalid = :goalId LIMIT 1")
    suspend fun getGoalById(goalId: Int): Goal?

    @Query("SELECT * FROM goals WHERE userid = :userId AND completed = :completed ORDER BY goalname ASC")
    fun getUserGoalsByCompletion(userId: Int, completed: Boolean): Flow<List<Goal>>

    @Query("UPDATE goals SET progress = :progress WHERE goalid = :goalId")
    suspend fun updateGoalProgress(goalId: Int, progress: Double)

    @Query("UPDATE goals SET completed = :completed WHERE goalid = :goalId")
    suspend fun updateGoalCompletion(goalId: Int, completed: Boolean)

    @Query("SELECT SUM(progress) FROM goals WHERE userid = :userId")
    suspend fun getTotalSaved(userId: Int): Double?

    @Query("SELECT COUNT(*) FROM goals WHERE userid = :userId AND completed = 1")
    suspend fun getCompletedGoalsCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM goals WHERE userid = :userId")
    suspend fun getTotalGoalsCount(userId: Int): Int
}