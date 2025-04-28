package com.example.budgetbudgies_prog7313_poe

import androidx.room.*
import java.util.Date
import com.example.budgetbudgies_prog7313_poe.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIncome(income: Income): Long

    @Query("SELECT * FROM incomes WHERE userid = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getUserIncomesInRange(userId: Int, startDate: Date, endDate: Date): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE userid = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getUserIncomesListInRange(userId: Int, startDate: Date, endDate: Date): List<Income>


    @Query("SELECT * FROM incomes WHERE userid = :userId ORDER BY date DESC")
    fun getAllUserIncomes(userId: Int): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE incomeid = :incomeId LIMIT 1")
    suspend fun getIncomeById(incomeId: Int): Income?

    @Update
    suspend fun updateIncome(income: Income)

    @Delete
    suspend fun deleteIncome(income: Income)
}