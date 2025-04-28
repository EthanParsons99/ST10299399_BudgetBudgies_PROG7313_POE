package com.example.budgetbudgies_prog7313_poe

import androidx.room.*
import com.example.budgetbudgies_prog7313_poe.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGoal(goal: Goal): Long

    @Query("SELECT * FROM goals WHERE userid = :userId ORDER BY goalname ASC")
    fun getUserGoals(userId: Int): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE userid = :userId ORDER BY goalname ASC")
    suspend fun getUserGoalsList(userId: Int): List<Goal>


    @Query("SELECT * FROM goals WHERE goalid = :goalId LIMIT 1")
    suspend fun getGoalById(goalId: Int): Goal?

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)
}